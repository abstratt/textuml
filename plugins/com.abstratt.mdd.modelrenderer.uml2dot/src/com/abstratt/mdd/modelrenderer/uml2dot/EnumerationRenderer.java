package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_ENUMERATIONS;

import java.util.List;

import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Generalization;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class EnumerationRenderer implements IElementRenderer<Enumeration> {
	public boolean renderObject(Enumeration element, IndentedPrintWriter w, IRenderingSession context) {
		if (!context.getSettings().getBoolean(SHOW_ENUMERATIONS))
			return false;

		w.println("// enum " + element.getQualifiedName());
		w.println('"' + element.getName() + "\" [");
		w.println("label=<");
		w.enterLevel();
		w.println("<TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\" cellborder=\"0\" port=\"port\">");
		w.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"3\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
		w.println("<TR><TD>");
		w.println(UML2DOTRenderingUtils.addGuillemots("enumeration"));
		w.println("</TD></TR>");
		w.print("<TR><TD>");
		w.print(element.getName());
		w.println("</TD></TR>");
		w.println("</TABLE></TD></TR>");

		if (!element.getAttributes().isEmpty() && !context.isShallow()) {
			w.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" ALIGN=\"LEFT\">");
			RenderingUtils.renderAll(context, element.getAttributes());
			w.println("</TABLE></TD></TR>");
		}
		if (!element.getOperations().isEmpty() && !context.isShallow()) {
			w.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" align=\"left\">");
			RenderingUtils.renderAll(context, element.getOperations());
			w.println("</TABLE></TD></TR>");
		}
		if (!element.getOwnedLiterals().isEmpty() && !context.isShallow()) {
			w.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" align=\"left\">");
			RenderingUtils.renderAll(context, element.getOwnedLiterals());
			w.println("</TABLE></TD></TR>");
		}
		w.exitLevel();
		w.println("</TABLE>>];");
		List<Generalization> generalizations = element.getGeneralizations();
		RenderingUtils.renderAll(context, generalizations);
		return true;
	}

}
