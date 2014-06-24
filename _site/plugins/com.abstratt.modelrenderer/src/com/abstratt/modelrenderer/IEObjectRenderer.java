package com.abstratt.modelrenderer;

import org.eclipse.emf.ecore.EObject;

/**
 * Specializes the generic {@link IRenderer} towards rendering ECore objects.
 */
public interface IEObjectRenderer<T extends EObject> extends IRenderer<EObject, T> {
	//
}
