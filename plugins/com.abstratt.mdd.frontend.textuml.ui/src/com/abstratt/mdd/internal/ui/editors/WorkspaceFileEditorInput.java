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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;

public class WorkspaceFileEditorInput implements IFileEditorInput {

	protected IFileEditorInput parent;

	public WorkspaceFileEditorInput(IFileEditorInput parent) {
		this.parent = parent;
	}

	@Override
	public boolean exists() {
		return parent.exists();
	}

	@Override
	public Object getAdapter(Class adapter) {
		return parent.getAdapter(adapter);
	}

	@Override
	public IFile getFile() {
		return parent.getFile();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return parent.getImageDescriptor();
	}

	@Override
	public String getName() {
		return parent.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return parent.getPersistable();
	}

	@Override
	public IStorage getStorage() throws CoreException {
		return parent.getStorage();
	}

	@Override
	public String getToolTipText() {
		return parent.getToolTipText();
	}

	/**
	 * 
	 * @return a working copy, or <code>null</code>
	 */
	public WorkingCopy getWorkingCopy() {
		WorkingCopyRegistry registry = WorkingCopyRegistry.getInstance();
		return registry.getWorkingCopy(getFile());
	}
}