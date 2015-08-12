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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class RunnableWithProgressWrapper implements IRunnableWithProgress {

	protected IWorkspaceRunnable workspaceRunnable;

	public RunnableWithProgressWrapper(IWorkspaceRunnable workspaceRunnable) {
		this.workspaceRunnable = workspaceRunnable;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			if (workspaceRunnable != null) {
				workspaceRunnable.run(monitor);
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}
}