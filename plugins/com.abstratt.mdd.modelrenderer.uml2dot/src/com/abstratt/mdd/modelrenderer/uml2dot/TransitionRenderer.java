package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.frontend.textuml.renderer.ActivityGenerator;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;

public class TransitionRenderer implements IElementRenderer<Transition> {
    @Override
    public boolean renderObject(Transition transition, IndentedPrintWriter out, IRenderingSession context) {
        Vertex source = transition.getSource();
        Vertex target = transition.getTarget();
        boolean mutual = target.getOutgoings().stream().anyMatch(it -> it.getTarget() == source);
        boolean constraint = mutual || (target instanceof Pseudostate);

        out.print("\"" + VertexRenderer.getVertexSymbol(source) + "\":out -- " + "\"" + VertexRenderer.getVertexSymbol(target) + "\":in ");
        out.println("[");
        out.enterLevel();
        DOTRenderingUtils.addAttribute(out, "label", getTransitionLabel(transition));
        DOTRenderingUtils.addAttribute(out, "constraint", "" + true);
        DOTRenderingUtils.addAttribute(out, "arrowhead", "open");
        DOTRenderingUtils.addAttribute(out, "arrowtail", "tail");
        DOTRenderingUtils.addAttribute(out, "style", "solid");
        out.exitLevel();
        out.println("]");
        return true;
    }

    private String getTransitionLabel(Transition transition) {
        EList<Trigger> triggers = transition.getTriggers();
        String triggerLabel = StringUtils.join(
                triggers.stream().map(t -> getEventName(t.getEvent())).collect(Collectors.toList()), ", ");
        String guardLabel = getGuardLabel(transition.getGuard());
        String effectLabel = getEffectLabel(transition.getEffect());
        return triggerLabel + guardLabel + effectLabel;
    }

    private String getEffectLabel(Behavior effect) {
        if (effect == null)
            return "";
        StructuredActivityNode rootAction = ActivityUtils.getRootAction((Activity) effect);
        List<Action> statements = ActivityUtils.findStatements(rootAction);
        ActivityGenerator activityGenerator = new ActivityGenerator();
        List<String> textumlStatements = statements.stream()
                .map(statement -> activityGenerator.generateAction(statement).toString()).collect(Collectors.toList());
        return " / " + StringUtils.join(textumlStatements, "; ");
    }

    private String getGuardLabel(Constraint guard) {
        if (guard == null)
            return "";
        Activity behavior = (Activity) ActivityUtils.resolveBehaviorReference(guard.getSpecification());
        CharSequence guardExpression = new ActivityGenerator().generateActivityAsExpression(behavior);
        return " [" + guardExpression + "]";
    }

    private String getEventName(Event event) {
        if (event instanceof CallEvent) {
            Operation operation = ((CallEvent) event).getOperation();
            return operation != null ? operation.getName() : "";
        }
        if (event instanceof SignalEvent) {
            Signal signal = ((SignalEvent) event).getSignal();
            return signal != null ? signal.getName() : "";
        }
        return event.eClass().getName();
    }
}
