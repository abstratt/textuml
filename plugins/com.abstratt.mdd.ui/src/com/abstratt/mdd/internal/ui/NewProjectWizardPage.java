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


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.ImportResourcesAction;

import com.abstratt.mdd.ui.Activator;

public class NewProjectWizardPage extends WizardPage {

	protected Text nameText;

	public NewProjectWizardPage() {
		super("New MDD Project");
		setDescription("Create a new MDD project.");
		setImageDescriptor(Activator.getImageDescriptor("icons/full/wizban/new_mdd_prj_wiz.png"));
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		setControl(composite);

		Label nameLabel = new Label(composite, SWT.LEFT);
		nameLabel.setText("Project name:");
		nameLabel.setLayoutData(new GridData(GridData.BEGINNING));

		nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		nameText.setFocus(); // initial widget with focus
		nameText.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				IPath projectPath = new Path(getProjectName().trim());
				boolean isValid = false;
				setMessage(null);
				if (projectPath.segmentCount() != 1 || !ResourcesPlugin.getWorkspace().validateName(projectPath.segment(0), IResource.PROJECT).isOK())
					setMessage("Enter a valid project name", ERROR);
				else if (ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath.segment(0)).exists())
					setMessage("A project with that name already exists", ERROR);
				else
					isValid = true;
				setPageComplete(isValid);
			}

		});
	}

	public String getProjectName() {
		return nameText.getText();
	}

}