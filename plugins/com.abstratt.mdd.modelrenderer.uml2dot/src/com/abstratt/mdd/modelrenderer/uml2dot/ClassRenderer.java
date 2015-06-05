package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.List;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.*;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.StateMachine;

import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class ClassRenderer extends ClassifierRenderer<Class> {

    @Override
    public boolean renderObject(Class element, IndentedPrintWriter w, IRenderingSession context) {
        boolean renderedClass = context.getSettings().getBoolean(SHOW_CLASSES) && super.renderObject(element, w, context);
        boolean renderedStateMachines = context.getSettings().getBoolean(SHOW_STATEMACHINES) && !element.getOwnedBehaviors().stream().allMatch(it -> {
            if (it instanceof StateMachine) {
                context.render(it);
                return false;
            } else {
                return true;
            }
        });
        return renderedClass || renderedStateMachines;
    }
    
    @Override
    protected void renderClassifierTypeAdornment(Class element, IndentedPrintWriter w, IRenderingSession session) {
        // classes are the real deal, they do not need an adornment
    }
	@Override
	protected void renderRelationships(Class element,
			IRenderingSession context) {
		super.renderRelationships(element, context);
		List<InterfaceRealization> realizations = element
				.getInterfaceRealizations();
		context.renderAll(realizations);
	}
}
