/*******************************************************************************
 * Copyright (c) 2006, 2009 Abstratt Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.frontend.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.texteditor.ITextEditor;

public interface IMDDEditor extends ITextEditor {
    public IFile getModelFile();
}