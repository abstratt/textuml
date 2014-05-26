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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.graphics.Image;

import com.abstratt.mdd.internal.ui.model.UIModelObject;

public class TextUMLLabelProvider extends LabelProvider {

	public Image getImage(Object element) {
		UIModelObject model = (UIModelObject) ((TreeNode) element).getValue();
		return model.getImage();
	}

	public String getText(Object element) {
		UIModelObject model = (UIModelObject) ((TreeNode) element).getValue();
		return model.getText();
	}
}