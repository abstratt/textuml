package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.ActivityNode;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class ActivityNodeRenderer implements IEObjectRenderer<ActivityNode> {

    @Override
    public boolean renderObject(ActivityNode element, IndentedPrintWriter out, IRenderingSession session) {
        out.println("/* TBD: " + element.eClass().getName() + " */");
        return true;
    }

}
