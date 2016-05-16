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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.Analysis;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAlt0ExpressionP1;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAlt0ExpressionP2;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAlt0ExpressionP3;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAlt0ExpressionP4;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAlt1ExpressionP1;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAlt1ExpressionP4;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAnnotations;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationRoleDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAttributeInvariant;
import com.abstratt.mdd.frontend.textuml.grammar.node.ABehavioralFeatureBody;
import com.abstratt.mdd.frontend.textuml.grammar.node.ABlockNonIfStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ACast;
import com.abstratt.mdd.frontend.textuml.grammar.node.ACatchSection;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClauseBody;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClosure;
import com.abstratt.mdd.frontend.textuml.grammar.node.AElseRestIf;
import com.abstratt.mdd.frontend.textuml.grammar.node.AElseifRestIf;
import com.abstratt.mdd.frontend.textuml.grammar.node.AFeatureDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AGlobalDirectiveSection;
import com.abstratt.mdd.frontend.textuml.grammar.node.AIfClause;
import com.abstratt.mdd.frontend.textuml.grammar.node.AIfStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ANamedArgument;
import com.abstratt.mdd.frontend.textuml.grammar.node.ANoIfStatementResolved;
import com.abstratt.mdd.frontend.textuml.grammar.node.ANonBlockNonIfStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationConstraint;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationPrecondition;
import com.abstratt.mdd.frontend.textuml.grammar.node.AParamDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.ARegularInvariantConstraint;
import com.abstratt.mdd.frontend.textuml.grammar.node.ARootExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASendSpecificStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASignature;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleExpressionBlock;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleStatementBlock;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStart;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStateBehavior;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStateDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStateMachineDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStereotypeDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStereotypePropertyDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASubNamespace;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATopLevelElement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATransitionDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATransitionGuard;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATryStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATupleComponentValue;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATupleComponentValueTail;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATupleConstructor;
import com.abstratt.mdd.frontend.textuml.grammar.node.AVarDeclSection;
import com.abstratt.mdd.frontend.textuml.grammar.node.AWhileStatement;
import com.abstratt.mdd.frontend.textuml.grammar.node.AWithIfStatementResolved;
import com.abstratt.mdd.frontend.textuml.grammar.node.AWordyBlock;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PGlobalDirective;
import com.abstratt.mdd.frontend.textuml.grammar.node.TAssignop;
import com.abstratt.mdd.frontend.textuml.grammar.node.TColon;
import com.abstratt.mdd.frontend.textuml.grammar.node.TComma;
import com.abstratt.mdd.frontend.textuml.grammar.node.TComment;
import com.abstratt.mdd.frontend.textuml.grammar.node.TModelComment;
import com.abstratt.mdd.frontend.textuml.grammar.node.TWhiteSpace;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;
import com.abstratt.pluginutils.LogUtils;

/**
 * Produces a formatted output from the parsed node.
 * 
 * Implementation notes:
 * 
 */
public class TextUMLFormatter {

    private static String LINE_ENDING = System.getProperty("line.separator");

    private static int LINE_ENDING_LENGTH = LINE_ENDING.length();

