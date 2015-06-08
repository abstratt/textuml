package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.EnumerationLiteral;

import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class EnumerationLiteralRenderer implements IElementRenderer<EnumerationLiteral> {

	public boolean renderObject(EnumerationLiteral literal, IndentedPrintWriter w, IRenderingSession context) {
		w.print("<TR><TD align=\"left\">"); 
		w.print(literal.getName());
		w.println("</TD></TR>");
		return true;
	}

}
