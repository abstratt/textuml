package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.ObjectNode;
import org.eclipse.uml2.uml.TemplateableElement;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.Variable;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.TypeMismatch;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.builder.ConditionalBuilderSet;
import com.abstratt.mdd.frontend.core.builder.ElementBuilder;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;
import com.abstratt.mdd.frontend.core.builder.ValueSpecificationBuilder;
import com.abstratt.mdd.frontend.core.spi.IActivityBuilder;

public abstract class ActionBuilder<A extends Action> extends ElementBuilder<A> {

	private EClass eClass;

	public ActionBuilder(EClass actionClass) {
		super(UML2ProductKind.forClass(actionClass));
		this.eClass = actionClass;
	}

	public ActionBuilder(UML2ProductKind kind) {
		super(kind);
		this.eClass = kind.getMetaClass();
	}

	protected void addSourceAction(ActionBuilder<?> sourceAction) {
		ensureSourceAction(sourceAction);
		sourceAction.addDependency(this);
	}

	protected void ensureSourceAction(ActionBuilder<?> sourceAction) {
		Assert.isLegal(sourceAction.isProducer(), sourceAction.getClass().getName());
	}

	@Override
	protected EClass getEClass() {
		return eClass;
	}

	@Override
	protected final void enhance() {
		TemplateableElement boundElement = getBoundElement();
		super.enhance();
		try {
			enhanceAction();
			Assert.isTrue(isProducer() != getProduct().getOutputs().isEmpty(), getProduct().getClass().getName()
			        + " has " + getProduct().getOutputs().size() + " outputs");
			Assert.isTrue(isConsumer() != getProduct().getInputs().isEmpty(), getProduct().getClass().getName()
			        + " has " + getProduct().getInputs().size() + " inputs");
		} finally {
			completeAction();
		}
		checkIncomings(boundElement);
	}

	protected void completeAction() {
		// close action originally created in #createProduct()
		activityBuilder().closeAction(false);
	}

	private void checkIncomings(TemplateableElement bound) {
		ObjectFlow incompatible = TypeUtils.checkCompatibility(getContext().getRepository(), getProduct(), bound);
		if (incompatible == null)
			return;
		final ObjectNode target = ((ObjectNode) incompatible.getTarget());
		final ObjectNode source = ((ObjectNode) incompatible.getSource());
		getContext().getProblemTracker().add(
		        new TypeMismatch(MDDUtil.getDisplayName(target), MDDUtil.getDisplayName(source)));
	}

	protected TemplateableElement getBoundElement() {
		Activity currentActivity = activityBuilder().getCurrentActivity();
		return (Class) MDDUtil.getNearest(currentActivity.getOwner(), IRepository.PACKAGE.getClass_());
	}

	/**
	 * Completes building of the product element, creating I/O pins and setting
	 * properties.
	 * 
	 * Subclasses to implement.
	 */
	protected abstract void enhanceAction();

	@Override
	protected A createProduct() {
		return (A) activityBuilder().createAction(getEClass());
	}

	protected IActivityBuilder activityBuilder() {
		return getContext().getActivityBuilder();
	}

	protected Variable getVariable(String variableName) {
		Variable variable = activityBuilder().getVariable(variableName);
		if (variable == null)
			abortStatement(new UnresolvedSymbol(variableName, Literals.VARIABLE));
		return variable;
	}

	public ReadStructuralFeatureActionBuilder readStructuralFeature(String featureName, ActionBuilder<?> target) {
		ReadStructuralFeatureActionBuilder child = as(StructuredActivityNodeBuilder.class).newChildBuilder(
		        UML2ProductKind.READ_STRUCTURAL_FEATURE_ACTION);
		return child.structuralFeature(featureName).target(target);
	}

	public CallOperationActionBuilder callOperation(String featureName, ActionBuilder<?> objectBuilder) {
		CallOperationActionBuilder child = as(StructuredActivityNodeBuilder.class).newChildBuilder(
		        UML2ProductKind.CALL_OPERATION_ACTION);
		return child.operation(featureName).target(objectBuilder);
	}

