/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Vladimir Sosnin - fix to bug 2796613
 *******************************************************************************/
package com.abstratt.mdd.internal.frontend.textuml;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLValidator;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.core.util.StructuralFeatureUtils;
import com.abstratt.mdd.frontend.core.InvalidConnector;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.AbortedCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.textuml.grammar.node.AConnectorEndList;
import com.abstratt.mdd.frontend.textuml.grammar.node.APathConnectorEnd;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleConnectorEnd;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;

public class ConnectorProcessor extends AbstractProcessor<AConnectorEndList, StructuredClassifier> {

    private Connector newConnector;

    public ConnectorProcessor(SourceCompilationContext<Node> sourceContext, StructuredClassifier namespace) {
        super(sourceContext, namespace);
        this.newConnector = namespace.createOwnedConnector(null);
    }

    @Override
    public void caseAPathConnectorEnd(final APathConnectorEnd node) {
        final String partName = sourceMiner.getIdentifier(node.getPrefix());
        final String partOrPortName = sourceMiner.getIdentifier(node.getPartOrPort());
        referenceTracker.add(new IDeferredReference() {
            @Override
            public void resolve(IBasicRepository repository) {
                Property pathPart = StructuralFeatureUtils.findAttribute(namespace, partName, false, true);
                if (pathPart == null) {
                    problemBuilder.addProblem(new UnresolvedSymbol(partName, UMLPackage.Literals.PROPERTY), node);
                    throw new AbortedScopeCompilationException();
                }
                if (!pathPart.isComposite()) {
                    problemBuilder.addProblem(new UnclassifiedProblem(pathPart.getName()
                            + " must be declared as a composition"), node);
                    throw new AbortedScopeCompilationException();
                }
                Classifier partType = (Classifier) pathPart.getType();
                if (partType == null) {
                    problemBuilder.addProblem(new UnclassifiedProblem("Part found, but type not resolved"), node);
                    throw new AbortedScopeCompilationException();
                }
                Property partOrPort = StructuralFeatureUtils.findProperty(partType, partOrPortName, false, true, null);
                if (partOrPort == null) {
                    problemBuilder.addProblem(new UnresolvedSymbol(partOrPortName, UMLPackage.Literals.PROPERTY), node);
                    throw new AbortedScopeCompilationException();
                }
                ConnectorEnd newEnd = addEnd(partOrPort);
                if (partOrPort instanceof Port)
                    newEnd.setPartWithPort(pathPart);
            }
        }, IReferenceTracker.Step.GENERAL_RESOLUTION);
        super.caseAPathConnectorEnd(node);
    }

    @Override
    public void caseASimpleConnectorEnd(final ASimpleConnectorEnd node) {
        final String ownPartOrPortName = sourceMiner.getIdentifier(node);
        referenceTracker.add(new IDeferredReference() {
            @Override
            public void resolve(IBasicRepository repository) {
                Property partOrPort = StructuralFeatureUtils.findAttribute(namespace, ownPartOrPortName, false, true);
                if (partOrPort == null) {
                    problemBuilder.addProblem(new UnresolvedSymbol(ownPartOrPortName, UMLPackage.Literals.PROPERTY),
                            node);
                    return;
                }
                addEnd(partOrPort);
            }
        }, IReferenceTracker.Step.GENERAL_RESOLUTION);
    }

    private void validateProduct(final Node node) {
        BasicDiagnostic diagnostics = new BasicDiagnostic();
        Map<Object, Object> context = new HashMap<Object, Object>();
        boolean valid = UMLValidator.INSTANCE.validate(newConnector, diagnostics, context);
        if (!valid) {
            problemBuilder.addProblem(new InvalidConnector(diagnostics.getChildren().get(0).getCode()), node);
            throw new AbortedCompilationException();
        }
    }

    public void process(final AConnectorEndList node) {
        node.apply(this);
        referenceTracker.add(new IDeferredReference() {
            @Override
            public void resolve(IBasicRepository repository) {
                validateProduct(node);
            }
        }, IReferenceTracker.Step.GENERAL_RESOLUTION);
    }

    public ConnectorEnd addEnd(Property newPort) {
        ConnectorEnd connectorEnd = newConnector.createEnd();
        connectorEnd.setRole(newPort);
        return connectorEnd;
    }
}