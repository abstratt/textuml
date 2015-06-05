package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.StateMachine;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class RegionRenderer implements IEObjectRenderer<Region> {
    @Override
    public boolean renderObject(Region element, IndentedPrintWriter out, IRenderingSession context) {
        element.getSubvertices().forEach(it -> context.render(it));
        return true;
    }
}