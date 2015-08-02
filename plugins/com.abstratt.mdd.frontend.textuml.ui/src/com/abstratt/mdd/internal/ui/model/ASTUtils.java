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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.frontend.core.ASTNode.VisitorResult;
import com.abstratt.mdd.frontend.core.ASTReverseVisitor;
import com.abstratt.mdd.frontend.core.ASTVisitor;
import com.abstratt.mdd.internal.frontend.textuml.node.APackageHeading;
import com.abstratt.mdd.internal.frontend.textuml.node.AQualifiedIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.AQualifiedIdentifierBase;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;

public class ASTUtils {

	/**
	 * Finds child nodes that match the given types.
	 */
	public static List<ASTNode<Token, Node>> findNodes(ASTNode<Token, Node> root, boolean reverse, final Class<? extends Node>... types) {
		final List<ASTNode<Token, Node>> result = new ArrayList<ASTNode<Token, Node>>();
		final Set<Class<?>> typeSet = new HashSet<Class<?>>(Arrays.asList(types));
		if (reverse) {
			root.accept(new ASTReverseVisitor<Token, Node>() {
				public boolean visit(ASTNode<Token, Node> node) {
					if (typeSet.contains(node.getBaseNode().getClass()))
						result.add(node);
					return true;
				}
			});
		} else {
			root.accept(new ASTVisitor<Token, Node>() {
				public ASTNode.VisitorResult visit(ASTNode<Token, Node> node) {
					if (typeSet.contains(node.getBaseNode().getClass())) {
						result.add(node);
					}
					return VisitorResult.CONTINUE;
				}
			});
		}
		return result;
	}

	public static String getModelName(ASTNode<Token, Node> target) {
		List<ASTNode<Token, Node>> nodes = ASTUtils.findNodes(target, false, APackageHeading.class);
		ASTNode modelNode = nodes.get(0);
		APackageHeading heading = (APackageHeading) modelNode.getBaseNode();
		AQualifiedIdentifier qId = (AQualifiedIdentifier) heading.getQualifiedIdentifier();
		AQualifiedIdentifierBase base = (AQualifiedIdentifierBase) qId.getQualifiedIdentifierBase();
		return base.getIdentifier().getText();
	}

	/**
	 * Given an ASTNode returns the root node.
	 */
	public static ASTNode<Token, Node> getRootNode(ASTNode<Token, Node> node) {
		if (node.getParent() == null) {
			return node;
		}
		return getRootNode(node.getParent());
	}

}