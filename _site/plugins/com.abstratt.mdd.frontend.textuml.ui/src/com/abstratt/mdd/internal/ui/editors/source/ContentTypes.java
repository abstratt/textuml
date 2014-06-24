/*******************************************************************************
 * Copyright (c) 2008 Massimiliano Federici and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Massimiliano Federici - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.internal.ui.editors.source;

import org.eclipse.jface.text.IDocument;

public abstract class ContentTypes {

	public static final String COMMENT_CONTENT_TYPE = ContentTypes.class.getName() + ".comment";

	public static final String DEFAULT_CONTENT_TYPE = IDocument.DEFAULT_CONTENT_TYPE;

	public static final String[] CONFIGURED_CONTENT_TYPES = new String[] { COMMENT_CONTENT_TYPE, DEFAULT_CONTENT_TYPE };
}
