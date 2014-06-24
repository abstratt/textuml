package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Interface;

import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class InterfaceRenderer extends ClassifierRenderer<Interface> {

	@Override
	protected void renderNameAdornments(Interface element, IndentedPrintWriter w, IRenderingSession context) {
		w.print("<TR><TD>");
		w.print(UML2DOTRenderingUtils.addGuillemots("interface"));
		w.print("</TD></TR>");
		super.renderNameAdornments(element, w, context);
	}
}
