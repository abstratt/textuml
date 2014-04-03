package com.abstratt.mdd.frontend.core.builder.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.StructuredActivityNode;

import com.abstratt.mdd.frontend.core.builder.ElementBuilder;
import com.abstratt.mdd.frontend.core.builder.IParentBuilder;
import com.abstratt.mdd.frontend.core.builder.UML2BuilderFactory;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class StructuredActivityNodeBuilder extends ActionBuilder<StructuredActivityNode> implements IParentBuilder<StructuredActivityNode> {
	private List<ElementBuilder<?>> childBuilders = new ArrayList<ElementBuilder<?>>();
	public StructuredActivityNodeBuilder() {
		super(UML2ProductKind.STRUCTURED_ACTIVITY_NODE);
	}  
	
	@Override
	protected StructuredActivityNode createProduct() {
		return activityBuilder().createBlock(getEClass());
	}

	@Override
	protected void completeAction() {
		activityBuilder().closeBlock(false);
	}
	
	@Override
	protected void enhanceAction() {
		for (ElementBuilder<?> childBuilder : this.childBuilders)
			if (childBuilder.getProduct() == null)
				childBuilder.build();
	}

	@Override
	public <EB extends ElementBuilder<? extends Element>> EB newChildBuilder(
			UML2ProductKind kind) {
		EB childBuilder = new UML2BuilderFactory().newBuilder(kind);
		return addChildBuilder(childBuilder);
	}

	@Override
	public <EB extends ElementBuilder<? extends Element>> EB addChildBuilder(EB childBuilder) {
		childBuilders.add(childBuilder);
		childBuilder.setParent(this);
		return  childBuilder;
	}
}
