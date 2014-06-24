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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SyntaxProblem extends Problem {
	// the format of lexer/parser exceptions is:
	// [<line-offset>,<char-offset>] <message>
	private static final Pattern MESSAGE_PATTERN = Pattern.compile("\\[(\\d+),(\\d+)\\](.*)");

	private String message;

	public SyntaxProblem(String message) {
		super(Severity.ERROR);
		Matcher matcher = MESSAGE_PATTERN.matcher(message);
		if (!matcher.matches() || matcher.groupCount() != 3) {
			this.message = message;
			setAttribute(LINE_NUMBER, 0);
			return;
		}
		setAttribute(LINE_NUMBER, Integer.parseInt(matcher.group(1)));
		this.message = matcher.group(3);
	}

	public String getMessage() {
		return message;
	}

}
