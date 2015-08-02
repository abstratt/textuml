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

import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Parameter;

import com.abstratt.mdd.internal.frontend.textuml.node.Node;

public class BehavioralFeatureSignatureProcessor extends SignatureProcessor {
    public BehavioralFeatureSignatureProcessor(SourceCompilationContext<Node> sourceContext, BehavioralFeature parent) {
        super(sourceContext, parent, true);
    }

    public BehavioralFeatureSignatureProcessor(SourceCompilationContext<Node> sourceContext, BehavioralFeature parent,
            boolean supportExceptions, boolean unnamedParameters) {
        super(sourceContext, parent, supportExceptions, unnamedParameters);
    }

    @Override
    protected void addRaisedException(Classifier exceptionClass) {
        ((BehavioralFeature) parent).getRaisedExceptions().add(exceptionClass);
    }

    @Override
    protected Parameter createParameter(String name) {
        Parameter parameter = ((BehavioralFeature) parent).createOwnedParameter(name, null);
        return parameter;
    }

    @Override
    protected Namespace getBaseLookupNamespace() {
        return parent;
    }
}
