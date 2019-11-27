package com.abstratt.mdd.frontend.textuml.renderer;

import com.abstratt.mdd.core.util.MDDExtensionUtils
import com.abstratt.mdd.target.base.IBasicBehaviorGenerator
import java.util.List
import org.eclipse.uml2.uml.Action
import org.eclipse.uml2.uml.Activity
import org.eclipse.uml2.uml.AddStructuralFeatureValueAction
import org.eclipse.uml2.uml.AddVariableValueAction
import org.eclipse.uml2.uml.CallOperationAction
import org.eclipse.uml2.uml.Clause
import org.eclipse.uml2.uml.ConditionalNode
import org.eclipse.uml2.uml.CreateLinkAction
import org.eclipse.uml2.uml.CreateObjectAction
import org.eclipse.uml2.uml.DataType
import org.eclipse.uml2.uml.DestroyObjectAction
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Feature
import org.eclipse.uml2.uml.InputPin
import org.eclipse.uml2.uml.NamedElement
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Parameter
import org.eclipse.uml2.uml.ReadExtentAction
import org.eclipse.uml2.uml.ReadSelfAction
import org.eclipse.uml2.uml.ReadStructuralFeatureAction
import org.eclipse.uml2.uml.ReadVariableAction
import org.eclipse.uml2.uml.SendSignalAction
import org.eclipse.uml2.uml.StructuredActivityNode
import org.eclipse.uml2.uml.TestIdentityAction
import org.eclipse.uml2.uml.Type
import org.eclipse.uml2.uml.ValueSpecification
import org.eclipse.uml2.uml.ValueSpecificationAction

import static extension com.abstratt.mdd.core.util.ActivityUtils.*
import static extension com.abstratt.mdd.core.util.FeatureUtils.*
import static extension com.abstratt.mdd.core.util.MDDExtensionUtils.*
import static extension com.abstratt.mdd.target.base.GeneratorUtils.*
import org.eclipse.uml2.uml.ReadLinkAction

class ActivityGenerator implements IBasicBehaviorGenerator {
	
   
    interface ElementFormatter {
    	def CharSequence generateLink(NamedElement element, CharSequence referenceText) {
    	    generateText(referenceText)
    	}
    	
    	def CharSequence formatElement(Element element, CharSequence referenceText) {
    	    generateText(referenceText)
    	}
    	
    	def CharSequence generateText(CharSequence referenceText) {
    	    referenceText	
    	} 
    }
    
    val ActivityGenerator.ElementFormatter linkGenerator
    
    new(ActivityGenerator.ElementFormatter linkGenerator) {
    	this.linkGenerator = linkGenerator
    }
    
    new() {
    	this.linkGenerator = new ElementFormatter() {}
    }
    
    override generateActivity(Activity activity) {
    	if (activity.isClosure) {
    		if (!activity.outputParameters.isEmpty) {
    			val singleStatement = activity.rootAction.findStatements.size == 1
    			if (singleStatement) {
					return activity.rootAction.findSingleStatement.inputs.get(0).sourceAction.generateAction
    			}
			}
		}
        val generated = activity.rootAction.generateAction
        return generated
    }
    
    def CharSequence generateActivityAsExpressionIfPossible(Activity toGenerate) {
        val statements = toGenerate.rootAction.findStatements
        return if (statements.size == 1 && statements.get(0).inputs.size == 1)
        	generateActivityAsExpression(toGenerate)
    	else
    	    generateActivity(toGenerate)
    }
    
    override generateActivityAsExpression(Activity toGenerate, boolean asClosure, List<Parameter> parameters) {
        val singleStatement = toGenerate.rootAction.findSingleStatement
		val sourceAction = singleStatement.sourceAction
		return sourceAction.generateAction
    }
    
    override generateAction(Action action, boolean delegate) {
        generateProperAction(action)
    }
    
    def dispatch generateProperAction(Action action) {
        '''/* TBD: «action.eClass.name» */'''
    }
    
    def dispatch generateProperAction(StructuredActivityNode action) {
        val isRoot = action.rootAction
        val statements = action.findStatements
        val isCast = MDDExtensionUtils.isCast(action)
        if (isCast)
        	'''(«action.inputs.get(0).sourceAction.generateAction» as «action.outputs.get(0).type.name»)'''
        else if (isRoot && !action.transactionalBlock) 
            statements.map[generateStatement].join('\n')
        else
        '''«generateBlock(action, statements)»'''
    }
				
