package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowMinimumVisibilityOptions;

public class RendererHelper {
	public static boolean shouldSkip(IRenderingSession context, NamedElement element) {
		switch (context.getSettings()
                .getSelection(ShowMinimumVisibilityOptions.class)) {
        case Private:
        	break;
        case Protected:
        	if (element.getVisibility() == VisibilityKind.PRIVATE_LITERAL)
        		return true;
        default:
        	if (element.getVisibility() != VisibilityKind.PUBLIC_LITERAL)
        		return true;
        }
		
		return false;
	}
}
