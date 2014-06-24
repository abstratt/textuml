/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/ 
package com.abstratt.mdd.frontend.textuml.renderer;

import java.io.OutputStream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.modelrenderer.IRendererSelector;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IRenderingSettings;
import com.abstratt.modelrenderer.IndentedPrintWriter;
import com.abstratt.modelrenderer.RendererSelector;
import com.abstratt.modelrenderer.RenderingSession;

public class TextUMLRenderer {
	public static final String PLUGIN_ID = TextUMLRenderer.class.getPackage().getName();
	
	private static final EClass ELEMENT_CLASS = UMLPackage.eINSTANCE.getElement();

	public void render(Resource resource, OutputStream stream) {
		IRendererSelector<?, ?> selector = new RendererSelector(getClass().getPackage().getName(), ELEMENT_CLASS) {};
		IndentedPrintWriter out = new IndentedPrintWriter(stream);
		IRenderingSession session = new RenderingSession(selector, IRenderingSettings.NO_SETTINGS, out);
		session.renderAll(resource.getContents());
		out.close();
	}
}
