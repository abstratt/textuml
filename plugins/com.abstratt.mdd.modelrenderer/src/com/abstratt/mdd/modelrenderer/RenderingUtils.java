package com.abstratt.mdd.modelrenderer;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;

public class RenderingUtils {
	public static <E extends EObject, C extends Collection<E>> boolean renderAll(IRenderingSession session, C toRender) {
		boolean[] anyRendered = { false };
		toRender.forEach(it -> anyRendered[0] |= session.render(it, session.isShallow()));
		return anyRendered[0];
	}
}
