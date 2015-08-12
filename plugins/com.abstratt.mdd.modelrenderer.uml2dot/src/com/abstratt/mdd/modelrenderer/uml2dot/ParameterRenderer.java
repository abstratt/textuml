package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_PARAMETER_DIRECTION;
import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_PARAMETER_NAMES;

import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Type;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class ParameterRenderer implements IElementRenderer<Parameter> {

	public boolean renderObject(Parameter parameter, IndentedPrintWriter w, IRenderingSession context) {
		if (context.getSettings().getBoolean(SHOW_PARAMETER_DIRECTION) && parameter.getDirection() != null)
			w.print(parameter.getDirection().getLiteral() + " ");
		if (context.getSettings().getBoolean(SHOW_PARAMETER_NAMES))
			w.print(parameter.getName());
		w.print(" : ");
		Type paramType = parameter.getType();
		if (paramType != null) {
			w.print(paramType.getName());
			w.print(UML2DOTRenderingUtils.renderMultiplicity(parameter, true));
		}
		return true;
	}
}
