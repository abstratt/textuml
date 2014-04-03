/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Vladimir Sosnin - extracted AbstractTypeResolver class (#2797252) 
 *******************************************************************************/ 
package com.abstratt.mdd.internal.frontend.textuml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.TemplateParameter;
import org.eclipse.uml2.uml.TemplateSignature;
import org.eclipse.uml2.uml.Type;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.util.TemplateUtils;
import com.abstratt.mdd.frontend.core.IncompatibleTemplateParameter;
import com.abstratt.mdd.frontend.core.NotATemplate;
import com.abstratt.mdd.frontend.core.UnboundTemplate;
import com.abstratt.mdd.frontend.core.UnknownType;
import com.abstratt.mdd.frontend.core.WrongNumberOfArguments;
import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.PQualifiedIdentifier;

public abstract class AbstractTypeResolver {

	public AbstractTypeResolver(CompilationContext context,
			Namespace currentNamespace) {
		this.context = context;
		this.problemBuilder = new ProblemBuilder<Node>(context.getProblemTracker(), new SCCTextUMLSourceMiner());
		this.currentNamespace = currentNamespace;
	}

	final private CompilationContext context;
	final protected ProblemBuilder<Node> problemBuilder;
	final private Namespace currentNamespace;
	protected List<PQualifiedIdentifier> parameterIdentifiers;

	protected CompilationContext getContext() {
		return context;
	}

	protected Namespace getCurrentNamespace() {
		return currentNamespace;
	}

	protected Type resolveType(final Node node, final String qualifiedIdentifier) {
		Type type = findType(qualifiedIdentifier);
		return basicResolveType(node, qualifiedIdentifier, type);
	}

	Type findType(final String qualifiedIdentifier) {
		return (Type) getContext().getRepository().findNamedElement(qualifiedIdentifier, IRepository.PACKAGE.getType(), getCurrentNamespace());
	}

	private Type basicResolveType(final Node node, final String qualifiedIdentifier, Type type) {
		if (type == null) {
			problemBuilder.addProblem(new UnknownType(qualifiedIdentifier), node);
			return null;
		}
		if (type instanceof Classifier) {
			final Classifier asClassifier = ((Classifier) type);
			if (asClassifier.isTemplate()) {
				if (this.parameterIdentifiers == null) {
					problemBuilder.addProblem(new UnboundTemplate(qualifiedIdentifier), node);
					return null;
				}
				type = createBinding(node, asClassifier);
			} else if (this.parameterIdentifiers != null) {
				problemBuilder.addProblem(new NotATemplate(qualifiedIdentifier), node);
				return null;
			}
		}
		return type;
	}

	private Classifier createBinding(final Node node, final Classifier template) {
		TemplateSignature signature = template.getOwnedTemplateSignature();
		List<TemplateParameter> formalParameters = signature.getParameters();
		final int parameterCount = formalParameters.size();
		if (parameterCount != this.parameterIdentifiers.size()) {
			problemBuilder.addProblem(new WrongNumberOfArguments(parameterCount, this.parameterIdentifiers.size()), node);
			return null;
		}
		List<Type> templateParameterTypes = new ArrayList<Type>(parameterCount);
		for (int i = 0; i < parameterCount; i++) {
			final String templateParameterName = TextUMLCore.getSourceMiner().getQualifiedIdentifier(parameterIdentifiers.get(i));
			final Type resolvedParameterType = findType(templateParameterName);
			if (resolvedParameterType == null) {
				problemBuilder.addProblem(new UnknownType(templateParameterName), parameterIdentifiers.get(i));
				return null;
			}
			if (!signature.getParameters().get(i).getParameteredElement().isCompatibleWith(resolvedParameterType)) {
				problemBuilder.addProblem(new IncompatibleTemplateParameter(), parameterIdentifiers.get(i));
				return null;
			}
			templateParameterTypes.add(resolvedParameterType);
		}
		// now we know the actual parameters match the formal ones - let's create the bound element and the template binding
		Classifier bound = TemplateUtils.createBinding(this.getCurrentNamespace().getNearestPackage(), template, templateParameterTypes);
		bound.setName(TemplateUtils.generateBoundElementName(template, templateParameterTypes));
		return bound;
	}

}