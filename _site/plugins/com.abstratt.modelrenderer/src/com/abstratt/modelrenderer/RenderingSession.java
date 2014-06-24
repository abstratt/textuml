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
package com.abstratt.modelrenderer;

import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

@SuppressWarnings("unchecked")
public class RenderingSession implements IRenderingSession {
	private IRendererSelector selector;
	private IndentedPrintWriter writer;
	private Map<EObject, Object> rendered = new WeakHashMap<EObject, Object>();
	private Stack<EObject> stack = new Stack<EObject>();
	private boolean shallow = false;
	private IRenderingSettings settings;

	public RenderingSession(IRendererSelector selector, IRenderingSettings settings,
			IndentedPrintWriter writer) {
		this.selector = selector;
		this.writer = writer;
		this.settings = settings;
	}
	
	@Override
	public IRenderingSettings getSettings() {
		return settings;
	}
	
	public <T extends java.util.Collection<? extends EObject>> void renderAll(T toRender) {
		for (EObject object : toRender)
			render((EObject) object, shallow);
	}

	public void render(EObject toRender) {
		render(toRender, false);
	}

	public void render(EObject toRender, boolean newShallow) {
//		if (this.shallow)
//			return;
		if (rendered.put(toRender, "") != null)
			return;
		stack.push(toRender);
		boolean originalShallow = this.shallow;
		this.shallow = newShallow;
		try {
			IRenderer renderer = selector.select(toRender);
			if (renderer != null)
				if (!renderer.renderObject(toRender, writer, this))
					rendered.remove(toRender);
		} finally {
			this.shallow = originalShallow;
			stack.pop();
		}
	}
	public boolean isShallow() {
		return shallow;
	}
	
	public EObject getRoot() {
		return stack.isEmpty() ? null : stack.get(0);
	}
	
	public EObject getCurrent() {
		return stack.isEmpty() ? null : stack.peek();
	}
	
	public EObject getPrevious(EClass eClass) {
		if (stack.size() <= 1)
			return null;
		int start = stack.size() - 2;
		for (int i = start; i >= 0; i--) {
			EObject current = stack.get(i);
			if (eClass.isInstance(current))
				return current;
		}
		return null;
	}

}