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
package com.abstratt.mdd.internal.frontend.textuml;

public enum Modifier {
	ABSTRACT, STATIC, PUBLIC, PRIVATE, PROTECTED, PACKAGE, DERIVED, READONLY, ID, IN, OUT, INOUT, READ, CREATE, UPDATE, DELETE, TERMINATE, INITIAL;

	public static Modifier fromToken(String token) {
		return Modifier.valueOf(token.toUpperCase());
	}
}
