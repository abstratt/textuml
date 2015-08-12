/*******************************************************************************
 * Copyright (c) 2009 Vladimir Sosnin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Sosnin - initial API and implementation
 *******************************************************************************/

package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.Dependency;

import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

/**
 * @author vas
 *
 */
public class DependencyRenderer implements IEObjectRenderer<Dependency> {

	public boolean renderObject(Dependency dependency, IndentedPrintWriter writer, IRenderingSession context) {
		if (dependency.getSuppliers().isEmpty())
			return false;
		writer.write("dependency ");
		writer.write(TextUMLRenderingUtils.getQualifiedNameIfNeeded(dependency.getSuppliers().get(0),
		        dependency.getNamespace()));
		writer.println(";");
		return true;
	}

}
