package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Pseudostate;

import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

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
        super.renderObject(element, out, context);

        out.print("\"START\"[");
        out.println("label = \"\"");
        out.println("shape = \"circle\"");
        out.println("style = \"filled\"");
        out.println("fillcolor= \"black\"");
        out.println("fixedsize= \"shape\"");
        out.println("width= \"0.3\"");
        out.println("height= \"0.3\"");  
        out.println("];");
        
        out.print("\"START\" -- " + "\"" + getVertexSymbol(element) + "\":in "); 
        out.println("[");
        out.enterLevel();
        DOTRenderingUtils.addAttribute(out, "constraint", "" + true);
        DOTRenderingUtils.addAttribute(out, "arrowhead", "open");
        DOTRenderingUtils.addAttribute(out, "arrowtail", "tail");
        DOTRenderingUtils.addAttribute(out, "style", "solid");
        out.exitLevel();
        out.println("]");
    }
    
    private void renderTerminateState(Pseudostate element, IndentedPrintWriter out, IRenderingSession context) {
        out.print("\"" + getVertexSymbol(element) + "\"[");
        out.println("xlabel = \"" + getVertexLabel(element) + "\"");
        out.println("label = \"\"");
        out.println("shape = \"doublecircle\"");
        out.println("style = \"filled\"");
        out.println("fillcolor= \"black\"");
        out.println("fixedsize= \"shape\"");
        out.println("width= \"0.3\"");
        out.println("height= \"0.3\"");
        out.println("];");
    }
}
