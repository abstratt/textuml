/*******************************************************************************
 * Copyright (c) 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.uml2.uml.ParameterableElement;
import org.eclipse.uml2.uml.TemplateParameterSubstitution;

/**
 * This class provides support for accessing version-specific API in UML2.
 */
public class UML2Compatibility {

	/**
	 * Cross-version utility method for retrieving a substitution's actual
	 * parameter in a version-independent way.
	 * 
	 * @see TemplateParameterSubstitution#getActual() - in UML 3.0+
	 * @see TemplateParameterSubstitution#getActuals() - in UML 2.2
	 */
	public static ParameterableElement getActualParameter(
			TemplateParameterSubstitution substitution) {
	    return substitution.getActual();
	}

	/**
	 * Cross-version utility method for setting a substitution's actual
	 * parameter in a version-independent way.
	 * 
	 * @see TemplateParameterSubstitution#setActual(ParameterableElement) - in UML 3.0+
	 * @see TemplateParameterSubstitution#getActuals() - in UML 2.2
	 */
	public static void setActualParameter(
			TemplateParameterSubstitution substitution,
			ParameterableElement actual) {
		substitution.setActual(actual);
	}

	/*
	 * Convenience method for finding a method in a less verbose way.
	 */
	private static Method getMethod(Class<?> clazz, String name, Class<?>... signature) {
		try {
			return clazz.getMethod(name, signature);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}
}