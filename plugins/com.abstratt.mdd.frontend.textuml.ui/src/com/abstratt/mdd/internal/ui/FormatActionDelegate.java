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

import com.abstratt.mdd.internal.ui.editors.TextUMLEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class FormatActionDelegate implements IEditorActionDelegate {
	private IEditorPart activeEditor;
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.activeEditor = targetEditor;
	}

	public void run(IAction action) {
		if (activeEditor instanceof TextUMLEditor)
			((TextUMLEditor) activeEditor).format();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// we don't support selection
	}
	
	
}
