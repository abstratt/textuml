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

import java.util.Set;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.ParameterEffectKind;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.NamedElementUtils;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.internal.frontend.textuml.analysis.DepthFirstAdapter;
import com.abstratt.mdd.internal.frontend.textuml.node.AComplexInitializationExpression;
import com.abstratt.mdd.internal.frontend.textuml.node.AOptionalReturnType;
import com.abstratt.mdd.internal.frontend.textuml.node.AParamDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.ARaisedExceptionItem;
import com.abstratt.mdd.internal.frontend.textuml.node.ASimpleInitialization;
import com.abstratt.mdd.internal.frontend.textuml.node.ASimpleInitializationExpression;
import com.abstratt.mdd.internal.frontend.textuml.node.ASimpleOptionalReturnType;
import com.abstratt.mdd.internal.frontend.textuml.node.ASimpleParamDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.POptionalParameterName;
import com.abstratt.mdd.internal.frontend.textuml.node.PTypeIdentifier;

public abstract class SignatureProcessor extends AbstractSignatureProcessor {
	
	private ModifierProcessor modifierProcessor = new ModifierProcessor(new SCCTextUMLSourceMiner()); 

	public SignatureProcessor(SourceCompilationContext<Node> sourceContext, Namespace parent, boolean supportExceptions) {
		super(sourceContext, parent, supportExceptions);
	}

	public SignatureProcessor(SourceCompilationContext<Node> sourceContext, Namespace parent, boolean supportExceptions, boolean unnamedParameters) {
		super(sourceContext, parent, supportExceptions, unnamedParameters);
	}

	@Override
	public void caseASimpleOptionalReturnType(ASimpleOptionalReturnType node) {
		createReturnFromNode(node);
	}

	private Parameter createReturnFromNode(ASimpleOptionalReturnType node) {
		PTypeIdentifier typeIdentifier = node.getTypeIdentifier();
		return createParameter(null, typeIdentifier, ParameterDirectionKind.RETURN_LITERAL);
	}
	
	@Override
	public void caseAOptionalReturnType(AOptionalReturnType node) {
		Parameter returnParameter = createReturnFromNode((ASimpleOptionalReturnType) node.getSimpleOptionalReturnType());
		if (node.getAnnotations() != null) {
			AnnotationProcessor annotationProcessor = new AnnotationProcessor(this.context.getReferenceTracker(), this.problemBuilder);
			annotationProcessor.process(node.getAnnotations());
			annotationProcessor.applyAnnotations(returnParameter, node.getAnnotations());
		}
	}

	@Override
	public void caseASimpleParamDecl(ASimpleParamDecl node) {
		createParameterFromNode(node);
	}

	private Parameter createParameterFromNode(final ASimpleParamDecl node) {
		POptionalParameterName parameterNameNode = node.getOptionalParameterName();
		String parameterName = TextUMLCore.getSourceMiner().getIdentifier(parameterNameNode);
		// set a default direction, may be overruled if parameter supports direction
		final Parameter createdParameter = createParameter(parameterName, node.getTypeIdentifier(), ParameterDirectionKind.IN_LITERAL);
		
		node.apply(new DepthFirstAdapter() {
			@Override
			public void caseASimpleInitializationExpression(ASimpleInitializationExpression expressionNode) {
			    ASimpleInitialization simpleInitialization = (ASimpleInitialization) ((ASimpleInitializationExpression) expressionNode).getSimpleInitialization();
				SimpleInitializationExpressionProcessor expressionProcessor = new SimpleInitializationExpressionProcessor(sourceContext, parent);
				expressionProcessor.process(node.getTypeIdentifier(), createdParameter, simpleInitialization.getLiteralOrIdentifier());
			}
			@Override
			public void caseAComplexInitializationExpression(final AComplexInitializationExpression node) {
	            sourceContext.getReferenceTracker().add(new IDeferredReference() {
	                @Override
	                public void resolve(IBasicRepository repository) {
	                    // required for resolving behavior
	                    Class nearestClass = (Class) MDDUtil.getNearest(createdParameter, UMLPackage.Literals.CLASS);
                        ComplexInitializationExpressionProcessor expressionProcessor = new ComplexInitializationExpressionProcessor(sourceContext, nearestClass);
	                    expressionProcessor.process(createdParameter, node);
	                }
	            }, IReferenceTracker.Step.GENERAL_RESOLUTION);
			}
		});
		return createdParameter;
	}
	
	@Override
	public void caseAParamDecl(AParamDecl node) {
		ASimpleParamDecl asSimpleParam = (ASimpleParamDecl) node.getSimpleParamDecl();
		Parameter parameter = createParameterFromNode(asSimpleParam);
		modifierProcessor.process(node.getParameterModifiers());
		applyModifiers(modifierProcessor.getModifiers(true), parameter);
		if (node.getAnnotations() != null) {
			AnnotationProcessor annotationProcessor = new AnnotationProcessor(this.context.getReferenceTracker(), this.problemBuilder);
			annotationProcessor.process(node.getAnnotations());
			annotationProcessor.applyAnnotations(parameter, node.getAnnotations());
		}
	}

	private void applyModifiers(Set<Modifier> modifiers, Parameter parameter) {
		ParameterDirectionKind directionKind = ParameterDirectionKind.IN_LITERAL;
		ParameterEffectKind effectKind = null;
		for (Modifier modifier : modifiers) {
			switch (modifier) {
			case IN : directionKind = ParameterDirectionKind.IN_LITERAL; break;
			case OUT : directionKind = ParameterDirectionKind.OUT_LITERAL; break;
			case INOUT : directionKind = ParameterDirectionKind.INOUT_LITERAL; break;
			case CREATE : effectKind = ParameterEffectKind.CREATE_LITERAL; break;
			case UPDATE: effectKind = ParameterEffectKind.UPDATE_LITERAL; break;
			case DELETE : effectKind = ParameterEffectKind.DELETE_LITERAL; break;
			case READ : effectKind = ParameterEffectKind.READ_LITERAL; break;
			}
		}
		parameter.setDirection(directionKind);
		parameter.setEffect(effectKind);
	}

	@Override
	public void caseARaisedExceptionItem(final ARaisedExceptionItem node) {
		String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getMinimalTypeIdentifier());
		if (!supportExceptions) {
			problemBuilder.addError("Cannot declare raised exceptions in this context", node);
			throw new AbortedScopeCompilationException();
		}
		addRaisedException(qualifiedIdentifier, node.getMinimalTypeIdentifier());
	}
}