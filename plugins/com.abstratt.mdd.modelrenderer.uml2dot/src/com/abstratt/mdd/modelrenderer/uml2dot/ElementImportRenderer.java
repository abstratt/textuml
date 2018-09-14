package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class ElementImportRenderer implements IElementRenderer<ElementImport>{

	@Override
	public boolean renderObject(ElementImport element, IndentedPrintWriter out, IRenderingSession session) {
		if (MDDExtensionUtils.isLibrary(element.getNearestPackage()))
			return false;
		if (element.getVisibility() != VisibilityKind.PUBLIC_LITERAL)
			return false;
		return session.render(element.getImportedElement());
	}

}
