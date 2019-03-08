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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.AddStructuralFeatureValueAction;
import org.eclipse.uml2.uml.AddVariableValueAction;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Clause;
import org.eclipse.uml2.uml.ConditionalNode;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.CreateObjectAction;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.DestroyObjectAction;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.ExceptionHandler;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.LinkEndData;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.LoopNode;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.ObjectNode;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.RaiseExceptionAction;
import org.eclipse.uml2.uml.ReadExtentAction;
import org.eclipse.uml2.uml.ReadIsClassifiedObjectAction;
import org.eclipse.uml2.uml.ReadLinkAction;
import org.eclipse.uml2.uml.ReadSelfAction;
import org.eclipse.uml2.uml.ReadStructuralFeatureAction;
import org.eclipse.uml2.uml.ReadVariableAction;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.TemplateableElement;
import org.eclipse.uml2.uml.TestIdentityAction;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.ValueSpecificationAction;
import org.eclipse.uml2.uml.Variable;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.uml2.uml.WriteLinkAction;

import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.Step;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.BasicTypeUtils;
import com.abstratt.mdd.core.util.ClassifierUtils;
import com.abstratt.mdd.core.util.DataTypeUtils;
import com.abstratt.mdd.core.util.FeatureUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.PackageUtils;
import com.abstratt.mdd.core.util.StateMachineUtils;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.CannotModifyADerivedAttribute;
import com.abstratt.mdd.frontend.core.FrontEnd;
import com.abstratt.mdd.frontend.core.InternalProblem;
import com.abstratt.mdd.frontend.core.MissingRequiredArgument;
import com.abstratt.mdd.frontend.core.NotAConcreteClassifier;
import com.abstratt.mdd.frontend.core.NotInAssociation;
import com.abstratt.mdd.frontend.core.QueryOperationsMustBeSideEffectFree;
import com.abstratt.mdd.frontend.core.ReadSelfFromStaticContext;
import com.abstratt.mdd.frontend.core.ReturnStatementRequired;
import com.abstratt.mdd.frontend.core.ReturnValueNotExpected;
import com.abstratt.mdd.frontend.core.ReturnValueRequired;
import com.abstratt.mdd.frontend.core.TypeMismatch;
import com.abstratt.mdd.frontend.core.UnknownAttribute;
import com.abstratt.mdd.frontend.core.UnknownOperation;
import com.abstratt.mdd.frontend.core.UnknownRole;
import com.abstratt.mdd.frontend.core.UnknownType;
import com.abstratt.mdd.frontend.core.spi.AbortedCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.IActivityBuilder;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltBinaryExpressionP1;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltBinaryExpressionP2;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltBinaryExpressionP3;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltBinaryExpressionP4;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltBinaryExpressionP5;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltExpressionP0;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltNestedExpressionP1;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltNestedExpressionP2;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltNestedExpressionP3;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltNestedExpressionP4;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltNestedExpressionP5;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltUnaryExpressionP1;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAltUnaryExpressionP4;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAttributeIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ABlockKernel;
import com.abstratt.mdd.frontend.textuml.grammar.node.ABooleanLiteral;
import com.abstratt.mdd.frontend.textuml.grammar.node.ABroadcastSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ACast;
import com.abstratt.mdd.frontend.textuml.grammar.node.ACatchSection;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassAttributeIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassOperationIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClosure;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClosureOperand;
import com.abstratt.mdd.frontend.textuml.grammar.node.ADestroySpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AElseRestIf;
import com.abstratt.mdd.frontend.textuml.grammar.node.AEmptyExpressionList;
import com.abstratt.mdd.frontend.textuml.grammar.node.AEmptyReturnSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AEmptySet;
import com.abstratt.mdd.frontend.textuml.grammar.node.AEqualsComparisonOperator;
import com.abstratt.mdd.frontend.textuml.grammar.node.AExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.AExpressionListElement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AExpressionSimpleBlockResolved;
import com.abstratt.mdd.frontend.textuml.grammar.node.AExtentIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.AFunctionIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.AGreaterOrEqualsComparisonOperator;
import com.abstratt.mdd.frontend.textuml.grammar.node.AGreaterThanComparisonOperator;
import com.abstratt.mdd.frontend.textuml.grammar.node.AIfClause;
import com.abstratt.mdd.frontend.textuml.grammar.node.AIfStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AIsClassExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ALinkIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ALinkRole;
import com.abstratt.mdd.frontend.textuml.grammar.node.ALinkSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ALiteralOperand;
import com.abstratt.mdd.frontend.textuml.grammar.node.ALoopSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ALoopTest;
import com.abstratt.mdd.frontend.textuml.grammar.node.AMinimalTypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.ANamedArgument;
import com.abstratt.mdd.frontend.textuml.grammar.node.ANewIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ANotEqualsComparisonOperator;
import com.abstratt.mdd.frontend.textuml.grammar.node.ANotSameComparisonOperator;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.AParenthesisOperand;
import com.abstratt.mdd.frontend.textuml.grammar.node.AQualifiedAssociationTraversal;
import com.abstratt.mdd.frontend.textuml.grammar.node.ARaiseSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ARepeatLoopBody;
import com.abstratt.mdd.frontend.textuml.grammar.node.ARootExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASameComparisonOperator;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASelfIdentifierExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASendSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleAssociationTraversal;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleExpressionBlock;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATarget;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATernaryExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATrueBoolean;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATryStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATupleComponentValue;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATupleConstructor;
import com.abstratt.mdd.frontend.textuml.grammar.node.AUnlinkSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AValuedReturnSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AVarDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AVariableAccess;
import com.abstratt.mdd.frontend.textuml.grammar.node.AWhileLoopBody;
import com.abstratt.mdd.frontend.textuml.grammar.node.AWriteAttributeSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AWriteClassAttributeSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AWriteVariableSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PAssociationTraversal;
import com.abstratt.mdd.frontend.textuml.grammar.node.PExpressionList;
import com.abstratt.mdd.frontend.textuml.grammar.node.POperand;
import com.abstratt.mdd.frontend.textuml.grammar.node.PTypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.TAnd;
import com.abstratt.mdd.frontend.textuml.grammar.node.TBangs;
import com.abstratt.mdd.frontend.textuml.grammar.node.TDiv;
import com.abstratt.mdd.frontend.textuml.grammar.node.TElvis;
import com.abstratt.mdd.frontend.textuml.grammar.node.TIs;
import com.abstratt.mdd.frontend.textuml.grammar.node.TLab;
import com.abstratt.mdd.frontend.textuml.grammar.node.TLabEquals;
import com.abstratt.mdd.frontend.textuml.grammar.node.TMinus;
import com.abstratt.mdd.frontend.textuml.grammar.node.TMult;
import com.abstratt.mdd.frontend.textuml.grammar.node.TNot;
import com.abstratt.mdd.frontend.textuml.grammar.node.TOr;
import com.abstratt.mdd.frontend.textuml.grammar.node.TPlus;
import com.abstratt.mdd.frontend.textuml.grammar.node.TQuestion;
import com.abstratt.mdd.frontend.textuml.grammar.node.TTrue;
import com.abstratt.pluginutils.LogUtils;

/**
 * This tree visitor will generate the behavioral model for a given input.
 */
public class BehaviorGenerator extends AbstractGenerator {
    class DeferredActivity {
        private Activity activity;
        private Node block;

        public DeferredActivity(Activity activity, Node block) {
            this.activity = activity;
            this.block = block;
        }

        public Activity getActivity() {
            return activity;
        }

        public Node getBlock() {
            return block;
        }
    }

    class OperationInfo {
        String operationName;
        // operand (target)[, operand (argument)], result (return)
        Classifier[] types;

        public OperationInfo(int numberOfTypes) {
            types = new Classifier[numberOfTypes];
        }
    }

    private IActivityBuilder builder;

    private List<DeferredActivity> deferredActivities;

    public BehaviorGenerator(SourceCompilationContext<Node> sourceContext) {
        super(sourceContext);
        deferredActivities = new LinkedList<DeferredActivity>();
        builder = FrontEnd.newActivityBuilder(getRepository());
    }

    /**
     * Creates an anonymous activity and returns a reference.
     */
    private Activity buildClosure(BehavioredClassifier parent, StructuredActivityNode context, AClosure node) {
        Activity newClosure = MDDExtensionUtils.createClosure(parent, context);
        // create activity parameters
        node.getSimpleSignature().apply(newSignatureProcessor(newClosure));
        deferBlockCreation(node.getBlock(), newClosure);
        return newClosure;
    }

    private ValueSpecificationAction buildValueSpecificationAction(ValueSpecification valueSpec, Node node) {
        ValueSpecificationAction action = (ValueSpecificationAction) builder.createAction(IRepository.PACKAGE
                .getValueSpecificationAction());
        try {
            action.setValue(valueSpec);
            builder.registerOutput(action.createResult(null, valueSpec.getType()));
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node, getBoundElement());
        return action;
    }

