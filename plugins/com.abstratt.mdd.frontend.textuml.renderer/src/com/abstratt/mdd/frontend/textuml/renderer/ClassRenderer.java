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

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.InterfaceRealization;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class ClassRenderer implements IEObjectRenderer<Class> {

    public boolean renderObject(Class clazz, IndentedPrintWriter writer, IRenderingSession context) {
        RenderingUtils.renderAll(context, ElementUtils.getComments(clazz));
        TextUMLRenderingUtils.renderStereotypeApplications(writer, clazz);
        if (clazz.isAbstract())
            writer.print("abstract ");
        writer.print("class " + name(clazz));
        List<Generalization> generalizations = clazz.getGeneralizations();
        StringBuilder specializationList = new StringBuilder();
        for (Generalization generalization : generalizations)
            specializationList.append(TextUMLRenderingUtils.getQualifiedNameIfNeeded(generalization.getGeneral(),
                    clazz.getNamespace())
                    + ", ");
        if (specializationList.length() > 0) {
            specializationList.delete(specializationList.length() - 2, specializationList.length());
            writer.print(" specializes ");
            writer.print(specializationList);
        }
        List<InterfaceRealization> realizations = clazz.getInterfaceRealizations();
        StringBuilder realizationList = new StringBuilder();
        for (InterfaceRealization realization : realizations)
            realizationList.append(TextUMLRenderingUtils.getQualifiedNameIfNeeded(realization.getContract(),
                    clazz.getNamespace())
                    + ", ");
        if (realizationList.length() > 0) {
            realizationList.delete(realizationList.length() - 2, realizationList.length());
            writer.print(" implements ");
            writer.print(realizationList);
        }
        writer.println();
        writer.enterLevel();
        RenderingUtils.renderAll(context, clazz.getOwnedAttributes());
        RenderingUtils.renderAll(context, clazz.getOwnedOperations());
        RenderingUtils.renderAll(context, clazz.getClientDependencies());
        writer.exitLevel();
        writer.println("end;");
        writer.println();
        return true;
    }
}
