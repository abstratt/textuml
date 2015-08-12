package com.abstratt.resman;

/**
 * Resource providers know what the resources actually are.
 */
public interface ResourceProvider<K extends ResourceKey> extends ActivatableFeatureProvider {
	Class<K> getTarget();

	/**
	 * Returns a new instance of the resource with the given id. This may be a
	 * long running operation as it may involve materializing a resource that is
	 * not readily available.
	 * 
	 * @param id
	 *            the resource id
	 * @return the resource
	 */
	void loadResource(Resource<K> toLoad) throws ResourceException;

	/**
	 * Notifies the resource provider that a resource is no longer in use.
	 * 
	 * @param id
	 *            the resource id
	 * @param resource
	 *            the resource itself
	 */
	void disposeResource(Resource<K> resource);

	boolean isOpen(Resource<K> resource);
}
