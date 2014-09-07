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
import com.abstratt.mdd.internal.frontend.textuml.node.AClassDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassHeader;
import com.abstratt.mdd.internal.frontend.textuml.node.ADatatypeClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.AOperationDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

public class DataType extends UIModelObject {

	protected static Class<? extends Node>[] CHILDREN_TYPES = (Class<? extends Node>[]) new Class<?>[] { AOperationDecl.class };

	public DataType(UIModelObject parent, ASTNode<Token, Node> node) {
		super(parent, node);
	}

	// protected org.eclipse.uml2.uml.Class getClass_() {
	//		
	// }

	@Override
	public Class<? extends Node>[] getChildrenTypes() {
		return CHILDREN_TYPES;
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(
				UIConstants.ICON_DATA_TYPE);
	}

	private AClassDef getModel() {
		return (AClassDef) node.getBaseNode();
	}

	public List getModelTargetConnections() {
		return Collections.EMPTY_LIST;
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
		ADatatypeClassType type = (ADatatypeClassType) header.getClassType();
		return type.getDatatype();
	}
}