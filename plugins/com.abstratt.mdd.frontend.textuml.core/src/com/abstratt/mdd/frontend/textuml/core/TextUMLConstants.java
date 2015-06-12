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
package com.abstratt.mdd.frontend.textuml.core;

public interface TextUMLConstants {
	public static String PLUGIN_ID = TextUMLConstants.class.getPackage()
			.getName();

	String FILE_EXTENSION = "tuml";

	// this is a pity but we don't know of any ways of retrieving the list of
	// tokens in SableCC
	/**
	 * Alphabetically ordered list of keywords.
	 */
	String[] KEYWORDS = new String[] { 
			"abstract", 
			"aggregation", 
			"alias",
			"and",
			"any",
			"apply", 
			"association", 
			"as", 
			"attribute", 
			"begin",
			"broadcast",
			"by",
			"call",
			"catch",
			"class", 
			"create", 
			"composition", 
			"constant", 
			"datatype", 
			"delete",
			"dependency", 
			"derived", 
			"destroy", 
			"do", 
			"else", 
			"elseif", 
			"end",
			"enumeration",
			"entry",
			"exit",
			"extends", 
			"extent", 
			"false", 
			"finally",
			"function", 
			"if",
			"implements", 
			"import", 
			"in",
			"initial",
			"inout", 
			"interface",
			"invariant",
			"link", 
			"load",
			"model", 
			"navigable", 
			"new", 
			"nonunique", 
			"not", 
			"null",
			"on",
			"operation", 
			"or", 
			"ordered", 
			"out", 
			"package",
			"port", 
			"postcondition",
			"precondition", 
			"primitive", 
			"private", 
			"profile", 
			"property",
			"protected", 
			"provided",
			"public", 
			"raise", 
			"raises",
			"read",
			"readonly",
			"reception",
			"reference", 
			"repeat",
			"required", 
			"return", 
			"role", 
			"self", 
			"send",
			"signal",
			"specializes", 
			"state",
			"statemachine",
			"static",
			"stereotype", 
			"subsets",
			"terminate",
			"then",
			"to",
			"transition",
			"true",
			"try",
			"type", 
			"unique",
			"unlink", 
			"unordered", 
			"until", 
			"update", 
			"var",
			"when",
			"where", 
			"while" 
		};
}
