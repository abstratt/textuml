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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.internal.frontend.textuml.Util;
import com.abstratt.mdd.internal.frontend.textuml.node.AAggregationAssociationKind;
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationAssociationKind;
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AAttributeDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassHeader;
import com.abstratt.mdd.internal.frontend.textuml.node.ACompositionAssociationKind;
import com.abstratt.mdd.internal.frontend.textuml.node.ADatatypeClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.ADependencyDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AImportDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AInterfaceClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.AOperationDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.APrimitiveDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AReferenceDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.PClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.Start;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;
import com.abstratt.mdd.internal.ui.TextUMLUIPlugin;
import com.abstratt.mdd.internal.ui.editors.WorkingCopy;
import com.abstratt.mdd.internal.ui.editors.WorkingCopyRegistry;

/**
 * The UI model representation.
 */
public abstract class UIModelObject {

	protected List<UIModelObject> children;

	protected ASTNode<Token, Node> node;
	protected UIModelObject parent;
	public static UIModelObject createModelObject(UIModelObject parent, ASTNode<Token, Node> node) {
		if (node.instanceOf(Start.class)) {			
			return new RootModelObject(parent, node);
		}
		if (node.instanceOf(AClassDef.class)) {
			PClassType classType = ((AClassHeader) ((AClassDef) node.getBaseNode()).getClassHeader()).getClassType();
			if (classType instanceof AClassClassType)
				return new Clazz(parent, node);
			if (classType instanceof AInterfaceClassType)
				return new Interface(parent, node);
			if (classType instanceof ADatatypeClassType) {
				if(!TextUMLUIPlugin.getDefault().isPreferencePresentInEditorOptions(TextUMLUIPlugin.SHOW_DATATYPE)) {
					return null;
				}
				return new DataType(parent, node);
			}
				
				
		}
		if (node.instanceOf(APrimitiveDef.class)) {
			return new PrimitiveType(parent, node);
		}
		if (node.instanceOf(AAttributeDecl.class)) {
			if(!TextUMLUIPlugin.getDefault().isPreferencePresentInEditorOptions(TextUMLUIPlugin.SHOW_ATTR)) {
				return null;
			}
			return new Attribute(parent, node);
		}
		if (node.instanceOf(AReferenceDecl.class)) {
			return new Reference(parent, node);
		}
		if (node.instanceOf(ADependencyDecl.class)) {
			if(!TextUMLUIPlugin.getDefault().isPreferencePresentInEditorOptions(TextUMLUIPlugin.SHOW_DEPS)) {
				return null;
			}
			return new Dependency(parent, node);
		}
		if (node.instanceOf(AOperationDecl.class)) {
			if(!TextUMLUIPlugin.getDefault().isPreferencePresentInEditorOptions(TextUMLUIPlugin.SHOW_OP)) {
				return null;
			}
			return new Operation(parent, node);
		}
		if (node.instanceOf(ACompositionAssociationKind.class)) {
			return new Composition(parent, node);
		}
		if (node.instanceOf(AAssociationDef.class)) {
			return null;
		}
		if (node.instanceOf(AAssociationAssociationKind.class)) {
			return new Association(parent, node);
		}
		if (node.instanceOf(AAggregationAssociationKind.class)) {
			return new Aggregation(parent, node);
		}
		if (node.instanceOf(AImportDecl.class)) {
			return new Import(parent, node);
		}
		throw new IllegalArgumentException("Unexpected object type: " + node.getBaseNode().getClass());
	}

	public UIModelObject(UIModelObject parent, ASTNode<Token, Node> node) {
		this.parent = parent;
		this.node = node;
	}

	public List<UIModelObject> getChildren() {
		if (children == null) {
			List<ASTNode<Token, Node>> nodes = ASTUtils.findNodes(getNode(), false, getChildrenTypes());
			children = new ArrayList<UIModelObject>(nodes.size());
			for (Iterator<ASTNode<Token, Node>> iter = nodes.iterator(); iter.hasNext();) {
				ASTNode<Token, Node> node = iter.next();
				UIModelObject child = UIModelObject.createModelObject(UIModelObject.this, node);
				if(child != null) {
					children.add(child);
				}
				
			}
		}
		return children;
	}

	protected Class<? extends Node>[] getChildrenTypes() {
		return (Class<? extends Node>[]) new Class<?>[0];
	}

	abstract public Image getImage();

	public ASTNode<Token, Node> getNode() {
		return node;
	}

	public UIModelObject getParent() {
		return parent;
	}

	/**
	 * Returns the root object in this structure.
	 */
	protected UIModelObject getRoot() {
		if (parent == null) {
			return this;
		}
		return parent.getRoot();
	}

	abstract public String getOriginalText();

	abstract public Token getToken();
	
	public String getText() {
		return Util.stripEscaping(getOriginalText());
	}

	/**
	 * Returns the working copy that contains this model object.
	 */
	protected WorkingCopy getWorkingCopy() {
		return WorkingCopyRegistry.getInstance().getWorkingCopy(getNode());
	}

	// for debug only
	@Override
	public String toString() {
		return getText();
	}
}