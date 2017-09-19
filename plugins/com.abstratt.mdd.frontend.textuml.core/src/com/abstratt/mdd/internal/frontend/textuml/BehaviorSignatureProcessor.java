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
import org.eclipse.uml2.uml.ParameterSet;

import com.abstratt.mdd.frontend.textuml.grammar.node.Node;

class BehaviorSignatureProcessor extends SignatureProcessor {
    public BehaviorSignatureProcessor(SourceCompilationContext<Node> sourceContext, Behavior parent) {
        super(sourceContext, parent, false);
    }

    protected Parameter createParameter(String name) {
        Parameter parameter = getBehavior().createOwnedParameter(name, null);
        return parameter;
    }
    
    @Override
    protected ParameterSet createParameterSet(String name) {
        return getBehavior().createOwnedParameterSet(name);
    }
    
    @Override
    protected Parameter getParameter(String name) {
        return getBehavior().getOwnedParameter(name, null);
    }

    private Behavior getBehavior() {
        return (Behavior) parent;
    }
    
    @Override
    protected Namespace getBaseLookupNamespace() {
        return parent;
    }
}