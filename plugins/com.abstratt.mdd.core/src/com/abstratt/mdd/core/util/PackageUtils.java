package com.abstratt.mdd.core.util;

import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;

public class PackageUtils {
	public static boolean isModelLibrary(Package toCheck) {
		return StereotypeUtils.hasStereotype(toCheck, "StandardProfile" + NamedElement.SEPARATOR + "ModelLibrary");
	}
}
