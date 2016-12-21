/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Vladimir Sosnin - #2798455
 *******************************************************************************/
package com.abstratt.mdd.frontend.textuml.renderer;

import static com.abstratt.mdd.frontend.textuml.renderer.TextUMLRenderingUtils.name;

import java.util.List;

import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class AssociationRenderer implements IEObjectRenderer<Association> {

    public boolean renderObject(Association association, IndentedPrintWriter writer, IRenderingSession context) {
        if (association instanceof Extension)
            return false;
        RenderingUtils.renderAll(context, ElementUtils.getComments(association));
        AggregationKind aggregationKind = AggregationKind.NONE_LITERAL;
        List<Property> memberEnds = association.getMemberEnds();
        for (Property property : memberEnds)
            if (property.getAggregation() != aggregationKind)
                aggregationKind = property.getAggregation();
        String keyword;
        switch (aggregationKind) {
        case COMPOSITE_LITERAL:
            keyword = "composition";
            break;
        case SHARED_LITERAL:
            keyword = "aggregation";
            break;
        default:
            keyword = "association";
        }
        TextUMLRenderingUtils.renderStereotypeApplications(writer, association);
        writer.print(keyword);
        if (name(association) != null)
            writer.print(" " + name(association));
        writer.println();
        writer.enterLevel();
        for (Property property : memberEnds) {
            if (property.getClass_() == null)
                continue;
            RenderingUtils.renderAll(context, ElementUtils.getComments(property));
            writer.write("role ");
            writer.write(TextUMLRenderingUtils.getQualifiedNameIfNeeded((NamedElement) property.getClass_(),
                    association.getNamespace()));
            writer.write(".");
            writer.print(name(property));
            writer.println(";");
        }
        List<Property> ownedEnds = association.getOwnedEnds();
        for (Property property : ownedEnds) {
            RenderingUtils.renderAll(context, ElementUtils.getComments(property));
            TextUMLRenderingUtils.renderStereotypeApplications(writer, property, true);
            if (property.isNavigable())
                writer.write("navigable ");
            writer.write("role ");
            writer.write((property.getName() == null || property.getName().length() == 0) ? "unnamed" : name(property));
            writer.write(" : ");
            writer.write(TextUMLRenderingUtils.getQualifiedNameIfNeeded(property));
            writer.println(";");
        }
        writer.exitLevel();
        writer.println("end;");
        writer.println();
        return true;
    }

}
