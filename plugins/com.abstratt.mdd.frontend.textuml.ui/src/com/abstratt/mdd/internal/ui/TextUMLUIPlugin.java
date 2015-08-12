/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.internal.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class TextUMLUIPlugin extends AbstractUIPlugin {

	public static final String FORMAT_ON_SAVE = "formatOnSave";
	public static final String OPTIONS = "options";
	public static final String SHOW_ATTR = "1";
	public static final String SHOW_OP = "2";
	public static final String SHOW_DATATYPE = "3";
	public static final String SHOW_DEPS = "4";
	public static final String SHOW_ASSOCINCLASS = "5";
	// The shared instance
	private static TextUMLUIPlugin plugin;

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static TextUMLUIPlugin getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	protected void initializeDefaultPluginPreferences() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(FORMAT_ON_SAVE, false);
		store.setDefault(OPTIONS, SHOW_ATTR + "," + SHOW_OP + "," + SHOW_DATATYPE + "," + SHOW_DEPS);
	}

	public boolean isPreferencePresentInEditorOptions(String preference) {
		boolean result = false;
		IPreferenceStore store = getPreferenceStore();
		String rawString = store.getString(OPTIONS);
		if (rawString != null && !rawString.equals("") && rawString.contains(preference)) {
			return true;
		}
		return result;
	}
}
