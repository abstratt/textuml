package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Element;

import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public interface IElementRenderer<E extends Element> extends IEObjectRenderer<E> {
	@Override
	public boolean renderObject(E element, IndentedPrintWriter out, IRenderingSession session);
}
