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

import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;

import com.abstratt.mdd.core.Problem;
import com.abstratt.mdd.core.util.MDDUtil;

public class TypeMismatch extends Problem {

    private String expected;
    private String found;

    private TypeMismatch(String expected, String found) {
        this(Severity.ERROR, expected, found);
    }
    
    private TypeMismatch(Severity severity, String expected, String found) {
        super(severity);
        this.expected = expected;
        this.found = found;
    }

    public String getMessage() {
        return "Type mismatch (expected: '" + expected + "',  found: '" + found + "')";
    }
    
    public static TypeMismatch build(TypedElement expected, TypedElement actual) {
        return new TypeMismatch(MDDUtil.getDisplayName(expected), MDDUtil.getDisplayName(actual));
    }
    
    public static TypeMismatch build(Type expected, Type actual) {
        return new TypeMismatch(MDDUtil.getTypeName(expected), MDDUtil.getTypeName(actual));
    }

}
