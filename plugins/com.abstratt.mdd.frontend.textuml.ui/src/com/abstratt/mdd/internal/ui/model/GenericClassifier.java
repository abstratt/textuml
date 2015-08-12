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

import org.eclipse.swt.graphics.Image;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

public class GenericClassifier extends UIModelObject {

	public GenericClassifier(UIModelObject parent, ASTNode<Token, Node> node) {
		super(parent, node);
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(UIConstants.ICON_CLASS);
	}

	private AClassDef getModel() {
		return (AClassDef) node.getBaseNode();
	}

	@Override
	public String getOriginalText() {
		AClassHeader header = (AClassHeader) getModel().getClassHeader();
		return header.getIdentifier().getText();
	}

	@Override
	public Token getToken() {
		AClassDef definition = getModel();
		AClassHeader header = (AClassHeader) definition.getClassHeader();
		final Token[] token = { null };
		header.apply(new DepthFirstAdapter() {
			public void defaultCase(Node node) {
				token[0] = (Token) node;
			}
		});
		return token[0];
	}
}