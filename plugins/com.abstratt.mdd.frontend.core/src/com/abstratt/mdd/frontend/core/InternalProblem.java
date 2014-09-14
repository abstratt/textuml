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

import com.abstratt.mdd.core.Problem;

public class InternalProblem extends Problem {
	private Throwable throwable;
	private String message;

	public InternalProblem(Throwable throwable) {
		super(Severity.ERROR);
		this.throwable = throwable;
	}

	public InternalProblem(String message) {
		super(Severity.ERROR);
		this.message = message;
	}

	public String getMessage() {
		if (message != null)
			return "Internal error: " + message;
		return "Internal error (" + throwable.toString() + ")";
	}

}
