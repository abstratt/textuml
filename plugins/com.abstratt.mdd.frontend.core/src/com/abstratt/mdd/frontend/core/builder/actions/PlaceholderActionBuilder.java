package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.uml2.uml.Action;

import com.abstratt.mdd.frontend.core.builder.ConditionalBuilderSet;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class PlaceholderActionBuilder extends ActionBuilder<Action> {

    private ConditionalBuilderSet conditionalSet;

    public PlaceholderActionBuilder(ConditionalBuilderSet conditionalSet) {
        super(UML2ProductKind.ACTION);
        this.conditionalSet = conditionalSet;
    }

    @Override
    public void enhanceAction() {
    }

    @Override
    public void build() {
        conditionalSet.getChosenBuilder().build();
    }

    @Override
    public Action getProduct() {
        return (Action) conditionalSet.getChosenBuilder().getProduct();
    }

    @Override
    protected boolean isProducer() {
        return true;
    }
}
