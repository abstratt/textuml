package com.abstratt.resman;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

public abstract class Resource<K extends ResourceKey> {
	public static class InvalidResourceException extends IllegalStateException {
		private static final long serialVersionUID = 1L;

		public InvalidResourceException(ResourceKey id) {
			super(id + " is not valid");
		}
	}

	private K id;
	protected Map<Class<?>, Object> features = new HashMap<Class<?>, Object>();
	protected Map<Class<?>, Map<String, Object>> context = new HashMap<Class<?>, Map<String, Object>>();

	protected Resource(K resourceId) {
		this.id = resourceId;
	}

	public <F> F getFeature(Class<F> featureClass) {
		Assert.isLegal(hasFeature(featureClass), "No feature: " + featureClass.getName());
		return (F) features.get(featureClass);
	}

	public <F> void setFeature(Class<F> featureClass, F feature) {
		ensureResourceValid();
		features.put(featureClass, feature);
	}

	public void ensureResourceValid() {
		if (!isValid())
			throw new InvalidResourceException(getId());
	}

	public Object getContextValue(Class<?> featureClass, String slot) {
		return context.get(featureClass).get(slot);
	}

	public void setContextValue(Class<?> featureClass, String slot, Object value) {
		this.context.get(featureClass).put(slot, value);
	}

	public K getId() {
		return id;
	}

	public boolean hasFeature(java.lang.Class<?> featureClass) {
		ensureResourceValid();
		return features.containsKey(featureClass);
	}

	public boolean isValid() {
		return features != null;
	}
}
