package com.abstratt.mdd.core;

import org.eclipse.core.runtime.CoreException;

import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.resman.Resource;
import com.abstratt.resman.ResourceException;
import com.abstratt.resman.ResourceProvider;

public class RepositoryProvider implements ResourceProvider<RepositoryKey> {
	@Override
	public Class<RepositoryKey> getTarget() {
		return RepositoryKey.class;
	}

	@Override
	public Class<?>[] getProvidedFeatureTypes() {
		return new Class<?>[] { IRepository.class, CacheHolder.class };
	}

	@Override
	public Class<?>[] getRequiredFeatureTypes() {
		return new Class<?>[0];
	}

	@Override
	public void initFeatures(Resource<?> resource) {
		// already done on load
	}

	@Override
	public void loadResource(Resource<RepositoryKey> resource) throws ResourceException {
		try {
			// install it right away as initial repository load relies on it
			CacheHolder newCacheHolder = CacheAdapterManager.install();
			resource.setFeature(CacheHolder.class, newCacheHolder);
			IRepository newRepo = MDDCore.createRepository(MDDUtil.fromJavaToEMF(resource.getId().getUri()));
			resource.setFeature(IRepository.class, newRepo);
			resource.setFeature(NamedElementLookupCache.class, newRepo.getLookupCache());
		} catch (CoreException e) {
			throw new ResourceException(e);
		}
		System.out.println("Loaded " + resource.getId());
	}

	@Override
	public void activateContext(Resource<?> resource) {
		CacheHolder cacheHolder = resource.getFeature(CacheHolder.class);
		CacheAdapterManager.restore(cacheHolder);
	}

	@Override
	public void deactivateContext(Resource<?> resource, boolean success) {
		// ensure no one else tries to use the same cache adapter
		CacheAdapterManager.remove();
	}

	@Override
	public void disposeResource(Resource<RepositoryKey> resource) {
		if (!resource.isValid())
			return;
		resource.getFeature(IRepository.class).close();
		CacheAdapterManager.remove();
		System.out.println("Disposed " + resource.getId());
	}

	@Override
	public boolean isOpen(Resource<RepositoryKey> resource) {
		return resource.isValid() && resource.getFeature(IRepository.class).isOpen();
	}
}