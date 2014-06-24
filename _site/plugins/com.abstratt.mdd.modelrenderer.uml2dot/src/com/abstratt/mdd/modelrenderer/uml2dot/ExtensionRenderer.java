package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.Stereotype;

import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class ExtensionRenderer extends AbstractRelationshipRenderer<Extension> {

	@Override
	protected boolean basicRenderObject(Extension element, IndentedPrintWriter pw, IRenderingSession context) {
		final Stereotype stereotype = element.getStereotype();
		final Class metaclass = element.getMetaclass();
		
		if (!shouldRender(context, stereotype, metaclass))
			return true;
		
		context.render(stereotype, stereotype.eResource() != element.eResource());
		context.render(metaclass, metaclass.eResource() != element.eResource());

		pw.print("edge ");
		// if (element.getName() != null)
		// pw.print("\"" + element.getName() + "\" ");
		pw.println("[");
		pw.enterLevel();
		pw.println("arrowtail = \"none\"");
		pw.println("arrowhead = \"normal\"");
		pw.println("taillabel = \"\"");
		pw.println("headlabel = \"\"");
		DOTRenderingUtils.addAttribute(pw, "constraint", "true");
		pw.println("style = \"none\"");
		pw.exitLevel();
		pw.println("]");
		pw.println(stereotype.getName() + ":port -- " + metaclass.getName() + ":port");
		return true;
	}

}
