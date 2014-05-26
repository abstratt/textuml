package com.abstratt.resman.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.abstratt.resman.FeatureProvider;
import com.abstratt.resman.Resource;
import com.abstratt.resman.Resource.InvalidResourceException;
import com.abstratt.resman.ResourceException;
import com.abstratt.resman.ResourceKey;
import com.abstratt.resman.ResourceProvider;
import com.abstratt.resman.Task;
import com.abstratt.resman.impl.BasicResourceManager;

public class ResourceManagerTests extends TestCase {
	
	public class SimpleResourceProvider implements ResourceProvider<StringKey> {

		final private Class<?>[] requiredFeatureTypes;
		public SimpleResourceProvider(Class<?>[] requiredFeatureTypes, Class<?>[] providedFeatureTypes) {
			this.requiredFeatureTypes = requiredFeatureTypes;
			this.providedFeatureTypes = providedFeatureTypes;
		}

		final private Class<?>[] providedFeatureTypes;
		private Map<Resource<?>, AtomicBoolean> open = new HashMap<Resource<?>, AtomicBoolean>();

		@Override
		public synchronized void activateContext(Resource<?> resource) {
		}

		@Override
		public synchronized void deactivateContext(Resource<?> resource, boolean operationSucceeded) {
		}

		@Override
		public void initFeatures(Resource<?> resource) {
		}

		@Override
		public Class<?>[] getProvidedFeatureTypes() {
			return providedFeatureTypes;
		}

		@Override
		public Class<?>[] getRequiredFeatureTypes() {
			return requiredFeatureTypes;
		}

		@Override
		public Class<StringKey> getTarget() {
			return StringKey.class;
		}

		@Override
		public final synchronized void loadResource(Resource<StringKey> toLoad) throws ResourceException {
            initResource(toLoad);
			open.put(toLoad, new AtomicBoolean(true));
		}
		
		protected void initResource(Resource<StringKey> toLoad) {
		}

		@Override
		public final synchronized void disposeResource(Resource<StringKey> resource) {
		    open.get(resource).set(false);
		}

		@Override
		public synchronized boolean isOpen(Resource<StringKey> resource) {
			AtomicBoolean value = open.get(resource);
			return value != null && value.get();
		}

	}

	public static class StringKey implements ResourceKey {
		private static final long serialVersionUID = 1L;

		private String value;
		
		public StringKey(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StringKey other = (StringKey) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}
	
	public static class SimpleResourceManager extends BasicResourceManager<StringKey> {

		public SimpleResourceManager(ResourceProvider<StringKey> provider, Collection<FeatureProvider> featureProviders) {
			super(4, provider, featureProviders);
		}
		
		public SimpleResourceManager(int bucketCap, ResourceProvider<StringKey> provider, Collection<FeatureProvider> featureProviders) {
			super(bucketCap, provider, featureProviders);
		}

		@Override
		protected Collection<FeatureProvider> createFeatureProviders() throws ResourceException {
			return featureProviders;
		}
	}

	public ResourceManagerTests(String name) {
		super(name);
	}
	
	public void testResourceIsValid() {
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[0]);
		final SimpleResourceManager resourceManager = new SimpleResourceManager(resourceProvider, Arrays.<FeatureProvider>asList());
        final Task<Resource<?>> task = new Task<Resource<?>>() {
			@Override
			public Resource<?> run(Resource<?> resource) {
				assertTrue(resourceManager.inTask());
				assertTrue(resource.isValid());
				return resource;
			}
        };
        assertFalse(resourceManager.inTask());
		resourceManager.runTask(new StringKey("id1"), task);
		assertFalse(resourceManager.inTask());
	}
	
