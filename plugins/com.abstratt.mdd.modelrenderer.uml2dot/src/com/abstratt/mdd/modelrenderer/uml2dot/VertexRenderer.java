package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Vertex;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class VertexRenderer<V extends Vertex> implements IElementRenderer<V> {
    @Override
    public boolean renderObject(V element, IndentedPrintWriter out, IRenderingSession context) {
        renderState(element, out, context);
        renderTransitions(element, out, context);
        return true;
    }

    protected void renderState(V element, IndentedPrintWriter out, IRenderingSession session) {
        out.println('"' + getVertexSymbol(element) + "\" [");
        out.enterLevel();
        out.println("shape = \"Mrecord\"");
        out.println("style = \"rounded\"");
        renderLabel(element, out, session);
        out.exitLevel();
        out.println("];");
    }

    protected void renderLabel(V element, IndentedPrintWriter out, IRenderingSession session) {
        out.println("label = \"" + getVertexLabel(element) + "\"");
    }

    public static String getVertexSymbol(Vertex element) {
        return element.getName() == null ? UML2DOTRenderingUtils.getXMIID(element) : element.getQualifiedName();
    }

    protected String getVertexLabel(Vertex element) {
        return element.getName();
    }

    private void renderTransitions(V element, IndentedPrintWriter out, IRenderingSession context) {
        RenderingUtils.renderAll(context, element.getOutgoings());
    }
}
