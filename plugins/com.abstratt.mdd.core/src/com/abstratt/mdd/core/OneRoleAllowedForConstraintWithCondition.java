/*******************************************************************************
 * Copyright (c) 2006, 2016 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.core;

public class OneRoleAllowedForConstraintWithCondition extends Problem {

    public OneRoleAllowedForConstraintWithCondition() {
        super(IProblem.Severity.ERROR);
    }

    public String getMessage() {
        return "When specifying a permission expression, you must specify one and only one role class";
    }

}
