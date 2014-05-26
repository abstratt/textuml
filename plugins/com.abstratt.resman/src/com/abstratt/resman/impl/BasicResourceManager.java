package com.abstratt.resman.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.RegistryFactory;

import com.abstratt.pluginutils.LogUtils;
import com.abstratt.pluginutils.NodeSorter;
import com.abstratt.resman.ActivatableFeatureProvider;
import com.abstratt.resman.ExceptionTranslationFeatureProvider;
import com.abstratt.resman.FeatureProvider;
import com.abstratt.resman.Resource;
import com.abstratt.resman.ResourceException;
import com.abstratt.resman.ResourceKey;
import com.abstratt.resman.ResourceManager;
import com.abstratt.resman.ResourceProvider;
import com.abstratt.resman.Task;
import com.abstratt.resman.impl.ReferencePool.ReferenceDisposer;

public class BasicResourceManager<K extends ResourceKey> extends ResourceManager<K> {

	private static final String PLUGIN_ID = ResourceManager.class.getPackage().getName();

	private static ThreadLocal<Resource<?>> currentResource = new ThreadLocal<Resource<?>>();

	private final ReferencePool<K, BasicResource<K>> pool;

	private final ResourceProvider<K> resourceProvider;

	protected final Set<FeatureProvider> featureProviders;
	
	private final Timer disposalThread = new Timer(true);

