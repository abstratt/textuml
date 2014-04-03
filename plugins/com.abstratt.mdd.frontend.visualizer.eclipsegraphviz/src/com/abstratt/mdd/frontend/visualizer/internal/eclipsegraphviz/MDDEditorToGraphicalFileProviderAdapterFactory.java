/*******************************************************************************
 * Copyright (c) 2006, 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/ 
package com.abstratt.mdd.frontend.visualizer.internal.eclipsegraphviz;

import java.lang.ref.WeakReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;

import com.abstratt.imageviewer.IGraphicalFileProvider;
import com.abstratt.mdd.frontend.ui.IMDDEditor;

public class MDDEditorToGraphicalFileProviderAdapterFactory implements
		IAdapterFactory {

	private static class GraphicalContentProvider implements
			IGraphicalFileProvider {
		private WeakReference<IMDDEditor> editorReference;

		private GraphicalContentProvider(IMDDEditor editor) {
			editorReference = new WeakReference<IMDDEditor>(editor);
		}

		public IFile getGraphicalFile() {
			IMDDEditor editor = editorReference.get();
			return editor == null ? null : editor.getModelFile();
		}
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IGraphicalFileProvider.class
				&& adaptableObject instanceof IMDDEditor)
			return new GraphicalContentProvider(
					(IMDDEditor) adaptableObject);
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IGraphicalFileProvider.class };
	}

}
