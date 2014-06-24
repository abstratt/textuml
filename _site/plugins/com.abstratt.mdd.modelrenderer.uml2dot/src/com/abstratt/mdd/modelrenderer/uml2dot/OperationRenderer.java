/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_STRUCTURAL_FEATURE_VISIBILITY;

import java.util.List;

import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

/**
 * 
 */
public class OperationRenderer implements IEObjectRenderer<Operation> {
	public boolean renderObject(Operation operation, IndentedPrintWriter w, IRenderingSession context) {
		if (operation.getName() == null || UML2DOTRenderingUtils.isTemplateInstance(operation))
			return false;
		w.print("<TR><TD align=\"left\">");
		// TODO this is duplicated in ClassRenderer#renderNameAdornments
		if (context.getSettings().getBoolean(UML2DOTPreferences.SHOW_FEATURE_STEREOTYPES) && !operation.getAppliedStereotypes().isEmpty()) {
			StringBuffer stereotypeList = new StringBuffer();
			for (Stereotype current : operation.getAppliedStereotypes()) {
				stereotypeList.append(current.getName());
				stereotypeList.append(", ");
			}
			stereotypeList.delete(stereotypeList.length() - 2, stereotypeList.length());
			w.print(UML2DOTRenderingUtils.addGuillemots(stereotypeList.toString()));
		}
		if (context.getSettings().getBoolean(SHOW_STRUCTURAL_FEATURE_VISIBILITY))
			w.print(UML2DOTRenderingUtils.renderVisibility(operation.getVisibility()));
		w.print(operation.getName());
		w.print("(");
		List<Parameter> parameters = operation.getOwnedParameters();
		Parameter returnParameter = null;
		for (int i = 0; i < parameters.size(); i++) {
			Parameter parameter = parameters.get(i);
			if (parameter.getDirection() == ParameterDirectionKind.RETURN_LITERAL)
				returnParameter = parameter;
			else {
				if (i > 0)
					w.print(", ");
				context.render(parameter);
			}
		}
		w.print(")");
		if (returnParameter != null) {
			w.print(" : "); 
			Type returnType = returnParameter.getType();
			w.print(returnType != null ? returnType.getName() : "null");
			w.print(UML2DOTRenderingUtils.renderMultiplicity(returnParameter, true));
		}
		w.print("</TD></TR>");
		return true;
	}
}
