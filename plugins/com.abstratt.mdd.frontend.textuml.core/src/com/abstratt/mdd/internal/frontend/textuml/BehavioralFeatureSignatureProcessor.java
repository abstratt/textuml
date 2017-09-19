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
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.ParameterSet;

import com.abstratt.mdd.frontend.textuml.grammar.node.Node;

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
        getBehavioralFeature().getRaisedExceptions().add(exceptionClass);
    }

    @Override
    protected Parameter createParameter(String name) {
        Parameter parameter = getBehavioralFeature().createOwnedParameter(name, null);
        return parameter;
    }
    
    private BehavioralFeature getBehavioralFeature() {
        return (BehavioralFeature) parent;
    }
    
    @Override
    protected Parameter getParameter(String name) {
        return getBehavioralFeature().getOwnedParameter(name, null);
    }    

    
    @Override
    protected ParameterSet createParameterSet(String name) {
        return getBehavioralFeature().createOwnedParameterSet(name);
    }
    
    @Override
    protected Namespace getBaseLookupNamespace() {
        return parent;
    }
}
