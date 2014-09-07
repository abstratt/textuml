package com.abstratt.resman;


public interface ActivatableFeatureProvider extends FeatureProvider {
	public void activateContext(Resource<?> resource);
	public void deactivateContext(Resource<?> resource, boolean operationSucceeded);
}
