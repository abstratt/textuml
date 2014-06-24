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

import static com.abstratt.mdd.frontend.textuml.renderer.TextUMLRenderingUtils.name;

import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class ParameterRenderer implements IEObjectRenderer<Parameter> {

	public boolean renderObject(Parameter parameter, IndentedPrintWriter w,
			IRenderingSession context) {
		TextUMLRenderingUtils.renderStereotypeApplications(w, parameter, false);
		ParameterDirectionKind direction = parameter.getDirection();
		if (direction != null)
			w.print(direction.getLiteral() + " ");
		w.print(name(parameter));
		w.print(" : ");
		w.print(TextUMLRenderingUtils.getQualifiedNameIfNeeded(parameter));
		return true;
	}
}
