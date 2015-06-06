package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_STATEMACHINES;

import org.eclipse.uml2.uml.StateMachine;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;
import com.abstratt.modelrenderer.RenderingUtils;

public class StateMachineRenderer implements IEObjectRenderer<StateMachine> {
    @Override
    public boolean renderObject(StateMachine element, IndentedPrintWriter out, IRenderingSession session) {
        boolean shouldRender = session.getSettings().getBoolean(SHOW_STATEMACHINES);
        return shouldRender && RenderingUtils.renderAll(session, element.getRegions());
    }
}
