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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAggregationAssociationKind;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationAssociationKind;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationRoleDeclList;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAttributeDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassClassType;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.ACompositionAssociationKind;
import com.abstratt.mdd.frontend.textuml.grammar.node.ADependencyDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AReferenceDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;
import com.abstratt.mdd.internal.ui.TextUMLUIPlugin;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

public class Clazz extends UIModelObject {

    protected static Class<? extends Node>[] CHILDREN_TYPES = (Class<? extends Node>[]) new Class<?>[] {
            AAttributeDecl.class, AReferenceDecl.class, ADependencyDecl.class, AOperationDecl.class };

    public Clazz(UIModelObject parent, ASTNode<Token, Node> node) {
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
        return Activator.getDefault().getImageRegistry().get(UIConstants.ICON_CLASS);
    }

    private AClassDef getModel() {
        return (AClassDef) node.getBaseNode();
    }

    @Override
    public List<UIModelObject> getChildren() {
        List<UIModelObject> result = super.getChildren();
        if (TextUMLUIPlugin.getDefault().isPreferencePresentInEditorOptions(TextUMLUIPlugin.SHOW_ASSOCINCLASS)) {
            result.addAll(getAssociationsToClass());
        }
        return result;
    }

    private List<UIModelObject> getAssocationsFromModel() {
        List<ASTNode<Token, Node>> nodes = ASTUtils
                .findNodes(getRoot().getNode(), false, AAssociationDef.class, ACompositionAssociationKind.class,
                        AAssociationAssociationKind.class, AAggregationAssociationKind.class);
        List<UIModelObject> c = new ArrayList<UIModelObject>(nodes.size());
        for (Iterator<ASTNode<Token, Node>> iter = nodes.iterator(); iter.hasNext();) {
            ASTNode<Token, Node> node = iter.next();
            UIModelObject child = UIModelObject.createModelObject(this, node);
            if (child != null) {
                c.add(child);
            }

        }
        return c;
    }

    private List<AbstractAssociation> getAssociationsToClass() {
        List<AbstractAssociation> result = new ArrayList<AbstractAssociation>();
        List children = getAssocationsFromModel();
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (element instanceof AbstractAssociation) {
                AbstractAssociation child = (AbstractAssociation) element;
                AAssociationDef definition = (AAssociationDef) child.getNode().getParent().getParent().getBaseNode();
                AAssociationRoleDeclList roleList = (AAssociationRoleDeclList) definition.getAssociationRoleDeclList();
                // This is a pretty dummy check for the assocs of the class
                // TODO Make this much better in the future
                if (roleList.toString().indexOf(" " + this.getText() + " ") > -1) {
                    result.add(child);
                }
            }
        }
        return result;

    }

    public List<AbstractAssociation> getModelSourceConnections() {
        List<AbstractAssociation> result = new ArrayList<AbstractAssociation>();
        List<UIModelObject> children = getRoot().getChildren();
        for (Iterator<UIModelObject> iter = children.iterator(); iter.hasNext();) {
            UIModelObject child = iter.next();
            if (child instanceof AbstractAssociation) {
                AbstractAssociation association = (AbstractAssociation) child;
                if (association.getSource() == this) {
                    result.add(association);
                }
            }
        }
        return result;

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
        // String type = getTypeFrom(roleList);
        // if (clazzName.equals(type)) {
        // result.add(child);
        // }
        // }
        // }
        // return result;
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
        AClassClassType type = (AClassClassType) header.getClassType();
        return type.getClazz();
    }
}