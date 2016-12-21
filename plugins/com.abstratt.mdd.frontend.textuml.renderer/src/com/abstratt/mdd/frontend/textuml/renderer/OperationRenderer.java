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

import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

/**
 * 
 */
public class OperationRenderer implements IEObjectRenderer<Operation> {
    public boolean renderObject(Operation operation, IndentedPrintWriter w, IRenderingSession context) {
        TextUMLRenderingUtils.renderStereotypeApplications(w, operation);
        RenderingUtils.renderAll(context, ElementUtils.getComments(operation));
        w.print(TextUMLRenderingUtils.renderVisibility(operation.getVisibility()));
        if (operation.isStatic())
            w.print("static ");
        if (operation.isAbstract())
            w.print("abstract ");
        w.print("operation ");
        w.print(name(operation));
        w.print("(");
        List<Parameter> parameters = operation.getOwnedParameters();
        Parameter returnParameter = operation.getReturnResult();
        int i = 0;
        int nonReturnParameters = parameters.size() - (returnParameter == null ? 0 : 1);
        for (Parameter parameter : parameters)
            if (parameter != returnParameter) {
                context.render(parameter);
                if (++i < nonReturnParameters)
                    w.write(", ");
            }
        w.print(")");
        if (returnParameter != null) {
            TextUMLRenderingUtils.renderStereotypeApplications(w, returnParameter, false);
            w.print(" : ");
            w.print(TextUMLRenderingUtils.getQualifiedNameIfNeeded(returnParameter.getType(), operation.getNamespace()));
            w.print(TextUMLRenderingUtils.renderMultiplicity(returnParameter, true));
        }
        w.println(";");
        RenderingUtils.renderAll(context, operation.getMethods());
        return true;
    }
}
