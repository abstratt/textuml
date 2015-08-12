package com.abstratt.mdd.frontend.core.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.uml2.uml.Element;

public class UML2BuilderFactory {

    public <B extends ElementBuilder<? extends Element>> B newBuilder(UML2ProductKind kind) {
        Class<B> builderClass = (Class<B>) kind.getBuilderClass();
        if (builderClass == null)
            throw new IllegalArgumentException(kind + " has no builder");
        try {
            Constructor<B> constructor = null;
            try {
                constructor = builderClass.getConstructor(UML2ProductKind.class);
                return constructor.newInstance(kind);
            } catch (NoSuchMethodException e) {
                constructor = builderClass.getConstructor();
                return constructor.newInstance();
            }
        } catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvocationTargetException e) {
            throw new UnsupportedOperationException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
