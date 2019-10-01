package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_STATEMACHINES;

import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.StateMachine;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class StateMachineRenderer implements IElementRenderer<StateMachine> {
    @Override
    public boolean renderObject(StateMachine element, IndentedPrintWriter out, IRenderingSession session) {
        boolean shouldRender = session.getSettings().getBoolean(SHOW_STATEMACHINES);
        if (!shouldRender)
            return false;
        out.println("compound = true;");
        out.println("subgraph \"cluster_" + element.getName() + "\" {");
        out.enterLevel();
        out.println("graph[");
        out.enterLevel();
        out.println("style=\"rounded, dashed\";");
        out.exitLevel();
        out.println("];");
        out.println("label = \"" + element.getNamespace().getName() + NamedElement.SEPARATOR + element.getName() + "\";");
        out.println("labeljust = \"l\";");
        out.println("fontcolor = \"blue\";");
        RenderingUtils.renderAll(session, element.getRegions());
        out.exitLevel();
        out.println("}");
        return true;
    }
}
