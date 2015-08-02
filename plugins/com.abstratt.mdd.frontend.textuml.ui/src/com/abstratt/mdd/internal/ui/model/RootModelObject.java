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
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationAssociationKind;
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassDef;
import com.abstratt.mdd.internal.frontend.textuml.node.ACompositionAssociationKind;
import com.abstratt.mdd.internal.frontend.textuml.node.APrimitiveDef;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;
import com.abstratt.mdd.internal.ui.TextUMLUIPlugin;

public class RootModelObject extends UIModelObject {

	protected static Class[] CHILDREN_TYPES = new Class[] {
	/*AImportDecl.class,*/AClassDef.class, AAssociationDef.class, ACompositionAssociationKind.class, AAssociationAssociationKind.class, AAggregationAssociationKind.class, APrimitiveDef.class};

	protected static Class[] CHILDREN_TYPES_NOASSOCS = new Class[] {
		/*AImportDecl.class,*/AClassDef.class,  APrimitiveDef.class};
	
	
	public RootModelObject(UIModelObject parent, ASTNode node) {
		super(parent, node);
	}

	@Override
	public Class[] getChildrenTypes() {
		if(TextUMLUIPlugin.getDefault().isPreferencePresentInEditorOptions(TextUMLUIPlugin.SHOW_ASSOCINCLASS)) {
			return CHILDREN_TYPES_NOASSOCS;
		}
		return CHILDREN_TYPES;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getOriginalText() {
		return "[root]";
	}

	@Override
	public Token getToken() {
		return null;
	}
}