package com.abstratt.mdd.frontend.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Namespace;

public abstract class DefaultParentBuilder<N extends Namespace> extends NamedElementBuilder<N> implements IParentBuilder<N> {
	
	protected List<ElementBuilder<? extends Element>> childBuilders = new ArrayList<ElementBuilder<?>>();
	
	public DefaultParentBuilder(UML2ProductKind kind) {
		super(kind);
	}
	
	@Override
	protected void enhance() {
		getContext().getNamespaceTracker().enterNamespace(getProduct());
		try {
			super.enhance();
			buildChildren();
		} finally {
			getContext().getNamespaceTracker().leaveNamespace();
		}
	}

	protected void buildChildren() {
		List<ElementBuilder<? extends Element>> toBuild = this.childBuilders;
		for (ElementBuilder<? extends Element> i : toBuild)
			((ElementBuilder<?>) i).build();
	}

	@Override
	public <EB extends ElementBuilder<? extends Element>> EB newChildBuilder(UML2ProductKind kind) {
		EB childBuilder = new UML2BuilderFactory().newBuilder(kind);
		return addChildBuilder(childBuilder);
	}
	
	@Override
	public <EB extends ElementBuilder<? extends Element>> EB addChildBuilder(EB childBuilder) {
		childBuilders.add(childBuilder);
		childBuilder.setParent(this);
		return childBuilder;
	}
}



