package com.abstratt.mdd.frontend.textuml.renderer;

import com.abstratt.mdd.target.base.IBasicBehaviorGenerator
import java.util.List
import org.eclipse.uml2.uml.Action
import org.eclipse.uml2.uml.Activity
import org.eclipse.uml2.uml.AddStructuralFeatureValueAction
import org.eclipse.uml2.uml.CallOperationAction
import org.eclipse.uml2.uml.DataType
import org.eclipse.uml2.uml.Feature
import org.eclipse.uml2.uml.InputPin
import org.eclipse.uml2.uml.LiteralNull
import org.eclipse.uml2.uml.LiteralString
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Parameter
import org.eclipse.uml2.uml.ReadSelfAction
import org.eclipse.uml2.uml.ReadStructuralFeatureAction
import org.eclipse.uml2.uml.StructuredActivityNode
import org.eclipse.uml2.uml.Type
import org.eclipse.uml2.uml.ValueSpecification
import org.eclipse.uml2.uml.ValueSpecificationAction

import static extension com.abstratt.mdd.core.util.ActivityUtils.*
import static extension com.abstratt.mdd.core.util.FeatureUtils.*

class ActivityGenerator implements IBasicBehaviorGenerator {
    
    override generateActivity(Activity activity) {
        activity.rootAction.generateAction
    }
    
    override generateActivityAsExpression(Activity toGenerate, boolean asClosure, List<Parameter> parameters) {
        toGenerate.rootAction.findSingleStatement.sourceAction.generateAction
    }
    
    override generateAction(Action action, boolean delegate) {
        generateProperAction(action)
    }
    
    def dispatch generateProperAction(Action action) {
        '''/* TBD: «action.eClass.name» */'''
    }
    
    def dispatch generateProperAction(StructuredActivityNode action) {
        val statements = action.findStatements
        '''
        begin
            «statements.map[generateStatement].join('\n')»
        end;
        '''
    }
    
    def dispatch generateProperAction(ReadSelfAction action) {
        'self'
    }
    
    def dispatch generateProperAction(AddStructuralFeatureValueAction action) {
        val base = generateFeatureActionBase(action.structuralFeature, action.object)
        '''«base» := «action.value.generateAction»'''
    }
    
    def dispatch generateProperAction(ReadStructuralFeatureAction action) {
        val base = generateFeatureActionBase(action.structuralFeature, action.object)
        '''«base»'''
    }
    
    def generateStatement(Action statementAction) {
        '''«statementAction.generateAction»;'''
    }
    
    
    def generateFeatureActionBase(Feature feature, InputPin targetPin) {
        val target = if (feature.static)
            '''«feature.owningClassifier.name»#'''
        else
            '''«targetPin.generateAction».'''
        return '''«target»«feature.name»'''
    }
    
    def dispatch generateProperAction(CallOperationAction action) {
        val asSpecialAction = generateAsSpecialAction(action)
        if (asSpecialAction != null)
            return asSpecialAction
        val base = generateFeatureActionBase(action.operation, action.target)
        '''«base»(«action.arguments.map[generateAction].join(', ')»)'''
    }
    
    def dispatch generateProperAction(ValueSpecificationAction action) {
        action.value.generateValue
    }
    
    def generateValue(ValueSpecification valueSpec) {
        switch (valueSpec) {
            LiteralNull : 'null'
            LiteralString : '''"«valueSpec.value»"'''
            default: valueSpec.stringValue
        }
    }
    
    def boolean needsParenthesis(Action action) {
        val targetAction = action.targetAction
        return if (targetAction instanceof CallOperationAction)
            // operators require the expression to be wrapped in parentheses
            targetAction.operation.isBasicTypeOperation && findOperator(targetAction.operationTarget, targetAction.operation) != null
        else
            false
    }
    
    def parenthesize(CharSequence toWrap, Action action) {
        val needsParenthesis = action.needsParenthesis
        if (needsParenthesis)
            '''(«toWrap»)'''
        else
            toWrap
    }
    
    def generateAsSpecialAction(CallOperationAction action) {
        val operator = findOperator(action.operationTarget, action.operation)
        return if (operator != null) {
            switch (action.arguments.size()) {
                // unary operator
                case 0:
                    '''«operator» «generateAction(action.target)»'''.parenthesize(action)
                case 1:
                    '''«generateAction(action.target)» «operator» «generateAction(action.arguments.head)»'''.
                        parenthesize(action)
            }
        }
    }
    
    def unsupported(String string) {
        '''/*«string»*/'''
    }
    
    def findOperator(Type type, Operation operation) {
        return switch (operation.name) {
            case 'add':
                '+'
            case 'subtract':
                '-'
            case 'multiply':
                '*'
            case 'divide':
                '/'
            case 'minus':
                '-'
            case 'and':
                'and'
            case 'or':
                'or'
            case 'not':
                'not'
            case 'lowerThan':
                '<'
            case 'greaterThan':
                '>'
            case 'lowerOrEquals':
                '<='
            case 'greaterOrEquals':
                '>='
            case 'same':
                '=='
            default:
                if (type instanceof DataType)
                    switch (operation.name) {
                        case 'equals': '=='
                    }
        }
    }
    
}