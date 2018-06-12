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
import com.abstratt.mdd.modelrenderer.RenderingUtils;

/**
 * 
 */
public class PackageRenderer implements IElementRenderer<Package> {
    public boolean renderObject(Package element, IndentedPrintWriter out, IRenderingSession session) {
    	
        out.println("compound = true;");
        out.println("subgraph \"cluster_" + element.getName() + "\" {");
        out.enterLevel();
        out.println("graph[");
        out.enterLevel();
        out.println("style=\"rounded\"; color=\"grey\";");
        out.exitLevel();
        out.println("];");
        out.println("label = \"" + element.getQualifiedName() + "\";");
        out.println("labeljust = \"l\";");
        boolean rendered = RenderingUtils.renderAll(session, element.getOwnedElements());
        out.exitLevel();
        out.println("}");

        return rendered;
    }

	private boolean renderPackageContents(Package allPackage, IRenderingSession context) {
		EList<Element> ownedElements = allPackage.getOwnedElements();
        Stream<Element> renderable = ownedElements.stream().filter(it -> !(it instanceof Relationship));
        boolean[] anyRendered = { false };
        renderable.forEach(it -> anyRendered[0] |= context.render((EObject) it));
        return anyRendered[0];
	}
}
