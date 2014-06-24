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
package com.abstratt.mdd.frontend.core;

public class ReadSelfFromStaticContext extends Problem {

	public ReadSelfFromStaticContext() {
		super(Severity.ERROR);
	}

	public String getMessage() {
		return "Cannot get current object from a static context";
	}

}