    public static void main(String[] args) {
        // String toFormat =
        // "package mypak; import yourpack1; import yourpack2; class
        // ExampleClass extends AnotherClass implements Interface1, Interface2,
        // Interface3 attribute attr1 : Integer; operation op1(); operation
        // op2(); begin if false then x := 1 else while true do begin x := x +
        // 1; end; end; operation op3() : Integer; begin var x : Integer, y :
        // Integer; x := 10; y := 1; while y < x do begin x := x + 1; end; y :=
        // self.compute(15,60); return x + y; end; end; operation
        // ExampleClass.op1;begin end; end.";
        // String toFormat =
        // "package mypak; import yourpack1; import yourpack2; [persistent]class
        // ExampleClass specializes AnotherClass implements Interface1,
        // Interface2, Interface3 [transient] attribute attr1 : Integer; [
        // transactional ]operation op1(); operation op2(); end; end.";

        String toFormat = "model    bank;apply foo;import xyz;apply java;[persistent      ,external     ( language=  \"Java\"   ,         className\n=     \n\"Foo\")]class Account     attribute accountNumber : base::String; [   transient]   attribute balance : base::Real;    attribute changes : AccountChange[0,*];    operation withdraw(amount : Real);   operation deposit(amount : Real);   operation balance() : Real;   operation transfer(other : Account, amount : Real); end;[persistent]class Client    operation getAccounts() : Account[0, *];end;enumeration TestEnum  VALUE1, VALUE2, VALUE3 end;class DateTime   /*  attribute day : Integer; attribute month : Integer; attribute year : Integer;  attribute hour : Integer; attribute minute : Integer; attribute second : Integer;  */  end; [persistent]abstract class AccountChange    attribute date : DateTime;end;composition AccountAccountChange  navigable role account : Account[1];  navigable role Account.changes;end;class SimpleAccountChange specializes AccountChange  attribute amount : Real; end;[persistent]class Deposit specializes SimpleAccountChange end;[persistent]class Withdrawal specializes SimpleAccountChange end;[persistent]class Transfer specializes AccountChange  attribute secondAccount : Account;end;  [persistent]class BusinessClient specializes Client    attribute businessNumber : String;end;aggregation ClientAccount    navigable role owner : Client[1];    navigable role account : Account[0,*];end;end.";
        System.out.println(new TextUMLCompiler().format(toFormat));
    }

    private Analysis ignoredTokens;

    /**
     * Reflection-based formatter lookup is slow, so we cache formatter methods
     * based on the node.
     */
    private Map<Class<? extends Node>, Method> formatters = new HashMap<Class<? extends Node>, Method>();

    public void format(AAnnotations node, StringBuilder output, int indentation) {
        boolean newLine = isAtNewLine(output);
        boolean returnTypeAnnotation = !newLine && isAt(output, ')');
        if (returnTypeAnnotation)
            addWhitespace(output);
        doGenericFormat(node, output, indentation);
        if (newLine)
            newLine(output);
        else if (!returnTypeAnnotation)
            addWhitespace(output);
    }

    public void format(ASubNamespace node, StringBuilder output, int indentation) {
        format(node.getPackageHeading(), output, indentation);
        newLine(output);
        format(node.getNamespaceContents(), output, indentation + 1);
        newLine(output);
        format(node.getEnd(), output, indentation);
        format(node.getSemicolon(), output, indentation);
    }

    public void format(AAssociationDef node, StringBuilder output, int indentation) {
        format(node.getAnnotations(), output, indentation);
        format(node.getAssociationHeader(), output, indentation);
        newLine(output);
        newLine(output);
        format(node.getAssociationRoleDeclList(), output, indentation + 1);
        format(node.getEnd(), output, indentation);
        format(node.getSemicolon(), output, indentation);
    }

    public void format(AAssociationRoleDecl node, StringBuilder output, int indentation) {
        doGenericFormat(node, output, indentation);
        newLine(output);
        newLine(output);
    }
    
    public void format(AAlt1ExpressionP1 node, StringBuilder output, int indentation) {
    	format(node.getOperand1(), node.getOperator(), node.getOperand2(), output, indentation);
    }
    
    public void format(AAlt0ExpressionP2 node, StringBuilder output, int indentation) {
    	format(node.getOperand1(), node.getOperator(), node.getOperand2(), output, indentation);
    }
    
    public void format(AAlt0ExpressionP3 node, StringBuilder output, int indentation) {
    	format(node.getOperand1(), node.getOperator(), node.getOperand2(), output, indentation);
    }
    
    public void format(AAlt1ExpressionP4 node, StringBuilder output, int indentation) {
    	format(node.getOperand1(), node.getOperator(), node.getOperand2(), output, indentation);
    }

    public void format(Node left, Node operator, Node right, StringBuilder output, int indentation) {
        format(left, output, indentation);
        addWhitespace(output);
        format(operator, output, indentation);
        addWhitespace(output);
        format(right, output, indentation);
    }

