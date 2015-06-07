package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.StructuredActivityNode;

import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;
import com.abstratt.modelrenderer.RenderingUtils;

public class StructuredActivityNodeRenderer implements IEObjectRenderer<StructuredActivityNode> {

    @Override
    public boolean renderObject(StructuredActivityNode element, IndentedPrintWriter out, IRenderingSession session) {
        out.println("begin");
        out.enterLevel();
        RenderingUtils.renderAll(session, ActivityUtils.findStatements(element));
        out.exitLevel();
        out.println("end");
        return true;
    }

}
