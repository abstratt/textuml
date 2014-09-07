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
package com.abstratt.mdd.internal.ui.model;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.internal.frontend.textuml.node.APrimitiveDef;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

public class PrimitiveType extends UIModelObject {

	public PrimitiveType(UIModelObject parent, ASTNode<Token, Node> node) {
		super(parent, node);
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(
				UIConstants.ICON_INTERFACE);
	}

	private APrimitiveDef getModel() {
		return (APrimitiveDef) node.getBaseNode();
	}

	public List getModelTargetConnections() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getOriginalText() {
		return getModel().getIdentifier().getText();
	}

	@Override
	public Token getToken() {
		APrimitiveDef definition = getModel();
		return definition.getIdentifier();
	}
}