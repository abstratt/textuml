package com.abstratt.resman;

import com.abstratt.resman.impl.BasicResourceManager;

/**
 * Manages access to shared/expensive resources.
 */
public abstract class ResourceManager<K extends ResourceKey> {

	public static final String ID = ResourceManager.class.getPackage().getName();

	/**
	 * Runs a task within the context of the given resource.
	 *
	 * At any time, at most one task may be running within a given resource
	 * instance.
	 * 
	 * If another task is already running within the resource, depending on the
	 * implementation, one of the following things may happen:
	 * <ul>
	 * <li>the task execution will block until the resource becomes available
	 * (potentially timing out)
	 * <li>a new instance of the resource may be created in order to run this
	 * task
	 * 
	 * @param resourceId
	 *            the resource id
	 * @param toRun
	 *            the task to run
	 * @return the task result
	 * @throws ResourceException
	 *             if any resource-related errors occur
	 */
	public abstract <S> S runTask(K resourceId, Task<S> toRun) throws ResourceException;

	/**
	 * Creates a new resource manager.
	 * 
	 * @param provider
	 * @return
	 */
	public static <K extends ResourceKey> ResourceManager<K> create(ResourceProvider<K> provider) {
		return new BasicResourceManager<K>(provider);
	}

	/**
	 * Creates a new resource manager.
	 * 
	 * @param provider
	 * @return
	 */
	public static <K extends ResourceKey> ResourceManager<K> create(int cap, ResourceProvider<K> provider) {
		return new BasicResourceManager<K>(cap, provider);
	}

	/**
	 * Requests a resource to be removed. Any existing instances of the resource
	 * are to be considered stale. New tasks against the given resource will
	 * work on a freshly loaded instance. Currently running tasks will be
	 * dealing with a stale version of the resource until they complete.
	 * 
	 * @param id
	 *            the resource id
	 */
	public abstract void remove(K id);

	/**
	 * Requests the current resource to be synchronized.
	 * 
	 * @throws ResourceException
	 */
	public abstract void synchronizeCurrent() throws ResourceException;

	public abstract Resource<K> getCurrentResource();

	public abstract boolean inTask();

	public abstract void clear();

	public abstract boolean isEmpty();
}
