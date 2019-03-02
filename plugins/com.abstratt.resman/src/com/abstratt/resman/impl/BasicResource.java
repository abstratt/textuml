package com.abstratt.resman.impl;

import java.util.HashMap;
import java.util.Map;

import com.abstratt.pluginutils.LogUtils;
import com.abstratt.resman.Resource;
import com.abstratt.resman.ResourceKey;
import com.abstratt.resman.ResourceManager;

public class BasicResource<K extends ResourceKey> extends Resource<K> {

	static final String PLUGIN_ID = ResourceManager.class.getPackage().getName();
	
	protected BasicResource(ResourceManager<K> manager, K resourceId) {
        super(manager, resourceId);
    }
	
    void invalidate() {
        LogUtils.debug(PLUGIN_ID, () -> "Invalidated " + getId());
        this.features = null;
    }

    void flushContext() {
        this.context = null;
    }

    void initContext() {
        this.context = new HashMap<Class<?>, Map<String, Object>>();
        for (Class<?> featureClass : features.keySet())
            this.context.put(featureClass, new HashMap<String, Object>());
    }
}
