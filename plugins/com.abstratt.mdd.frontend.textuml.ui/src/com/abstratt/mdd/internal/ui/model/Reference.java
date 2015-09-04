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
import com.abstratt.mdd.frontend.textuml.grammar.node.AAggregationReferenceType;
import com.abstratt.mdd.frontend.textuml.grammar.node.ACompositionReferenceType;
import com.abstratt.mdd.frontend.textuml.grammar.node.AReferenceDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PReferenceType;
import com.abstratt.mdd.frontend.textuml.grammar.node.PTypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

public class Reference extends LeafModelObject {

    private PReferenceType type;

    public Reference(UIModelObject parent, ASTNode<Token, Node> node) {
        super(parent, node);
        this.type = ((AReferenceDecl) node.getBaseNode()).getReferenceType();
    }

    @Override
    public Image getImage() {
        if (type instanceof AAggregationReferenceType)
            return Activator.getDefault().getImageRegistry().get(UIConstants.ICON_AGGREGATION);
        if (type instanceof ACompositionReferenceType)
            return Activator.getDefault().getImageRegistry().get(UIConstants.ICON_COMPOSITION);
        return Activator.getDefault().getImageRegistry().get(UIConstants.ICON_ASSOCIATION);
    }

    protected AReferenceDecl getModel() {
        return (AReferenceDecl) node.getBaseNode();
    }

    @Override
    public String getOriginalText() {
        AReferenceDecl declaration = getModel();
        StringBuffer text = new StringBuffer();
        text.append(declaration.getIdentifier().getText());
        PTypeIdentifier type = declaration.getTypeIdentifier();
        text.append(" : ");
        text.append(type);
        return text.toString();
    }

    @Override
    public Token getToken() {
        return getModel().getIdentifier();
    }

}