package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.StateMachine;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class StateMachineRenderer implements IEObjectRenderer<StateMachine> {
    @Override
    public boolean renderObject(StateMachine element, IndentedPrintWriter out, IRenderingSession context) {
        element.getRegions().forEach(it -> context.render(it));
        return true;
    }
}
