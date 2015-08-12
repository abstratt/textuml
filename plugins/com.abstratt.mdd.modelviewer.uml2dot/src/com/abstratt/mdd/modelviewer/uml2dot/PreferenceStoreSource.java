package com.abstratt.mdd.modelviewer.uml2dot;

import org.eclipse.jface.preference.IPreferenceStore;

import com.abstratt.mdd.modelrenderer.IRenderingSettings.SettingsSource;

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
