/*******************************************************************************
 * Copyright (c) 2019 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.frontend.core;

import java.util.Optional;

import com.abstratt.mdd.core.Problem;

public class RequiredValueExpected extends Problem {
    private String context;

    public RequiredValueExpected(String site) {
        super(Severity.ERROR);
        this.context = site;
    }
    
    public RequiredValueExpected() {
        this(null);
    }
    
    @Override
    public String getMessage() {
        return super.getMessage() + Optional.ofNullable(context).map(it -> " - " + it).orElse("");
    }
}
