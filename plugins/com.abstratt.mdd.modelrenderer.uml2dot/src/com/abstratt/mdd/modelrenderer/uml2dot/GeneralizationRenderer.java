/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Generalization;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;

/**
 * 
 */
public class GeneralizationRenderer extends AbstractRelationshipRenderer<Generalization> {

    @Override
    protected boolean basicRenderObject(Generalization element, IndentedPrintWriter pw, IRenderingSession context) {
        if (!shouldRender(context, element.getSpecific(), element.getGeneral()))
            return true;
        context.render(element.getGeneral(), element.getGeneral().eResource() != element.eResource());
        if (!context.isRendered(element.getGeneral()))
        	return false;
        pw.print("edge ");
        // if (element.getName() != null)
        // pw.print("\"" + element.getName() + "\" ");
        pw.println("[");
        pw.enterLevel();
        pw.println("arrowtail = \"empty\"");
        pw.println("arrowhead = \"none\"");
        pw.println("taillabel = \"\"");
        pw.println("headlabel = \"\"");
        DOTRenderingUtils.addAttribute(pw, "constraint", "true");
        DOTRenderingUtils.addAttribute(pw, "style", "none");
        pw.exitLevel();
        pw.println("]");
        pw.println("\"" + element.getGeneral().getName() + "\":port" + " -- \"" + element.getSpecific().getName()
                + "\":port");
        return true;
    }

}
