/*******************************************************************************
 * Copyright (c) 2006, 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.modelrenderer;

import org.eclipse.emf.ecore.EObject;

/**
 * A visitor that chooses what renderer to use for each object in the graph.
 * 
 * Clients to implement.
 */
public interface IRendererSelector<C extends EObject> {
	/**
	 * Selects the appropriate renderer for the given element. Returns
	 * <code>null</code> if the object (and any of its descendants) should not
	 * be rendered.
	 * 
	 * @param element
	 * @return
	 */
	public IRenderer<C> select(C element);
}
