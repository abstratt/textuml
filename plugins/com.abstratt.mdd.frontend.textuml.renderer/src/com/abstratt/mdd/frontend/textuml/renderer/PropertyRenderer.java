/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Vladimir Sosnin - #2798743
 *******************************************************************************/
package com.abstratt.mdd.frontend.textuml.renderer;

import static com.abstratt.mdd.frontend.textuml.renderer.TextUMLRenderingUtils.name;

import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.ValueSpecification;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class PropertyRenderer implements IEObjectRenderer<Property> {
    public boolean renderObject(Property property, IndentedPrintWriter writer, IRenderingSession context) {
        if (property.getAssociation() instanceof Extension)
            // association of a stereotype with an extended metaclass
            return false;
        RenderingUtils.renderAll(context, ElementUtils.getComments(property));
        TextUMLRenderingUtils.renderStereotypeApplications(writer, property);
        writer.print(TextUMLRenderingUtils.renderVisibility(property.getVisibility()));
        if (property.isReadOnly())
            writer.print("readonly ");
        else {
            if (property.isStatic())
                writer.print("static ");
            if (property.getOwner() instanceof Stereotype)
                writer.print("property ");
            else
                writer.print("attribute ");
        }
        writer.print(name(property));
        writer.print(" : ");
        writer.print(TextUMLRenderingUtils.getQualifiedNameIfNeeded(property));
        ValueSpecification defaultValue = property.getDefaultValue();
        if (defaultValue != null) {
            writer.print(" := ");
            context.render(defaultValue);
        }
        writer.println(";");
        return true;
    }
}
