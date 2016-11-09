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
package com.abstratt.mdd.frontend.cli;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class EclipseCompilationDirectorCLI implements IApplication {
	public Object start(IApplicationContext appContext) throws Exception {
		new CompilationDirectorCLI().doIt(null);
		return null;
	}

	public void stop() {
		throw new UnsupportedOperationException();
	}
}
