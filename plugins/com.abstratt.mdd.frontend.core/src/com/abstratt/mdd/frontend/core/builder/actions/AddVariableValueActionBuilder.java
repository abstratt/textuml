package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.uml2.uml.AddVariableValueAction;

import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class AddVariableValueActionBuilder extends ActionBuilder<AddVariableValueAction> {
    private ActionBuilder<?> valueBuilder;
    private String variableName;
    private boolean replace;

    public AddVariableValueActionBuilder() {
        super(UML2ProductKind.ADD_VARIABLE_VALUE_ACTION);
    }

    @Override
    public void enhanceAction() {
        getProduct().setIsReplaceAll(replace);
        getProduct().setVariable(getVariable(variableName));
        getProduct().createValue(null, null);
        TypeUtils.copyType(getProduct().getVariable(), getProduct().getValue(), getBoundElement());
        buildSource(getProduct().getValue(), valueBuilder);
    }

    public AddVariableValueActionBuilder variable(String variableName) {
        this.variableName = variableName;
        return this;
    }

    public AddVariableValueActionBuilder replace(boolean replace) {
        this.replace = replace;
        return this;
    }

    public AddVariableValueActionBuilder value(ActionBuilder<?> valueBuilder) {
        addSourceAction(valueBuilder);
        this.valueBuilder = valueBuilder;
        return this;
    }

    public ActionBuilder<?> value() {
        return valueBuilder;
    }

    @Override
    protected boolean isConsumer() {
        return true;
    }
}
