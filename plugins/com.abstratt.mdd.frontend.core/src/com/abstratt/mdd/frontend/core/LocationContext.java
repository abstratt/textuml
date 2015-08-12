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
package com.abstratt.mdd.frontend.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;

/**
 * Provides all the location-related context required by the compilation
 * process.
 */
public class LocationContext {

    private IFileStore defaultOutputPath;
    private List<IFileStore> relatedPaths = new ArrayList<IFileStore>();
    private List<IFileStore> outputPaths = new ArrayList<IFileStore>();
    private List<IFileStore> sourcePaths = new ArrayList<IFileStore>();

    /**
     * Creates a compilation context having the given path as the default output
     * path.
     * 
     * @param defaultOutputPath
     */
    public LocationContext(IFileStore defaultOutputPath) {
        Assert.isNotNull(defaultOutputPath);
        this.defaultOutputPath = defaultOutputPath;
    }

    /**
     * Adds the given related path.
     * 
     * @param relatedPath
     */
    public void addRelatedPath(IFileStore relatedPath) {
        if (relatedPaths.indexOf(relatedPath) == -1)
            relatedPaths.add(relatedPath);
    }

    /**
     * Adds the given source path with an optional output path.
     * 
     * @param sourcePath
     * @param outputPath
     */
    public void addSourcePath(IFileStore sourcePath, IFileStore outputPath) {
        int index = sourcePaths.indexOf(sourcePath);
        if (index >= 0) {
            outputPaths.set(index, outputPath);
            return;
        }
        sourcePaths.add(sourcePath);
        outputPaths.add(outputPath);
    }

    /**
     * Returns the default output path.
     * 
     * @return
     */
    public IFileStore getDefaultOutputPath() {
        return defaultOutputPath;
    }

    /**
     * Returns the output path corresponding to the given source path, or
     * <code>null</code>.
     * 
     * @param sourcePath
     * @return
     */
    public IFileStore getOutputPath(IFileStore sourcePath) {
        int index = sourcePaths.indexOf(sourcePath);
        return index == -1 ? null : (IFileStore) outputPaths.get(index);
    }

    /**
     * Returns all output paths.
     * 
     * @return
     */
    public IFileStore[] getOutputPaths() {
        Set<IFileStore> result = new HashSet<IFileStore>(sourcePaths.size());
        for (IFileStore source : sourcePaths) {
            IFileStore output = getOutputPath(source);
            if (output != null)
                result.add(output);
        }
        if (defaultOutputPath != null)
            result.add(defaultOutputPath);
        return copy(result);
    }

    /**
     * Returns all source paths.
     * 
     * @return
     */
    public IFileStore[] getSourcePaths() {
        return copy(sourcePaths);
    }

    /**
     * Returns all related paths.
     * 
     * @return
     */
    public IFileStore[] getRelatedPaths() {
        return copy(relatedPaths);
    }

    private IFileStore[] copy(Collection<IFileStore> stores) {
        return stores.toArray(new IFileStore[stores.size()]);
    }

    /**
     * Sets the given output path as default.
     * 
     * @param outputPath
     */
    public void setDefaultOutputPath(IFileStore outputPath) {
        this.defaultOutputPath = outputPath;
    }
}