    public void format(ARootExpression node, StringBuilder output, int indentation) {
        addWhitespace(output);
        format(node.getExpression(), output, indentation);
    }

    public void format(ATupleConstructor node, StringBuilder output, int indentation) {
        format(node.getLCurlyBracket(), output, indentation);
        newLine(output);
        format(node.getTupleComponentValue(), output, indentation);
        format(node.getTupleComponentValueTail(), output, indentation);
        newLine(output);
        format(node.getRCurlyBracket(), output, indentation);
    }

    public void format(ATupleComponentValue node, StringBuilder output, int indentation) {
        format(node.getIdentifier(), output, indentation + 1);
        format(node.getAssignop(), output, indentation + 1);
        format(node.getExpression(), output, indentation + 1);
    }

    public void format(ATupleComponentValueTail node, StringBuilder output, int indentation) {
        format(node.getComma(), output, indentation);
        newLine(output);
        format(node.getTupleComponentValue(), output, indentation);
    }

    public void format(AClassDef node, StringBuilder output, int indentation) {
        format(node.getAnnotations(), output, indentation);
        format(node.getClassHeader(), output, indentation);
        format(node.getFeatureDeclList(), output, indentation + 1);
        newLine(output);
        format(node.getEnd(), output, indentation);
        format(node.getSemicolon(), output, indentation);
    }

    public void format(AStereotypeDef node, StringBuilder output, int indentation) {
        format(node.getAnnotations(), output, indentation);
        format(node.getStereotypeDefHeader(), output, indentation);
        format(node.getStereotypePropertyDecl(), output, indentation + 1);
        newLine(output);
        format(node.getEnd(), output, indentation);
        format(node.getSemicolon(), output, indentation);
    }

    public void format(AFeatureDecl node, StringBuilder output, int indentation) {
        newLine(output);
        newLine(output);
        doGenericFormat(node, output, indentation);
    }

    public void format(AStereotypePropertyDecl node, StringBuilder output, int indentation) {
        newLine(output);
        doGenericFormat(node, output, indentation);
    }

    public void format(AOperationDecl node, StringBuilder output, int indentation) {
        format(node.getOperationHeader(), output, indentation);
        format(node.getOperationConstraint(), output, indentation + 1);
        format(node.getSemicolon(), output, indentation);
        format(node.getOptionalBehavioralFeatureBody(), output, indentation);
    }

    public void format(ASignature node, StringBuilder output, int indentation) {
        format(node.getLParen(), output, indentation);
        format(node.getParamDeclList(), output, indentation);
        format(node.getRParen(), output, indentation);
        format(node.getOptionalReturnType(), output, indentation);
        if (node.getOptionalRaisesSection() != null) {
            addWhitespace(output);
            format(node.getOptionalRaisesSection(), output, indentation);
        }
    }

    public void format(AParamDecl node, StringBuilder output, int indentation) {
        if (getColumn(output) > 80) {
            indentation += 2;
            newLine(output);
        }
        doGenericFormat(node, output, indentation);
    }

    private void breakIfNeeded(Node node, StringBuilder output, int indentation, int limit, boolean space) {
        if (getColumn(output) + node.toString().length() > limit) {
            newLine(output);
            format(node, output, indentation + 1);
            newLine(output);
        } else {
            if (space)
                addWhitespace(output);
            format(node, output, indentation);
            if (space)
                addWhitespace(output);
        }

    }

    public void format(AAttributeInvariant node, StringBuilder output, int indentation) {
        newLine(output);
        format(node.getModelComment(), output, indentation + 1);
        format(node.getAnnotations(), output, indentation + 1);
        format(node.getInvariantKernel(), output, indentation + 1);
    }
    
    public void format(AOperationConstraint node, StringBuilder output, int indentation) {
    	newLine(output);
    	format(node.getModelComment(), output, indentation);
    	format(node.getOperationConstraintKernel(), output, indentation);
    }

