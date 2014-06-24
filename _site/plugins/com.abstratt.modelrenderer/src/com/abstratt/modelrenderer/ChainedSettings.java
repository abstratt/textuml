package com.abstratt.modelrenderer;

import com.abstratt.modelrenderer.IRenderingSettings.SettingsSource;

public class ChainedSettings implements SettingsSource {

	private SettingsSource[] chain;
	
	public ChainedSettings(SettingsSource... chain) {
		this.chain = chain;
	}

	@Override
	public String getSetting(String optionKey) {
		for (SettingsSource settingsSource : this.chain) {
		    String setting = settingsSource.getSetting(optionKey);
		    if (setting != null)
		    	return setting;
		}
		return null;
	}

}
