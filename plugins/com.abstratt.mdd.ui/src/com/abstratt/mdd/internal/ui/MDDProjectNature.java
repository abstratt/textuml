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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.abstratt.mdd.ui.UIConstants;

public class MDDProjectNature implements IProjectNature {

	protected IProject project;

	public void configure() throws CoreException {
		// add mdd builder
		IProjectDescription description = project.getDescription();
		ICommand command = description.newCommand();
		command.setBuilderName(UIConstants.BUILDER_ID);
		description.setBuildSpec(new ICommand[] {command});
		project.setDescription(description, null);
	}

	public void deconfigure() throws CoreException {
		// remove mdd builder
		IProjectDescription description = project.getDescription();
		ICommand[] oldCommands = description.getBuildSpec();
		ICommand[] newCommands = new ICommand[oldCommands.length - 1];
		for (int i = 0, j = 0; i < oldCommands.length; i++) {
			ICommand command = oldCommands[i];
			if (!UIConstants.BUILDER_ID.equals(command.getBuilderName())) {
				newCommands[j++] = oldCommands[i];
			}
		}
		description.setBuildSpec(newCommands);
		project.setDescription(description, null);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
}