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
package com.abstratt.mdd.frontend.core;

public interface ASTVisitor<T, N> {

    /**
     * Visits a node in the AST.
     * 
     * @param node
     *            the node we are visiting
     * @return what to do next (continue, skip children, stop visiting)
     */
    ASTNode.VisitorResult visit(ASTNode<T, N> node);
}