    public void format(AOperationPrecondition node, StringBuilder output, int indentation) {
        format(node.getPrecondition(), output, indentation);
        addWhitespace(output);
        format(node.getIdentifier(), output, indentation);
        format(node.getPreconditionSignature(), output, indentation);
        format(node.getConstraintException(), output, indentation);
        addWhitespace(output);
        format(node.getExpressionBlock(), output, indentation);
    }

    protected void addWhitespace(StringBuilder output) {
        if (!isAtWhitespace(output) && !isAtNewLine(output))
            output.append(' ');
    }

    public void format(ARegularInvariantConstraint node, StringBuilder output, int indentation) {
        format(node.getInvariant(), output, indentation);
        format(node.getIdentifier(), output, indentation);
        format(node.getConstraintException(), output, indentation);
        addWhitespace(output);
        format(node.getExpressionBlock(), output, indentation);
    }

    public void format(ACast node, StringBuilder output, int indentation) {
        addWhitespace(output);
        format(node.getAs(), output, indentation);
        addWhitespace(output);
        format(node.getSingleTypeIdentifier(), output, indentation);
    }

    public void format(AClosure node, StringBuilder output, int indentation) {
        format(node.getSimpleSignature(), output, indentation);
        addWhitespace(output);
        format(node.getBlock(), output, indentation);
    }

    public void format(ABehavioralFeatureBody node, StringBuilder output, int indentation) {
        newLine(output);
        format(node.getBlock(), output, indentation);
        format(node.getSemicolon(), output, indentation);
    }

    public void format(ASendSpecificStatement node, StringBuilder output, int indentation) {
        format(node.getSend(), output, indentation);
        format(node.getSignal(), output, indentation);
        format(node.getLParen(), output, indentation);
        format(node.getNamedArgumentList(), output, indentation + 1);
        format(node.getRParen(), output, indentation);
        addWhitespace(output);
        format(node.getTo(), output, indentation);
        addWhitespace(output);
        format(node.getTarget(), output, indentation);
    }

    public void format(ANamedArgument node, StringBuilder output, int indentation) {
        newLine(output);
        format(node.getIdentifier(), output, indentation);
        format(node.getAssignop(), output, indentation);
        addWhitespace(output);
        format(node.getExpression(), output, indentation);
    }

    public void format(ASimpleStatementBlock node, StringBuilder output, int indentation) {
        format(node.getLCurlyBracket(), output, indentation);
        newLine(output);
        format(node.getBlockKernel(), output, indentation + 1);
        if (!isAtNewLine(output))
            newLine(output);
        format(node.getRCurlyBracket(), output, indentation);
    }

    public void format(ASimpleExpressionBlock node, StringBuilder output, int indentation) {
        format(node.getLCurlyBracket(), output, indentation);
        breakIfNeeded(node.getRootExpression(), output, indentation, 80, true);
        format(node.getRCurlyBracket(), output, indentation);
    }

    public void format(AStateMachineDecl node, StringBuilder output, int indentation) {
        format(node.getStatemachine(), output, indentation);
        format(node.getIdentifier(), output, indentation);
        newLine(output);
        format(node.getStateDecl(), output, indentation + 1);
        newLine(output);
        format(node.getEnd(), output, indentation);
        format(node.getSemicolon(), output, indentation);
        newLine(output);
    }

    public void format(AStateDecl node, StringBuilder output, int indentation) {
        newLine(output);
        format(node.getModelComment(), output, indentation);
        format(node.getStateModifierList(), output, indentation);
        format(node.getState(), output, indentation);
        format(node.getIdentifier(), output, indentation);
        if (!node.getStateBehavior().isEmpty()) {
            format(node.getStateBehavior(), output, indentation + 1);
            newLine(output);
        }
        if (!node.getTransitionDecl().isEmpty()) {
            format(node.getTransitionDecl(), output, indentation + 1);
            newLine(output);
        }
        addWhitespace(output);
        format(node.getEnd(), output, indentation);
        format(node.getSemicolon(), output, indentation);
        newLine(output);
    }

