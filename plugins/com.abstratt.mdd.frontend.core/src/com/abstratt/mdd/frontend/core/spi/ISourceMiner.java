/*******************************************************************************
 * Copyright (c) 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.frontend.core.spi;

import java.util.List;

/**
 * An object that knows how to extract source information for a given compiler.
 */
public interface ISourceMiner<N> {
	int getLineNumber(N node);

	String getText(N node);

	<P extends N, C extends N> P findParent(C start, Class<P> nodeType);

	<P extends N, C extends N> C findChild(P start, Class<C> nodeType, boolean first);

	<P extends N, C extends N> List<C> findChildren(P start, Class<C> nodeType);

	String getQualifiedIdentifier(N node);

	String getIdentifier(N node);
}
