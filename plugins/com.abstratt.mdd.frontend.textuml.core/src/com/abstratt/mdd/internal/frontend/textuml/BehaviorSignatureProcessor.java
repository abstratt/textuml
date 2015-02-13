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
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Parameter;

import com.abstratt.mdd.internal.frontend.textuml.node.Node;

class BehaviorSignatureProcessor extends SignatureProcessor {
	public BehaviorSignatureProcessor(SourceCompilationContext<Node> sourceContext, Behavior parent) {
		super(sourceContext, parent, false);
	}

	protected Parameter createParameter(String name) {
		Parameter parameter = ((Behavior) parent).createOwnedParameter(name, null);
		return parameter;
	}
	
	@Override
	protected Namespace getBaseLookupNamespace() {
	    return parent;
	}
}