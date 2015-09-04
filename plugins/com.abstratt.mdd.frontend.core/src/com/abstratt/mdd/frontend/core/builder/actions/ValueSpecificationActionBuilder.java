package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.ValueSpecificationAction;

import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;
import com.abstratt.mdd.frontend.core.builder.ValueSpecificationBuilder;

public class ValueSpecificationActionBuilder extends ActionBuilder<ValueSpecificationAction> {

    private ValueSpecificationBuilder<?> valueBuilder;

    public ValueSpecificationActionBuilder() {
        super(UML2ProductKind.VALUE_SPECIFICATION_ACTION);
    }

    public ValueSpecificationActionBuilder value(ValueSpecificationBuilder<?> valueBuilder) {
        this.valueBuilder = valueBuilder;
        return this;
    }

    @Override
    public void enhanceAction() {
        valueBuilder.build();
        getProduct().setValue(valueBuilder.getProduct());
        OutputPin result = getProduct().createResult(null, valueBuilder.getProduct().getType());
    }

    @Override
    protected boolean isProducer() {
        return true;
    }
}
