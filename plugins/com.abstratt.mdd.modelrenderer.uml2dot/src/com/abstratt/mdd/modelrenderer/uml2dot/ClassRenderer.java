package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.List;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.InterfaceRealization;

import com.abstratt.modelrenderer.IRenderingSession;

public class ClassRenderer extends ClassifierRenderer<Class> {

	@Override
	protected void renderRelationships(Class element,
			IRenderingSession context) {
		super.renderRelationships(element, context);
		List<InterfaceRealization> realizations = element
				.getInterfaceRealizations();
		context.renderAll(realizations);
	}
}
