package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.NamedElement;

public abstract class NamedElementBuilder<NE extends NamedElement> extends ElementBuilder<NE> {
	private String name;

	public NamedElementBuilder(UML2ProductKind kind) {
		super(kind);
	}

	public NamedElementBuilder<NE> name(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	@Override
	protected void enhance() {
		super.enhance();
		getProduct().setName(name);
	}

	protected String getUserName() {
		return getName() + "[" + super.getUserName() + "]";
	}
}
