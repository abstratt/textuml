/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Reception;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

/**
 * 
 */
public class ReceptionRenderer implements IElementRenderer<Reception> {
	public boolean renderObject(Reception reception, IndentedPrintWriter w, IRenderingSession context) {
		w.print("<TR><TD align=\"left\">");
		w.print(UML2DOTRenderingUtils.addGuillemots("signal"));
		w.print(" ");
		w.print(reception.getSignal().getName());
		w.print("</TD></TR>");
		return true;
	}
}
