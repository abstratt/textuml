package com.abstratt.mdd.internal.frontend.textuml;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.ValueSpecification;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.frontend.textuml.grammar.node.AComplexInitializationExpression;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;

public class ComplexInitializationExpressionProcessor {

    private ProblemBuilder<Node> problemBuilder;
    private SourceCompilationContext<Node> sourceContext;
    private BehavioredClassifier currentClass;

    ComplexInitializationExpressionProcessor(SourceCompilationContext<Node> sourceContext, BehavioredClassifier currentClass) {
        this.problemBuilder = sourceContext.getProblemBuilder();
        this.sourceContext = sourceContext;
        this.currentClass = currentClass;
    }

    public void process(final TypedElement initializableElement,
            final AComplexInitializationExpression initializationExpression) {
        sourceContext.getNamespaceTracker().enterNamespace(currentClass);
        try {
            Activity activity = (Activity) currentClass.createOwnedBehavior(null, IRepository.PACKAGE.getActivity());
            activity.setIsReadOnly(true);
            activity.setName("defaultValue_" + initializableElement.getName());
            Parameter activityReturn = activity.createOwnedParameter(null, null);
            activityReturn.setDirection(ParameterDirectionKind.RETURN_LITERAL);
            TypeUtils.copyType(initializableElement, activityReturn);
            BehaviorGenerator behaviorGenerator = new BehaviorGenerator(sourceContext);
            behaviorGenerator.createBody(initializationExpression.getExpressionBlock(), activity);
            ValueSpecification reference = ActivityUtils.buildBehaviorReference(currentClass.getNearestPackage(),
                    activity, null);

            MDDExtensionUtils.makeDerivation(initializableElement, activity);
            if (initializableElement instanceof Property)
                ((Property) initializableElement).setDefaultValue(reference);
            else if (initializableElement instanceof Parameter)
                ((Parameter) initializableElement).setDefaultValue(reference);
            else
                problemBuilder.addError("Element is not initializable: " + initializableElement.getName() + " : "
                        + initializableElement.eClass().getName(), initializationExpression);
        } finally {
            sourceContext.getNamespaceTracker().leaveNamespace();
        }
    }

}
