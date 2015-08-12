package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_PRIMITIVES;

import org.eclipse.uml2.uml.DataType;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class PrimitiveTypeRenderer extends ClassifierRenderer<DataType> {

	@Override
	public boolean renderObject(DataType element, IndentedPrintWriter w, IRenderingSession context) {
		if (!context.getSettings().getBoolean(SHOW_PRIMITIVES))
			return false;
		return super.renderObject(element, w, context);
	}
}
