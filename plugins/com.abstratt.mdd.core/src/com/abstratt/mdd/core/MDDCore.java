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
package com.abstratt.mdd.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;

import com.abstratt.mdd.internal.core.Repository;

public class MDDCore {
    public static final String PLUGIN_ID = "com.abstratt.mdd.core";

    public static IRepository createRepository(URI baseURI) throws CoreException {
        return new Repository(baseURI, true);
    }

    public static IRepository getInProgressRepository() {
        return Repository.getInProgress();
    }
}
