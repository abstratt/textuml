/*******************************************************************************
 * Copyright (c) 2009 Vladimir Sosnin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Sosnin - initial API and implementation
 *******************************************************************************/

package com.abstratt.mdd.internal.ui.model;

import org.eclipse.swt.graphics.Image;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.frontend.textuml.grammar.node.ADependencyDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PTypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

/**
 * @author vas
 *
 */
public class Dependency extends LeafModelObject {

    public Dependency(UIModelObject parent, ASTNode<Token, Node> node) {
        super(parent, node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abstratt.mdd.internal.ui.model.UIModelObject#getImage()
     */
    @Override
    public Image getImage() {
        return Activator.getDefault().getImageRegistry().get(UIConstants.ICON_DEPENDENCY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abstratt.mdd.internal.ui.model.UIModelObject#getOriginalText()
     */
    protected ADependencyDecl getModel() {
        return (ADependencyDecl) node.getBaseNode();
    }

    @Override
    public String getOriginalText() {
        ADependencyDecl declaration = getModel();
        StringBuffer text = new StringBuffer();
        PTypeIdentifier type = declaration.getTypeIdentifier();
        text.append(type);
        return text.toString();
    }

    @Override
    public Token getToken() {
        return getModel().getDependency();
    }

}
