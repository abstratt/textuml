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
package com.abstratt.mdd.internal.frontend.textuml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.ParameterableElement;
import org.eclipse.uml2.uml.TemplateableElement;

import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATemplateParameter;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATemplateParameterList;
import com.abstratt.mdd.frontend.textuml.grammar.node.PQualifiedIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.PSingleTypeIdentifier;

public class TemplateBindingProcessor<TE extends TemplateableElement, PE extends ParameterableElement> implements
        NodeProcessor<PSingleTypeIdentifier> {

	private List<PQualifiedIdentifier> parameterIdentifiers;

	public TemplateBindingProcessor() {
		super();
	}

	public void process(PSingleTypeIdentifier node) {
		node.apply(new Visitor());
	}

	private class Visitor extends DepthFirstAdapter {
		@Override
		public void caseATemplateParameter(ATemplateParameter node) {
			parameterIdentifiers.add(node.getQualifiedIdentifier());
		}

		@Override
		public void caseATemplateParameterList(ATemplateParameterList node) {
			parameterIdentifiers = new ArrayList<PQualifiedIdentifier>();
			super.caseATemplateParameterList(node);
		}
	}

	public List<PQualifiedIdentifier> getParameterIdentifiers() {
		return parameterIdentifiers;
	}
}
