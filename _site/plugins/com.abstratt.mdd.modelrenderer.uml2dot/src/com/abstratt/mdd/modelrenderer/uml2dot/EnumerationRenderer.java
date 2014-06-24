package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.List;

import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Generalization;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class EnumerationRenderer implements IEObjectRenderer<Enumeration> {
	public boolean renderObject(Enumeration element, IndentedPrintWriter w, IRenderingSession context) {
		w.println("// enum " + element.getQualifiedName());
		w.println('"' + element.getName() + "\" [");
		w.println("label=<");
		w.enterLevel();
		w
				.println("<TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\" cellborder=\"0\" port=\"port\">");
		w
				.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"3\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
		w.println("<TR><TD>");
		w.println(UML2DOTRenderingUtils.addGuillemots("enumeration"));
		w.println("</TD></TR>");
		w.print("<TR><TD>");
		w.print(element.getName());
		w.println("</TD></TR>");
		w.println("</TABLE></TD></TR>");

		if (!element.getAttributes().isEmpty() && !context.isShallow()) {
			w
					.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" ALIGN=\"LEFT\">");
			context.renderAll(element.getAttributes());
			w.println("</TABLE></TD></TR>");
		}
		if (!element.getOperations().isEmpty() && !context.isShallow()) {
			w
					.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" align=\"left\">");
			context.renderAll(element.getOperations());
			w.println("</TABLE></TD></TR>");
		}
		if (!element.getOwnedLiterals().isEmpty() && !context.isShallow()) {
			w
					.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" align=\"left\">");
			context.renderAll(element.getOwnedLiterals());
			w.println("</TABLE></TD></TR>");
		}
		w.exitLevel();
		w.println("</TABLE>>];");
		List<Generalization> generalizations = element.getGeneralizations();
		context.renderAll(generalizations);
		return true;
	}

}
