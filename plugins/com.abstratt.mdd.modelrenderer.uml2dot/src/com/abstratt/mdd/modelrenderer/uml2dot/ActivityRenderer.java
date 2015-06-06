package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Activity;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class ActivityRenderer implements IEObjectRenderer<Activity> {
    @Override
    public boolean renderObject(Activity element, IndentedPrintWriter out, IRenderingSession session) {
        out.print("TBD");
        return true;
    }
}