	protected def CharSequence generateBlock(StructuredActivityNode action, List<Action> statements) {
		var localVariables = action.variables
		'''
	        begin
	        «IF !action.ownedComments.isEmpty»
	        «action.ownedComments.generateMany('\n')[
	        formatElement(it, '''
		        (* 
		            «it.body»
		        *)
		    ''')]»
	        «ENDIF»
	        «IF !localVariables.isEmpty»
	            var «action.variables.generateMany([ '''«it.name» : «it.type.name»'''], ', ')»;
	        «ENDIF»
	            «statements.map[generateStatement].join('\n')»
	        end
        '''
	}
    
    def dispatch generateProperAction(ReadSelfAction action) {
        'self'
    }
    
    def dispatch generateProperAction(CreateObjectAction action) {
        '''new «generateLink(action.classifier, action.classifier.name)»'''
    }
    
    def dispatch generateProperAction(CreateLinkAction action) {
    	val end0 = action.endData.get(0)
    	val end1 = action.endData.get(1)
        '''link «generateLink(action.association, action.association.name)»(«end0.end.name» := «end0.value.generateAction», «end1.end.name» := «end1.value.generateAction»)'''
    }
    
    
    def dispatch generateProperAction(SendSignalAction action) {
        '''send «generateLink(action.signal, action.signal.name)»(«action.arguments.generateMany(', ', ['''«it.name» := «it.generateAction»'''])») to «action.target.generateAction»'''
    }
    
    def dispatch generateProperAction(ReadExtentAction action) {
        '''«generateLink(action.classifier, action.classifier.name)» extent'''
    }
    
    def dispatch generateProperAction(DestroyObjectAction action) {
        '''delete «action.target.generateAction»'''
    }
    
    
    def dispatch generateProperAction(ConditionalNode action) {
    	val clauses = action.clauses
    	clauses.generateMany(false, '''
    	
    	else
    		''' )[generateClause(it)]
    }
	
	def CharSequence generateClause(Clause clause) {
		val test = clause.decider.owningAction.generateAction
		'''
		«IF test != 'true'»
		if («test») then
		«ENDIF»
		    «clause.bodies.generateMany('\\n')[generateAction(it as Action)]»
		'''
	}
    
    
    def dispatch generateProperAction(AddVariableValueAction action) {
    	val sourceExpression = action.sourceAction.generateAction
    	if (action.variable.returnVariable)
        	'''return «sourceExpression»'''
		else
			'''«action.variable.name» := «sourceExpression»'''
    }
    
    def dispatch generateProperAction(ReadVariableAction action) {
        action.variable.name
    }
    
    def dispatch generateProperAction(TestIdentityAction action) {
        '''«action.first.generateAction» == «action.second.generateAction»'''
    }
    
    
    
    def dispatch generateProperAction(AddStructuralFeatureValueAction action) {
        val base = generateFeatureActionBase(action.structuralFeature, action.object)
        '''«base» := «action.value.generateAction»'''
    }
    
    def dispatch generateProperAction(ReadStructuralFeatureAction action) {
        val base = generateFeatureActionBase(action.structuralFeature, action.object)
        '''«base»'''
    }
    
    def dispatch generateProperAction(ReadLinkAction action) {
        val fedEndData = action.endData.get(0)
        val target = fedEndData.value
        val associationName = action.association.name
        val fedEnd = fedEndData.end
        val oppositeEnd = fedEnd?.opposite
        return if (associationName != null) 
    	'''
    	«target.generateAction» <- «associationName» -> «oppositeEnd.name»
    	'''
    	else
    	'''
        «target».«fedEnd.name»
        '''
    }
    
    def generateStatement(Action statementAction) {
        '''«statementAction.generateAction»;'''
    }
    
    
    def generateFeatureActionBase(Feature feature, InputPin targetPin) {
        val target = if (feature.static)
            '''«generateLink(feature.owningClassifier, feature.owningClassifier.name)»#'''
        else
            '''«targetPin.generateAction».'''
        return '''«target»«generateLink(feature, feature.name)»'''
    }
	
	def CharSequence generateLink(NamedElement element, CharSequence referenceText) {
		linkGenerator.generateLink(element, referenceText)
	}
	
	def CharSequence formatElement(Element element, CharSequence referenceText) {
		linkGenerator.formatElement(element, referenceText)
	}
    
    def dispatch generateProperAction(CallOperationAction action) {
        val asSpecialAction = generateAsSpecialAction(action)
        if (asSpecialAction !== null)
            return asSpecialAction
        val base = generateFeatureActionBase(action.operation, action.target)
        '''«base»(«action.arguments.map[generateAction].join(', ')»)'''
    }
    
    def dispatch generateProperAction(ValueSpecificationAction action) {
        generateLink(action.value, action.value.generateValue())
    }
    
    def CharSequence generateValue(ValueSpecification valueSpec) {
    	return TextUMLRenderingUtils.renderValue(valueSpec, this)
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
            case 'notEquals':
                '!='
            case 'equals':
                '='
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