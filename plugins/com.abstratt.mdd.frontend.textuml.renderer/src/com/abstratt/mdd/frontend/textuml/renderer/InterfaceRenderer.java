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

import java.util.List;

import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interface;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class InterfaceRenderer implements IEObjectRenderer<Interface> {

    public boolean renderObject(Interface interface_, IndentedPrintWriter writer, IRenderingSession context) {
        RenderingUtils.renderAll(context, ElementUtils.getComments(interface_));
        TextUMLRenderingUtils.renderStereotypeApplications(writer, interface_);
        writer.print("interface " + name(interface_));
        List<Generalization> generalizations = interface_.getGeneralizations();
        StringBuilder specializationList = new StringBuilder();
        for (Generalization generalization : generalizations)
            specializationList.append(TextUMLRenderingUtils.getQualifiedNameIfNeeded(generalization.getGeneral(),
                    interface_.getNamespace()) + ", ");
        if (specializationList.length() > 0) {
            specializationList.delete(specializationList.length() - 2, specializationList.length());
            writer.print(" specializes ");
            writer.print(specializationList);
        }
        writer.println();
        writer.enterLevel();
        RenderingUtils.renderAll(context, interface_.getOwnedAttributes());
        RenderingUtils.renderAll(context, interface_.getOwnedOperations());
        RenderingUtils.renderAll(context, interface_.getClientDependencies());
        writer.exitLevel();
        writer.println("end;");
        writer.println();
        return true;
    }

}
