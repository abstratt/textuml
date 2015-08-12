package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.State;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class StateRenderer extends VertexRenderer<State> {
	@Override
	protected void renderLabel(State element, IndentedPrintWriter out, IRenderingSession session) {
		if (element.getDoActivity() == null && element.getEntry() == null && element.getExit() == null)
			super.renderLabel(element, out, session);
		else {
			out.print("label = \"{<f0>" + getVertexLabel(element));
			if (element.getEntry() != null) {
				out.print("|<entry>entry/\\l");
				session.render(element.getEntry());
			}
			if (element.getDoActivity() != null) {
				out.println("|<do>do/\\l");
				session.render(element.getDoActivity());
			}
			if (element.getExit() != null) {
				out.println("|<exit>exit/\\l");
				session.render(element.getExit());
			}
			out.println("}\"");
		}
	}
}
