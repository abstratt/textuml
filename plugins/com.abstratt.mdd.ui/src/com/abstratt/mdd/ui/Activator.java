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
package com.abstratt.mdd.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.abstratt.mdd.internal.ui.RepositoryCache;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {

    // The shared instance.
    private static Activator plugin;

    public static String PLUGIN_ID = "com.abstratt.mdd.ui";

    /**
     * Returns the shared instance.
     *
     * @return the shared instance.
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path.
     *
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("com.abstratt.mdd.ui", path);
    }

    /**
     * The constructor.
     */
    public Activator() {
        plugin = this;
    }

    protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        String pluginId = "org.eclipse.uml2.uml.edit";
        reg.put(UIConstants.ICON_CLASS, imageDescriptorFromPlugin(pluginId, "icons/full/obj16/Class.gif"));
        reg.put(UIConstants.ICON_OPERATION, imageDescriptorFromPlugin(pluginId, "icons/full/obj16/Operation.gif"));
        reg.put(UIConstants.ICON_ATTRIBUTE, imageDescriptorFromPlugin(pluginId, "icons/full/obj16/Property.gif"));
        reg.put(UIConstants.ICON_ASSOCIATION,
                imageDescriptorFromPlugin(pluginId, "icons/full/obj16/Association_none.gif"));
        reg.put(UIConstants.ICON_DEPENDENCY, imageDescriptorFromPlugin(pluginId, "icons/full/obj16/Dependency.gif"));
        reg.put(UIConstants.ICON_COMPOSITION,
                imageDescriptorFromPlugin(pluginId, "icons/full/obj16/Association_composite.gif"));
        reg.put(UIConstants.ICON_AGGREGATION,
                imageDescriptorFromPlugin(pluginId, "icons/full/obj16/Association_shared.gif"));
        reg.put(UIConstants.ICON_IMPORT, imageDescriptorFromPlugin(pluginId, "icons/full/obj16/PackageImport.gif"));
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        RepositoryCache.getInstance().start();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        RepositoryCache.getInstance().stop();
        plugin = null;
    }
}