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
package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.PackageImport;


import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

import static com.abstratt.mdd.frontend.textuml.renderer.TextUMLRenderingUtils.*;

public class PackageImportRenderer implements IEObjectRenderer<PackageImport> {

	public boolean renderObject(PackageImport element, IndentedPrintWriter out,
			IRenderingSession context) {
		out.println("import " + qualifiedName(element.getImportedPackage()) + ";");
		return true;
	}

}
