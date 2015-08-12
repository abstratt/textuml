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
package com.abstratt.mdd.internal.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.abstratt.mdd.internal.ui.editors.source.SourceEditor;
import com.abstratt.mdd.ui.UIUtils;

public class TextUMLEditor extends MultiPageEditorPart {

	public final static String PLUGIN_ID = "com.abstratt.mdd.frontend.textuml.ui";

	protected SourceEditor sourceEditor;

	public TextUMLEditor() {
		super();
	}

	protected IEditorInput createEditorInput() {
		return new WorkspaceFileEditorInput((IFileEditorInput) getEditorInput());
	}

	@Override
	protected void createPages() {
		try {
			sourceEditor = new SourceEditor();
			int index = addPage(sourceEditor, createEditorInput());
			setPageText(index, "Source");

			// GraphicalEditor graphicalEditor = new GraphicalEditor();
			// index = addPage(graphicalEditor, createEditorInput());
			// setPageText(index, "Graphical");

		} catch (PartInitException e) {
			UIUtils.log(e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		sourceEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		sourceEditor.doSaveAs();
	}

	public void format() {
		sourceEditor.format();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return sourceEditor.isSaveAsAllowed();
	}

	// TODO: fix the nested editor inputs when the main one changes
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
	}

}