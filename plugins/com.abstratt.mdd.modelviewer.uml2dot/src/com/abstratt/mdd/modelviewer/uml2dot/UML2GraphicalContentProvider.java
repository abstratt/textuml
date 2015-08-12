/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelviewer.uml2dot;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.uml2.uml.Element;

import com.abstratt.mdd.modelrenderer.IRendererSelector;
import com.abstratt.mdd.modelrenderer.IRenderingSettings;
import com.abstratt.mdd.modelrenderer.RenderingSettings;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOT;
import com.abstratt.mdd.modelviewer.AbstractModelGraphicalContentProvider;

/**
 * 
 */
public class UML2GraphicalContentProvider extends AbstractModelGraphicalContentProvider implements
        IPreferenceChangeListener {

    @Override
    protected IRenderingSettings getSettings() {
        return new RenderingSettings(UMLPreferences.getPreferences());
    }

    @Override
    protected IRendererSelector<Element> getRendererSelector() {
        return UML2DOT.getRendererSelector();
    }

    public UML2GraphicalContentProvider() {
        new InstanceScope().getNode(UML.PLUGIN_ID).addPreferenceChangeListener(this);
    }

    @Override
    public void dispose() {
        new InstanceScope().getNode(UML.PLUGIN_ID).removePreferenceChangeListener(this);
        super.dispose();
    }

    public void preferenceChange(PreferenceChangeEvent event) {
        reload();
    }
}
