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

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.abstratt.mdd.ui.UIUtils;


/**
 * Creates a new MDD project.
 */
public class NewProjectWizard extends Wizard implements INewWizard {

	protected NewProjectWizardPage page;

	public NewProjectWizard() {
		super();
		setWindowTitle("New MDD Project");
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		super.addPages();
		addPage(page);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new NewProjectWizardPage();
	}

	public boolean performFinish() {
		String name = page.getProjectName();
		CreateMDDProjectOperation operation = new CreateMDDProjectOperation(name);
		try {
			getContainer().run(false, false, new RunnableWithProgressWrapper(operation));
			return true;
		} catch (InvocationTargetException e) {
			UIUtils.log(e);
			ErrorDialog.openError(getShell(), "Problem creating project", "A problem occurred during the project creation.", UIUtils.getStatus(e));
		} catch (InterruptedException e) {
			UIUtils.log(e);
		}
		return false;
	}
	
	@Override
	public boolean canFinish() {
		return super.canFinish();
	}
}