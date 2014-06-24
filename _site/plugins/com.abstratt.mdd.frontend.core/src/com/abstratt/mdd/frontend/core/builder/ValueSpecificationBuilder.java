package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.ValueSpecification;

public abstract class ValueSpecificationBuilder<T extends ValueSpecification> extends ElementBuilder<T> {

	public ValueSpecificationBuilder(UML2ProductKind kind) {
		super(kind);
	}
}
