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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.abstratt.mdd.modelrenderer.IRendererSelector;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IRenderingSettings;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingSession;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

public class TextUMLRenderer {
    public static final String PLUGIN_ID = TextUMLRenderer.class.getPackage().getName();

    public void render(Resource resource, OutputStream stream) {
        IRendererSelector<EObject> selector = new TextUMLRendererSelector();
        IndentedPrintWriter out = new IndentedPrintWriter(stream);
        IRenderingSession session = new RenderingSession(selector, IRenderingSettings.NO_SETTINGS, out);
        RenderingUtils.renderAll(session, resource.getContents());
        out.close();
    }
}
