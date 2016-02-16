package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Port;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class PortRenderer implements IElementRenderer<Port> {

	@Override
	public boolean renderObject(Port element, IndentedPrintWriter out, IRenderingSession session) {
		// do not render ports
		return false;
	}
}
