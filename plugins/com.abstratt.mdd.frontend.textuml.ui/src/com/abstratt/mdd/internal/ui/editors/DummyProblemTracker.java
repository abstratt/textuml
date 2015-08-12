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
package com.abstratt.mdd.internal.ui.editors;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;

/**
 * Does not do anything.
 */
public class DummyProblemTracker implements IProblemTracker {

	public void add(IProblem toReport) {
		// does not do anything
	}

	public IProblem[] getAllProblems() {
		return null;
	}

	public boolean hasProblems(Severity minimumSeverity) {
		return false;
	}

}