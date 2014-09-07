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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.abstratt.mdd.ui.UIConstants;

/**
 * Creates and configures a new MDD project.
 */
public class CreateMDDProjectOperation implements IWorkspaceRunnable {

	protected String name;

	public CreateMDDProjectOperation(String name) {
		this.name = name;
	}

	public void run(IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			monitor.beginTask("Create MDD Project", 100);

			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			project.create(new SubProgressMonitor(monitor, 40));
			project.open(new SubProgressMonitor(monitor, 30));

			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] {UIConstants.NATURE_ID});
			project.setDescription(description, new SubProgressMonitor(monitor, 30));
		} finally {
			monitor.done();
		}
	}

}