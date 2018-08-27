package com.abstratt.mdd.modelrenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

public class RenderingUtils {
    public static <E extends EObject, C extends Collection<E>> boolean renderAll(IRenderingSession session, C toRender) {
        List<E> toRenderAsList = new ArrayList<>(toRender);
        boolean[] anyRendered = { false };
        toRenderAsList.forEach(it -> anyRendered[0] |= session.render(it, session.isShallow()));
        return anyRendered[0];
    }
}
