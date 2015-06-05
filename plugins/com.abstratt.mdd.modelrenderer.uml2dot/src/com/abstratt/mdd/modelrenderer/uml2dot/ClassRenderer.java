package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.List;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.InterfaceRealization;

import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class ClassRenderer extends ClassifierRenderer<Class> {

    
    @Override
    protected void renderClassifierTypeAdornment(Class element, IndentedPrintWriter w, IRenderingSession session) {
        // classes are the real deal, they do not need an adornment
    }
	@Override
	protected void renderRelationships(Class element,
			IRenderingSession context) {
		super.renderRelationships(element, context);
		List<InterfaceRealization> realizations = element
				.getInterfaceRealizations();
		context.renderAll(realizations);
	}
}
