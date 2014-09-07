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

import java.util.List;

import org.eclipse.jface.viewers.TreeNode;

import com.abstratt.mdd.internal.ui.model.UIModelObject;

public class TextUMLTreeNode extends TreeNode {

	public TextUMLTreeNode(UIModelObject model) {
		super(model);
		List children = model.getChildren();
		if (!children.isEmpty()) {
			TextUMLTreeNode[] nodes = new TextUMLTreeNode[children.size()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = new TextUMLTreeNode((UIModelObject) children.get(i));
			}
			setChildren(nodes);
		}
	}

	// for debug
	public String toString() {
		if (value == null)
			return "[null]";
		return value.toString();
	}
}