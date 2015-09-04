package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.ReadStructuralFeatureAction;
import org.eclipse.uml2.uml.StructuralFeature;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class ReadStructuralFeatureActionBuilder extends ActionBuilder<ReadStructuralFeatureAction> {
    private String featureName;
    private ActionBuilder<?> targetBuilder;

    public ReadStructuralFeatureActionBuilder() {
        super(UML2ProductKind.READ_STRUCTURAL_FEATURE_ACTION);
    }

    public ReadStructuralFeatureActionBuilder structuralFeature(String featureName) {
        this.featureName = featureName;
        return this;
    }

    @Override
    public void enhanceAction() {
        StructuralFeature feature = findNamedElement(featureName, Literals.STRUCTURAL_FEATURE);
        getProduct().setStructuralFeature(feature);
        getProduct().createObject(null, null);
        buildSource(getProduct().getObject(), targetBuilder);
        final OutputPin result = getProduct().createResult(null, null);
        TypeUtils.copyType(feature, result, getBoundElement());
    }

    public ReadStructuralFeatureActionBuilder target(ActionBuilder<?> targetBuilder) {
        addSourceAction(targetBuilder);
        this.targetBuilder = targetBuilder;
        return this;
    }

    @Override
    protected boolean isProducer() {
        return true;
    }

    @Override
    protected boolean isConsumer() {
        return true;
    }
}
