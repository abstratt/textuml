package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.Element;

public interface IElementBuilder<E extends Element> {

    <T extends ElementBuilder<? extends Element>> T as(Class<T> type);

    E getProduct();
}