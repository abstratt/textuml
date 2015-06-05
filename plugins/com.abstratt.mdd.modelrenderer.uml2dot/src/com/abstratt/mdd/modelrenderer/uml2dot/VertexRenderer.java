package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;
import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class VertexRenderer implements IEObjectRenderer<Vertex> {
    @Override
    public boolean renderObject(Vertex element, IndentedPrintWriter out, IRenderingSession context) {
        out.print('"' + element.getName() + "\" [");
        out.println("label = \"" + element.getName() + "\"");
        out.println("shape = \"mrecord\"");
        out.println("style = \"rounded\""); 
        out.println("];");
        Vertex source = element;
        
        element.getOutgoings().forEach((Transition transition) -> {
            Vertex target = transition.getTarget();
            boolean mutual = target.getOutgoings().stream().anyMatch(it -> it.getTarget() == source);
            
            transition.getTriggers().forEach((Trigger trigger) -> {
                
            
                out.print("\"" + source.getName() + "\":out -- " + "\"" + target.getName()
                        + "\":in "); 
                out.println("[");
                out.enterLevel();
                String triggerLabel = getEventName(trigger.getEvent());
                DOTRenderingUtils.addAttribute(out, "label", triggerLabel);
                DOTRenderingUtils.addAttribute(out, "constraint", "" + mutual);
                DOTRenderingUtils.addAttribute(out, "arrowhead", "open");
                DOTRenderingUtils.addAttribute(out, "arrowtail", "tail");
                DOTRenderingUtils.addAttribute(out, "style", "solid");
                out.exitLevel();
                out.println("]");
            });
        });
        
        return true;
    }

    private String getEventName(Event event) {
        if (event instanceof CallEvent)
            return ((CallEvent) event).getOperation().getName();
        if (event instanceof SignalEvent)
            return ((SignalEvent) event).getSignal().getName();
        return event.eClass().getName();
    }
}