	public AddStructuralFeatureValueActionBuilder writeStructuralFeature(String featureName, ActionBuilder<?> target,
	        ActionBuilder<?> value) {
		AddStructuralFeatureValueActionBuilder child = as(StructuredActivityNodeBuilder.class).newChildBuilder(
		        UML2ProductKind.ADD_STRUCTURAL_FEATURE_VALUE_ACTION);
		return child.structuralFeature(featureName).replace(true).target(target).value(value);
	}

	public ReadVariableActionBuilder readVariable(final String variableName) {
		ReadVariableActionBuilder child = as(StructuredActivityNodeBuilder.class).newChildBuilder(
		        UML2ProductKind.READ_VARIABLE_ACTION);
		return child.variable(variableName);
	}

	public AddVariableValueActionBuilder writeVariable(final String variableName, ActionBuilder<?> value) {
		return addVariable(variableName, true, value, null);
	}

	public AddVariableValueActionBuilder addVariable(final String variableName, final boolean replace,
	        final ActionBuilder<?> valueBuilder, final ActionBuilder<?> at) {
		AddVariableValueActionBuilder child = as(StructuredActivityNodeBuilder.class).newChildBuilder(
		        UML2ProductKind.ADD_VARIABLE_VALUE_ACTION);
		return child.variable(variableName).replace(true).value(valueBuilder);
	}

	public ValueSpecificationActionBuilder value(ValueSpecificationBuilder<?> valueBuilder) {
		ValueSpecificationActionBuilder child = as(StructuredActivityNodeBuilder.class).newChildBuilder(
		        UML2ProductKind.VALUE_SPECIFICATION_ACTION);
		return child.value(valueBuilder);
	}

	/**
	 * Subclasses must invoke this method on each input pin passing the
	 * corresponding value action builder.
	 * 
	 * @param input
	 *            an input pin
	 * @param inputValueBuilder
	 *            the corresponding value bulder
	 */
	protected void buildSource(InputPin input, ActionBuilder<?> inputValueBuilder) {
		if (inputValueBuilder.getProduct() == null)
			inputValueBuilder.build();
		ActivityUtils.connect(getProduct().getInStructuredNode(),
		        ActivityUtils.getActionOutputs(inputValueBuilder.getProduct()).get(0), input);
	}

	public ReadExtentActionBuilder readExtent(final String classifierName) {
		ReadExtentActionBuilder child = as(StructuredActivityNodeBuilder.class).newChildBuilder(
		        UML2ProductKind.READ_EXTENT_ACTION);
		return child.classifier(classifierName);
	}

	public ReadSelfActionBuilder readSelf() {
		return as(StructuredActivityNodeBuilder.class).newChildBuilder(UML2ProductKind.READ_SELF_ACTION);
	}

	public StructuredActivityNodeBuilder newBlock() {
		return as(StructuredActivityNodeBuilder.class).newChildBuilder(UML2ProductKind.STRUCTURED_ACTIVITY_NODE);
	}

	/**
	 * Convenience method that tries to resolve a symbol. Failure to do so will
	 * result in a problem being logged and the current statement to be aborted.
	 * 
	 * @param name
	 * @param eClass
	 * @return
	 */
	<NE extends NamedElement> NE findNamedElement(String name, EClass eClass) {
		final NE found = getContext().getRepository().findNamedElement(name, eClass,
		        getContext().getNamespaceTracker().currentNamespace(null));
		if (found == null)
			abortStatement(new UnresolvedSymbol(name, eClass));
		return found;
	}

	@Override
	public void setConditionalSet(ConditionalBuilderSet conditionalBuilderSet) {
		super.setConditionalSet(conditionalBuilderSet);
	}

	protected boolean isConsumer() {
		return false;
	};

	protected boolean isProducer() {
		return false;
	};
}
