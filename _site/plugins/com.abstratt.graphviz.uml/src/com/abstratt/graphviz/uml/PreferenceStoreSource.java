package com.abstratt.graphviz.uml;

import org.eclipse.jface.preference.IPreferenceStore;

import com.abstratt.modelrenderer.IRenderingSettings.SettingsSource;

public class PreferenceStoreSource implements SettingsSource {

	private IPreferenceStore store;
	
	public PreferenceStoreSource(IPreferenceStore store) {
		this.store = store;
	}

	@Override
	public String getSetting(String optionKey) {
		return store.getString(optionKey);
	}

}
