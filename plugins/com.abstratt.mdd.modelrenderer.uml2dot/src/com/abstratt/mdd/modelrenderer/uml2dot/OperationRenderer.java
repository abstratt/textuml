/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_PARAMETERS;
import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_RETURN_PARAMETER;
import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_FEATURE_VISIBILITY;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

/**
 * 
 */
public class OperationRenderer implements IElementRenderer<Operation> {
    public boolean renderObject(Operation operation, IndentedPrintWriter w, IRenderingSession context) {
        if (operation.getName() == null || UML2DOTRenderingUtils.isTemplateInstance(operation))
            return false;
        if (RendererHelper.shouldSkip(context, operation))
        	return false;
        if (operation.isStatic() && !context.getSettings().getBoolean(UML2DOTPreferences.SHOW_STATIC_FEATURES, true))
			return false;
        
        w.print("<TR><TD align=\"left\">");
        // TODO this is duplicated in ClassRenderer#renderNameAdornments
        if (context.getSettings().getBoolean(UML2DOTPreferences.SHOW_FEATURE_STEREOTYPES)
                && !operation.getAppliedStereotypes().isEmpty()) {
            StringBuffer stereotypeList = new StringBuffer();
            for (Stereotype current : operation.getAppliedStereotypes()) {
                stereotypeList.append(current.getName());
                stereotypeList.append(", ");
            }
            stereotypeList.delete(stereotypeList.length() - 2, stereotypeList.length());
            w.print(UML2DOTRenderingUtils.addGuillemots(stereotypeList.toString()));
        }
        if (context.getSettings().getBoolean(SHOW_FEATURE_VISIBILITY))
            w.print(UML2DOTRenderingUtils.renderVisibility(operation.getVisibility()));
        if (operation.isStatic())
        	w.print("<u>");
        if (operation.isAbstract())
        	w.print("<i>");
        w.print(operation.getName());
        if (operation.isAbstract())
        	w.print("</i>");
        if (operation.isStatic())
        	w.print("</u>");
        List<Parameter> parameters = operation.getOwnedParameters();
        Parameter returnParameter = parameters.stream()
                .filter(p -> p.getDirection() == ParameterDirectionKind.RETURN_LITERAL).findAny().orElse(null);
        List<Parameter> inOutParameters = parameters.stream()
                .filter(p -> p.getDirection() != ParameterDirectionKind.RETURN_LITERAL).collect(Collectors.toList());
        w.print("(");
        if (context.getSettings().getBoolean(SHOW_PARAMETERS)) {
            for (int i = 0; i < inOutParameters.size(); i++) {
                Parameter parameter = inOutParameters.get(i);
                if (i > 0)
                    w.print(", ");
                context.render(parameter);
            }
        } else {
            if (!inOutParameters.isEmpty())
                w.print("...");
        }
        w.print(")");
        if (returnParameter != null && context.getSettings().getBoolean(SHOW_RETURN_PARAMETER)) {
            w.print(" : ");
            Type returnType = returnParameter.getType();
            w.print(returnType != null ? returnType.getName() : "null");
            w.print(UML2DOTRenderingUtils.renderMultiplicity(returnParameter, true));
        }
        if (operation.isQuery())
        	w.print("{query}");
        w.print("</TD></TR>");
        return true;
    }
}
