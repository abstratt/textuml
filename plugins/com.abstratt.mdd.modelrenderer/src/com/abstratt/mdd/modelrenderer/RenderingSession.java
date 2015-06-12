/*******************************************************************************
 * Copyright (c) 2006, 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.modelrenderer;

import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class RenderingSession<E extends EObject> implements IRenderingSession<E> {
    private IRendererSelector<E> selector;
    private IndentedPrintWriter writer;
    private Map<EObject, Object> rendered = new WeakHashMap<EObject, Object>();
    private Stack<E> stack = new Stack<E>();
    private boolean shallow = false;
    private IRenderingSettings settings;

    public RenderingSession(IRendererSelector<E> selector, IRenderingSettings settings, IndentedPrintWriter writer) {
        this.selector = selector;
        this.writer = writer;
        this.settings = settings;
    }

    @Override
    public IRenderingSettings getSettings() {
        return settings;
    }

    @Override
    public boolean render(E toRender) {
        return render(toRender, false);
    }

    @Override
    public boolean render(E toRender, boolean newShallow) {
        // if (this.shallow)
        // return;
        if (rendered.put(toRender, "") != null)
            // already rendered
            return false;
        stack.push(toRender);
        boolean originalShallow = this.shallow;
        this.shallow = newShallow;
        try {
            boolean actuallyRendered = false;
            IRenderer<E> renderer = selector.select(toRender);
            if (renderer != null) {
                actuallyRendered = renderer.renderObject(toRender, writer, this);
                if (!actuallyRendered)
                    rendered.remove(toRender);
            }
            return actuallyRendered;
        } finally {
            this.shallow = originalShallow;
            stack.pop();
        }
    }

    public boolean isShallow() {
        return shallow;
    }

    public E getRoot() {
        return stack.isEmpty() ? null : stack.get(0);
    }

    public E getCurrent() {
        return stack.isEmpty() ? null : stack.peek();
    }

    public E getPrevious(EClass eClass) {
        if (stack.size() <= 1)
            return null;
        int start = stack.size() - 2;
        for (int i = start; i >= 0; i--) {
            E current = stack.get(i);
            if (eClass.isInstance(current))
                return current;
        }
        return null;
    }

}