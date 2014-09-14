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


public class StateMachineMustHaveOneInitialState extends Problem {

	public StateMachineMustHaveOneInitialState() {
		super(Severity.ERROR);
	}

	@Override
	public String getMessage() {
		return "A state machine must have one (and only one) initial state";
	}

}