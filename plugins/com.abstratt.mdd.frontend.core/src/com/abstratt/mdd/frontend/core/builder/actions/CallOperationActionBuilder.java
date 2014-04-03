package com.abstratt.mdd.frontend.core.builder.actions;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.StructuralFeatureUtils;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.WrongNumberOfArguments;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class CallOperationActionBuilder extends
		ActionBuilder<CallOperationAction> {
	private String operationName;
	private ActionBuilder<?> targetBuilder;
	private List<ActionBuilder<?>> argumentBuilders = new LinkedList<ActionBuilder<?>>();

	public CallOperationActionBuilder() {
		super(UML2ProductKind.CALL_OPERATION_ACTION);
	}

	public CallOperationActionBuilder operation(String featureName) {
		this.operationName = featureName;
		return this;
	}
	
	@Override
	public void enhanceAction() {
		
		Operation operation = findNamedElement(operationName, Literals.OPERATION);
		getProduct().setOperation(operation);
		
		List<Parameter> signatureParameters = StructuralFeatureUtils.filterParameters(operation.getOwnedParameters(), ParameterDirectionKind.IN_LITERAL, ParameterDirectionKind.INOUT_LITERAL, ParameterDirectionKind.OUT_LITERAL);
		int parameterCount = signatureParameters.size();
		int argumentCount = argumentBuilders.size();
		if (parameterCount != argumentCount)
			abortStatement(new WrongNumberOfArguments(parameterCount, argumentCount));
		//TODO only if not static
		getProduct().createTarget(null, null);
		buildSource(getProduct().getTarget(), targetBuilder);
		Classifier targetClassifier = (Classifier) ActivityUtils.getSource(getProduct().getTarget()).getType();
		getProduct().getTarget().setType(targetClassifier);
        // matches parameters and arguments		
		for (int i = 0; i < parameterCount; i++) {
			Parameter current = signatureParameters.get(i);
			InputPin argument = getProduct().createArgument(null, null);
			argument.setName(current.getName());
			TypeUtils.copyType(current, argument, targetClassifier);
			buildSource(argument, argumentBuilders.get(i));
		}
		// optional result
		if (operation.getReturnResult() != null) {
			final OutputPin result = getProduct().createResult(null, null);
			TypeUtils.copyType(operation.getReturnResult(), result, getBoundElement());
		}
	}
	
	public CallOperationActionBuilder  target(
			ActionBuilder<?> targetBuilder) {
		addSourceAction(targetBuilder);
		this.targetBuilder = targetBuilder;
		return this;
	}
	
	@Override
	protected boolean isProducer() {
		return getProduct().getOperation().getReturnResult() != null;
	}
	
	@Override
	protected boolean isConsumer() {
		return !getProduct().getOperation().isStatic() || !argumentBuilders.isEmpty();
	}

	public void argument(ActionBuilder<?> argumentBuilder) {
		addSourceAction(argumentBuilder);
		this.argumentBuilders.add(argumentBuilder);
	}
}
