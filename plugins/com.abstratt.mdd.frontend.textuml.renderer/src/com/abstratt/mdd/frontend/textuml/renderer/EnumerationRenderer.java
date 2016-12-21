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

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class EnumerationRenderer implements IEObjectRenderer<Enumeration> {
    public boolean renderObject(Enumeration enumeration, IndentedPrintWriter writer, IRenderingSession context) {
        RenderingUtils.renderAll(context, ElementUtils.getComments(enumeration));
        TextUMLRenderingUtils.renderStereotypeApplications(writer, enumeration);
        writer.println("enumeration " + name(enumeration));
        writer.enterLevel();
        EList<EnumerationLiteral> literals = enumeration.getOwnedLiterals();
        StringBuilder builder = new StringBuilder();
        for (EnumerationLiteral enumerationLiteral : literals) {
            builder.append(name(enumerationLiteral));
            builder.append(", ");
        }
        if (builder.length() > 0) {
            builder.delete(builder.length() - 2, builder.length());
            writer.print(builder);
            writer.println();
        }
        RenderingUtils.renderAll(context, enumeration.getOwnedAttributes());
        RenderingUtils.renderAll(context, enumeration.getOwnedOperations());
        writer.exitLevel();
        writer.println("end;");
        writer.println();
        return true;
    }

}
