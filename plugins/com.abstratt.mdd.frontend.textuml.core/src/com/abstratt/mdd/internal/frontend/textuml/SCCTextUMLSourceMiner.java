/*******************************************************************************
 * Copyright (c) 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/ 
package com.abstratt.mdd.internal.frontend.textuml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.uml2.uml.NamedElement;

import com.abstratt.mdd.frontend.core.spi.ISourceMiner;
import com.abstratt.mdd.internal.frontend.textuml.analysis.DepthFirstAdapter;
import com.abstratt.mdd.internal.frontend.textuml.node.AForcefullyQualifiedIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.AQualifiedIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.AQualifiedIdentifierBase;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.TIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.TNamespaceSeparator;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;

public class SCCTextUMLSourceMiner implements ISourceMiner<Node> {
	@SuppressWarnings("serial")
	private static class NodeFoundTrap extends RuntimeException {};

	private RuntimeException trap = new NodeFoundTrap();
	
	@Override
	public <P extends Node, C extends Node> C findChild(P node, final Class<C> required, final boolean first) {
		if (node == null)
			return null;
		final Node[] found = { null };
		try {
			node.apply(new DepthFirstAdapter() {
				// for non-terminal productions 
				public void defaultIn(Node node) {
					match(node);
				}
				// for tokens
				public void defaultCase(Node node) {
					match(node);
				}
				private void match(Node node) {
					if (required.isInstance(node)) {
						found[0] = node;
						if (first)
							throw trap;
					}
				}
			});
		} catch (NodeFoundTrap t) {
			// found 
		}
		return (C) found[0];
	}
	
	@Override
	public <P extends Node, C extends Node> List<C> findChildren(P node, final Class<C> required) {
		final List<C> found = new ArrayList<C>();
		if (node == null)
			return found;
		node.apply(new DepthFirstAdapter() {
			// for non-terminal productions 
			public void defaultIn(Node node) {
				match(node);
			}
			// for tokens
			public void defaultCase(Node node) {
				match(node);
			}
			private void match(Node node) {
				if (required.isInstance(node))
					found.add((C) node);
			}
		});
		return found;
	}


	@Override
	public <P extends Node, C extends Node> P findParent(C start, Class<P> required) {
		if (start == null)
			return null;
		if (required.isInstance(start.parent()))
			return (P) start.parent();
		return findParent(start.parent(), required);
	}

	@Override
	public int getLineNumber(Node node) {
		return findToken(node).getLine();
	}	

	@Override
	public String getText(Node node) {
		return node == null ? null : node.toString().trim();
	}
	
	/**
	 * Returns the last token found in the given tree or <code>null</code> if
	 * none is found (an empty production).
	 * 
	 * @return a token, or <code>null</code>
	 */
	public Token findToken(Node node) {
		return (Token) findChild(node, Token.class, false);
	}
	
	@Override
	public String getQualifiedIdentifier(Node node) {
		final String[] qualifiedIdentifier = { null };
		node.apply(new DepthFirstAdapter() {
			private boolean initialize() {
				if (qualifiedIdentifier[0] != null)
					// avoid processing multiple QIs
					return false;
				qualifiedIdentifier[0] = "";
				return true;
			}
			public void caseAQualifiedIdentifier(AQualifiedIdentifier node) {
				if (initialize())
				    super.caseAQualifiedIdentifier(node);
			}
			@Override
			public void caseAForcefullyQualifiedIdentifier(
					AForcefullyQualifiedIdentifier node) {
				if (initialize()) {
					appendIdentifier(node.getIdentifier());
    				super.caseAForcefullyQualifiedIdentifier(node);
				}
			}			
			public void caseAQualifiedIdentifierBase(
					AQualifiedIdentifierBase node) {
				Assert.isTrue(qualifiedIdentifier[0] != null);
				appendIdentifier(node.getIdentifier());
				super.caseAQualifiedIdentifierBase(node);
			}
			private void appendIdentifier(TIdentifier identifier) {
				qualifiedIdentifier[0] += stripEscaping(identifier.getText());
			}
			
			@Override
			public void caseTNamespaceSeparator(TNamespaceSeparator node) {
				Assert.isTrue(qualifiedIdentifier[0] != null);
				qualifiedIdentifier[0] += NamedElement.SEPARATOR;
			}
		});
		Assert.isNotNull(qualifiedIdentifier[0]);
		return qualifiedIdentifier[0];
	}
	
	@Override
	public String getIdentifier(Node node) {
		if (node == null)
			return null;
		final String[] identifier = { null };
		node.apply(new DepthFirstAdapter() {
			public void caseTIdentifier(TIdentifier node) {
				// ignore other identifiers under the given node
				if (identifier[0] == null)
					identifier[0] = stripEscaping(node.getText());
			}
		});
		return identifier[0];
	}	
	
	private String stripEscaping(String text) {
		return text.replace("\\", "");
	}

	public static int parseLineNumber(String errorMessage) {
        Matcher matcher = Pattern.compile("\\[([0-9]+),([0-9]+)\\].*").matcher(errorMessage);
        if (matcher.matches())
        	return Integer.parseInt(matcher.group(1));
        return -1;
	}
}
