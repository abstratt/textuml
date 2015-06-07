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
package com.abstratt.modelrenderer;

import org.eclipse.emf.ecore.EObject;

/**
 * A renderer knows how to render a specific type of object.
 * 
 * Clients to implement.
 */
public interface IRenderer<A extends EObject> {
	/**
	 * Requests the given object to be rendered. 
	 *  
	 * @param element element to be rendered
	 * @param out the rendering output
	 * @param session the rendering context
	 * @return <code>true</code> if the object was actually rendered, <code>false</code> otherwise
	 */
	public boolean renderObject(A element, IndentedPrintWriter out, IRenderingSession session);
}
