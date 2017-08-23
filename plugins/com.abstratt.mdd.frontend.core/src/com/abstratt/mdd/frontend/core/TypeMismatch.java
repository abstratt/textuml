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

public class TypeMismatch extends Problem {

    private String expected;
    private String found;

    public TypeMismatch(String expected, String found) {
        this(Severity.ERROR, expected, found);
    }
    
    public TypeMismatch(Severity severity, String expected, String found) {
        super(severity);
        this.expected = expected;
        this.found = found;
    }

    public String getMessage() {
        return "Type mismatch (expected: '" + expected + "',  found: '" + found + "')";
    }

}
