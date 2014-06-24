package com.abstratt.kirra.mdd.core;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.resman.FeatureProvider;
import com.abstratt.resman.Resource;

public class KirraMetadataProvider implements FeatureProvider {
	
	@Override
	public Class<?>[] getRequiredFeatureTypes() {
		return new Class<?>[] { IRepository.class };
	}
	@Override
	public Class<?>[] getProvidedFeatureTypes() {
		return new Class<?>[] { KirraHelper.Metadata.class };
	}
	@Override
	public void initFeatures(Resource<?> resource) {
		resource.setFeature(KirraHelper.Metadata.class, new KirraHelper.Metadata());
	}
}
