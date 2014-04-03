package com.abstratt.modelrenderer;

import org.eclipse.emf.ecore.EObject;

import com.abstratt.modelrenderer.IRenderer;

/**
 * Specializes the generic {@link IRenderer} towards rendering ECore objects.
 */
public interface IEObjectRenderer<T extends EObject> extends IRenderer<EObject, T> {
	//
}
