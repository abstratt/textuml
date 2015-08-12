package com.abstratt.mdd.modelrenderer;

import java.util.Map;

import com.abstratt.mdd.modelrenderer.IRenderingSettings.SettingsSource;

public class MapBackedSettingsSource implements SettingsSource {

	private Map<String, String> values;

	public MapBackedSettingsSource(Map<String, String> values) {
		this.values = values;
	}

	@Override
	public String getSetting(String optionKey) {
		return values.get(optionKey);
	}

}
