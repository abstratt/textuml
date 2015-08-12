package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.ReadVariableAction;
import org.eclipse.uml2.uml.Variable;

import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class ReadVariableActionBuilder extends ActionBuilder<ReadVariableAction> {
    private String variableName;

    public ReadVariableActionBuilder() {
        super(UML2ProductKind.READ_VARIABLE_ACTION);
    }

    public ReadVariableActionBuilder variable(String varName) {
        this.variableName = varName;
        return this;
    }

    @Override
    public void enhanceAction() {
        Variable variable = getVariable(variableName);
        getProduct().setVariable(variable);
        final OutputPin result = getProduct().createResult(null, null);
        TypeUtils.copyType(variable, result, getBoundElement());
    }

    @Override
    protected boolean isProducer() {
        return true;
    }
}
