package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;

public class ParameterBuilder extends TypedElementBuilder<Parameter> {
    private ParameterDirectionKind direction = ParameterDirectionKind.IN_LITERAL;

    public ParameterBuilder(UML2ProductKind kind) {
        super(kind);
    }

    @Override
    protected Parameter createProduct() {
        if (getParentProduct() != null)
            return ((Operation) getParentProduct()).createOwnedParameter(null, null);
        return (Parameter) EcoreUtil.create(getEClass());
    }

    public ParameterBuilder direction(String direction) {
        this.direction = ParameterDirectionKind.getByName(direction);
        return this;
    }

    @Override
    protected void enhance() {
        super.enhance();
        getProduct().setDirection(direction);
    }
}
