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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.ParameterableElement;
import org.eclipse.uml2.uml.TemplateParameter;
import org.eclipse.uml2.uml.TemplateSignature;
import org.eclipse.uml2.uml.TemplateableElement;

import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AFormalTemplateParameter;
import com.abstratt.mdd.frontend.textuml.grammar.node.POptionalFormalTemplateParameters;

public class TemplateProcessor implements NodeProcessor<POptionalFormalTemplateParameters> {
    private TemplateableElement classifier;
    private EClass parameterableElementClass;
    private EClass templateParameterClass;
    private EClass templateSignatureClass;

    public TemplateProcessor(TemplateableElement templateable, EClass templateSignatureClass,
            EClass templateParameterClass, EClass parameterableElementClass) {
        super();
        this.classifier = templateable;
        this.templateSignatureClass = templateSignatureClass;
        this.templateParameterClass = templateParameterClass;
        this.parameterableElementClass = parameterableElementClass;
    }

    private void createTemplateParameter(String name) {
        TemplateSignature signature = classifier.getOwnedTemplateSignature();
        if (!classifier.isTemplate())
            signature = classifier.createOwnedTemplateSignature(templateSignatureClass);
        TemplateParameter parameter = signature.createOwnedParameter(templateParameterClass);
        ParameterableElement parameterableElement = parameter.createOwnedParameteredElement(parameterableElementClass);
        if (parameterableElement instanceof NamedElement)
            ((NamedElement) parameterableElement).setName(name);
    }

    public void process(POptionalFormalTemplateParameters node) {
        if (node != null)
            node.apply(new DepthFirstAdapter() {
                @Override
                public void caseAFormalTemplateParameter(AFormalTemplateParameter node) {
                    createTemplateParameter(TextUMLCore.getSourceMiner().getIdentifier(node));
                }
            });
    }
}
