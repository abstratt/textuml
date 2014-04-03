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
package com.abstratt.mdd.internal.frontend.textuml;

import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Parameter;

import com.abstratt.mdd.frontend.core.spi.CompilationContext;

class BehaviorSignatureProcessor extends SignatureProcessor {
	public BehaviorSignatureProcessor(CompilationContext context, Behavior parent) {
		super(context, parent, false);
	}

	protected Parameter createParameter(String name) {
		Parameter parameter = ((Behavior) parent).createOwnedParameter(name, null);
		return parameter;
	}
}