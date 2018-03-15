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
import java.util.stream.Stream;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.ParameterEffectKind;
import org.eclipse.uml2.uml.ParameterSet;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.Step;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.ISourceMiner;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AComplexInitializationExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOptionalReturnType;
import com.abstratt.mdd.frontend.textuml.grammar.node.AParamDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AParametersetDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.ARaisedExceptionItem;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleInitialization;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleInitializationExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleOptionalReturnType;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleParamDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.POptionalParameterName;
import com.abstratt.mdd.frontend.textuml.grammar.node.PTypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.TIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;

public abstract class SignatureProcessor extends AbstractSignatureProcessor {
    
    final private ModifierProcessor modifierProcessor;

    public SignatureProcessor(SourceCompilationContext<Node> sourceContext, Namespace parent, boolean supportExceptions) {
        super(sourceContext, parent, supportExceptions);
        modifierProcessor = new ModifierProcessor(sourceContext.getSourceMiner());
    }

    public SignatureProcessor(SourceCompilationContext<Node> sourceContext, Namespace parent,
            boolean supportExceptions, boolean unnamedParameters) {
        super(sourceContext, parent, supportExceptions, unnamedParameters);
        modifierProcessor = new ModifierProcessor(sourceContext.getSourceMiner());
    }

    @Override
    public void caseASimpleOptionalReturnType(ASimpleOptionalReturnType node) {
        createReturnFromNode(node);
    }

    @Override
    protected Parameter getParameter(String name) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected ParameterSet createParameterSet(String name) {
        throw new UnsupportedOperationException();
    }
    
    private Parameter createReturnFromNode(ASimpleOptionalReturnType node) {
        PTypeIdentifier typeIdentifier = node.getTypeIdentifier();
        return createParameter(null, typeIdentifier, ParameterDirectionKind.RETURN_LITERAL);
    }

    @Override
    public void caseAOptionalReturnType(AOptionalReturnType node) {
        Parameter returnParameter = createReturnFromNode((ASimpleOptionalReturnType) node.getSimpleOptionalReturnType());
        if (node.getAnnotations() != null) {
            AnnotationProcessor annotationProcessor = new AnnotationProcessor(this.context.getReferenceTracker(),
                    this.problemBuilder);
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
        // set a default direction, may be overruled if parameter supports
        // direction
        final Parameter createdParameter = createParameter(parameterName, node.getTypeIdentifier(),
                ParameterDirectionKind.IN_LITERAL);

        node.apply(new DepthFirstAdapter() {
            @Override
            public void caseASimpleInitializationExpression(ASimpleInitializationExpression expressionNode) {
                ASimpleInitialization simpleInitialization = (ASimpleInitialization) ((ASimpleInitializationExpression) expressionNode)
                        .getSimpleInitialization();
                SimpleInitializationExpressionProcessor expressionProcessor = new SimpleInitializationExpressionProcessor(
                        sourceContext, parent);
                expressionProcessor.process(node.getTypeIdentifier(), createdParameter,
                        simpleInitialization.getLiteralOrIdentifier());
            }

            @Override
            public void caseAComplexInitializationExpression(final AComplexInitializationExpression node) {
                sourceContext.getReferenceTracker().add(new IDeferredReference() {
                    @Override
                    public void resolve(IBasicRepository repository) {
                        // required for resolving behavior
                        Class nearestClass = (Class) MDDUtil.getNearest(createdParameter, UMLPackage.Literals.CLASS);
                        ComplexInitializationExpressionProcessor expressionProcessor = new ComplexInitializationExpressionProcessor(
                                sourceContext, nearestClass);
                        expressionProcessor.process(createdParameter, node);
                    }
                }, Step.GENERAL_RESOLUTION);
            }
        });
        return createdParameter;
    }

    @Override
    public void caseAParamDecl(AParamDecl node) {
        ASimpleParamDecl asSimpleParam = (ASimpleParamDecl) node.getSimpleParamDecl();
        Parameter parameter = createParameterFromNode(asSimpleParam);
        modifierProcessor.process(node.getParameterModifiers());
        CommentUtils.applyComment(node.getModelComment(), parameter);
        applyModifiers(modifierProcessor.getModifiers(true), parameter);
        if (node.getAnnotations() != null) {
            AnnotationProcessor annotationProcessor = new AnnotationProcessor(this.context.getReferenceTracker(),
                    this.problemBuilder);
            annotationProcessor.process(node.getAnnotations());
            annotationProcessor.applyAnnotations(parameter, node.getAnnotations());
        }
    }
    
    @Override
    public void caseAParametersetDecl(AParametersetDecl node) {
        String setName = getSourceMiner().findFirstChild(node.getName(), Token.class).map(it -> it.getText()).orElse(null);
        ParameterSet newParameterSet = createParameterSet(setName);
        Stream<TIdentifier> parameterRefs = getSourceMiner().findChildren(node.getParameters(), TIdentifier.class).stream();
        parameterRefs.forEach(parameterRef-> {
            String parameterName = parameterRef.getText();
            Parameter parameter = getParameter(parameterName);
            problemBuilder.ensure(parameter != null, parameterRef, () -> new UnresolvedSymbol(parameterName, UMLPackage.Literals.PARAMETER));
            parameter.getParameterSets().add(newParameterSet);
        });
        CommentUtils.applyComment(node.getModelComment(), newParameterSet);
    }

    private ISourceMiner<Node> getSourceMiner() {
        return sourceContext.getSourceMiner();
    }

    private void applyModifiers(Set<Modifier> modifiers, Parameter parameter) {
        ParameterDirectionKind directionKind = ParameterDirectionKind.IN_LITERAL;
        ParameterEffectKind effectKind = null;
        for (Modifier modifier : modifiers) {
            switch (modifier) {
            case IN:
                directionKind = ParameterDirectionKind.IN_LITERAL;
                break;
            case OUT:
                directionKind = ParameterDirectionKind.OUT_LITERAL;
                break;
            case INOUT:
                directionKind = ParameterDirectionKind.INOUT_LITERAL;
                break;
            case CREATE:
                effectKind = ParameterEffectKind.CREATE_LITERAL;
                break;
            case UPDATE:
                effectKind = ParameterEffectKind.UPDATE_LITERAL;
                break;
            case DELETE:
                effectKind = ParameterEffectKind.DELETE_LITERAL;
                break;
            case READ:
                effectKind = ParameterEffectKind.READ_LITERAL;
                break;
            }
        }
        parameter.setDirection(directionKind);
        if (effectKind != null)
            // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=522336
            parameter.setEffect(effectKind);
    }

    @Override
    public void caseARaisedExceptionItem(final ARaisedExceptionItem node) {
        String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(
                node.getMinimalTypeIdentifier());
        if (!supportExceptions) {
            problemBuilder.addError("Cannot declare raised exceptions in this context", node);
            throw new AbortedScopeCompilationException();
        }
        addRaisedException(qualifiedIdentifier, node.getMinimalTypeIdentifier());
    }
}