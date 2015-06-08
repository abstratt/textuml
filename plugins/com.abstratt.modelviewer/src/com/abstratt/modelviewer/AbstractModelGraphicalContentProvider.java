/*******************************************************************************
 * Copyright (c) 2007 EclipseGraphviz contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     abstratt technologies
 *******************************************************************************/
package com.abstratt.modelviewer;

 import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.abstratt.graphviz.ui.DOTGraphicalContentProvider;
import com.abstratt.mdd.modelrenderer.dot.DOTRendering;
import com.abstratt.modelrenderer.IRendererSelector;
import com.abstratt.modelrenderer.IRenderingSettings;

public abstract class AbstractModelGraphicalContentProvider extends
		DOTGraphicalContentProvider {

	protected abstract IRendererSelector<? extends EObject> getRendererSelector();

	/*
	 * (non-Javadoc)
	 * @see com.abstratt.graphviz.ui.DOTGraphicalContentProvider#loadImage(org.eclipse.swt.widgets.Display, org.eclipse.swt.graphics.Point, java.lang.Object)
	 */
	public Image loadImage(Display display, Point desiredSize, Object newInput) throws CoreException {
		if (newInput == null)
			// for a reload, it will be null
			return new Image(display, 1, 1);
		byte[] dotContents = DOTRendering.generateDOTFromModel((URI) newInput, getRendererSelector(), getSettings());
		if (dotContents != null)
			return super.loadImage(display, desiredSize, dotContents);
		return new Image(display, 1, 1);
	}
	
	@Override
	public void saveImage(Display display, Point suggestedSize, Object input, IPath outputLocation, int fileFormat)
					throws CoreException {
		byte[] dotContents = DOTRendering.generateDOTFromModel((URI) input, getRendererSelector(), getSettings());
		if (dotContents == null)
			dotContents = new byte[0];
		super.saveImage(display, suggestedSize, dotContents, outputLocation, fileFormat);
	}

	protected IRenderingSettings getSettings() {
		return IRenderingSettings.NO_SETTINGS;
	}
}
