package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;

public class OperationBuilder extends DefaultParentBuilder<Operation> {

    public OperationBuilder(UML2ProductKind kind) {
        super(kind);
    }

    @Override
    protected Operation createProduct() {
        if (getParentProduct() != null) {
            if (getParentProduct() instanceof org.eclipse.uml2.uml.Class)
                return ((Class) getParentProduct()).createOwnedOperation(null, null, null);
            if (getParentProduct() instanceof DataType)
                return ((DataType) getParentProduct()).createOwnedOperation(null, null, null);
            if (getParentProduct() instanceof Interface)
                return ((Interface) getParentProduct()).createOwnedOperation(null, null, null);
        }
        return (Operation) EcoreUtil.create(getEClass());
    }

    public ParameterBuilder newParameter() {
        return newChildBuilder(UML2ProductKind.PARAMETER);
    }

    public ActivityBuilder newMethod() {
        ActivityBuilder newActivity = as(ClassifierBuilder.class).newActivity();
        return newActivity.specification(this);
    }
}
