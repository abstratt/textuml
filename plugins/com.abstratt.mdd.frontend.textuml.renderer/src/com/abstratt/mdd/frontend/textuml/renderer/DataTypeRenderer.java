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

import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Generalization;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class DataTypeRenderer implements IEObjectRenderer<DataType> {

    public boolean renderObject(DataType dataType, IndentedPrintWriter writer, IRenderingSession context) {
        RenderingUtils.renderAll(context, ElementUtils.getComments(dataType));
        TextUMLRenderingUtils.renderStereotypeApplications(writer, dataType);
        if (dataType.isAbstract())
            writer.print("abstract ");
        writer.print("datatype " + name(dataType));
        List<Generalization> generalizations = dataType.getGeneralizations();
        StringBuilder specializationList = new StringBuilder();
        for (Generalization generalization : generalizations)
            specializationList.append(TextUMLRenderingUtils.getQualifiedNameIfNeeded(generalization.getGeneral(),
                    dataType.getNamespace()) + ", ");
        if (specializationList.length() > 0) {
            specializationList.delete(specializationList.length() - 2, specializationList.length());
            writer.print(" specializes ");
            writer.print(specializationList);
        }
        writer.println();
        writer.enterLevel();
        RenderingUtils.renderAll(context, dataType.getOwnedAttributes());
        RenderingUtils.renderAll(context, dataType.getOwnedOperations());
        writer.exitLevel();
        writer.println("end;");
        writer.println();
        return true;
    }
}
