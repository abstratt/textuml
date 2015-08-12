package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.Action;

import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class ActionRenderer implements IEObjectRenderer<Action> {

    @Override
    public boolean renderObject(Action element, IndentedPrintWriter out, IRenderingSession session) {
        out.println("/* TBD: " + element.eClass().getName() + " */");
        return true;
    }

}
