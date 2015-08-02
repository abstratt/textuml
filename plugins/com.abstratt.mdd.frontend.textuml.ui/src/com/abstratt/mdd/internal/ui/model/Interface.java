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
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.AInterfaceClassType;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

public class Interface extends UIModelObject {

	protected static Class<? extends Node>[] CHILDREN_TYPES = (Class<? extends Node>[]) new Class<?>[] { AOperationDecl.class };

	public Interface(UIModelObject parent, ASTNode<Token, Node> node) {
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
				UIConstants.ICON_INTERFACE);
	}

	private AClassDef getModel() {
		return (AClassDef) node.getBaseNode();
	}

	public List getModelTargetConnections() {
		return Collections.EMPTY_LIST;
		// String clazzName = clazz.getText();
		// List<AbstractAssociation> result = new
		// ArrayList<AbstractAssociation>();
		// List children = getChildren();
		// for (Iterator iter = children.iterator(); iter.hasNext();) {
		// Object element = iter.next();
		// if (element instanceof AbstractAssociation) {
		// AbstractAssociation child = (AbstractAssociation)element;
		// AAssociationDef definition = (AAssociationDef)
		// child.getNode().getParent().getParent().getBaseNode();
		// AAssociationRoleDeclList roleList = (AAssociationRoleDeclList)
		// definition.getAssociationRoleDeclList();
		// AAssociationRoleDeclList anotherList = (AAssociationRoleDeclList)
		// roleList.getAssociationRoleDeclList();
		// String type = getTypeFrom(anotherList);
		// if (clazzName.equals(type)) {
		// result.add(child);
		// }
		// }
		// }
		// return result;
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
		AInterfaceClassType type = (AInterfaceClassType) header.getClassType();
		return type.getInterface();
	}
}