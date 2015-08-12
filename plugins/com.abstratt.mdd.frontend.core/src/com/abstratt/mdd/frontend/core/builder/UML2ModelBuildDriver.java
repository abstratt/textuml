package com.abstratt.mdd.frontend.core.builder;

import java.util.Arrays;

/**
 * Drives a build session.
 */
public class UML2ModelBuildDriver {

    private static ThreadLocal<UML2BuildContext> context = new ThreadLocal<UML2BuildContext>();

    protected static UML2BuildContext getContext() {
        if (context.get() == null)
            throw new IllegalStateException("Can only be called from the context of a build");
        return context.get();
    }

    public void build(UML2BuildContext context, BuilderBuilder builderBuilder) {
        setContext((UML2BuildContext) context);
        try {
            for (ElementBuilder<?> topBuilder : builderBuilder.buildRootBuilders())
                topBuilder.build();
            context.getReferenceTracker().resolve(getContext().getRepository(), getContext().getProblemTracker());
        } finally {
            setContext(null);
        }
    }

    public static interface BuilderBuilder {
        public Iterable<ElementBuilder<?>> buildRootBuilders();
    }

    public void build(UML2BuildContext context, final ElementBuilder<?>... elementBuilder) {
        this.build(context, new BuilderBuilder() {
            @Override
            public Iterable<ElementBuilder<?>> buildRootBuilders() {
                return Arrays.<ElementBuilder<?>> asList(elementBuilder);
            }
        });
    }

    private static void setContext(UML2BuildContext newContext) {
        if (newContext != null && context.get() != null)
            throw new IllegalStateException();
        context.set(newContext);
    }
}
