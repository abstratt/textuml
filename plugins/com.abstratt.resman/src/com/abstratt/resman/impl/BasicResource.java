package com.abstratt.resman.impl;

import java.util.HashMap;
import java.util.Map;

import com.abstratt.resman.Resource;
import com.abstratt.resman.ResourceKey;

public class BasicResource<K extends ResourceKey> extends Resource<K> {
    protected BasicResource(K resourceId) {
        super(resourceId);
    }

    void invalidate() {
        System.out.println("Invalidated " + getId());
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
