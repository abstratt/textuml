package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;
import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;
import com.abstratt.modelrenderer.RenderingUtils;

public class VertexRenderer<V extends Vertex> implements IElementRenderer<V> {
    @Override
    public boolean renderObject(V element, IndentedPrintWriter out, IRenderingSession context) {
        renderState(element, out, context);
        renderTransitions(element, out, context);
        return true;
    }

    protected void renderState(V element, IndentedPrintWriter out, IRenderingSession session) {
        out.print('"' + getVertexSymbol(element) + "\" [");
        out.println("shape = \"Mrecord\"");
        out.println("style = \"rounded\"");
        renderLabel(element, out, session);
        out.println("];");
    }

    protected void renderLabel(V element, IndentedPrintWriter out, IRenderingSession session) {
        out.println("label = \"" + getVertexLabel(element) + "\"");
    }

    protected String getVertexSymbol(V element) {
        return element.getName();
    }

    protected String getVertexLabel(V element) {
        return element.getName();
    }
    
    private void renderTransitions(V element, IndentedPrintWriter out, IRenderingSession context) {
        RenderingUtils.renderAll(context, element.getOutgoings());
    }
}
