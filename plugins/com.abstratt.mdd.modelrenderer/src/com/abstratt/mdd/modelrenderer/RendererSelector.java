/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/ 
package com.abstratt.mdd.modelrenderer;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import com.abstratt.pluginutils.LogUtils;

/**
 * Default renderer selector. Abstract so clients need to subclass and thus this class can find client classes.
 */
public abstract class RendererSelector<E extends EObject> implements IRendererSelector<E> {

	private String packageName;
	private List<EClass> filter;

	public RendererSelector(String packageName, EClass... filter) {
		this.packageName = packageName;
		this.filter = Arrays.asList(filter);
	}

	private Class<? extends IEObjectRenderer<E>> findRenderer(EClass elementClass) {
		if (!shouldRender(elementClass))
			return null;
		String className = elementClass.getName();
		String rendererClassName = packageName + '.' + className + "Renderer";
		try {
			return (Class<? extends IEObjectRenderer<E>>) Class.forName(rendererClassName, true, getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			// try parent
			EList<EClass> superTypes = elementClass.getESuperTypes();
			for (EClass superType : superTypes) {
				Class<? extends IEObjectRenderer<E>> renderer = findRenderer(superType);
				if (renderer != null)
					return renderer;
			}
			return null;
		}
	}

	private boolean shouldRender(EClass elementClass) {
		boolean matched = false;
		if (!filter.isEmpty()) {
			for (EClass rootClass : filter) {
				if (rootClass.isSuperTypeOf(elementClass)) {
					matched = true;
					break;
				}
			}
		}
		return matched;
	}

	public IEObjectRenderer<E> select(EObject element) {
		Class<?> rendererClass = findRenderer(element.eClass());
		if (rendererClass == null)
			return null;
		try {
			return (IEObjectRenderer<E>) rendererClass.newInstance();
		} catch (InstantiationException e) {
			LogUtils.logError(packageName, "Error instantiating renderer for " + element.eClass().getName(), e);
		} catch (IllegalAccessException e) {
			LogUtils.logError(packageName, "Error instantiating renderer for " + element.eClass().getName(), e);
		}
		return null;
	}
}
