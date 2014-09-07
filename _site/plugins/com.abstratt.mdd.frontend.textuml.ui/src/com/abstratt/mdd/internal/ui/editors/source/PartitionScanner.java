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

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class PartitionScanner extends RuleBasedPartitionScanner {

	public PartitionScanner() {
		super();
		final IToken comment = new Token(ContentTypes.COMMENT_CONTENT_TYPE);
		final IPredicateRule[] rules = new IPredicateRule[2];
		rules[0] = new MultiLineRule("/*", "*/", comment);
		rules[1] = new MultiLineRule("(*", "*)", comment);
		this.setPredicateRules(rules);
	}

}
