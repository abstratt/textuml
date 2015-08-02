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

package com.abstratt.mdd.internal.frontend.textuml.core;

import java.util.List;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Type;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAnySingleTypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.AQualifiedSingleTypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PTypeIdentifier;

/**
 * This deferred type resolver adds the resolved type to a target collection of types ({@link #target}.
 * 
 * @author vas
 */
public class DeferredCollectionFiller extends AbstractTypeResolver implements NodeProcessor<PTypeIdentifier> {
	class Visitor extends DepthFirstAdapter {

		@Override
		public void caseAAnySingleTypeIdentifier(AAnySingleTypeIdentifier node) {
			final Type anyType =
				(Type) getContext().getRepository().findNamedElement(TypeUtils.ANY_TYPE,
								IRepository.PACKAGE.getType(), null);
			if (anyType == null) {
				problemBuilder.addProblem(new UnresolvedSymbol(TypeUtils.ANY_TYPE), node);
				throw new AbortedStatementCompilationException();
			}
			addElement(anyType);
		}

		@Override
		public void caseAQualifiedSingleTypeIdentifier(AQualifiedSingleTypeIdentifier node) {
			super.caseAQualifiedSingleTypeIdentifier(node);
			TemplateBindingProcessor<Classifier, Type> tbp = new TemplateBindingProcessor<Classifier, Type>();
			tbp.process(node);
			parameterIdentifiers = tbp.getParameterIdentifiers();
			final String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getMinimalTypeIdentifier());
			Type type = resolveType(node.getMinimalTypeIdentifier(), qualifiedIdentifier);
			if (type != null) 
				addElement(type);
		}

		private void addElement(NamedElement element) {
			target.add(element);
		}

		@Override
		public void caseATypeIdentifier(ATypeIdentifier node) {
			node.getSingleTypeIdentifier().apply(this);
		}
		
	}
	private List<NamedElement> target;

	public DeferredCollectionFiller(SourceCompilationContext<Node> sourceContext, Namespace currentPackage, List<NamedElement> target ) {
		super(sourceContext, currentPackage);
		this.target = target;
	}

	public void process(final PTypeIdentifier node) {
		getContext().getReferenceTracker().add(new IDeferredReference() {
			public void resolve(IBasicRepository repository) {
				node.apply(new Visitor());
			}
		}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}
}
