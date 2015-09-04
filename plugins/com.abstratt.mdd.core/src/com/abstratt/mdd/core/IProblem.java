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

public interface IProblem {

    public static enum Severity {
        ERROR, INFO, WARNING
    }

    public static String FILE_NAME = "fileName";

    public static String LINE_NUMBER = "lineNumber";

    /**
     * Returns the value of the attribute with the given key, or
     * <code>null</code> if none exists.
     * 
     * @param key
     *            the attribute key
     * @return the attribute value, or <code>null</code>
     */
    public Object getAttribute(String key);

    /**
     * Returns the problem description.
     * 
     * @return the problem description
     */
    public String getMessage();

    /**
     * Returns the problem severity.
     * 
     * @return the problem severity
     */
    public Severity getSeverity();

    /**
     * Attaches an attribute to this problem.
     * 
     * @param key
     *            the attribute key
     * @param value
     *            the attribute value
     */
    public void setAttribute(String key, Object value);
}