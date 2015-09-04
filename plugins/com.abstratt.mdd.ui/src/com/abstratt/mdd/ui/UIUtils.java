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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.MDDCore;
import com.abstratt.mdd.internal.ui.RepositoryCache;

/**
 * General utilities for the MDD-Eclipse integration.
 */
public class UIUtils {

    /**
     * From a Behavioral or Structural file get the corresponding model file.
     */
    public static IFile getModelFile(IFile target) {
        String fileName = getModelName(target);
        IPath path = target.getProjectRelativePath().removeLastSegments(1);
        path = path.append(fileName).addFileExtension(UIConstants.FILE_EXTENSION);
        return target.getProject().getFile(path);
    }

    /**
     * For now we assume the file name without the extension is the model name.
     */
    public static String getModelName(IFile target) {
        String name = target.getFullPath().removeFileExtension().lastSegment();
        return name;
    }

    /**
     * Opens a repository under the given project. It is the client's
     * responsibility to dispose of the repository properly.
     * 
     * @param project
     * @return
     * @throws CoreException
     */
    public static IRepository getRepository(IProject project) throws CoreException {
        return MDDCore.createRepository(getRepositoryBaseURI(project));
    }

    /**
     * Returns a repository under the given project, caching it. Client should
     * not dispose of repository. Returned instance will be valid for a short
     * duration, client should not cache the reference.
     * 
     * @param project
     * @return a repository
     * @throws CoreException
     */
    public static IRepository getCachedRepository(IProject project) throws CoreException {
        URI projectRepositoryURI = getRepositoryBaseURI(project);
        return RepositoryCache.getInstance().getRepository(projectRepositoryURI);
    }

    public static URI getRepositoryBaseURI(IProject project) {
        return URI.createURI(project.getLocationURI().toString());
    }

    /**
     * Helper method getting an IStatus from an exception.
     */
    public static IStatus getStatus(Throwable e) {
        IStatus status = null;
        // reuse an existing status if available
        if (e instanceof CoreException) {
            status = ((CoreException) e).getStatus();
        } else {
            // create a new status
            String message = e.getMessage();
            if (message == null)
                message = ""; // null messages are not allowed
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, message, e);
        }
        return status;
    }

    /**
     * Helper method for logging exceptions.
     */
    public static void log(Exception e) {
        Activator.getDefault().getLog().log(getStatus(e));
    }

    /**
     * Wraps an exception in a CoreException and throws it instead.
     */
    public static void throwException(Exception e) throws CoreException {
        IStatus status = getStatus(e);
        throw new CoreException(status);
    }
}