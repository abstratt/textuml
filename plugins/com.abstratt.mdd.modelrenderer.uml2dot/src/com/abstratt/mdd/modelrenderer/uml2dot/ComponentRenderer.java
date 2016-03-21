package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Port;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class ComponentRenderer implements IElementRenderer<Component> {

	@Override
	public boolean renderObject(Component element, IndentedPrintWriter out, IRenderingSession session) {
		// do not render components
		return false;
	}
}
