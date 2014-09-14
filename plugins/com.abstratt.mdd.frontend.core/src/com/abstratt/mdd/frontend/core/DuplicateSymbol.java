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

import org.eclipse.emf.ecore.EClass;

import com.abstratt.mdd.core.Problem;


public class DuplicateSymbol extends Problem {

	private String message;

	public DuplicateSymbol(String name, EClass class1) {
		super(Severity.ERROR);
		this.message = class1.getName() + " already exists with name " + name;
	}

	public String getMessage() {
		return message;
	}

}
