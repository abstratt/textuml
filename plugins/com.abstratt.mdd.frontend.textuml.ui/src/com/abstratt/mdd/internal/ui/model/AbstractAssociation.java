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

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationHeader;

public abstract class AbstractAssociation extends LeafModelObject {

	protected UIModelObject source;
	protected UIModelObject target;

	public AbstractAssociation(UIModelObject parent, ASTNode node) {
		super(parent, node);
	}

	/**
	 * Assume source is index 0.
	 */
	public UIModelObject getSource() {
		return source;
	}

	/**
	 * Assume target is index 1.
	 */
	public UIModelObject getTarget() {
		return target;
	}

	@Override
	public String getOriginalText() {
		ASTNode parent = node.getParent();
		AAssociationHeader header = (AAssociationHeader) parent.getBaseNode();
		return header.getIdentifier() == null ? "" : header.getIdentifier().getText();
	}
}