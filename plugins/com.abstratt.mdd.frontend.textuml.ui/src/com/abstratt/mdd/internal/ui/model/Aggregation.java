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
import com.abstratt.mdd.internal.frontend.textuml.node.AAggregationAssociationKind;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

public class Aggregation extends AbstractAssociation {

	public Aggregation(UIModelObject parent, ASTNode node) {
		super(parent, node);
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(UIConstants.ICON_AGGREGATION);
	}

	protected AAggregationAssociationKind getModel() {
		return (AAggregationAssociationKind) node.getBaseNode();
	}

	@Override
	public Token getToken() {
		return getModel().getAggregation();
	}

}