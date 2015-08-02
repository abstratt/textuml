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

import com.abstratt.mdd.frontend.core.spi.ISourceMiner;
import com.abstratt.mdd.internal.frontend.textuml.SCCTextUMLSourceMiner;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;

public class TextUMLCore {
	public static String PLUGIN_ID = TextUMLCore.class.getPackage().getName();
	private static ISourceMiner<Node> sourceMiner = new SCCTextUMLSourceMiner();

	public static ISourceMiner<Node> getSourceMiner() {
		return sourceMiner ;
	}
}
