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

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.frontend.core.spi.IActivityBuilder;
import com.abstratt.mdd.frontend.internal.core.ActivityBuilder;
import com.abstratt.mdd.frontend.internal.core.CompilationDirector;

public class FrontEnd {

    public static final String PLUGIN_ID = FrontEnd.class.getPackage().getName();

    public static IActivityBuilder newActivityBuilder(IRepository repository) {
        return new ActivityBuilder(repository);
    }

    public static ICompilationDirector getCompilationDirector() {
        return CompilationDirector.getInstance();
    }
}