	private ReferenceDisposer<BasicResource<K>> disposer = new ReferencePool.ReferenceDisposer<BasicResource<K>>() {
		@Override
		public void dispose(final BasicResource<K> toDispose) {
			disposalThread.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						activateContext(toDispose);
						try {
							resourceProvider.disposeResource(toDispose);
						} finally {
							deactivateContext(toDispose, false);
						}
					} catch (Throwable e) {
						LogUtils.logError(ResourceManager.ID, "Error disposing resource " + toDispose.getId(), e);
					}
				}
			}, 100);
		}
	};

	public BasicResourceManager(int bucketCap, ResourceProvider<K> provider) {
		this.pool = initPool(bucketCap);
		this.resourceProvider = provider;
		this.featureProviders = sortFeatureProviders(createFeatureProviders());
	}
	
	protected BasicResourceManager(int bucketCap, ResourceProvider<K> provider, Collection<FeatureProvider> featureProviders) {
		this.pool = initPool(bucketCap);
		this.resourceProvider = provider;
		this.featureProviders = new HashSet<FeatureProvider>(featureProviders);
	}

	public BasicResourceManager(ResourceProvider<K> provider) {
		this(4, provider);
	}

	private void disposeResource(BasicResource<K> toDispose) {
		resourceProvider.disposeResource(toDispose);
		pool.forget(toDispose.getId(), toDispose);
		toDispose.invalidate();
	}

	@Override
	public void synchronizeCurrent() throws ResourceException {
		BasicResource<K> current = (BasicResource<K>) getCurrentResource();
		releaseResource(current, true);
		disposeResource(current);
		BasicResource<K> reloaded = acquireResource(current.getId(), true);
		currentResource.set(reloaded);
	}

	private void releaseResource(BasicResource<K> toRelease, boolean operationSucceeded) throws ResourceException {
		deactivateContext(toRelease, operationSucceeded);
		pool.release(toRelease.getId(), toRelease);
	}

	private void deactivateContext(BasicResource<K> deactivating, boolean operationSucceeded) throws ResourceException {
		Throwable deactivationException = null;
		Set<FeatureProvider> allFeatureProviders = getReverseFeatureProviders();
		allFeatureProviders.add(resourceProvider);
		for (FeatureProvider featureProvider : allFeatureProviders)
			if (featureProvider instanceof ActivatableFeatureProvider)
				try {
					((ActivatableFeatureProvider) featureProvider).deactivateContext(deactivating, operationSucceeded);
				} catch (Throwable t) {
					if (deactivationException == null)
						deactivationException = t;
					operationSucceeded = false;
				}
		deactivating.flushContext();
		if (deactivationException != null)
			if (deactivationException instanceof ResourceException)
				throw (ResourceException) deactivationException;
			else if (deactivationException instanceof Error)
				throw (Error) deactivationException;
			else
				throw new ResourceException((Exception) deactivationException);
	}

	@Override
	public <E> E runTask(K resourceId, Task<E> toRun) throws ResourceException {
		if (currentResource.get() != null)
			throw new IllegalStateException("A resource already in use");

		BasicResource<K> resource = acquireResource(resourceId, false);
		Assert.isLegal(resource.isValid());
		boolean operationSucceeded = false;
		Throwable caught = null;
		try {
			currentResource.set(resource);
			E result = (E) toRun.run(resource);
			operationSucceeded = true;
			return result;
		} catch (Throwable t) {
			caught = t;
		} finally {
			try {
				caught = extractCause(caught);
				if (caught instanceof Error)
					throw (Error) caught;
				// previous instance may have been unloaded/reloaded
				// so we can't just use the local reference
				resource = (BasicResource<K>) currentResource.get();
				try {
					// this can cause an exception as well (saving context)
					// make sure to run it through translators
    				releaseResource(resource, operationSucceeded);
				} catch (RuntimeException e) {
					if (caught == null)
						caught = extractCause(e);
				}
				if (caught != null) {
 					for (FeatureProvider featureProvider : getFeatureProviders())
						if (featureProvider instanceof ExceptionTranslationFeatureProvider)
							caught = ((ExceptionTranslationFeatureProvider) featureProvider).translate(caught);
					if (caught instanceof RuntimeException)
						throw (RuntimeException) caught;
					throw new ResourceException((Exception) caught);
				}
			} finally {
				currentResource.remove();
			}
		}
		// never runs
		return null;
	}

	protected Throwable extractCause(Throwable caught) {
		if (caught instanceof ResourceException && caught.getCause() != null)
			caught = caught.getCause();
		return caught;
	}

	/**
	 * Acquires an instance of the resource identified. Can reuse an existing
	 * instance if one is available.
	 * 
	 * @param resourceId
	 *            the id of the resource
	 * @param forceNew
	 *            whether to always grab a new instance or to try to reuse an
	 *            existing one
	 * @return an instance of the requested resource
	 * @throws ResourceException
	 */
	private BasicResource<K> acquireResource(K resourceId, boolean forceNew) throws ResourceException {
		if (!forceNew) {
			// if an existing instance is already available, just grab it
			BasicResource<K> existing = basicAcquireResource(resourceId);
			if (existing != null) {
				if (existing.isValid()) {
					if (resourceProvider.isOpen(existing)) {
						activateContext(existing);
						return existing;
					}
				}
			}
		}
		BasicResource<K> newResource = new BasicResource<K>(resourceId);
		initResource(newResource);
		pool.add(resourceId, newResource);
		activateContext(newResource);
		return newResource;
	}

	protected BasicResource<K> basicAcquireResource(K resourceId) {
		return pool.acquire(resourceId);
	}

	private void activateContext(BasicResource<?> activating) throws ResourceException {
		activating.initContext();
		resourceProvider.activateContext(activating);
		for (FeatureProvider featureProvider : getFeatureProviders())
			if (featureProvider instanceof ActivatableFeatureProvider)
				((ActivatableFeatureProvider) featureProvider).activateContext(activating);
	}

	private void initResource(Resource<K> resource) throws ResourceException {
		resourceProvider.loadResource(resource);
		Set<FeatureProvider> featureProviders = getFeatureProviders();
		for (FeatureProvider featureProvider : featureProviders)
			featureProvider.initFeatures(resource);
	}

	private Set<FeatureProvider> getReverseFeatureProviders() throws ResourceException {
		ArrayList<FeatureProvider> copy = new ArrayList<FeatureProvider>(getFeatureProviders());
		Collections.reverse(copy);
		return new LinkedHashSet<FeatureProvider>(copy);
	}

	private Set<FeatureProvider> getFeatureProviders() throws ResourceException {
		return featureProviders;
	}

	private Set<FeatureProvider> sortFeatureProviders(final Collection<FeatureProvider> created) {
		final Map<Class<?>, FeatureProvider> features = new HashMap<Class<?>, FeatureProvider>();
		for (FeatureProvider featureProvider : created)
			for (Class<?> featureType : featureProvider.getProvidedFeatureTypes())
				features.put(featureType, featureProvider);
		List<Class<?>> sorted = NodeSorter.sort(features.keySet(), new NodeSorter.NodeHandler<Class<?>>() {
			@Override
			public Collection<Class<?>> next(Class<?> featureClass) {
				return Arrays.asList(features.get(featureClass).getRequiredFeatureTypes());
			}
		});
		Collections.reverse(sorted);
		Set<FeatureProvider> providers = new LinkedHashSet<FeatureProvider>();
		for (Class<?> featureClass : sorted)
			providers.add(features.get(featureClass));
		return providers;
	}

	protected Collection<FeatureProvider> createFeatureProviders() throws ResourceException {
		IExtensionPoint point = RegistryFactory.getRegistry().getExtensionPoint(PLUGIN_ID, "features");
		Collection<FeatureProvider> providers = new HashSet<FeatureProvider>();
		providers.add(resourceProvider);
		Set<Class<?>> knownFeatures = new HashSet<Class<?>>();
		IConfigurationElement[] configurationElements = point.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			try {
				FeatureProvider currentFeatureProvider = (FeatureProvider) configurationElements[i]
						.createExecutableExtension("provider");
				Set<Class<?>> provided = new HashSet<Class<?>>(Arrays.asList(currentFeatureProvider
						.getProvidedFeatureTypes()));
				provided.retainAll(knownFeatures);
				if (!provided.isEmpty())
					throw new ResourceException("Provider " + currentFeatureProvider.getClass().getSimpleName()
							+ " provides redundant features: " + provided);
				knownFeatures.addAll(Arrays.asList(currentFeatureProvider.getProvidedFeatureTypes()));
				providers.add(currentFeatureProvider);
			} catch (CoreException e) {
				throw new ResourceException(e);
			}
		}
		return providers;
	}

	@Override
	public void remove(K id) {
		pool.remove(id);
	}

	@Override
	public Resource<K> getCurrentResource() {
		final Resource<K> resource = (Resource<K>) currentResource.get();
		Assert.isTrue(resource != null, "No current resource");
		Assert.isTrue(resource.isValid());
		return resource;
	}

	@Override
	public boolean inTask() {
		return (Resource<K>) currentResource.get() != null;
	}

	@Override
	public void clear() {
		currentResource.remove();
		this.pool.clear();
	}

	@Override
	public boolean isEmpty() {
		return this.pool.isEmpty();
	}

	private ReferencePool<K, BasicResource<K>> initPool(int bucketCap) {
		return new ReferencePool<K, BasicResource<K>>(bucketCap, 30, disposer);
	}
}