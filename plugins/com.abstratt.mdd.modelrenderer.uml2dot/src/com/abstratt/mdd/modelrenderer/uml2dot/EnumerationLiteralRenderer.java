package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.EnumerationLiteral;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class EnumerationLiteralRenderer implements IElementRenderer<EnumerationLiteral> {

	public boolean renderObject(EnumerationLiteral literal, IndentedPrintWriter w, IRenderingSession context) {
		w.print("<TR><TD align=\"left\">"); 
		w.print(literal.getName());
		w.println("</TD></TR>");
		return true;
	}

}
