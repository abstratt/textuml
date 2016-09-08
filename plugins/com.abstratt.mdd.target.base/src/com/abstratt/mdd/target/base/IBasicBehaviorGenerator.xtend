package com.abstratt.mdd.target.base

import java.util.Arrays
import java.util.List
import org.eclipse.uml2.uml.Action
import org.eclipse.uml2.uml.Activity
import org.eclipse.uml2.uml.CallOperationAction
import org.eclipse.uml2.uml.Classifier
import org.eclipse.uml2.uml.InputPin
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Parameter

import static extension com.abstratt.mdd.core.util.ActivityUtils.*
import static extension com.abstratt.mdd.core.util.FeatureUtils.*
import static extension com.abstratt.mdd.core.util.StereotypeUtils.*

public interface IBasicBehaviorGenerator {
    def CharSequence generateActivity(Activity activity)

    def CharSequence generateActivityAsExpression(Activity toGenerate, boolean asClosure, List<Parameter> parameters)
    
    def CharSequence generateActivityAsExpression(Activity toGenerate) {
        return this.generateActivityAsExpression(toGenerate, false, Arrays.<Parameter>asList())
    }

    def generateActivityAsExpression(Activity toGenerate, boolean asClosure) {
        generateActivityAsExpression(toGenerate, asClosure, toGenerate.closureInputParameters)
    }
    
    def Classifier getOperationTarget(CallOperationAction action) {
        return if(action.target != null && !action.target.multivalued) action.target.type as Classifier else action.
            operation.owningClassifier
    }
    
    def boolean isBasicTypeOperation(Operation operation) {
        operation.owningClassifier.package.hasStereotype("ModelLibrary")
    }
    
    def CharSequence generateAction(Action action, boolean delegate)
    
    def CharSequence generateAction(Action action) {
        generateAction(action, true)
    }
        
    def CharSequence generateAction(Void input) {
        throw new NullPointerException
    }

    def CharSequence generateAction(InputPin input) {
        generateAction(input.sourceAction, true)
    }
}

public class DelegatingBehaviorGenerator implements IBasicBehaviorGenerator {
    
    protected IBasicBehaviorGenerator target
    
    new(IBasicBehaviorGenerator target) {
        this.target = target
    } 
    
    override generateAction(Action action, boolean delegate) {
        target.generateAction(action, delegate)
    }
    
    override generateActivity(Activity activity) {
        target.generateActivity(activity)
    }
    
    override generateActivityAsExpression(Activity toGenerate, boolean asClosure, List<Parameter> parameters) {
        target.generateActivityAsExpression(toGenerate, asClosure, parameters)
    }
    
}