package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Region;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class RegionRenderer implements IElementRenderer<Region> {
    @Override
    public boolean renderObject(Region element, IndentedPrintWriter out, IRenderingSession context) {
        return RenderingUtils.renderAll(context, element.getSubvertices());
    }
}
