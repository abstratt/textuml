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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class StereotypeRenderer implements IEObjectRenderer<Stereotype> {

    public boolean renderObject(Stereotype stereotype, IndentedPrintWriter writer, IRenderingSession context) {
        RenderingUtils.renderAll(context, ElementUtils.getComments(stereotype));
        TextUMLRenderingUtils.renderStereotypeApplications(writer, stereotype);
        if (stereotype.isAbstract())
            writer.print("abstract ");
        writer.print("stereotype " + name(stereotype));
        List<Extension> extensions = new ArrayList<Extension>();
        for (Property property : stereotype.getOwnedAttributes())
            if (property.getAssociation() instanceof Extension)
                extensions.add((Extension) property.getAssociation());
        if (!extensions.isEmpty()) {
            StringBuilder extensionList = new StringBuilder();
            for (Extension extension : extensions) {
                String extendedMetaClassName = TextUMLRenderingUtils.getQualifiedNameIfNeeded(extension.getMetaclass(),
                        stereotype.getNamespace());
                extensionList.append(extendedMetaClassName);
                if (extension.isRequired())
                    extensionList.append(" required");
                extensionList.append(", ");
            }
            extensionList.delete(extensionList.length() - 2, extensionList.length());
            writer.print(" extends ");
            writer.print(extensionList);
        }
        List<Generalization> generalizations = stereotype.getGeneralizations();
        StringBuilder specializationList = new StringBuilder();
        for (Generalization generalization : generalizations)
            specializationList.append(TextUMLRenderingUtils.getQualifiedNameIfNeeded(generalization.getGeneral(),
                    stereotype.getNamespace()) + ", ");
        if (specializationList.length() > 0) {
            specializationList.delete(specializationList.length() - 2, specializationList.length());
            writer.print(" specializes ");
            writer.print(specializationList);
        }
        writer.println();
        writer.enterLevel();
        RenderingUtils.renderAll(context, stereotype.getOwnedAttributes());
        RenderingUtils.renderAll(context, stereotype.getOwnedOperations());
        writer.exitLevel();
        writer.println("end;");
        writer.println();
        return true;
    }

}
