package com.abstratt.mdd.core;

import org.eclipse.core.runtime.Assert;
import org.eclipse.uml2.common.util.CacheAdapter;

import com.abstratt.mdd.core.CacheHolder.CustomCacheAdapter;

public class CacheAdapterManager extends CacheAdapter {
	public static boolean isEnabled() {
		return CacheAdapter.THREAD_LOCAL != null;
	}
	
	static void remove() {
		setAdapter(null);
	}
	
	static CacheHolder install() {
		CacheHolder created = new CacheHolder();
		setAdapter(created);
		return created;
	}
	
	static void restore(CacheHolder toRestore) {
		Assert.isNotNull(toRestore);
		setAdapter(toRestore);
	}
	
	private static void setAdapter(CacheHolder toRestore) {
		if (!isEnabled())
			return;
		CustomCacheAdapter newAdapter = toRestore != null ? toRestore.getAdapter() : null;
		CacheAdapter.THREAD_LOCAL.set(newAdapter);
	}
}