package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.AddStructuralFeatureValueAction;
import org.eclipse.uml2.uml.InputPin;

import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;

public class AddStructuralFeatureValueActionRenderer implements IEObjectRenderer<AddStructuralFeatureValueAction> {
	public boolean renderObject(AddStructuralFeatureValueAction element,
	        com.abstratt.mdd.modelrenderer.IndentedPrintWriter out, IRenderingSession session) {
		InputPin target = element.getObject();
		return true;
	}

}