    public void format(AStateBehavior node, StringBuilder output, int indentation) {
        newLine(output);
        format(node.getStateBehaviorModifier(), output, indentation);
        addWhitespace(output);
        format(node.getStateBehaviorDefinition(), output, indentation);
        format(node.getSemicolon(), output, indentation);
    }

    public void format(AAlt0ExpressionP1 node, StringBuilder output, int indentation) {
        format(node.getOperator(), output, indentation);
        addWhitespace(output);
        format(node.getOperand(), output, indentation);
    }
    
    public void format(AAlt0ExpressionP4 node, StringBuilder output, int indentation) {
        format(node.getOperator(), output, indentation);
        addWhitespace(output);
        format(node.getOperand(), output, indentation);
    }

    public void format(ATransitionDecl node, StringBuilder output, int indentation) {
        newLine(output);
        format(node.getModelComment(), output, indentation);
        format(node.getTransition(), output, indentation);
        format(node.getTransitionTriggers(), output, indentation);
        addWhitespace(output);
        format(node.getTo(), output, indentation);
        format(node.getDestination(), output, indentation);
        format(node.getTransitionGuard(), output, indentation);
        format(node.getTransitionEffect(), output, indentation);
        format(node.getSemicolon(), output, indentation);
    }

    public void format(ATransitionGuard node, StringBuilder output, int indentation) {
        format(node.getWhen(), output, indentation);
        addWhitespace(output);
        format(node.getExpressionBlock(), output, indentation);
    }

    public void format(AStart node, StringBuilder output, int indentation) {
        format(node.getPackageHeading(), output, indentation);
        newLine(output);
        format(node.getGlobalDirectiveSection(), output, indentation);
        format(node.getNamespaceContents(), output, indentation);
        newLine(output);
        format(node.getEnd(), output, indentation);
        format(node.getDot(), output, indentation);
    }

    public void format(AStatement node, StringBuilder output, int indentation) {
        format(node.getStatementResolved(), output, indentation);
        // omit semicolon/newline - leave it to specific formatters to handle it
    }

    public void format(AIfStatement node, StringBuilder output, int indentation) {
        format(node.getIf(), output, indentation);
        addWhitespace(output);
        format(node.getIfClause(), output, indentation);
        format(node.getRestIf(), output, indentation);
    }

    public void format(AElseRestIf node, StringBuilder output, int indentation) {
        newLine(output);
        format(node.getElse(), output, indentation);
        format(node.getClauseBody(), output, indentation);
    }

    public void format(AIfClause node, StringBuilder output, int indentation) {
        format(node.getTest(), output, indentation);
        addWhitespace(output);
        format(node.getThen(), output, indentation);
        format(node.getClauseBody(), output, indentation);
    }

    public void format(AClauseBody node, StringBuilder output, int indentation) {
        newLine(output);
        doGenericFormat(node, output, isBlock(node) ? indentation : indentation + 1);
    }

    public void format(AElseifRestIf node, StringBuilder output, int indentation) {
        newLine(output);
        format(node.getElseif(), output, indentation);
        addWhitespace(output);
        format(node.getIfClause(), output, indentation);
        format(node.getRestIf(), output, indentation);
    }

    public void format(ANoIfStatementResolved node, StringBuilder output, int indentation) {
        format(node.getNonIfStatement(), output, indentation);
        addSemicolonAndNewline(output);
    }

    protected void addSemicolonAndNewline(StringBuilder output) {
        addSemicolon(output);
        newLine(output);
    }

    protected void addSemicolon(StringBuilder output) {
        output.append(";");
    }

    public void format(AWithIfStatementResolved node, StringBuilder output, int indentation) {
        format(node.getIfStatement(), output, indentation);
        addSemicolonAndNewline(output);
    }

    public void format(ATopLevelElement node, StringBuilder output, int indentation) {
        newLine(output);
        if (node.getModelComment() != null)
            format(node.getModelComment(), output, indentation);
        format(node.getTopLevelElementChoice(), output, indentation);
        newLine(output);
    }

