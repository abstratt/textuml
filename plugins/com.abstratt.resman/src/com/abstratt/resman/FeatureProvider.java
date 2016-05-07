package com.abstratt.resman;

public interface FeatureProvider {
    public void initFeatures(Resource<?> resource);

    public Class<?>[] getProvidedFeatureTypes();

    public Class<?>[] getRequiredFeatureTypes();
    
    public default boolean isEnabled() {
    	return true;
    }
}
