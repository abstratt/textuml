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
package com.abstratt.mdd.frontend.core;

import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;

import com.abstratt.mdd.core.util.FeatureUtils;
import com.abstratt.mdd.core.util.MDDUtil;

public class UnknownOperation extends UnresolvedSymbol {

    private String classifier;
    private String operation;
    private String argList;
    private boolean isStatic;
    private String alternative;

    public UnknownOperation(String type, String operation, String argList, boolean isStatic, Operation alternative) {
        super(type + NamedElement.SEPARATOR + operation);
        this.classifier = type;
        this.operation = operation;
        this.argList = argList;
        this.isStatic = isStatic;
        if (alternative != null)
            this.alternative = alternative.getName()
                    + MDDUtil.getArgumentListString(FeatureUtils.getInputParameters(alternative.getOwnedParameters()));
    }

    public UnknownOperation(String type, String operation, String argList, boolean isStatic) {
        super(type + NamedElement.SEPARATOR + operation);
        this.classifier = type;
        this.operation = operation;
        this.argList = argList;
        this.isStatic = isStatic;
    }

    @Override
    public String getMessage() {
        String message = "Unknown " + (isStatic ? "static" : "") + " operation '" + operation + argList + "' in '"
                + classifier + "'";
        if (alternative != null)
            message += (" - alternative: " + alternative);
        return message;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getOperation() {
        return operation;
    }
}