	public void testInTask() {
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[0]);
		final SimpleResourceManager resourceManager = new SimpleResourceManager(resourceProvider, Arrays.<FeatureProvider>asList());
        final Task<Resource<?>> task = new Task<Resource<?>>() {
			@Override
			public Resource<?> run(Resource<?> resource) {
				assertTrue(resourceManager.inTask());
				return resource;
			}
        };
        assertFalse(resourceManager.inTask());
		resourceManager.runTask(new StringKey("id1"), task);
		assertFalse(resourceManager.inTask());
	}
	
	
	public void testResourceIsCurrent() {
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[0]);
		final SimpleResourceManager resourceManager = new SimpleResourceManager(resourceProvider, Arrays.<FeatureProvider>asList());
        final Task<Resource<?>> task = new Task<Resource<?>>() {
			@Override
			public Resource<?> run(Resource<?> resource) {
				assertNotNull(resource);
				assertSame(resource, resourceManager.getCurrentResource());
				return resource;
			}
        };
		resourceManager.runTask(new StringKey("id1"), task);
	}

	public void testResourceIsReused() {
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[0]);
		SimpleResourceManager resourceManager = new SimpleResourceManager(1, resourceProvider, Arrays.<FeatureProvider>asList());
        final Task<Resource<?>> task = new Task<Resource<?>>() {
			@Override
			public Resource<?> run(Resource<?> resource) {
				return resource;
			}
        };
		Resource<?> first = resourceManager.runTask(new StringKey("id1"), task);
        assertTrue(first != null);
        assertTrue(first.isValid());
		Resource<?> second = resourceManager.runTask(new StringKey("id1"), task);
        assertSame(first, second);
	}
	
	public void testFeature() {
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[0]);
		SimpleResourceManager resourceManager = new SimpleResourceManager(resourceProvider, Arrays.<FeatureProvider>asList(new SimpleResourceProvider(new Class[0], new Class[] {Integer.class}) {
			@Override
			public void initFeatures(Resource<?> resource) {
				resource.setFeature(Integer.class, 42);
			}
		}));
        resourceManager.runTask(new StringKey("id1"), new Task<Boolean>() {
			@Override
			public Boolean run(Resource<?> resource) {
				assertTrue(resource.hasFeature(Integer.class));
				assertEquals(Integer.valueOf(42), resource.getFeature(Integer.class));
				return true;
			}
        });
	}
	
	public void testSyncDeactivates() {
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[] {Integer.class});
		final List<Resource<?>> activated = new ArrayList<Resource<?>>();
		final List<Resource<?>> deactivated = new ArrayList<Resource<?>>();
		
		final SimpleResourceManager resourceManager = new SimpleResourceManager(resourceProvider, Arrays.<FeatureProvider>asList(new SimpleResourceProvider(new Class[0], new Class[] {Boolean.class}) {
			@Override
			public void deactivateContext(Resource<?> resource, boolean operationSucceeded) {
				deactivated.add(resource);
			}
			@Override
			public void activateContext(Resource<?> resource) {
				activated.add(resource);
			}
		}));
        resourceManager.runTask(new StringKey("id1"), new Task<Object>() {
			@Override
			public Object run(Resource<?> resource) {
				assertEquals(1, activated.size());
				assertEquals(0, deactivated.size());
				assertSame(resource, activated.get(0));
				
				resourceManager.synchronizeCurrent();
				
				Resource<StringKey> newResource = resourceManager.getCurrentResource();
				
				assertEquals(2, activated.size());
				assertEquals(1, deactivated.size());
				assertSame(resource, deactivated.get(0));
				assertSame(newResource, activated.get(1));
				return null;
			}
        });
	}
	
	public void testSync() {
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[0]);
		final SimpleResourceManager resourceManager = new SimpleResourceManager(resourceProvider, Arrays.<FeatureProvider>asList());
        boolean ran = resourceManager.runTask(new StringKey("id1"), new Task<Boolean>() {
			@Override
			public Boolean run(Resource<?> resource) {
				assertTrue(resource.isValid());
				resourceManager.synchronizeCurrent();
				assertFalse(resource.isValid());
				Resource<?> newResource = resourceManager.getCurrentResource();
				assertTrue(newResource.isValid());
				assertEquals(resource.getId(), newResource.getId());
				try {
				    resource.getFeature(String.class);
				    fail("Should have failed");
				} catch (InvalidResourceException e) {
					// expected
				}
				return true;
			}
        });
        assertTrue(ran);
	}

	/**
	 * Shows that two requests can be served simultaneously but will get different copies of the resource.
	 */
	public void testConcurrency() throws Exception {
		final String originalValue = "foo";
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[0]) {
			@Override
			public void initResource(Resource<StringKey> toLoad) throws ResourceException {
				toLoad.setFeature(String.class, new String(originalValue));
			}
		};
		final SimpleResourceManager resourceManager = new SimpleResourceManager(resourceProvider, Arrays.<FeatureProvider>asList());

		ExecutorService executor = Executors.newFixedThreadPool(2);
		
		final CyclicBarrier barrier = new CyclicBarrier(3);
		
		Callable<String> backgroundJob = new Callable<String>() {
			@Override
			public String call() throws Exception {
				// before running
				barrier.await(1, TimeUnit.SECONDS);
				final Task<String> task = new Task<String>() {
					@Override
					public String run(Resource<?> resource) {
						try {
							// while running
							barrier.await(1, TimeUnit.SECONDS);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						return resource.getFeature(String.class);
					}
				};
				String result = resourceManager.runTask(new StringKey(""), task);
				// after running
				barrier.await(1, TimeUnit.SECONDS);
				return result;
			}
		};
		Future<String> result1 = executor.submit(backgroundJob);
		Future<String> result2 = executor.submit(backgroundJob);
		
		// before
		barrier.await(1, TimeUnit.SECONDS);
		// within
		barrier.await(1, TimeUnit.SECONDS);
		// after
		barrier.await(1, TimeUnit.SECONDS);

		assertEquals("should match the original value", originalValue, result1.get());
		assertEquals("should be the same value", result1.get(), result2.get());
		assertFalse("should not share state", result1.get() == result2.get());
	}

	/**
	 * Shows that two requests will be serialized and will share the same copy of the resource 
	 * if the limit of copies is 1.
	 */
	public void testSerialization() throws Exception {
		final String originalValue = "foo";
		ResourceProvider<StringKey> resourceProvider = new SimpleResourceProvider(new Class[0], new Class[0]) {
			@Override
			public void initResource(Resource<StringKey> toLoad) throws ResourceException {
				toLoad.setFeature(String.class, new String(originalValue));
			}
		};
		final SimpleResourceManager resourceManager = new SimpleResourceManager(1, resourceProvider, Arrays.<FeatureProvider>asList());

		final Resource<?>[] resourceSeen = {null};
		// run a no-op task just to get the resource instance allocated
		resourceManager.runTask(new StringKey(""), new Task<String>() {
			@Override
			public String run(Resource<?> resource) {
				resourceSeen[0] = resource;
				return null;
			}
		});
		assertNotNull(resourceSeen[0]);
		
		final AtomicInteger syncValue = new AtomicInteger(0);

		
		class SimpleTask implements Task<String>, Runnable {
			CyclicBarrier barrier = new CyclicBarrier(2);
			String result;
			List<Throwable> errors = Collections.synchronizedList(new ArrayList<Throwable>());
			@Override
			public String run(Resource<?> resource) {
				await(false, true);
				assertSame(resourceSeen[0], resource);
				assertEquals(1, syncValue.incrementAndGet());
				await(false, true);
				final String featureValue = resource.getFeature(String.class);
				assertEquals(0, syncValue.decrementAndGet());
				return featureValue;
			}
			protected void await(boolean ensureClean, boolean shouldSucceed) {
				if (ensureClean)
					ensureCleanErrors();
				try {
					if (shouldSucceed) 
						barrier.await();
					else
				        barrier.await(1, TimeUnit.SECONDS);
				} catch (BrokenBarrierException e) {
					throw new RuntimeException(e);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (TimeoutException e) {
					if (!shouldSucceed)
						return;
					throw new RuntimeException(e);
				}
				assertTrue(shouldSucceed);
			}
			private void ensureCleanErrors() {
				// ensure no task errors occurred
				for (Throwable throwable : errors)
					throw new RuntimeException(throwable);
			}
			@Override
			public void run() {
				await(false, true);
				try {
					result = resourceManager.runTask(new StringKey(""), this);
					await(true, true);
				} catch (Throwable e) {
					errors.add(e);
				}
			}
		};
		SimpleTask task1 = new SimpleTask();
		SimpleTask task2 = new SimpleTask();

		new Thread(task1, "Task 1").start();
		new Thread(task2, "Task 2").start();
		
		// gets task1 submitted
		task1.await(true, true);
		// wait for the task to actually commence - it will block again after modifying the shared state
		task1.await(true, true);
		// gets task2 submitted
		task2.await(true, true);
        // task2 will be blocked by task1, expect timeout
		task2.await(true, false);
		// reset barrier so we can use it again
		task2.barrier.reset();
		// let task1 complete so task2 can get started
		task1.await(true, true);
		// only now task2 should be able to commence
		task2.await(true, true);
		// let task2 complete
		task2.await(true, true);

		// wait for threads to collect results
		task1.await(true, true);
		task2.await(true, true);
		
		assertEquals("should match the original value", originalValue, task1.result);
		assertEquals("should be the same value", task1.result, task2.result);
		assertTrue("should share state", task1.result == task2.result);
	}

	
	public static Test suite() {
		return new TestSuite(ResourceManagerTests.class);
	}
}