package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.*;

import org.eclipse.uml2.uml.Interface;

import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class InterfaceRenderer extends ClassifierRenderer<Interface> {
    @Override
    public boolean renderObject(Interface element, IndentedPrintWriter w, IRenderingSession context) {
        if (!context.getSettings().getBoolean(SHOW_INTERFACES))
            return false;
       return super.renderObject(element, w, context);

    }
}