    public void format(ATryStatement node, StringBuilder output, int indentation) {
        format(node.getTry(), output, indentation);
        newLine(output);
        format(node.getProtectedBlock(), output, indentation + 1);
        newLine(output);
        format(node.getCatchSection(), output, indentation);
        format(node.getFinallySection(), output, indentation);
        format(node.getEnd(), output, indentation);
        newLine(output);
    }

    public void format(ACatchSection node, StringBuilder output, int indentation) {
        format(node.getCatch(), output, indentation);
        format(node.getLParen(), output, indentation);
        format(node.getVarDecl(), output, indentation);
        format(node.getRParen(), output, indentation);
        newLine(output);
        format(node.getHandlerBlock(), output, indentation + 1);
        newLine(output);
    }

    public void format(AVarDeclSection node, StringBuilder output, int indentation) {
        doGenericFormat(node, output, indentation);
        newLine(output);
    }

    public void format(AWhileStatement node, StringBuilder output, int indentation) {
        format(node.getWhile(), output, indentation);
        format(node.getLoopTest(), output, indentation);
        format(node.getDo(), output, indentation);
        newLine(output);
        boolean block = isBlock(node.getWhileLoopBody());
        format(node.getWhileLoopBody(), output, block ? indentation : indentation + 1);
    }

    public void format(AWordyBlock node, StringBuilder output, int indentation) {
        format(node.getBegin(), output, indentation);
        newLine(output);
        format(node.getBlockKernel(), output, indentation + 1);
        format(node.getEnd(), output, indentation);
    }

    public String format(Node toFormat, Analysis ignoredTokens) {
        this.ignoredTokens = ignoredTokens;
        StringBuilder result = new StringBuilder();
        format(toFormat, result, 0);
        return result.toString();
    }

    private void format(List<? extends Node> nodes, StringBuilder output, int indentation) {
        for (Node node : nodes)
            format(node, output, indentation);
    }

    /**
     * Tries to find a more specific formatter for the given node type. If none
     * can be found, falls back to the generic formatter.
     * 
     * @param node
     * @param output
     * @param indentation
     */
    private void format(Node node, StringBuilder output, int indentation) {
        if (node == null)
            return;
        if (!formatters.containsKey(node.getClass())) {
            Method method = MethodUtils.getMatchingAccessibleMethod(this.getClass(), "format",
                    new Class[] { node.getClass(), StringBuilder.class, Integer.class });
            formatters.put(node.getClass(), method);
        }
        Method formatterMethod = formatters.get(node.getClass());
        if (formatterMethod == null) {
            doGenericFormat(node, output, indentation);
            return;
        }
        try {
            formatterMethod.invoke(this, new Object[] { node, output, indentation });
        } catch (IllegalArgumentException e) {
            LogUtils.logError(TextUMLCore.PLUGIN_ID, "Unexpected  exception", e);
        } catch (IllegalAccessException e) {
            LogUtils.logError(TextUMLCore.PLUGIN_ID, "Unexpected  exception", e);
        } catch (InvocationTargetException e) {
            LogUtils.logError(TextUMLCore.PLUGIN_ID, "Unexpected  exception", e);
        }
    }

