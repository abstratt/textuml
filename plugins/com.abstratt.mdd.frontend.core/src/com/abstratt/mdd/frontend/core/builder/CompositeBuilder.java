package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.Element;

public class CompositeBuilder extends ElementBuilder<Element> implements IParentBuilder<Element> {

	public CompositeBuilder(IParentBuilder<?> parentBuilder) {
		super();
		parentBuilder.addChildBuilder(this);
	}
	
	@Override
	public <EB extends ElementBuilder<? extends Element>> EB newChildBuilder(UML2ProductKind kind) {
		EB childBuilder = new UML2BuilderFactory().newBuilder(kind);
		return addChildBuilder(childBuilder);
	}
	
	@Override
	public <EB extends ElementBuilder<? extends Element>> EB addChildBuilder(EB childBuilder) {
		// we do not require a reference from the composite to the children (subclasses can keep their own)
		childBuilder.setParent(this);
		return childBuilder;
	}
	
	protected final <E extends Element> E createChildBuilderProduct(ElementBuilder<E> childBuilder) {
		return childBuilder.createProduct();
	}
	
	@Override
	public <T extends ElementBuilder<? extends Element>> T as(Class<T> type) {
		if (super.as(type) == getParent())
			return (T) this;
		return super.as(type);
	}

	@Override
	protected Element createProduct() {
		return null;
	}
	
	@Override
	protected void enhance() {
		// TODO Auto-generated method stub
		super.enhance();
	}

}
