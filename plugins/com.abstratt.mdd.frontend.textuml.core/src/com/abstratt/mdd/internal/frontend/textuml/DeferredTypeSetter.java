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

import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.TypedElement;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;

/**
 * A type setter that defers resolution of type references to a later step. 
 */
public class DeferredTypeSetter extends TypeSetter {
	public DeferredTypeSetter(SourceCompilationContext<Node> sourceContext, Namespace currentPackage, TypedElement target) {
		super(sourceContext, currentPackage, target);
	}

	@Override
	public void process(final Node node) {
		getContext().getReferenceTracker().add(new IDeferredReference() {
			public void resolve(IBasicRepository repository) {
				doProcess(node);
			}
		}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}
	
	public void doProcess(final Node node) {
		super.process(node);
	}
}