/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Relationship;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

/**
 * 
 */
public class PackageRenderer implements IEObjectRenderer<Package> {
	public boolean renderObject(Package allPackage, IndentedPrintWriter pw, IRenderingSession context) {
		EList<Element> ownedElements = allPackage.getOwnedElements();
		for (Element element : ownedElements)
			if (!(element instanceof Relationship))
				context.render(element);
		return true;
	}
}
