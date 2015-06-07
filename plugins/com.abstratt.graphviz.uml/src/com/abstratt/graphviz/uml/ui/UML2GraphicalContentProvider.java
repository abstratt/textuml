/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.graphviz.uml.ui;

 import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.abstratt.graphviz.uml.UML;
import com.abstratt.graphviz.uml.UMLPreferences;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOT;
import com.abstratt.modelrenderer.IRendererSelector;
import com.abstratt.modelrenderer.IRenderingSettings;
import com.abstratt.modelrenderer.RenderingSettings;
import com.abstratt.modelviewer.AbstractModelGraphicalContentProvider;

/**
 * 
 */
public class UML2GraphicalContentProvider extends
		AbstractModelGraphicalContentProvider implements
		IPreferenceChangeListener {
	
	@Override
	protected IRenderingSettings getSettings() {
		return new RenderingSettings(UMLPreferences.getPreferences());
	}

	@Override
	protected IRendererSelector<?> getRendererSelector() {
		return UML2DOT.getRendererSelector();
	}

	public UML2GraphicalContentProvider() {
		new InstanceScope().getNode(UML.PLUGIN_ID).addPreferenceChangeListener(
				this);
	}

	@Override
	public void dispose() {
		new InstanceScope().getNode(UML.PLUGIN_ID)
				.removePreferenceChangeListener(this);
		super.dispose();
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		reload();
	}
}
