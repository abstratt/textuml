package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.LiteralSpecification;

import com.abstratt.mdd.core.util.MDDUtil;

public class LiteralSpecificationBuilder extends ValueSpecificationBuilder<LiteralSpecification> {

	private Object value;

	public LiteralSpecificationBuilder(UML2ProductKind kind) {
		super(kind);
	}

	@Override
	protected LiteralSpecification createProduct() {
		return MDDUtil.createLiteralValue(value, getEClass(), as(PackageBuilder.class).getProduct());
	}

	public LiteralSpecificationBuilder value(Object value) {
		this.value = value;
		return this;
	}
}
