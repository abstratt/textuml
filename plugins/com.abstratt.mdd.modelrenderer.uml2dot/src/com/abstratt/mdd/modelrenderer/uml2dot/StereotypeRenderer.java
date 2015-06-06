package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.Property;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;
import com.abstratt.modelrenderer.RenderingUtils;

public class StereotypeRenderer implements IElementRenderer<Class> {

	public boolean renderObject(Class element, IndentedPrintWriter w,
			IRenderingSession context) {
		w.println("// stereotype " + element.getQualifiedName());
		w.print('"' + element.getName() + "\" [");
		w.println("label=<");
		w.enterLevel();
		w
				.println("<TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\" cellborder=\"0\" port=\"port\">");
		w
				.print("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"3\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
		w.print("<TR><TD>");
		w.print(UML2DOTRenderingUtils.addGuillemots("stereotype"));
		w.print("</TD></TR>");
		w.print("<TR><TD>");
		w.print(element.getName());
		w.print("</TD></TR>");
		if (context.isShallow()) {
			w.print("<TR><TD>");
			w.print("(from " + element.getNearestPackage().getQualifiedName()
					+ ")");
			w.print("</TD></TR>");
		}
		w.print("</TABLE></TD></TR>");

		List<Property> properties = new ArrayList<Property>();
		for (Property property : element.getOwnedAttributes())
			if (!(property.getAssociation() instanceof Extension))
				properties.add(property);
		if (!properties.isEmpty() && !context.isShallow()) {
			w
					.print("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" ALIGN=\"LEFT\">");
			RenderingUtils.renderAll(context, properties);
			w.print("</TABLE></TD></TR>");
		}
		if (!element.getOperations().isEmpty() && !context.isShallow()) {
			w
					.print("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" align=\"left\">");
			RenderingUtils.renderAll(context, element.getOperations());
			w.print("</TABLE></TD></TR>");
		}
		w.exitLevel();
		w.println("</TABLE>>];");
		w.enterLevel();
		List<Generalization> generalizations = element.getGeneralizations();
		RenderingUtils.renderAll(context, generalizations);
		List<InterfaceRealization> realizations = element
				.getInterfaceRealizations();
		RenderingUtils.renderAll(context, realizations);
		// render extensions
		for(Property property : element.getOwnedAttributes())
			if (property.getAssociation() instanceof Extension) {
				Extension extension = (Extension) property.getAssociation(); 
				context.render(extension, false);
			}		
		return true;
	}
}
