package com.abstratt.mdd.core;

import org.eclipse.uml2.common.util.CacheAdapter;

public class CacheHolder {

    /**
     * This class only exists so we know when we are dealing with a UML-2
     * created cache adapter versus one installed via resource management.
     *
     */
    static class CustomCacheAdapter extends CacheAdapter {
    }

    final private CustomCacheAdapter instance = new CustomCacheAdapter();

    public CustomCacheAdapter getAdapter() {
        return instance;
    }

    @Override
    public String toString() {
        return super.toString() + " / " + instance;
    }
}
