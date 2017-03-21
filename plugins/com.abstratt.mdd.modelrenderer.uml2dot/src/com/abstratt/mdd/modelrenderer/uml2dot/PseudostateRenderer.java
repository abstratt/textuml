package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Pseudostate;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class PseudostateRenderer extends VertexRenderer<Pseudostate> {
    @Override
    public boolean renderObject(Pseudostate element, IndentedPrintWriter out, IRenderingSession context) {
        switch (element.getKind()) {
        case INITIAL_LITERAL:
            renderInitialState(element, out, context);
            break;
        case TERMINATE_LITERAL:
            renderTerminateState(element, out, context);
            break;
        }
        return true;
    }

    private void renderInitialState(Pseudostate element, IndentedPrintWriter out, IRenderingSession context) {

        // we render the initial node as a separate node with a transition
        // coming into the current node
        out.print("\"" + getVertexSymbol(element) + "\"[");
        out.println("label = \"\"");
        out.println("shape = \"circle\"");
        out.println("style = \"filled\"");
        out.println("fillcolor= \"black\"");
        out.println("fixedsize= \"shape\"");
        out.println("width= \"0.25\"");
        out.println("height= \"0.25\"");
        out.println("];");
        
        context.render(element.getOutgoings().get(0));
    }

    private void renderTerminateState(Pseudostate element, IndentedPrintWriter out, IRenderingSession context) {
        out.print("\"" + getVertexSymbol(element) + "\"[");
        out.println("label = \"\"");
        out.println("shape = \"doublecircle\"");
        out.println("style = \"filled\"");
        out.println("fillcolor= \"black\"");
        out.println("fixedsize= \"shape\"");
        out.println("width= \"0.25\"");
        out.println("height= \"0.25\"");
        out.println("];");
    }
}
