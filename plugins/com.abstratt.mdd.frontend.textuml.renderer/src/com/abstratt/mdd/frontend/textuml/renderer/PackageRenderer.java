/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.frontend.textuml.renderer;

import static com.abstratt.mdd.frontend.textuml.renderer.TextUMLRenderingUtils.name;
import static com.abstratt.mdd.frontend.textuml.renderer.TextUMLRenderingUtils.qualifiedName;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.ProfileApplication;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;

/**
 * 
 */
public class PackageRenderer implements IEObjectRenderer<Package> {
    public boolean renderObject(Package package_, IndentedPrintWriter pw, IRenderingSession context) {
        renderPrologue(package_, pw, context);
        pw.println();

        List<ProfileApplication> profileApplications = package_.getProfileApplications();
        if (!profileApplications.isEmpty()) {
            RenderingUtils.renderAll(context, profileApplications);
            pw.println();
        }

        List<PackageImport> packageImports = package_.getPackageImports();
        if (!packageImports.isEmpty()) {
            RenderingUtils.renderAll(context, packageImports);
            pw.println();
        }

        List<ElementImport> elementImports = package_.getElementImports();
        if (!elementImports.isEmpty()) {
            RenderingUtils.renderAll(context, elementImports);
            pw.println();
        }

        final Collection<Classifier> subPackages = EcoreUtil.getObjectsByType(package_.getOwnedElements(),
                UMLPackage.Literals.PACKAGE);
        RenderingUtils.renderAll(context, subPackages);

        final Collection<Classifier> classifiers = EcoreUtil.getObjectsByType(package_.getOwnedElements(),
                UMLPackage.Literals.CLASSIFIER);
        RenderingUtils.renderAll(context, classifiers);

        renderEpilogue(package_, pw, context);
        return true;
    }

    public void renderPrologue(Package package_, IndentedPrintWriter pw, IRenderingSession context) {
        TextUMLRenderingUtils.renderStereotypeApplications(pw, package_);
        RenderingUtils.renderAll(context, ElementUtils.getComments(package_));
        if (package_.getOwner() != null) {
            pw.println(getPackageTypeName(package_) + " " + name(package_) + ";");
            pw.enterLevel();
        } else
            pw.println(getPackageTypeName(package_) + " " + qualifiedName(package_) + ";");
    }

    protected String getPackageTypeName(Package package_) {
        return package_.eClass().getName().toLowerCase();
    }

    public void renderEpilogue(Package package_, IndentedPrintWriter pw,
            @SuppressWarnings("unused") IRenderingSession context) {
        if (package_.getOwner() != null)
            pw.exitLevel();
        pw.print("end");
        if (package_.getOwner() == null)
            pw.println(".");
        else {
            pw.println(";");
            pw.println();
        }
    }
}
