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

import com.abstratt.mdd.core.Problem;

public class NotInAssociation extends Problem {

    private String classifierName;
    private String associationName;

    public NotInAssociation(String classifierName, String associationName) {
        super(Severity.ERROR);
        this.classifierName = classifierName;
        this.associationName = associationName;
    }

    public String getMessage() {
        return classifierName + " is not involved in association '" + associationName + "'";
    }

    public String getAssociationName() {
        return associationName;
    }

    public String getClassifierName() {
        return classifierName;
    }
}