    @Override
    public void caseAAttributeIdentifierExpression(AAttributeIdentifierExpression node) {
        ReadStructuralFeatureAction action = (ReadStructuralFeatureAction) builder.createAction(IRepository.PACKAGE
                .getReadStructuralFeatureAction());
        final String attributeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
        try {
            builder.registerInput(action.createObject(null, null));
            super.caseAAttributeIdentifierExpression(node);
            builder.registerOutput(action.createResult(null, null));
            final ObjectNode source = ActivityUtils.getSource(action.getObject());
            Classifier targetClassifier = (Classifier) TypeUtils.getTargetType(getRepository(), source, true);
            if (targetClassifier == null) {
                problemBuilder.addError("Object type not determined for '"
                        + ((ATarget) node.getTarget()).getOperand().toString().trim() + "'", node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            Property attribute = FeatureUtils.findAttribute(targetClassifier, attributeIdentifier, false, true);
            if (attribute == null) {
                problemBuilder.addProblem(new UnknownAttribute(targetClassifier.getName(), attributeIdentifier, false),
                        node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            if (attribute.isStatic()) {
                problemBuilder.addError("Non-static attribute expected: '" + attributeIdentifier + "' in '"
                        + targetClassifier.getName() + "'", node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            action.setStructuralFeature(attribute);
            action.getObject().setType(source.getType());
            TypeUtils.copyType(attribute, action.getResult(), targetClassifier);
            if (!TypeUtils.isRequiredPin(source) && !TypeUtils.isMultivalued(source))
            	// it is a tentative access, result must be optional
            	action.getResult().setLower(0);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getIdentifier(), getBoundElement());
    }
    
    @Override
    public void caseAIsClassExpression(AIsClassExpression expressionNode) {
    	handleBinaryExpression(expressionNode.getOperand1(), expressionNode.getOperator(), expressionNode.getOperand2());
    }
    
    @Override
    public void caseAAltUnaryExpressionP1(AAltUnaryExpressionP1 node) {
    	handleUnaryExpression(node.getOperator(), node.getOperand());
    }
    
    @Override
    public void caseAAltUnaryExpressionP4(AAltUnaryExpressionP4 node) {
    	handleUnaryExpression(node.getOperator(), node.getOperand());
    }
    
    @Override
    public void caseAAltBinaryExpressionP1(AAltBinaryExpressionP1 node) {
    	handleBinaryExpression(node.getOperand1(), node.getOperator(), node.getOperand2());
    }
    
    @Override
    public void caseAAltBinaryExpressionP2(AAltBinaryExpressionP2 node) {
    	handleBinaryExpression(node.getOperand1(), node.getOperator(), node.getOperand2());
    }
    
    @Override
    public void caseAAltBinaryExpressionP3(AAltBinaryExpressionP3 node) {
    	handleBinaryExpression(node.getOperand1(), node.getOperator(), node.getOperand2());
    }
    
    @Override
    public void caseAAltBinaryExpressionP4(AAltBinaryExpressionP4 node) {
    	handleBinaryExpression(node.getOperand1(), node.getOperator(), node.getOperand2());
    }
    
    @Override
    public void caseAAltBinaryExpressionP5(AAltBinaryExpressionP5 node) {
    	handleBinaryExpression(node.getOperand1(), node.getOperator(), node.getOperand2());
    }
    
    private void handleUnaryExpression(Node operator, Node operand) {
    	final String[] operationName = { null };
    	boolean[] specialHandling = { false };
		operator.apply(new DepthFirstAdapter() {
			@Override
			public void caseTMinus(TMinus node) {
				operationName[0] = "subtract";
			}
			@Override
			public void caseTNot(TNot node) {
				operationName[0] = "not";
			}
			@Override
			public void caseTQuestion(TQuestion node) {
				operationName[0] = "notNull";
			}
			public void caseTBangs(TBangs node) {
				specialHandling[0] = true;
				handleRequiredUnaryOperator(operand);
			}
		});
		if (!specialHandling[0]) {
			ensure(operationName[0] != null, operator, () -> new UnclassifiedProblem("Unknown unary operator: " + operator.toString()));
			handleUnaryExpressionAsOperation(operator, operand, operationName[0]);
		}
    }
    
	private void handleBinaryExpression(Node left, Node operator, Node right) {
		String[] operationName = { null };
		boolean[] specialHandling = { false };
		operator.apply(new DepthFirstAdapter() {
		    @Override
		    public void caseANotEqualsComparisonOperator(ANotEqualsComparisonOperator node) {
				operationName[0] = "notEquals";
			}
			@Override
			public void caseASameComparisonOperator(ASameComparisonOperator node) {
			    handleSameBinaryOperator(left, right);
			    specialHandling[0] = true;
			}
			
			@Override
			public void caseANotSameComparisonOperator(ANotSameComparisonOperator node) {
			    handleUnaryExpressionAsOperation(node, "not", () -> {
			        handleSameBinaryOperator(left, right);
			        return node;
			    });
			    specialHandling[0] = true;
			}

			@Override
			public void caseAEqualsComparisonOperator(AEqualsComparisonOperator node) {
				operationName[0] = "equals";
			}

			@Override
			public void caseAGreaterOrEqualsComparisonOperator(AGreaterOrEqualsComparisonOperator node) {
				operationName[0] = "greaterOrEquals";
			}

			@Override
			public void caseAGreaterThanComparisonOperator(AGreaterThanComparisonOperator node) {
				operationName[0] = "greaterThan";
			}

			@Override
			public void caseTIs(TIs node) {
				handleIsClassifiedOperator(left, right);
				specialHandling[0] = true;
			}
			
			@Override
			public void caseTElvis(TElvis node) {
				handleElvisOperator(left, right);
				specialHandling[0] = true;
			}

			@Override
			public void caseTLab(TLab node) {
				operationName[0] = "lowerThan";
			}

			@Override
			public void caseTLabEquals(TLabEquals node) {
				operationName[0] = "lowerOrEquals";
			}

			@Override
			public void caseTAnd(TAnd node) {
				operationName[0] = "and";
			}

			@Override
			public void caseTDiv(TDiv node) {
				operationName[0] = "divide";
			}

			@Override
			public void caseTMinus(TMinus node) {
				operationName[0] = "subtract";
			}

			@Override
			public void caseTMult(TMult node) {
				operationName[0] = "multiply";
			}

			@Override
			public void caseTOr(TOr node) {
				operationName[0] = "or";
			}

			@Override
			public void caseTPlus(TPlus node) {
				operationName[0] = "add";
			}
		});
		if (!specialHandling[0]) {
			ensure(operationName[0] != null, operator, () -> new UnclassifiedProblem("Unknown binary operator: " + operator.toString()));
		    handleBinaryExpressionAsOperation(left, operator, right, operationName[0]);
		}
	}
	
    public void handleBinaryExpressionAsOperation(Node operand1, Node operator, Node operand2, String operationName) {
        handleBinaryExpressionAsOperation(operator, () -> {
            operand1.apply(this);
            return operand1;
        }, () -> {
            operand2.apply(this);
            return operand2;
        }, operationName);
    }
    
    public void handleBinaryExpressionAsOperation(Node contextNode, Supplier<Node> operand1, Supplier<Node> operand2, String operationName) {
        CallOperationAction action = (CallOperationAction) builder.createAction(IRepository.PACKAGE
                .getCallOperationAction());
        try {
            // register the target input pin
            builder.registerInput(action.createTarget(null, null));
            // register the argument input pins
            builder.registerInput(action.createArgument(null, null));
            // process the target and argument expressions - this will connect
            // their output pins to the input pins we just created
            Node targetNode = operand1.get();
            Node argumentNode = operand2.get();
            
            InputPin target = action.getTarget();
            InputPin argument = action.getArguments().get(0);
            Type targetType = ActivityUtils.getSource(target).getType();
            Type argumentType = ActivityUtils.getSource(argument).getType();
            
            ensure(targetType != null, targetNode, () -> new UnclassifiedProblem("No type information for " + operand1));
            ensure(argumentType != null, argumentNode, () -> new UnclassifiedProblem("No type information for " + operand2));
            
            TypeUtils.copyType(ActivityUtils.getSource(target), target);
            TypeUtils.copyType(ActivityUtils.getSource(argument), argument);
            
            List<TypedElement> argumentList = Collections.singletonList((TypedElement) argument);
            Operation operation = FeatureUtils.findOperation(getRepository(), (Classifier) targetType, operationName,
                    argumentList, false, true);
            if (operation == null)
                missingOperation(true, contextNode, (Classifier) targetType, operationName,
                        argumentList, false);
            if (operation.isStatic())
                missingOperation(true, contextNode, (Classifier) targetType, operationName, argumentList,
                        false);
            List<Parameter> parameters = operation.getOwnedParameters();
            if (parameters.size() != 2 && parameters.get(0).getDirection() != ParameterDirectionKind.IN_LITERAL
                    && parameters.get(1).getDirection() != ParameterDirectionKind.RETURN_LITERAL) {
                problemBuilder.addError("Unexpected signature: '" + operationName + "' in '"
                        + target.getType().getQualifiedName() + "'", contextNode);
                throw new AbortedStatementCompilationException();
            }
            // register the result output pin
            OutputPin result = action.createResult(null, operation.getType());
            builder.registerOutput(result);
            result.setLower(Math.min(target.getLower(), operation.getReturnResult().getLower()));
            action.setOperation(operation);
            fillDebugInfo(action, contextNode.parent());
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, contextNode, getBoundElement());
    }
    
    private ConditionalNode buildConditionalExpression(Node trueResultNode, Node falseResultNode, Runnable conditionExpressionBuilder) {
    	return createBlock(IRepository.PACKAGE.getConditionalNode(), true, (ConditionalNode action) -> {
        	Clause testClause = action.createClause();
        	createClauseTest(testClause, conditionExpressionBuilder);
        	createClauseBody(testClause, () -> trueResultNode.apply(this));
        	
        	Clause elseClause = action.createClause();
        	createClauseTest(elseClause, () -> buildValueSpecificationAction(MDDUtil.createLiteralBoolean(namespaceTracker.currentPackage(), true), falseResultNode));
        	createClauseBody(elseClause, () -> falseResultNode.apply(this));
        	
        	// ensure the test expression is boolean
        	if (testClause.getDecider().getType() != elseClause.getDecider().getType()) {
        		reportIncompatibleTypes(trueResultNode.parent(), elseClause.getDecider(), testClause.getDecider());
        		throw new AbortedStatementCompilationException();
        	}
        	
        	OutputPin result = action.createResult(null, null);
        	OutputPin optionalValueOutput = testClause.getBodyOutputs().get(0);
        	OutputPin defaultValueOutput = elseClause.getBodyOutputs().get(0);
        	
        	if (!TypeUtils.isCompatible(getRepository(), defaultValueOutput.getType(), optionalValueOutput.getType(), null)
        			|| !TypeUtils.isMultiplicityCompatible(optionalValueOutput, defaultValueOutput, false, true)) {
        		reportIncompatibleTypes(falseResultNode, defaultValueOutput, optionalValueOutput);
        		throw new AbortedStatementCompilationException();
        	}
        	
			TypeUtils.copyType(optionalValueOutput, result);
			result.setLower(Math.max(optionalValueOutput.getLower(),  defaultValueOutput.getLower()));
			builder.registerOutput(result);
            fillDebugInfo(action, trueResultNode.parent());
        });
    }

    private void handleElvisOperator(Node left, Node right) {
    	buildConditionalExpression(left, right, () -> handleUnaryExpressionAsOperation(left, left, "notNull"));
    }
    
    @Override
    public void caseATernaryExpression(ATernaryExpression node) {
    	buildConditionalExpression(node.getTrueExpression(), node.getFalseExpression(), () -> node.getCondition().apply(this));
    }
    
	private void handleSameBinaryOperator(Node left, Node right) {
        TestIdentityAction action = (TestIdentityAction) builder.createAction(IRepository.PACKAGE
                .getTestIdentityAction());
        try {
            builder.registerInput(action.createFirst(null, null));
            builder.registerInput(action.createSecond(null, null));
            left.apply(this);
            right.apply(this);
            TypeUtils.copyType(ActivityUtils.getSource(action.getFirst()), action.getFirst());
            TypeUtils.copyType(ActivityUtils.getSource(action.getSecond()), action.getSecond());
            Classifier expressionType = BasicTypeUtils.findBuiltInType("Boolean");
            builder.registerOutput(action.createResult(null, expressionType));
            fillDebugInfo(action, left.parent());
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, left.parent(), getBoundElement());
    }
	
	private void handleRequiredUnaryOperator(Node operand) {
		// cast input as required (preserving type information
		buildCast(operand, action -> {
			InputPin input = action.getStructuredNodeInputs().get(0);
			OutputPin output = action.getStructuredNodeOutputs().get(0);
			TypeUtils.copyType(input, output);
			output.setLower(1);
		});
	}

    private void handleIsClassifiedOperator(Node left, Node right) {
        ReadIsClassifiedObjectAction action = (ReadIsClassifiedObjectAction) builder.createAction(IRepository.PACKAGE
                .getReadIsClassifiedObjectAction());
        Node parent = left.parent();
        try {
        	if (!(right instanceof AMinimalTypeIdentifier)) {
                problemBuilder.addError("A qualified identifier is expected",
                        right);
                throw new AbortedStatementCompilationException();
        	}
            String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(right);
            Classifier classifier = (Classifier) getRepository().findNamedElement(qualifiedIdentifier,
                    IRepository.PACKAGE.getClassifier(), namespaceTracker.currentPackage());
            
            ensure(classifier != null, right, () -> new UnknownType(qualifiedIdentifier));

            builder.registerInput(action.createObject(null, null));
            left.apply(this);
            Classifier expressionType = BasicTypeUtils.findBuiltInType("Boolean");
            builder.registerOutput(action.createResult(null, expressionType));
            action.setClassifier(classifier);
			fillDebugInfo(action, parent);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, parent, getBoundElement());
    }

    /**
     * Blocks can be simple (curly brackets) or wordy (begin...end). Regardless
     * the delimiters, the kernel of the block is processed here.
     */
    @Override
    public void caseABlockKernel(ABlockKernel node) {
        createBlock(IRepository.PACKAGE.getStructuredActivityNode(), false, block -> {
        	CommentUtils.applyComment(node.getModelComment(), block);
            fillDebugInfo(builder.getCurrentBlock(), node);
            BehaviorGenerator.super.caseABlockKernel(node);
            // isolation determines whether the block should be treated as a
            // transaction
            Activity currentActivity = builder.getCurrentActivity();
            if (!MDDExtensionUtils.isClosure(currentActivity))
                if (ActivityUtils.shouldIsolate(builder.getCurrentBlock())) {
                    builder.getCurrentBlock().setMustIsolate(true);
                    // if blocks themselves are isolated, the main body does not
                    // need isolation
                    ActivityUtils.getBodyNode(currentActivity).setMustIsolate(false);
                }
        });
    }

    @Override
    public void caseAExpressionSimpleBlockResolved(AExpressionSimpleBlockResolved node) {
    	createBlock(false, () -> 
            buildReturnStatement(node.getSimpleExpressionBlock())
        );
    }

    @Override
    public void caseAClassAttributeIdentifierExpression(AClassAttributeIdentifierExpression node) {
        String typeIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getMinimalTypeIdentifier());
        Classifier targetClassifier = (Classifier) getRepository().findNamedElement(typeIdentifier,
                IRepository.PACKAGE.getClassifier(), namespaceTracker.currentNamespace(null));
        if (targetClassifier == null) {
            problemBuilder.addError("Class reference expected: '" + typeIdentifier + "'",
                    node.getMinimalTypeIdentifier());
            throw new AbortedStatementCompilationException();
        }
        String attributeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
        Property attribute = FeatureUtils.findAttribute(targetClassifier, attributeIdentifier, false, true);
        if (attribute != null) {
            buildReadStaticStructuralFeature(targetClassifier, attribute, node);
            return;
        }
        if (targetClassifier instanceof Enumeration) {
            EnumerationLiteral enumerationValue = ((Enumeration) targetClassifier).getOwnedLiteral(attributeIdentifier);
            if (enumerationValue != null) {
                InstanceValue valueSpec = (InstanceValue) namespaceTracker.currentPackage().createPackagedElement(null,
                        IRepository.PACKAGE.getInstanceValue());
                valueSpec.setInstance(enumerationValue);
                valueSpec.setType(targetClassifier);
                buildValueSpecificationAction(valueSpec, node);
                return;
            }
        }
        if (targetClassifier instanceof StateMachine) {
            Vertex state = StateMachineUtils.getState((StateMachine) targetClassifier, attributeIdentifier);
            if (state != null) {
                ValueSpecification stateReference = MDDExtensionUtils.buildVertexLiteral(
                        namespaceTracker.currentPackage(), state);
                buildValueSpecificationAction(stateReference, node);
                return;
            }
        }
        problemBuilder.addProblem(new UnknownAttribute(targetClassifier.getName(), attributeIdentifier, true),
                node.getIdentifier());
        throw new AbortedStatementCompilationException();
    }

    private void buildReadStaticStructuralFeature(Classifier targetClassifier, Property attribute,
            AClassAttributeIdentifierExpression node) {
        ReadStructuralFeatureAction action = (ReadStructuralFeatureAction) builder.createAction(IRepository.PACKAGE
                .getReadStructuralFeatureAction());
        try {
            // // according to UML 2.1 §11.1, "(...) The semantics for static
            // features is undefined. (...)"
            // // our intepretation is that they are allowed and the input is a
            // null value spec
            // builder.registerInput(action.createObject(null, null));
            // LiteralNull literalNull = (LiteralNull)
            // currentPackage().createPackagedElement(null,
            // IRepository.PACKAGE.getLiteralNull());
            // buildValueSpecificationAction(literalNull, node);
            builder.registerOutput(action.createResult(null, targetClassifier));
            if (!attribute.isStatic()) {
                problemBuilder.addError("Static attribute expected: '" + attribute.getName() + "' in '"
                        + targetClassifier.getName() + "'", node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            action.setStructuralFeature(attribute);
            // action.getObject().setType(targetClassifier);
            TypeUtils.copyType(attribute, action.getResult(), targetClassifier);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getIdentifier(), getBoundElement());
    }

    @Override
    public void caseAClassOperationIdentifierExpression(final AClassOperationIdentifierExpression node) {
        String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(
                node.getMinimalTypeIdentifier());
        Class targetClassifier = (Class) getRepository().findNamedElement(qualifiedIdentifier,
                IRepository.PACKAGE.getClass_(), namespaceTracker.currentPackage());
        if (targetClassifier == null) {
            problemBuilder.addError("Class reference expected: '" + qualifiedIdentifier + "'",
                    node.getMinimalTypeIdentifier());
            throw new AbortedStatementCompilationException();
        }
        CallOperationAction action = (CallOperationAction) builder.createAction(IRepository.PACKAGE
                .getCallOperationAction());
        try {
            int argumentCount = countElements(node.getExpressionList());
            for (int i = 0; i < argumentCount; i++)
                builder.registerInput(action.createArgument(null, null));
            super.caseAClassOperationIdentifierExpression(node);
            // collect sources so we can match the right operation (in
            // case of overloading)
            List<ObjectNode> sources = new ArrayList<ObjectNode>();
            for (InputPin argument : action.getArguments())
                sources.add(ActivityUtils.getSource(argument));
            String operationName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
            Operation operation = findOperation(node.getIdentifier(), targetClassifier, operationName,
                    new ArrayList<TypedElement>(sources), true, true);
            if (!operation.isQuery() && !PackageUtils.isModelLibrary(operation.getNearestPackage()))
                ensureNotQuery(node);
            if (operation.getReturnResult() == null)
                ensureTerminal(node.getIdentifier());
            action.setOperation(operation);
            List<Parameter> inputParameters = FeatureUtils.getInputParameters(operation.getOwnedParameters());
            Map<Type, Type> wildcardSubstitutions = FeatureUtils.buildWildcardSubstitutions(new HashMap<Type, Type>(),
                    inputParameters, sources);
            int argumentPos = 0;
            for (Parameter current : operation.getOwnedParameters()) {
                switch (current.getDirection().getValue()) {
                case ParameterDirectionKind.IN:
                    if (argumentPos == argumentCount) {
                        problemBuilder.addError("Wrong number of arguments", node.getLParen());
                        throw new AbortedStatementCompilationException();
                    }
                    InputPin argument = action.getArguments().get(argumentPos++);
                    argument.setName(current.getName());
                    TypeUtils.copyType(current, argument, targetClassifier);
                    break;
                case ParameterDirectionKind.RETURN:
                    // there should be only one of these
                    Assert.isTrue(action.getResults().isEmpty());
                    OutputPin result = builder.registerOutput(action.createResult(null, null));
                    TypeUtils.copyType(current, result, targetClassifier);
                    resolveWildcardTypes(wildcardSubstitutions, current, result);
                    break;
                case ParameterDirectionKind.OUT:
                case ParameterDirectionKind.INOUT:
                    Assert.isTrue(false);
                }
            }
            if (argumentPos != argumentCount) {
                problemBuilder.addError("Wrong number of arguments", node.getLParen());
                return;
            }
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getIdentifier(), getBoundElement());
    }

    /**
     * In the context of an operation call, copies types from source to target
     * for every target that still has a wildcard type.
     * 
     * In the case of a target that is a signature,
     * 
     * @param wildcardSubstitutions
     * @param source
     * @param target
     */
    private void resolveWildcardTypes(Map<Type, Type> wildcardSubstitutions, TypedElement source, TypedElement target) {
        if (wildcardSubstitutions.isEmpty())
            return;
        if (MDDExtensionUtils.isWildcardType(target.getType())) {
            Type subbedType = wildcardSubstitutions.get(source.getType());
            if (subbedType != null)
                target.setType(subbedType);
        } else if (MDDExtensionUtils.isSignature(target.getType())) {
            List<Parameter> originalSignatureParameters = MDDExtensionUtils.getSignatureParameters(target.getType());
            boolean signatureUsesWildcardTypes = false;
            for (Parameter parameter : originalSignatureParameters) {
                if (MDDExtensionUtils.isWildcardType(parameter.getType())) {
                    signatureUsesWildcardTypes = true;
                    break;
                }
            }
            if (signatureUsesWildcardTypes) {
                Activity closure = (Activity) ActivityUtils.resolveBehaviorReference((Action) ((OutputPin) source)
                        .getOwner());
                Type resolvedSignature = MDDExtensionUtils.createSignature(namespaceTracker.currentNamespace(null));
                for (Parameter closureParameter : closure.getOwnedParameters()) {
                    Parameter resolvedParameter = MDDExtensionUtils.createSignatureParameter(resolvedSignature,
                            closureParameter.getName(), closureParameter.getType());
                    resolvedParameter.setDirection(closureParameter.getDirection());
                    resolvedParameter.setUpper(closureParameter.getUpper());
                }
                target.setType(resolvedSignature);
            }
        }
    }

    @Override
    public void caseAClosureOperand(AClosureOperand node) {
        BehavioredClassifier parent = builder.getCurrentActivity();
        StructuredActivityNode closureContext = builder.getCurrentBlock();
        Activity closure = buildClosure(parent, closureContext, (AClosure) node.getClosure());
        buildValueSpecificationAction(
                ActivityUtils.buildBehaviorReference(namespaceTracker.currentPackage(), closure, null), node);
    }

    @Override
    public void caseADestroySpecificStatement(ADestroySpecificStatement node) {
        final DestroyObjectAction action = (DestroyObjectAction) builder.createAction(IRepository.PACKAGE
                .getDestroyObjectAction());
        final InputPin object;
        try {
            object = action.createTarget(null, null);
            builder.registerInput(object);
            super.caseADestroySpecificStatement(node);
            final Type expressionType = ActivityUtils.getSource(object).getType();
            object.setType(expressionType);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node, getBoundElement());
    }

    @Override
    public void caseAElseRestIf(AElseRestIf node) {
        // all this gymnastic is to create the always-true test action
        ValueSpecificationAction action = buildValueSpecificationAction(MDDUtil.createLiteralBoolean(namespaceTracker.currentPackage(), true), node);
        Clause newClause = createClause();
        newClause.getTests().add(action);
        newClause.setDecider(action.getResult());
        createClauseBody(newClause, () -> node.getClauseBody().apply(this));
        checkIncomings(action, node.getElse(), getBoundElement());
    }

    @Override
    public void caseAExtentIdentifierExpression(AExtentIdentifierExpression node) {
        String classifierName = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getMinimalTypeIdentifier());
        final Classifier classifier = (Classifier) getRepository().findNamedElement(classifierName,
                IRepository.PACKAGE.getClassifier(), namespaceTracker.currentPackage());
        if (classifier == null) {
            problemBuilder.addError("Unknown classifier '" + classifierName + "'", node.getMinimalTypeIdentifier());
            throw new AbortedStatementCompilationException();
        }
        ReadExtentAction action = (ReadExtentAction) builder.createAction(IRepository.PACKAGE.getReadExtentAction());
        try {
            action.setClassifier(classifier);
            final OutputPin result = action.createResult(null, classifier);
            result.setUpperValue(MDDUtil.createLiteralUnlimitedNatural(namespaceTracker.currentPackage(),
                    LiteralUnlimitedNatural.UNLIMITED));
            result.setIsUnique(true);
            builder.registerOutput(result);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
    }

    // TODO function call temporarily disabled
    // /**
    // * Processes a function invocation expression. We restrict function
    // * invocations to be variable based.
    // */
    // @Override
    // public void
    // caseAFunctionIdentifierExpression(AFunctionIdentifierExpression node) {
    // DynamicCallBehaviorAction action =
    // (DynamicCallBehaviorAction) builder.createAction(MetaPackage.eINSTANCE
    // .getDynamicCallBehaviorAction());
    // try {
    // // register the behavior input pin
    // builder.registerInput(action.createBehavior(null, null));
    // // register the argument input pins
    // int argumentCount = countElements(node.getExpressionList());
    // for (int i = 0; i < argumentCount; i++)
    // builder.registerInput(action.createArgument(null, null));
    // // process the variable and argument expressions - this will connect
    // // their output pins to the input pins we just created
    // super.caseAFunctionIdentifierExpression(node);
    // // match the list of arguments with the behavior parameters
    // final Type functionType =
    // ActivityUtils.getSource(action.getBehavior()).getType();
    // if (!(functionType instanceof Signature)) {
    // problemBuilder.addProblem( new TypeMismatch("function type",
    // functionType.getName()), node
    // .getVariableAccess());
    // throw new AbortedStatementCompilationException();
    // }
    // Signature signature = (Signature) functionType;
    // Assert.isNotNull(signature, "Function not found");
    // // collect argument types so we can match the right operation (in
    // // case of overloading)
    // List<ObjectNode> argumentSources = new ArrayList<ObjectNode>();
    // for (InputPin argument : action.getArguments())
    // argumentSources.add(ActivityUtils.getSource(argument));
    // final List<Parameter> inputParameters =
    // FeatureUtils.filterParameters(signature.getOwnedParameters(),
    // ParameterDirectionKind.IN_LITERAL);
    // if (!TypeUtils.isCompatible(getRepository(), argumentSources,
    // inputParameters, null)) {
    // problemBuilder.addProblem( new UnresolvedSymbol("Unknown function"),
    // node.getVariableAccess());
    // throw new AbortedStatementCompilationException();
    // }
    // int argumentPos = 0;
    // for (Parameter current : signature.getOwnedParameters()) {
    // OutputPin result;
    // InputPin argument;
    // switch (current.getDirection().getValue()) {
    // case ParameterDirectionKind.IN:
    // if (argumentPos == argumentCount) {
    // problemBuilder.addError("Wrong number of arguments", node.getLParen());
    // throw new AbortedStatementCompilationException();
    // }
    // argument = action.getArguments().get(argumentPos++);
    // argument.setName(current.getName());
    // TypeUtils.copyType(current, argument);
    // break;
    // case ParameterDirectionKind.RETURN:
    // // there should be only one of these
    // result = builder.registerOutput(action.createResult(null, null));
    // TypeUtils.copyType(current, result);
    // break;
    // case ParameterDirectionKind.OUT:
    // case ParameterDirectionKind.INOUT:
    // Assert.isTrue(false);
    // }
    // }
    // if (argumentPos != argumentCount) {
    // problemBuilder.addError("Wrong number of arguments", node.getLParen());
    // return;
    // }
    // // action.getBehavior().setType(targetClassifier);
    // fillDebugInfo(action, node);
    // } finally {
    // builder.closeAction();
    // }
    // checkIncomings(action, node.getVariableAccess());
    // }

    @Override
    public void caseAIfClause(AIfClause node) {
        Clause newClause = createClause();
        createClauseTest(newClause, () -> node.getTest().apply(this));
        createClauseBody(newClause, () -> node.getClauseBody().apply(this));
    }

    @Override
    public void caseAIfStatement(AIfStatement node) {
    	createBlock(IRepository.PACKAGE.getConditionalNode(), false, () ->
            super.caseAIfStatement(node)
        );
    }

    @Override
    public void caseALinkIdentifierExpression(ALinkIdentifierExpression node) {
        ReadLinkAction action = (ReadLinkAction) builder.createAction(IRepository.PACKAGE.getReadLinkAction());
        try {
            InputPin linkEndValue = builder.registerInput(action.createInputValue(null, null));
            node.getIdentifierExpression().apply(this);
            ObjectNode source = ActivityUtils.getSource(linkEndValue);
            Classifier targetClassifier = (Classifier) TypeUtils.getTargetType(getRepository(), source, true);
            Property openEnd = parseRole(targetClassifier, node.getAssociationTraversal());
            Association association = openEnd.getAssociation();
            linkEndValue.setType(targetClassifier);
            int openEndIndex = association.getMemberEnds().indexOf(openEnd);
            Property fedEnd = association.getMemberEnds().get(1 - openEndIndex);
            LinkEndData endData = action.createEndData();
            endData.setEnd(fedEnd);
            endData.setValue(linkEndValue);
            builder.registerOutput(action.createResult(null, openEnd.getType()));
            TypeUtils.copyType(openEnd, action.getResult());
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getAssociationTraversal(), getBoundElement());
    }

    private Property parseRole(Classifier sourceType, PAssociationTraversal node) {
        return (node instanceof ASimpleAssociationTraversal) ? parseSimpleTraversal(sourceType,
                (ASimpleAssociationTraversal) node) : parseQualifiedTraversal(sourceType,
                (AQualifiedAssociationTraversal) node);
    }

    private Property parseSimpleTraversal(Classifier sourceType, ASimpleAssociationTraversal node) {
        final String openEndName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
        Property openEnd = sourceType.getAttribute(openEndName, null);
        Association association;
        if (openEnd == null) {
            EList<Association> associations = sourceType.getAssociations();
            for (Association current : associations)
                if ((openEnd = current.getMemberEnd(openEndName, null)) != null)
                    break;
            if (openEnd == null) {
                problemBuilder.addProblem(new UnknownRole(sourceType.getQualifiedName() + NamedElement.SEPARATOR
                        + openEndName), node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
        }
        association = openEnd.getAssociation();
        if (association == null) {
            problemBuilder.addError(openEndName + " is not an association member end", node.getIdentifier());
            throw new AbortedStatementCompilationException();
        }
        return openEnd;
    }

    private Property parseQualifiedTraversal(Classifier sourceType, AQualifiedAssociationTraversal node) {
        String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(
                node.getMinimalTypeIdentifier());
        final Association association = (Association) getRepository().findNamedElement(qualifiedIdentifier,
                IRepository.PACKAGE.getAssociation(), namespaceTracker.currentPackage());
        if (association == null) {
            problemBuilder.addError("Unknown association '" + qualifiedIdentifier + "'",
                    node.getMinimalTypeIdentifier());
            throw new AbortedStatementCompilationException();
        }
        Classifier associated = ClassifierUtils.findUpHierarchy(getRepository(), sourceType, it -> it.getRelationships(IRepository.PACKAGE.getAssociation()).contains(association) ? it : null);
        if (associated == null) {
            problemBuilder.addProblem(
                    new NotInAssociation(sourceType.getQualifiedName(), association.getQualifiedName()),
                    node.getMinimalTypeIdentifier());
            throw new AbortedStatementCompilationException();
        }
        final String openEndName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
        // XXX implementation limitation: only binary associations are
        // supported
        Property openEnd = association.getMemberEnd(openEndName, null);
        if (openEnd == null) {
            problemBuilder.addProblem(new UnknownRole(association.getQualifiedName(), openEndName),
                    node.getIdentifier());
            throw new AbortedStatementCompilationException();
        }
        return openEnd;
    }

    @Override
    public void caseALinkSpecificStatement(ALinkSpecificStatement node) {
        buildWriteLinkAction(node, node.getMinimalTypeIdentifier(), IRepository.PACKAGE.getCreateLinkAction());
    }

    private void buildWriteLinkAction(Node linkStatementNode, Node associationIdentifierNode, EClass linkActionClass) {
        String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(associationIdentifierNode);
        final Association association = (Association) getRepository().findNamedElement(qualifiedIdentifier,
                IRepository.PACKAGE.getAssociation(), namespaceTracker.currentPackage());
        if (association == null) {
            problemBuilder.addError("Unknown association '" + qualifiedIdentifier + "'", associationIdentifierNode);
            throw new AbortedStatementCompilationException();
        }
        final WriteLinkAction action = (WriteLinkAction) builder.createAction(linkActionClass);
        try {
            linkStatementNode.apply(new DepthFirstAdapter() {
                @Override
                public void caseALinkRole(ALinkRole node) {
                    String roleName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
                    Property end = association.getMemberEnd(roleName, null);
                    if (end == null) {
                        problemBuilder.addProblem(new UnknownRole(association.getQualifiedName(), roleName),
                                node.getIdentifier());
                        throw new AbortedStatementCompilationException();
                    }
                    LinkEndData endData = action.createEndData();
                    endData.setEnd(end);
                    InputPin input = action.createInputValue(null, end.getType());
                    input.setLower(end.getLower());
                    input.setUpper(end.getUpper());
                    endData.setValue(input);
                    builder.registerInput(input);
                    node.getRootExpression().apply(BehaviorGenerator.this);
                }
            });
            // TODO need to validate that all ends declared have input values,
            // no repetitions, etc
            fillDebugInfo(action, linkStatementNode);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, linkStatementNode, getBoundElement());
    }

    @Override
    public void caseAFunctionIdentifierExpression(AFunctionIdentifierExpression node) {
        String functionName = sourceMiner.getIdentifier(node.getVariableAccess());
        problemBuilder.addProblem(new UnclassifiedProblem("Function call not supported yet: " + functionName),
                node.getVariableAccess());
        throw new AbortedStatementCompilationException();
    }

    @Override
    public void caseALiteralOperand(ALiteralOperand node) {
        ValueSpecification value = LiteralValueParser.parseLiteralValue(node.getLiteral(),
                namespaceTracker.currentPackage(), problemBuilder);
        buildValueSpecificationAction(value, node);
    }

    @Override
    public void caseAEmptySet(AEmptySet node) {
        final ValueSpecificationAction action = (ValueSpecificationAction) builder.createAction(IRepository.PACKAGE
                .getValueSpecificationAction());
        String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(
                node.getMinimalTypeIdentifier());
        Classifier classifier = (Classifier) getRepository().findNamedElement(qualifiedIdentifier,
                IRepository.PACKAGE.getClassifier(), namespaceTracker.currentPackage());
        if (classifier == null) {
            problemBuilder.addProblem(new UnknownType(qualifiedIdentifier), node.getMinimalTypeIdentifier());
            throw new AbortedStatementCompilationException();
        }
        try {
        	ValueSpecification emptySetValue = MDDExtensionUtils.buildEmptySet(namespaceTracker.currentPackage(), classifier);
            action.setValue(emptySetValue);
            OutputPin result = action.createResult(null, classifier);
            result.setUpperValue(MDDUtil.createLiteralUnlimitedNatural(namespaceTracker.currentPackage(),
                    LiteralUnlimitedNatural.UNLIMITED));
            builder.registerOutput(result);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node, getBoundElement());
    }

    @Override
    public void caseALoopSpecificStatement(ALoopSpecificStatement node) {
        LoopNode loop = (LoopNode) createBlock(IRepository.PACKAGE.getLoopNode(), false, () -> 
            super.caseALoopSpecificStatement(node)
        );
        loop.setIsTestedFirst(true);
    }

    @Override
    public void caseALoopTest(ALoopTest node) {
        LoopNode currentLoop = (LoopNode) builder.getCurrentBlock();
        createBlock(IRepository.PACKAGE.getStructuredActivityNode(), false, () -> {
            super.caseALoopTest(node);
            currentLoop.getTests().add(builder.getCurrentBlock());
            final OutputPin decider = ActivityUtils.getActionOutputs(builder.getLastRootAction()).get(0);
            currentLoop.setDecider(decider);
        });
    }

    @Override
    public void caseANewIdentifierExpression(final ANewIdentifierExpression node) {
        ensureNotQuery(node);
        final CreateObjectAction action = (CreateObjectAction) builder.createAction(IRepository.PACKAGE
                .getCreateObjectAction());
        try {
            super.caseANewIdentifierExpression(node);
            final OutputPin output = builder.registerOutput(action.createResult(null, null));
            String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(
                    node.getMinimalTypeIdentifier());
            Classifier classifier = (Classifier) getRepository().findNamedElement(qualifiedIdentifier,
                    IRepository.PACKAGE.getClassifier(), namespaceTracker.currentPackage());
            if (classifier == null) {
                problemBuilder.addError("Unknown classifier '" + qualifiedIdentifier + "'",
                        node.getMinimalTypeIdentifier());
                throw new AbortedStatementCompilationException();
            }
            if (classifier.isAbstract()) {
                problemBuilder.addProblem(new NotAConcreteClassifier(qualifiedIdentifier),
                        node.getMinimalTypeIdentifier());
                throw new AbortedStatementCompilationException();
            }
            output.setType(classifier);
            action.setClassifier(classifier);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getNew(), getBoundElement());
    }

    private void ensureNotQuery(Node node) {
        boolean isReadOnly = builder.getCurrentActivity().isReadOnly();
        QueryOperationsMustBeSideEffectFree.ensure(!isReadOnly, problemBuilder, node);
    }

    @Override
    public void caseASendSpecificStatement(ASendSpecificStatement node) {
        ensureNotQuery(node);
        final SendSignalAction action = (SendSignalAction) builder.createAction(IRepository.PACKAGE
                .getSendSignalAction());
        try {
            final String signalIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getSignal());
            final Signal signal = this.context.getRepository().findNamedElement(signalIdentifier,
                    UMLPackage.Literals.SIGNAL, namespaceTracker.currentNamespace(null));
            if (signal == null) {
                problemBuilder.addError("Unknown signal '" + signalIdentifier + "'", node.getSignal());
                throw new AbortedStatementCompilationException();
            }
            builder.registerInput(action.createTarget(null, null));
            node.getTarget().apply(this);
            if (node.getNamedArgumentList() != null)
                node.getNamedArgumentList().apply(new DepthFirstAdapter() {
                    @Override
                    public void caseANamedArgument(ANamedArgument node) {
                        String attributeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
                        Property attribute = FeatureUtils.findAttribute(signal, attributeIdentifier, false, true);
                        if (attribute == null) {
                            problemBuilder.addProblem(
                                    new UnknownAttribute(signal.getName(), attributeIdentifier, false),
                                    node.getIdentifier());
                            throw new AbortedStatementCompilationException();
                        }
                        InputPin signalArgument = action.createArgument(attribute.getName(), null);
                        TypeUtils.copyType(attribute, signalArgument);
						builder.registerInput(signalArgument);
                        node.getExpression().apply(BehaviorGenerator.this);
                    }
                });
            for (Property signalAttribute : signal.getAllAttributes())
                if (signalAttribute.getLower() == 1 && signalAttribute.getDefaultValue() == null
                        && action.getArgument(signalAttribute.getName(), signalAttribute.getType()) == null) {
                    problemBuilder.addProblem(new MissingRequiredArgument(signalAttribute.getName()), node);
                    throw new AbortedStatementCompilationException();
                }
            action.setSignal(signal);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getSignal(), getBoundElement());
    }

    @Override
    public void caseABroadcastSpecificStatement(ABroadcastSpecificStatement node) {
        SendSignalAction action = (SendSignalAction) builder.createAction(IRepository.PACKAGE.getSendSignalAction());
        try {
            super.caseABroadcastSpecificStatement(node);
            final String signalIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getSignal());
            Signal signal = this.context.getRepository().findNamedElement(signalIdentifier, UMLPackage.Literals.SIGNAL,
                    namespaceTracker.currentNamespace(null));
            if (signal == null) {
                problemBuilder.addError("Unknown signal '" + signalIdentifier + "'", node.getSignal());
                throw new AbortedStatementCompilationException();
            }
            action.setSignal(signal);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getSignal(), getBoundElement());
    }

    // FIXME a lot of duplication between this and
    // caseAClassOperationIdentifierExpression
    @Override
    public void caseAOperationIdentifierExpression(AOperationIdentifierExpression node) {
        Classifier targetClassifier = null;
        String operationName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
        CallOperationAction action = (CallOperationAction) builder.createAction(IRepository.PACKAGE
                .getCallOperationAction());
        try {
            // register the target input pin
            builder.registerInput(action.createTarget(null, null));
            // register the argument input pins
            int argumentCount = countElements(node.getExpressionList());
            for (int i = 0; i < argumentCount; i++)
                builder.registerInput(action.createArgument(null, null));
            // process the target and argument expressions - this will connect
            // their output pins to the input pins we just created
            super.caseAOperationIdentifierExpression(node);
            final ObjectNode targetSource = ActivityUtils.getSource(action.getTarget());
            targetClassifier = (Classifier) TypeUtils.getTargetType(getRepository(), targetSource, true);
            if (targetClassifier == null) {
                problemBuilder.addProblem(new UnclassifiedProblem(Severity.ERROR,
                        "Could not determine the type of the target"), node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            // collect sources so we can match the right operation (in
            // case of overloading)
            List<TypedElement> sources = new ArrayList<TypedElement>();
            for (InputPin argument : action.getArguments()) {
                ObjectNode argumentSource = ActivityUtils.getSource(argument);
                if (argumentSource == null) {
                    problemBuilder.addProblem(new UnclassifiedProblem(Severity.ERROR,
                            "One of the arguments does not produce a result value"), node.getExpressionList());
                    throw new AbortedStatementCompilationException();
                }
                sources.add(argumentSource);
            }
            Operation operation = findOperation(node.getIdentifier(), targetClassifier, operationName, sources, false,
                    true);
            if (!operation.isQuery() && !PackageUtils.isModelLibrary(operation.getNearestPackage()))
                ensureNotQuery(node);
            if (operation.getReturnResult() == null)
                ensureTerminal(node.getIdentifier());
            action.setOperation(operation);
            List<Parameter> inputParameters = FeatureUtils.getInputParameters(operation.getOwnedParameters());
            Map<Type, Type> wildcardSubstitutions = FeatureUtils.buildWildcardSubstitutions(new HashMap<Type, Type>(),
                    inputParameters, sources);
            int argumentPos = 0;
            for (Parameter current : operation.getOwnedParameters()) {
                OutputPin result;
                InputPin argument;
                switch (current.getDirection().getValue()) {
                case ParameterDirectionKind.IN:
                case ParameterDirectionKind.INOUT:
                    if (argumentPos == argumentCount) {
                        problemBuilder.addError("Wrong number of arguments", node.getLParen());
                        throw new AbortedStatementCompilationException();
                    }
                    argument = action.getArguments().get(argumentPos++);
                    argument.setName(current.getName());
                    TypeUtils.copyType(current, argument, targetClassifier);
                    break;
                case ParameterDirectionKind.RETURN:
                    // there should be only one of these
                    Assert.isTrue(action.getResults().isEmpty());
                    result = builder.registerOutput(action.createResult(null, null));
                    TypeUtils.copyType(current, result, targetClassifier);
                    resolveWildcardTypes(wildcardSubstitutions, current, result);
                    if (!TypeUtils.isRequiredPin(targetSource) && !TypeUtils.isMultivalued(targetSource))
                    	// it is a tentative call, result must be optional
                    	result.setLower(0);
                    break;
                case ParameterDirectionKind.OUT:
                    Assert.isTrue(false);
                }
            }
            if (argumentPos != argumentCount) {
                problemBuilder.addError("Wrong number of arguments", node.getLParen());
                return;
            }
            // set the type of the target input pin using copy type so we
            // understand collections
            TypeUtils.copyType(targetSource, action.getTarget(), targetClassifier);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getIdentifier(), targetClassifier);
    }

    private void ensureTerminal(Node identifierNode) {
        if (!builder.isCurrentActionTerminal()) {
            String operationName = TextUMLCore.getSourceMiner().getIdentifier(identifierNode);
            problemBuilder.addProblem(
                    new UnclassifiedProblem("Operation " + operationName + " does not have a result"), identifierNode);
            throw new AbortedStatementCompilationException();
        }
    }

    @Override
    public void caseARaiseSpecificStatement(ARaiseSpecificStatement node) {
        final RaiseExceptionAction action = (RaiseExceptionAction) builder.createAction(IRepository.PACKAGE
                .getRaiseExceptionAction());
        final InputPin exception;
        try {
            exception = action.createException(null, null);
            builder.registerInput(exception);
            super.caseARaiseSpecificStatement(node);
            final Type exceptionType = ActivityUtils.getSource(exception).getType();
            exception.setType(exceptionType);
            if (ActivityUtils.findHandler(action, (Classifier) exceptionType, true) == null)
                if (!builder.getCurrentActivity().getSpecification().getRaisedExceptions().contains(exceptionType))
                    problemBuilder.addWarning("Exception '" + exceptionType.getQualifiedName()
                            + "' is not declared by operation", node.getRootExpression());
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getRaise(), getBoundElement());
        // a raise exception action is a final action
        ActivityUtils.makeFinal(builder.getCurrentBlock(), action);
    }

    @Override
    public void caseARepeatLoopBody(ARepeatLoopBody node) {
        LoopNode currentLoop = (LoopNode) builder.getCurrentBlock();
        currentLoop.setIsTestedFirst(false);
        createBlock(IRepository.PACKAGE.getStructuredActivityNode(), false, () -> {
            super.caseARepeatLoopBody(node);
            currentLoop.getBodyParts().add(builder.getCurrentBlock());
        });
    }

    @Override
    public void caseATryStatement(ATryStatement node) {
        if (node.getCatchSection() == null && node.getFinallySection() == null) {
            problemBuilder.addError("One or both catch and finally sections are required", node.getTry());
            throw new AbortedStatementCompilationException();
        }
        createBlock(false, () -> {
            if (node.getCatchSection() != null)
                node.getCatchSection().apply(this);
            node.getProtectedBlock().apply(this);
        });
    }

    @Override
    public void caseACatchSection(ACatchSection node) {
        StructuredActivityNode protectedBlock = builder.getCurrentBlock();
        createBlock(IRepository.PACKAGE.getStructuredActivityNode(), false, (handlerBlock) -> {
            // declare exception variable
            node.getVarDecl().apply(this);
            node.getHandlerBlock().apply(this);
            Variable exceptionVar = handlerBlock.getVariables().get(0);
            ExceptionHandler exceptionHandler = protectedBlock.createHandler();
            exceptionHandler.getExceptionTypes().add((Classifier) exceptionVar.getType());
            InputPin exceptionInputPin = (InputPin) handlerBlock.createNode(exceptionVar.getName(),
                    UMLPackage.Literals.INPUT_PIN);
            exceptionInputPin.setType(exceptionVar.getType());
            exceptionHandler.setExceptionInput(exceptionInputPin);
            exceptionHandler.setHandlerBody(handlerBlock);
        });
    }

    @Override
    public void caseASelfIdentifierExpression(ASelfIdentifierExpression node) {
        ReadSelfAction action = (ReadSelfAction) builder.createAction(IRepository.PACKAGE.getReadSelfAction());
        try {
            super.caseASelfIdentifierExpression(node);
            Activity currentActivity = builder.getCurrentActivity();
            while (MDDExtensionUtils.isClosure(currentActivity)) {
                // TODO refactor to use ActivityUtils
                ActivityNode rootNode = MDDExtensionUtils.getClosureContext(currentActivity);
                currentActivity = ActivityUtils.getActionActivity(rootNode);
            }
            final BehavioralFeature operation = currentActivity.getSpecification();
            boolean staticContext = false;
            if (operation != null) {
            	staticContext = operation.isStatic();
            } else if (MDDExtensionUtils.isConstraintBehavior(currentActivity)) {
            	Constraint constraint = MDDExtensionUtils.getBehaviorConstraint(currentActivity);
            	staticContext = MDDExtensionUtils.isStaticConstraint(constraint);
            }
            if (staticContext) {
                problemBuilder.addProblem(new ReadSelfFromStaticContext(), node);
                throw new AbortedStatementCompilationException();
            }
            Classifier currentClassifier = ActivityUtils.getContext(currentActivity);
            if (currentClassifier == null) {
                problemBuilder.addProblem(new InternalProblem("Could not determine context"), node);
                throw new AbortedStatementCompilationException();
            }
            OutputPin result = action.createResult(null, currentClassifier);
            result.setLower(1);
            result.setUpper(1);
			builder.registerOutput(result);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getSelf(), getBoundElement());
    }

    // TODO temporarily disabled during refactor to remove metamodel extensions
    // @Override
    // public void
    // caseASetLiteralIdentifierExpression(ASetLiteralIdentifierExpression node)
    // {
    // String qualifiedIdentifier =
    // Util.parseQualifiedIdentifier(node.getMinimalTypeIdentifier());
    // final Classifier classifier =
    // (Classifier) getRepository().findNamedElement(qualifiedIdentifier,
    // IRepository.PACKAGE.getClassifier(), currentPackage());
    // if (classifier == null) {
    // problemBuilder.addError("Unknown classifier '" + qualifiedIdentifier +
    // "'", node
    // .getMinimalTypeIdentifier());
    // throw new AbortedStatementCompilationException();
    // }
    //
    // final CollectionLiteral valueSpec =
    // (CollectionLiteral) currentPackage().createPackagedElement(null,
    // MetaPackage.eINSTANCE.getCollectionLiteral());
    // valueSpec.setType(classifier);
    // node.apply(new DepthFirstAdapter() {
    // @Override
    // public void caseASetValue(ASetValue node) {
    // // TODO create a value specification and add it to
    // ValueSpecification value =
    // MDDUtil.createUnlimitedNatural(classifier.getNearestPackage(),
    // valueSpec.getValues()
    // .size());
    // valueSpec.getValues().add(value);
    // super.caseASetValue(node);
    // }
    // });
    // ValueSpecificationAction valueSpecAction =
    // buildValueSpecificationAction(valueSpec, node);
    // final OutputPin outputPin = valueSpecAction.getOutputs().get(0);
    // outputPin.setLowerValue(MDDUtil.createUnlimitedNatural(classifier.getNearestPackage(),
    // 0));
    // outputPin.setUpperValue(MDDUtil.createUnlimitedNatural(classifier.getNearestPackage(),
    // null));
    // }

    @Override
    public void caseAStatement(AStatement node) {
        try {
            super.caseAStatement(node);
            processAnnotations(node.getAnnotations(), this.builder.getLastRootAction());
        } catch (AbortedStatementCompilationException e) {
            // aborted statement compilation...
            if (builder.isDebug())
                LogUtils.logWarning(TextUMLCore.PLUGIN_ID, null, e);
        } catch (AbortedCompilationException e) {
            // we don't handle those here
            throw e;
        } catch (AssertionError e) {
            LogUtils.logError(TextUMLCore.PLUGIN_ID, "Assertion failed", e);
            problemBuilder.addError("Exception: " + e.toString(), node);
        } catch (RuntimeException e) {
            LogUtils.logError(TextUMLCore.PLUGIN_ID, "Unexpected exception", e);
            problemBuilder.addError("Exception: " + e.toString(), node);
        }
    }

    @Override
    public void caseAParenthesisOperand(AParenthesisOperand node) {
        ACast cast = (ACast) node.getCast();
		if (cast == null) {
            // no casting, just process inner expression
            node.getExpression().apply(this);
            return;
        }
		PTypeIdentifier typeIdentifier = cast.getTypeIdentifier();
        buildCast(node.getExpression(), action ->  
    		new TypeSetter(
    				sourceContext, 
    				namespaceTracker.currentNamespace(null), 
    				action.getStructuredNodeOutputs().get(0)
			).process(typeIdentifier)
		);
    }

	private void buildCast(Node expression, Consumer<StructuredActivityNode> doIt) {
		StructuredActivityNode action = (StructuredActivityNode) builder
                .createAction(Literals.STRUCTURED_ACTIVITY_NODE);
        MDDExtensionUtils.makeCast(action);
        try {
            // register the target input pin (type is null)
            InputPin inputPin = (InputPin) action.createStructuredNodeInput(null, null);
            builder.registerInput(inputPin);
            // process the target expression - this will connect its output pin
            // to the input pin we just created
            expression.apply(this);
            // copy whatever multiplicity coming into the source to the source
            // but leave type as undefined as this action is supposed to accept any type of input
            ObjectNode inputSource = ActivityUtils.getSource(inputPin);
			TypeUtils.copyMultiplicity((MultiplicityElement) inputSource, inputPin);
            // register the result output pin
            OutputPin outputPin = (OutputPin) action.createStructuredNodeOutput(null, null);
            doIt.accept(action);
            builder.registerOutput(outputPin);
            fillDebugInfo(action, expression);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, expression, getBoundElement());
	}
    
    public void handleUnaryExpressionAsOperation(Node operator, Node operand, String operationName) {
        handleUnaryExpressionAsOperation(operator, operationName, () -> {
            operand.apply(BehaviorGenerator.this);
            return operand;
        });
    }
    
    public void handleUnaryExpressionAsOperation(Node contextNode, String operationName, Supplier<Node> childProcessor) {
        CallOperationAction action = (CallOperationAction) builder.createAction(IRepository.PACKAGE
                .getCallOperationAction());
        try {
            // register the target input pin
            builder.registerInput(action.createTarget(null, null));
            // process the target expression - this will connect its output pin
            // to the input pin we just created
            Node childNode = childProcessor.get();
            
            InputPin target = action.getTarget();
            ObjectNode targetSource = ActivityUtils.getSource(target);
			Type targetType = targetSource.getType();
            
            ensure(targetType != null, childNode, () -> new UnclassifiedProblem("No type information for " + childNode));
            Operation operation = FeatureUtils.findOperation(getRepository(), (Classifier) targetType, operationName, Arrays.asList(), false, true);
            
            TypeUtils.copyType(targetSource, target);
            
            if (operation == null) {
                missingOperation(true, contextNode, (Classifier) targetType, operationName,
                        Arrays.asList(), false);
            }
            
            // register the result output pin
            OutputPin result = action.createResult(null, operation.getType());
			builder.registerOutput(result);
            result.setLower(Math.min(target.getLower(), operation.getReturnResult().getLower()));
            action.setOperation(operation);
            fillDebugInfo(action, contextNode.parent());
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, contextNode, getBoundElement());
    }


    @Override
    public void caseAUnlinkSpecificStatement(AUnlinkSpecificStatement node) {
        buildWriteLinkAction(node, node.getMinimalTypeIdentifier(), IRepository.PACKAGE.getDestroyLinkAction());
    }

    @Override
    public void caseAEmptyReturnSpecificStatement(AEmptyReturnSpecificStatement node) {
        Variable variable = builder.getReturnValueVariable();
        if (variable != null) {
            problemBuilder.addProblem(new ReturnValueRequired(), node.getReturn());
            throw new AbortedScopeCompilationException();
        }
        Action previousStatement = builder.getLastRootAction();
        if (previousStatement != null)
            ActivityUtils.makeFinal(builder.getCurrentBlock(), previousStatement);
    }

    @Override
    public void caseAValuedReturnSpecificStatement(AValuedReturnSpecificStatement node) {
        buildReturnStatement(node.getRootExpression());
    }

    protected void buildReturnStatement(Node node) {
        TemplateableElement bound = (Class) MDDUtil.getNearest(builder.getCurrentActivity(),
                IRepository.PACKAGE.getClass_());
        AddVariableValueAction action = (AddVariableValueAction) builder.createAction(IRepository.PACKAGE
                .getAddVariableValueAction());
        try {
            Variable variable = builder.getReturnValueVariable();
            if (variable == null) {
                problemBuilder.addProblem(new ReturnValueNotExpected(), node);
                throw new AbortedScopeCompilationException();
            }
            final InputPin value = builder.registerInput(action.createValue(null, null));
            node.apply(this);
            action.setVariable(variable);
            TypeUtils.copyType(variable, value, bound);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node, getBoundElement());
        ActivityUtils.makeFinal(builder.getCurrentBlock(), action);
    }

    @Override
    public void caseAVarDecl(final AVarDecl node) {
        super.caseAVarDecl(node);
        String varIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
        final Variable var = builder.getCurrentBlock().createVariable(varIdentifier, null);
        if (node.getOptionalType() != null)
            // type is optional for local vars
            new TypeSetter(sourceContext, namespaceTracker.currentNamespace(null), var).process(node.getOptionalType());
        else {
            // ensure a type is eventually inferred
            defer(Step.BEHAVIOR, r -> {
                ensure(var.getType() != null, node.getIdentifier(), Severity.ERROR, () -> "Could not infer a type for variable '" + var.getName() + "'");
            });
        }
    }

    @Override
    public void caseAVariableAccess(AVariableAccess node) {
        ReadVariableAction action = (ReadVariableAction) builder.createAction(IRepository.PACKAGE
                .getReadVariableAction());
        try {
            super.caseAVariableAccess(node);
            final OutputPin result = action.createResult(null, null);
            builder.registerOutput(result);
            String variableName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
            Variable variable = builder.getVariable(variableName);
            ensure(variable != null, node.getIdentifier(), Severity.ERROR, () -> "Unknown local variable '" + variableName + "'");
            action.setVariable(variable);
            TypeUtils.copyType(variable, result, getBoundElement());
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getIdentifier(), getBoundElement());
    }

    private Class getBoundElement() {
        return (Class) MDDUtil.getNearest(builder.getCurrentActivity().getOwner(), IRepository.PACKAGE.getClass_());
    }

    @Override
    public void caseAWhileLoopBody(AWhileLoopBody node) {
        LoopNode currentLoop = (LoopNode) builder.getCurrentBlock();
        currentLoop.setIsTestedFirst(true);
        createBlock(IRepository.PACKAGE.getStructuredActivityNode(), false, () -> {
            super.caseAWhileLoopBody(node);
            currentLoop.getBodyParts().add(builder.getCurrentBlock());
        });
    }

    @Override
    public void caseAWriteAttributeSpecificStatement(AWriteAttributeSpecificStatement node) {
        AddStructuralFeatureValueAction action = (AddStructuralFeatureValueAction) builder
                .createAction(IRepository.PACKAGE.getAddStructuralFeatureValueAction());
        final String attributeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
        action.setIsReplaceAll(true);
        try {
            builder.registerInput(action.createObject(null, null));
            builder.registerInput(action.createValue(null, null));
            super.caseAWriteAttributeSpecificStatement(node);
            Classifier targetClassifier = (Classifier) ActivityUtils.getSource(action.getObject()).getType();
            if (targetClassifier == null) {
                problemBuilder.addError("Object type not determined for '"
                        + ((ATarget) node.getTarget()).getOperand().toString().trim() + "'", node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            Property attribute = FeatureUtils.findAttribute(targetClassifier, attributeIdentifier, false, true);
            if (attribute == null) {
                problemBuilder.addError(
                        "Unknown attribute '" + attributeIdentifier + "' in '" + targetClassifier.getName() + "'",
                        node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            if (attribute.isDerived()) {
                problemBuilder.addProblem(new CannotModifyADerivedAttribute(), node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            ensureNotQuery(node);
            action.setStructuralFeature(attribute);
            action.getObject().setType(targetClassifier);
            TypeUtils.copyType(attribute, action.getValue(), targetClassifier);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getIdentifier(), getBoundElement());
    }

    @Override
    public void caseAWriteClassAttributeSpecificStatement(AWriteClassAttributeSpecificStatement node) {
        ensureNotQuery(node);
        TemplateableElement bound = null;
        AddStructuralFeatureValueAction action = (AddStructuralFeatureValueAction) builder
                .createAction(IRepository.PACKAGE.getAddStructuralFeatureValueAction());
        action.setIsReplaceAll(true);
        try {
            String typeIdentifier = TextUMLCore.getSourceMiner()
                    .getQualifiedIdentifier(node.getMinimalTypeIdentifier());
            Classifier targetClassifier = (Classifier) getRepository().findNamedElement(typeIdentifier,
                    IRepository.PACKAGE.getClassifier(), namespaceTracker.currentPackage());
            if (targetClassifier == null) {
                problemBuilder.addError("Class reference expected: '" + typeIdentifier + "'",
                        node.getMinimalTypeIdentifier());
                throw new AbortedStatementCompilationException();
            }
            bound = targetClassifier;
            final String attributeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
            Property attribute = FeatureUtils.findAttribute(targetClassifier, attributeIdentifier, false, true);
            if (attribute == null) {
                problemBuilder.addError(
                        "Unknown attribute '" + attributeIdentifier + "' in '" + targetClassifier.getName() + "'",
                        node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            if (attribute.isDerived()) {
                problemBuilder.addProblem(new CannotModifyADerivedAttribute(), node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            if (!attribute.isStatic()) {
                problemBuilder.addError("Static attribute expected: '" + attributeIdentifier + "' in '"
                        + targetClassifier.getName() + "'", node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            builder.registerInput(action.createValue(null, null));
            // builds expression
            node.getRootExpression().apply(this);
            action.setStructuralFeature(attribute);
            TypeUtils.copyType(attribute, action.getValue(), targetClassifier);
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getIdentifier(), bound);
    }

    @Override
    public void caseAWriteVariableSpecificStatement(AWriteVariableSpecificStatement node) {
        AddVariableValueAction action = (AddVariableValueAction) builder.createAction(IRepository.PACKAGE
                .getAddVariableValueAction());
        action.setIsReplaceAll(true);
        try {
            final InputPin value = builder.registerInput(action.createValue(null, null));
            super.caseAWriteVariableSpecificStatement(node);
            String variableName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
            Variable variable = builder.getVariable(variableName);
            if (variable == null) {
                problemBuilder.addError("Unknown local variable '" + variableName + "'", node.getIdentifier());
                throw new AbortedStatementCompilationException();
            }
            action.setVariable(variable);
            if (variable.getType() == null)
                // infer variable type if omitted
                TypeUtils.copyType(ActivityUtils.getSource(value), variable, getBoundElement());
            TypeUtils.copyType(variable, value, getBoundElement());
            fillDebugInfo(action, node);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node.getIdentifier(), getBoundElement());
    }

    private void checkIncomings(final Action action, final Node node, TemplateableElement bound) {
        ObjectFlow incompatible = TypeUtils.checkCompatibility(getRepository(), action, bound);
        if (incompatible == null)
            return;
        final ObjectNode target = ((ObjectNode) incompatible.getTarget());
        final ObjectNode source = ((ObjectNode) incompatible.getSource());
        final Type anyType = findBuiltInType(TypeUtils.ANY_TYPE, node);
        if (target.getType() != null && target.getType() != anyType
                && (source.getType() == null || source.getType() == anyType))
            source.setType(target.getType());
        else
            reportIncompatibleTypes(node, target, source);
        checkAllInputsHaveFlows(action, node);
    }

	private void checkAllInputsHaveFlows(Action action, Node node) {
    	boolean alreadyReportedErrors = !problemBuilder.hasErrors();
		if (!alreadyReportedErrors)
	    	ActivityUtils.getActionInputs(action).stream().forEach(input -> {
	    		if (input.getIncomings().isEmpty())
	    			problemBuilder.addProblem(new UnclassifiedProblem("Missing incoming flow for input pin " + StringUtils.trimToEmpty(input.getName())), node);
	    	});
	}

	private void reportIncompatibleTypes(final Node context, final ObjectNode expected, final ObjectNode actual) {
		problemBuilder.addProblem(new TypeMismatch(MDDUtil.getDisplayName(expected), MDDUtil.getDisplayName(actual)),
		        context);
	}

    private int countElements(PExpressionList list) {
        if (list instanceof AEmptyExpressionList)
            return 0;
        final int[] counter = { 0 };
        list.apply(new DepthFirstAdapter() {
            @Override
            public void caseAExpressionListElement(AExpressionListElement node) {
                counter[0]++;
            }
        });
        return counter[0];
    }

    /**
     * Fills in the given activity with behavior parsed from the given node.
     */
    public void createBody(Node bodyNode, Activity activity) {
        namespaceTracker.enterNamespace(activity);
        try {
            builder.createRootBlock(activity);
            try {
                bodyNode.apply(this);
            } finally {
                builder.closeRootBlock();
            }
        } finally {
            namespaceTracker.leaveNamespace();
        }
        
        defer(Step.BEHAVIOR, r -> validateReturnStatement(bodyNode, activity));
        // process any deferred activities
        while (!deferredActivities.isEmpty()) {
            DeferredActivity next = deferredActivities.remove(0);
            createBody(next.getBlock(), next.getActivity());
        }
    }

    public void validateReturnStatement(Node bodyNode, Activity activity) {
        boolean hasReturnWithValue = ActivityUtils.getFinalAction(ActivityUtils.getBodyNode(activity)) == null;
        if (!problemBuilder.hasErrors() && hasReturnWithValue) {
            boolean returnValueRequired = FeatureUtils.findReturnParameter(activity.getOwnedParameters()) != null;
            ensure(!returnValueRequired, bodyNode, () -> new ReturnStatementRequired());
        }
    }

    private Clause createClause() {
        ConditionalNode currentConditional = (ConditionalNode) builder.getCurrentBlock();
        boolean hasClauses = !currentConditional.getClauses().isEmpty();
        Clause newClause = currentConditional.createClause();
        if (hasClauses) {
            Clause previousClause = currentConditional.getClauses().get(currentConditional.getClauses().size() - 1);
            previousClause.getSuccessorClauses().add(newClause);
        }
        return newClause;
    }

    private <T extends StructuredActivityNode> T createBlock(boolean dataFlows, Runnable builderBehavior) {
        return createBlock(IRepository.PACKAGE.getStructuredActivityNode(), dataFlows, builderBehavior);
    }

    private <T extends StructuredActivityNode> T createBlock(EClass activityNode, boolean dataFlows, Runnable builderBehavior) {
        return createBlock(activityNode, dataFlows, it -> builderBehavior.run());
    }
    
    private <T extends StructuredActivityNode> T createBlock(EClass activityNode, boolean dataFlows, Consumer<T> builderBehavior) {
        T block = (T) builder.createBlock(activityNode, dataFlows);
        try {
        	builderBehavior.accept(block);
        } catch (AbortedScopeCompilationException e) {
        	if (!ActivityUtils.isBodyNode(block)) {
        		// keep throwing exception
        		throw e;
        	}
            // aborted activity block compilation...
            if (builder.isDebug())
                LogUtils.logWarning(TextUMLCore.PLUGIN_ID, null, e);
        } finally {
            builder.closeBlock();
        }
        return block;
    }
    
    private void createClauseBody(Clause clause, Runnable builderBlock) {
    	createBlock(false, () -> {
        	builderBlock.run();
            clause.getBodies().add(builder.getCurrentBlock());
            Action lastRootAction = builder.getLastRootAction();
            if (lastRootAction != null) {
            	List<OutputPin> bodyOutput = ActivityUtils.getActionOutputs(lastRootAction);
				clause.getBodyOutputs().addAll(bodyOutput);
        	}
        });
    }

    private void createClauseTest(Clause clause, Runnable builderBlock) {
    	createBlock(false, () -> {
        	builderBlock.run();
        	clause.getTests().add(builder.getCurrentBlock());
            Action lastRootAction = builder.getLastRootAction();
			OutputPin decider = ActivityUtils.getActionOutputs(lastRootAction).get(0);
            clause.setDecider(decider);
        });
    }
    

    private void deferBlockCreation(Node block, Activity activity) {
        deferredActivities.add(new DeferredActivity(activity, block));
    }

    private Operation findOperation(Node node, Classifier classifier, String operationName,
            List<TypedElement> arguments, boolean isStatic, boolean required) {
        Operation found = FeatureUtils
                .findOperation(getRepository(), classifier, operationName, arguments, false, true);
        if (found != null) {
            if (found.isStatic() != isStatic) {
                problemBuilder.addError((isStatic ? "Static" : "Non-static") + " operation expected: '" + operationName
                        + "' in '" + found.getType().getQualifiedName() + "'", node);
                throw new AbortedStatementCompilationException();
            }
            return found;
        }
        return missingOperation(required, node, classifier, operationName, arguments, isStatic);
    }

    protected Operation missingOperation(boolean required, Node node, Classifier classifier, String operationName,
            List<TypedElement> arguments, boolean isStatic) {
        if (!required)
            return null;
        Operation alternative = FeatureUtils.findOperation(getRepository(), classifier, operationName, null, false,
                true);
        problemBuilder.addProblem(
                new UnknownOperation(classifier.getQualifiedName(), operationName, MDDUtil
                        .getArgumentListString(arguments), isStatic, alternative), node);
        throw new AbortedStatementCompilationException();
    }

//    OperationInfo parseOperationInfo(Node operatorNode, int infoCount) {
//        final OperationInfo info = new OperationInfo(infoCount);
//        operatorNode.apply(new DepthFirstAdapter() {
//
//            @Override
//            public void caseAArithmeticBinaryOperatorP1(AArithmeticBinaryOperatorP1 node) {
//                super.caseAArithmeticBinaryOperatorP1(node);
//                handleArithmeticOperator(info);
//            }
//            
//            @Override
//            public void caseAArithmeticBinaryOperatorP2(AArithmeticBinaryOperatorP2 node) {
//                super.caseAArithmeticBinaryOperatorP2(node);
//                handleArithmeticOperator(info);
//            }
//
//			private void handleArithmeticOperator(final OperationInfo info) {
//				info.types[0] = info.types[1] = info.types[2] = (Classifier) getRepository().findNamedElement(
//                        "base::Integer", IRepository.PACKAGE.getType(), null);
//			}
//
//            @Override
//            public void caseAComparisonBinaryOperatorP2(AComparisonBinaryOperatorP2 node) {
//                super.caseAComparisonBinaryOperatorP2(node);
//                handleComparisonOperator(info);
//            }
//
//			private void handleComparisonOperator(final OperationInfo info) {
//				info.types[0] = info.types[1] = (Classifier) getRepository().findNamedElement("base::Comparable",
//                        IRepository.PACKAGE.getType(), null);
//                info.types[2] = (Classifier) getRepository().findNamedElement("base::Boolean",
//                        IRepository.PACKAGE.getType(), null);
//			}
//
//            @Override
//            public void caseAEqualsComparisonBinaryOperatorP2(AEqualsComparisonBinaryOperatorP2 node) {
//                info.operationName = "equals";
//            }
//
//            @Override
//            public void caseANotEqualsComparisonBinaryOperatorP2(ANotEqualsComparisonBinaryOperatorP2 node) {
//                info.operationName = "not";
//            }
//
//            @Override
//            public void caseAGreaterOrEqualsComparisonBinaryOperatorP2(AGreaterOrEqualsComparisonBinaryOperatorP2 node) {
//                info.operationName = "greaterOrEquals";
//            }
//
//            @Override
//            public void caseAGreaterThanComparisonBinaryOperatorP2(AGreaterThanComparisonBinaryOperatorP2 node) {
//                info.operationName = "greater";
//            }
//            
//            @Override
//            public void caseAIdentityComparisonBinaryOperatorP2(AIdentityComparisonBinaryOperatorP2 node) {
//            	super.caseAIdentityComparisonBinaryOperatorP2(node);
//                info.operationName = "same";
//                info.types[0] = info.types[1] = (Classifier) getRepository().findNamedElement("base::Any",
//                        IRepository.PACKAGE.getClass_(), null);
//                info.types[2] = (Classifier) getRepository().findNamedElement("base::Boolean",
//                        IRepository.PACKAGE.getType(), null);
//            }
//
//            @Override
//            public void caseALogicalBinaryOperatorP2(ALogicalBinaryOperatorP2 node) {
//                super.caseALogicalBinaryOperatorP2(node);
//                handleLogicalBinaryOperator();
//            }
//            
//            @Override
//            public void caseALogicalBinaryOperatorP1(ALogicalBinaryOperatorP1 node) {
//                super.caseALogicalBinaryOperatorP1(node);
//                handleLogicalBinaryOperator();
//            }
//            
//            private void handleLogicalBinaryOperator() {
//                info.types[0] = info.types[1] = info.types[2] = (Classifier) getRepository().findNamedElement(
//                        "base::Boolean", IRepository.PACKAGE.getType(), null);
//            }
//            
//            @Override
//            public void caseALowerOrEqualsComparisonBinaryOperatorP2(ALowerOrEqualsComparisonBinaryOperatorP2 node) {
//                info.operationName = "lowerOrEquals";
//            }
//
//            @Override
//            public void caseALowerThanComparisonBinaryOperatorP2(ALowerThanComparisonBinaryOperatorP2 node) {
//                info.operationName = "lowerThan";
//            }
//
//            @Override
//            public void caseAMinusUnaryOperator(AMinusUnaryOperator node) {
//                super.caseAMinusUnaryOperator(node);
//                info.types[0] = info.types[1] = (Classifier) getRepository().findNamedElement("base::Integer",
//                        IRepository.PACKAGE.getType(), null);
//            }
//
//            @Override
//            public void caseANotUnaryOperator(ANotUnaryOperator node) {
//                super.caseANotUnaryOperator(node);
//                info.types[0] = info.types[1] = (Classifier) getRepository().findNamedElement("base::Boolean",
//                        IRepository.PACKAGE.getType(), null);
//            }
//
//            @Override
//            public void caseANotNullUnaryOperator(ANotNullUnaryOperator node) {
//                super.caseANotNullUnaryOperator(node);
//                info.types[0] = (Classifier) getRepository().findNamedElement("base::Basic",
//                        IRepository.PACKAGE.getType(), null);
//                info.types[1] = (Classifier) getRepository().findNamedElement("base::Boolean",
//                        IRepository.PACKAGE.getType(), null);
//            }
//
//            public void caseTAnd(TAnd node) {
//                info.operationName = "and";
//            }
//
//            public void caseTDiv(TDiv node) {
//                info.operationName = "divide";
//            }
//
//            public void caseTMinus(TMinus node) {
//                info.operationName = "subtract";
//            }
//
//            public void caseTMult(TMult node) {
//                info.operationName = "multiply";
//            }
//
//            @Override
//            public void caseTNot(TNot node) {
//                info.operationName = "not";
//            }
//
//            public void caseTNotNull(TNotNull node) {
//                info.operationName = "notNull";
//            }
//
//            public void caseTOr(TOr node) {
//                info.operationName = "or";
//            }
//
//            public void caseTPlus(TPlus node) {
//                info.operationName = "add";
//            }
//        });
//        return info;
//    }

    @Override
    public void caseATupleConstructor(ATupleConstructor node) {
        final StructuredActivityNode action = (StructuredActivityNode) builder.createAction(IRepository.PACKAGE
                .getStructuredActivityNode());
        MDDExtensionUtils.makeObjectInitialization(action);
        try {
            node.apply(new DepthFirstAdapter() {
                @Override
                public void caseATupleComponentValue(ATupleComponentValue node) {
                    String slotName = sourceMiner.getIdentifier(node.getIdentifier());
                    builder.registerInput(action.createStructuredNodeInput(slotName, null));
                    node.getExpression().apply(BehaviorGenerator.this);
                }
            });
            builder.registerOutput(action.createStructuredNodeOutput(null, null));

            // now we determine the types of the incoming flows and build a
            // corresponding data type on the fly
            List<String> slotNames = new ArrayList<>();
            List<Type> slotTypes = new ArrayList<>();
            action.getStructuredNodeInputs().forEach((input) -> {
                slotNames.add(input.getName());
                slotTypes.add(input.getType());
            });

            DataType dataType = DataTypeUtils.findOrCreateDataType(namespaceTracker.currentPackage(), slotNames,
                    slotTypes);
            action.getStructuredNodeOutputs().get(0).setType(dataType);
        } finally {
            builder.closeAction();
        }
        checkIncomings(action, node, getBoundElement());
    }

    public void createBodyLater(final Node bodyNode, final Activity body) {
        defer(Step.BEHAVIOR, r -> createBody(bodyNode, body));
    }

    public void createConstraintBehaviorLater(final BehavioredClassifier parent, final Constraint constraint,
            final Node constraintBlock, final List<Parameter> parameters) {
        defer(Step.BEHAVIOR, repository -> createConstraintBehavior(parent, constraint, constraintBlock, parameters));
    }

    public Activity createConstraintBehavior(BehavioredClassifier parent, Constraint constraint, Node constraintBlock,
            List<Parameter> parameters) {
        Activity activity = MDDExtensionUtils.createConstraintBehavior(parent, constraint);

        for (Parameter parameter : parameters)
            activity.getOwnedParameters().add(parameter);

        Classifier constraintType = BasicTypeUtils.findBuiltInType("Boolean");
        activity.createOwnedParameter(null, constraintType).setDirection(ParameterDirectionKind.RETURN_LITERAL);
        
        if (constraintBlock == null) {
			constraintBlock = new AExpressionSimpleBlockResolved(
        		new ASimpleExpressionBlock(
					null, 
					buildOperandExpression(new ALiteralOperand(
						new ABooleanLiteral(
							new ATrueBoolean(new TTrue())
						)
					)), 
					null
				)
			);
		}
        createBody(constraintBlock, activity);

        ValueSpecification reference = ActivityUtils.buildBehaviorReference(constraint.getNearestPackage(), activity,
                constraintType);
        constraint.setSpecification(reference);
        return activity;
    }

	private ARootExpression buildOperandExpression(POperand operand) {
		return buildExpression(new AAltNestedExpressionP1(new AAltExpressionP0(operand)));
	}

	private ARootExpression buildExpression(AAltNestedExpressionP1 expressionP1) {
		return buildExpression(new AAltNestedExpressionP2(expressionP1));
	}

	private ARootExpression buildExpression(AAltNestedExpressionP2 expressionP2) {
		return buildExpression(new AAltNestedExpressionP3(expressionP2));
	}

	private ARootExpression buildExpression(AAltNestedExpressionP3 expressionP3) {
		return buildExpression(new AAltNestedExpressionP4(expressionP3));
	}

	private ARootExpression buildExpression(AAltNestedExpressionP4 expression4) {
		return buildExpression(new AAltNestedExpressionP5(expression4));
	}

	private ARootExpression buildExpression(AAltNestedExpressionP5 expressionP5) {
		return new ARootExpression(new AExpression(expressionP5));
	}
}