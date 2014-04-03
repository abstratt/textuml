package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.DataType;

import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class DataTypeRenderer extends ClassifierRenderer<DataType> {

	@Override
	protected void renderNameAdornments(DataType element, IndentedPrintWriter w, IRenderingSession context) {
		w.print("<TR><TD>");
		w.print(UML2DOTRenderingUtils.addGuillemots("dataType"));
		w.print("</TD></TR>");
		super.renderNameAdornments(element, w, context);
	}
}
