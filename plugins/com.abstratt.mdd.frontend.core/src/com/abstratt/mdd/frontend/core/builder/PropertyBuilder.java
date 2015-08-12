package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredClassifier;

import com.abstratt.mdd.core.UnclassifiedProblem;

public class PropertyBuilder extends TypedElementBuilder<Property> {

	public PropertyBuilder(UML2ProductKind kind) {
		super(kind);
	}

	@Override
	protected Property createProduct() {
		if (getParentProduct() instanceof StructuredClassifier)
			return ((StructuredClassifier) getParentProduct()).createOwnedAttribute(null, null, getEClass());
		else if (getParentProduct() instanceof Association)
			return ((Association) getParentProduct()).createOwnedEnd(null, null, getEClass());
		// not a structured classifier, not an association, what is this thing?
		abortScope(new UnclassifiedProblem("Unexpected container: " + getParentProduct().eClass().getName()));
		// never gets here
		return null;
	}
}
