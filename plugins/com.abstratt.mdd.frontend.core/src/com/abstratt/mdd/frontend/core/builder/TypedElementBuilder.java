package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;

public abstract class TypedElementBuilder<TE extends TypedElement> extends NamedElementBuilder<TE> {

	protected NameReference type;
	private boolean required = true;
	private boolean multiple;

	public TypedElementBuilder(UML2ProductKind kind) {
		super(kind);
	}

	public TypedElementBuilder<TE> type(String typeName) {
		this.type = reference(typeName, UML2ProductKind.CLASSIFIER);
		return this;
	}

	@Override
	protected void enhance() {
		super.enhance();
		if (type != null)
			resolveType();
		applyMultiplicity();
	}

	private void applyMultiplicity() {
		MultiplicityElement asMultiplicity = (MultiplicityElement) getProduct();
		asMultiplicity.setUpper(multiple ? LiteralUnlimitedNatural.UNLIMITED : 1);
		asMultiplicity.setLower(required ? 1 : 0);
	}

	protected void resolveType() {
		new ReferenceSetter<Type>(type, getParentProduct(), getContext()) {
			protected void link(Type type) {
				linkType(type);
			}
		};
	}

	protected void linkType(Type type) {
		getProduct().setType(type);
	}

	public TypedElementBuilder<TE> required(boolean required) {
		this.required = required;
		return this;
	}

	public TypedElementBuilder<TE> multiple(boolean multiple) {
		this.multiple = multiple;
		return this;
	}

}