    public void format(AGlobalDirectiveSection node, StringBuilder output, int indentation) {
        List<PGlobalDirective> sortedDirectives = new ArrayList<PGlobalDirective>(node.getGlobalDirective());
        Collections.sort(sortedDirectives, new Comparator<PGlobalDirective>() {
            public int compare(PGlobalDirective o1, PGlobalDirective o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        Class<?> previousClass = null;
        for (PGlobalDirective globalDirective : sortedDirectives) {
            if (globalDirective.getClass() != previousClass)
                newLine(output);
            doGenericFormat(globalDirective, output, indentation);
            newLine(output);
            previousClass = globalDirective.getClass();
        }
    }

    public void format(TAssignop node, StringBuilder output, int indentation) {
        output.append(" ");
        output.append(node.getText());
        output.append(" ");
    }

    public void format(TColon node, StringBuilder output, int indentation) {
        output.append(" ");
        output.append(node.getText());
        output.append(" ");
    }

    public void format(TComma node, StringBuilder output, int indentation) {
        output.append(node.getText());
        output.append(" ");
    }

    public void format(TComment node, StringBuilder output, int indentation) {
        boolean newLine = isAtNewLine(output);
        if (!newLine && !isAtWhitespace(output))
            addWhitespace(output);
        doGenericFormat(node, output, indentation);
        if (newLine)
            newLine(output);
        else
            addWhitespace(output);
    }

    public void format(TWhiteSpace node, StringBuilder output, int indentation) {
        // omit whitespaces
    }

    public void format(TModelComment node, StringBuilder output, int indentation) {
        if (node != null) {
            doGenericFormat(node, output, indentation);
            newLine(output);
        }
    }

    private boolean isBlock(Node node) {
        final boolean[] block = { false };
        node.apply(new DepthFirstAdapter() {
            @Override
            public void caseABlockNonIfStatement(ABlockNonIfStatement node) {
                block[0] = true;
            }

            @Override
            public void caseANonBlockNonIfStatement(ANonBlockNonIfStatement node) {
                // not really necessary
                block[0] = false;
            }
        });
        return block[0];
    }

    private static boolean isPunctuation(String text) {
        return !Character.isJavaIdentifierStart(text.charAt(0)) && text.charAt(0) != '\\';
    }

    private static void newLine(StringBuilder output) {
        output.append(LINE_ENDING);
    }

    @SuppressWarnings("unchecked")
    private void doGenericFormat(Node node, StringBuilder output, int indentation) {
        if (node == null)
            return;
        List<Node> ignored = (List<Node>) ignoredTokens.getIn(node);
        if (ignored != null)
            for (Node ignoredNode : ignored)
                format(ignoredNode, output, indentation);
        if (node instanceof Token) {
            final Token token = ((Token) node);
            final boolean newLine = isAtNewLine(output);
            if (newLine)
                for (int i = 0; i < indentation; i++)
                    output.append("    ");
            else if (!isPunctuation(token.getText()) && !isPunctuation("" + output.charAt(output.length() - 1)))
                addWhitespace(output);
            output.append(token.getText());
        } else {
            ASTNode<Token, Node> astNode = ASTNode.<Token, Node> buildTree(node);
            List<ASTNode<Token, Node>> children = astNode.getChildren();
            for (Iterator<ASTNode<Token, Node>> i = children.iterator(); i.hasNext();) {
                ASTNode<Token, Node> element = (ASTNode<Token, Node>) i.next();
                format((Node) element.getBaseNode(), output, indentation);
            }
        }
    }

    private static boolean isAtNewLine(StringBuilder output) {
        return output.length() == 0 || endsWith(output, LINE_ENDING);
    }

    private static int getColumn(StringBuilder output) {
        int last = output.length() - LINE_ENDING_LENGTH;
        int column = 0;
        while (!LINE_ENDING.equals(output.substring(last - column, last - column + LINE_ENDING_LENGTH)))
            column++;
        return column;
    }

    private static boolean isAtWhitespace(StringBuilder output) {
        return output.length() > 0 && Character.isWhitespace(output.charAt(output.length() - 1));
    }

    private static boolean isAt(StringBuilder output, char... chars) {
        if (output.length() <= 0)
            return false;
        for (char c : chars)
            if (output.charAt(output.length() - 1) == c)
                return true;
        return false;
    }

    private static boolean endsWith(StringBuilder toCheck, String ending) {
        final int suffixLength = ending.length();
        final int bufferLength = toCheck.length();
        int bufferOffset = bufferLength - suffixLength;
        if (bufferOffset < 0)
            return false;
        for (int i = 0; i < suffixLength; i++)
            if (ending.charAt(i) != toCheck.charAt(bufferOffset + i))
                return false;
        return true;
    }

}
