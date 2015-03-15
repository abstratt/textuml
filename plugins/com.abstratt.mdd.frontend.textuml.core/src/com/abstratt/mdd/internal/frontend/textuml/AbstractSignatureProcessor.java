/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/ 
package com.abstratt.mdd.internal.frontend.textuml;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.internal.frontend.textuml.analysis.DepthFirstAdapter;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.PTypeIdentifier;

public abstract class AbstractSignatureProcessor extends DepthFirstAdapter {
	protected final CompilationContext context;
	protected final ProblemBuilder<Node> problemBuilder;
	protected final Namespace parent;
	protected final boolean supportExceptions;
	protected final boolean unnamedParameters;
    protected SourceCompilationContext<Node> sourceContext;

	public AbstractSignatureProcessor(SourceCompilationContext<Node> sourceContext, Namespace parent, boolean supportExceptions) {
		this(sourceContext, parent, supportExceptions, false);
	}

	public AbstractSignatureProcessor(SourceCompilationContext<Node> sourceContext, Namespace parent, boolean supportExceptions, boolean unnamedParameters) {
		this.parent = parent;
		this.supportExceptions = supportExceptions;
		this.sourceContext = sourceContext;
		this.context = sourceContext.getContext();
		this.problemBuilder = new ProblemBuilder<Node>(context.getProblemTracker(), new SCCTextUMLSourceMiner());
		this.unnamedParameters = unnamedParameters;
	}

	protected void addRaisedException(
	Classifier exceptionClass) {
		throw new UnsupportedOperationException();
	}

	public void addRaisedException(String qualifiedIdentifier, final Node node) {
		Classifier exceptionClass = (Classifier) context.getRepository().findNamedElement(qualifiedIdentifier, UMLPackage.Literals.CLASSIFIER, parent.getNearestPackage());
		if (exceptionClass == null) {
			problemBuilder.addError("Could not find exception class: '" + qualifiedIdentifier + "'", node);
			return;
		}
		addRaisedException(exceptionClass);
	}

	protected abstract Parameter createParameter(String name);

	protected Parameter createParameter(String name, PTypeIdentifier typeNode, ParameterDirectionKind direction) {
		if (name == null && direction != ParameterDirectionKind.RETURN_LITERAL && !unnamedParameters) {
			problemBuilder.addError("Parameter names are required in this context", typeNode.parent());
			return null;
		}
		Parameter parameter = createParameter(name);
		parameter.setDirection(direction);
		createParameterTypeSetter(parameter).process(typeNode);
		return parameter;
	}

    protected TypeSetter createParameterTypeSetter(Parameter parameter) {
        return new TypeSetter(sourceContext, getBaseLookupNamespace(), parameter);
    }

	protected Namespace getBaseLookupNamespace() {
		return this.parent;
	}

}
