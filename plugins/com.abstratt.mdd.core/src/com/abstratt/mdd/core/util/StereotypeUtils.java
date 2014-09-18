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
package com.abstratt.mdd.core.util;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.MDDCore;

public class StereotypeUtils {
	public static boolean hasStereotype(Element element, String stereotypeQName) {
		for (Stereotype s : element.getAppliedStereotypes())
			if (stereotypeQName.equals(s.getName()) || stereotypeQName.equals(s.getQualifiedName()))
				return true;
		if (element instanceof Classifier)
			for (Classifier general : ((Classifier) element).getGenerals())
				if (hasStereotype(general, stereotypeQName))
					return true;
		return false;
	}
	
	public static Stereotype getStereotype(Element element, String stereotypeQName) {
		for (Stereotype s : element.getAppliedStereotypes())
			if (stereotypeQName.equals(s.getName()) || stereotypeQName.equals(s.getQualifiedName()))
				return s;
		if (element instanceof Classifier)
			for (Classifier general : ((Classifier) element).getGenerals()) {
				Stereotype found = getStereotype(general, stereotypeQName);
				if (found != null)
					return found;
			}
		return null;
	}
	
	public static boolean hasProfile(org.eclipse.uml2.uml.Package element, String profileName) {
		for (Profile p : element.getAppliedProfiles())
			if (p.getName().equals(profileName))
				return true;
		return false;
	}

	public static Stereotype findStereotype(String stereotypeName) {
		return MDDCore.getInProgressRepository().findNamedElement(stereotypeName, Literals.STEREOTYPE, null);
	}
	
	public static void safeApplyStereotype(Element e, Stereotype s) {
		if (!e.isStereotypeApplied(s))
			e.applyStereotype(s); 
	}

    public static boolean isApplicable(Element element, String stereotypeQName) {
        Stereotype stereotype = findStereotype(stereotypeQName);
        return stereotype != null && element.isStereotypeApplicable(stereotype);
    }
}