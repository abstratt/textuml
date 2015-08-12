package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.uml2.uml.AddStructuralFeatureValueAction;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.StructuralFeatureUtils;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.CannotModifyADerivedAttribute;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class AddStructuralFeatureValueActionBuilder extends ActionBuilder<AddStructuralFeatureValueAction> {
	private String featureName;
	private ActionBuilder<?> targetBuilder;
	private boolean replace;
	private ActionBuilder<?> valueBuilder;

	public AddStructuralFeatureValueActionBuilder() {
		super(UML2ProductKind.ADD_STRUCTURAL_FEATURE_VALUE_ACTION);
	}

	public AddStructuralFeatureValueActionBuilder structuralFeature(String featureName) {
		this.featureName = featureName;
		return this;
	}

	@Override
	public void enhanceAction() {
		getProduct().createObject(null, null);
		buildSource(getProduct().getObject(), targetBuilder);

		Classifier targetClassifier = (Classifier) ActivityUtils.getSource(getProduct().getObject()).getType();
		getProduct().getObject().setType(targetClassifier);

		Property attribute = StructuralFeatureUtils.findAttribute(targetClassifier, featureName, false, true);
		if (attribute == null)
			abortStatement(new UnresolvedSymbol(featureName, Literals.STRUCTURAL_FEATURE));
		if (attribute.isDerived())
			abortStatement(new CannotModifyADerivedAttribute());
		getProduct().setStructuralFeature(attribute);

		getProduct().createValue(null, null);
		TypeUtils.copyType(attribute, getProduct().getValue(), targetClassifier);
		buildSource(getProduct().getValue(), valueBuilder);
	}

	public AddStructuralFeatureValueActionBuilder target(ActionBuilder<?> targetBuilder) {
		addSourceAction(targetBuilder);
		this.targetBuilder = targetBuilder;
		return this;
	}

	@Override
	protected boolean isConsumer() {
		return true;
	}

	public AddStructuralFeatureValueActionBuilder replace(boolean replace) {
		this.replace = replace;
		return this;
	}

	public AddStructuralFeatureValueActionBuilder value(ActionBuilder<?> valueBuilder) {
		addSourceAction(valueBuilder);
		this.valueBuilder = valueBuilder;
		return this;
	}

}
