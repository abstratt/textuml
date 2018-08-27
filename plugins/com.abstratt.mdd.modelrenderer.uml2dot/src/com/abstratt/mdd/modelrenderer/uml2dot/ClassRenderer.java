package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_CLASSES;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.StateMachine;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class ClassRenderer extends ClassifierRenderer<Class> {

    @Override
    public boolean renderObject(Class element, IndentedPrintWriter w, IRenderingSession session) {
        boolean renderedClass = session.getSettings().getBoolean(SHOW_CLASSES)
                && super.renderObject(element, w, session);
        List<Behavior> stateMachines = element.getOwnedBehaviors().stream().filter(it -> it instanceof StateMachine)
                .collect(Collectors.toList());
        return renderedClass | RenderingUtils.renderAll(session, stateMachines);
    }

    @Override
    protected void renderClassifierTypeAdornment(Class element, IndentedPrintWriter w, IRenderingSession session) {
        // classes are the real deal, they do not need an adornment
    	
    }

    @Override
    protected void renderRelationships(Class element, IRenderingSession context) {
        super.renderRelationships(element, context);
        List<InterfaceRealization> realizations = element.getInterfaceRealizations();
        RenderingUtils.renderAll(context, realizations);
    }
}
