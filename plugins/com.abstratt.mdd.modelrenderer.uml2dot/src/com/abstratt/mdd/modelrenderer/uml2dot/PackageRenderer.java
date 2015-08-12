/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Relationship;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

/**
 * 
 */
public class PackageRenderer implements IElementRenderer<Package> {
	public boolean renderObject(Package allPackage, IndentedPrintWriter pw, IRenderingSession context) {
		EList<Element> ownedElements = allPackage.getOwnedElements();
		Stream<Element> renderable = ownedElements.stream().filter(it -> !(it instanceof Relationship));
		boolean[] anyRendered = { false };
		renderable.forEach(it -> anyRendered[0] |= context.render((EObject) it));
		return anyRendered[0];
	}
}
