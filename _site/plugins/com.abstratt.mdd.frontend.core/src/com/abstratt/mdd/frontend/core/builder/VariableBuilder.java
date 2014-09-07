package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Variable;

public class VariableBuilder extends TypedElementBuilder<Variable> {

	public VariableBuilder(UML2ProductKind kind) {
		super(kind);
	}
	@Override
	protected Variable createProduct() {
		if (getParentProduct() != null)
			return ((StructuredActivityNode) getParentProduct()).createVariable(null, null);
		return (Variable) EcoreUtil.create(getEClass());
	}
}
