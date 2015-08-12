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

public class UnknownAttribute extends UnresolvedSymbol {

    private String classifier;
    private String attribute;
    private boolean isStatic;

    public UnknownAttribute(String type, String attribute, boolean isStatic) {
        super(type + NamedElement.SEPARATOR + attribute);
        this.classifier = type;
        this.attribute = attribute;
        this.isStatic = isStatic;
    }

    @Override
    public String getMessage() {
        return "Unknown " + (isStatic ? "static" : "") + " attribute '" + attribute + "' in '" + classifier;
    }

    public String getClassifier() {
        return classifier;
    }

    public Object getAttribute() {
        return attribute;
    }
}
