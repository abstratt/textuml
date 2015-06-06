package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.State;

import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class StateRenderer extends VertexRenderer<State> {
    @Override
    protected void renderLabel(State element, IndentedPrintWriter out, IRenderingSession session) {
        if (element.getDoActivity() == null && element.getEntry() == null && element.getExit() == null) 
            super.renderLabel(element, out, session);
        else {
            out.print("label = \"{<f0>" + getVertexLabel(element));
            if (element.getEntry() != null) {
                out.print("|<entry>entry/");
                session.render(element.getEntry());
            }
            if (element.getDoActivity() != null) {
                out.println("|<do>do/");
                session.render(element.getDoActivity());
            }
            if (element.getExit() != null) {
                out.println("|<exit>exit/");
                session.render(element.getExit());
            }
            out.println("}\"");
        }
    }
}
