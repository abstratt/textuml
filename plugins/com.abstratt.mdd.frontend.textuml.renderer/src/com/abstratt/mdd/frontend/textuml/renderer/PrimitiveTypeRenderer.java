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

import org.eclipse.uml2.uml.PrimitiveType;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class PrimitiveTypeRenderer implements IEObjectRenderer<PrimitiveType> {
    public boolean renderObject(PrimitiveType toRender, IndentedPrintWriter writer, IRenderingSession context) {
        RenderingUtils.renderAll(context, ElementUtils.getComments(toRender));
        TextUMLRenderingUtils.renderStereotypeApplications(writer, toRender);
        writer.println("primitive " + name(toRender) + ";");
        writer.println();
        return true;
    }
}
