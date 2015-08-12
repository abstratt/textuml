package com.abstratt.mdd.modelrenderer;

import org.eclipse.emf.ecore.EObject;

/**
 * Specializes the generic {@link IRenderer} towards rendering ECore objects.
 */
public interface IEObjectRenderer<T extends EObject> extends IRenderer<T> {
    @Override
    public boolean renderObject(T element, IndentedPrintWriter out, IRenderingSession session);
}
