package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.Element;

public interface IParentBuilder<T extends Element> extends IElementBuilder<T> {
    <EB extends ElementBuilder<? extends Element>> EB newChildBuilder(UML2ProductKind kind);

    <EB extends ElementBuilder<? extends Element>> EB addChildBuilder(EB childBuilder);
}
