package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.modelrenderer.RendererSelector;

public class TextUMLRendererSelector extends RendererSelector<Element> {
    public TextUMLRendererSelector() {
        super(TextUMLRendererSelector.class.getPackage().getName(), UMLPackage.eINSTANCE.getElement());
    }
}
