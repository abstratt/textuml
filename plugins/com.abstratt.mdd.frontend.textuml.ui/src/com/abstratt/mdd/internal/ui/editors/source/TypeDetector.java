/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.internal.ui.editors.source;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * Detects a Type (e.g. Class).
 */
public class TypeDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		return Character.isJavaIdentifierPart(c);
	}

	public boolean isWordStart(char c) {
		return Character.isJavaIdentifierStart(c) && Character.isUpperCase(c);
	}
}