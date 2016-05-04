package com.abstratt.mdd.core.util;

import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.Profile;

public class PackageUtils {
    public static boolean isModelLibrary(Package toCheck) {
        return StereotypeUtils.hasStereotype(toCheck, "StandardProfile" + NamedElement.SEPARATOR + "ModelLibrary");
    }
    
    public static void safeApplyProfile(org.eclipse.uml2.uml.Package package_, Profile profile) {
    	if (package_.isProfileApplied(profile))
    		return;
    	package_.applyProfile(profile);
    }

	public static PackageImport importPackage(Package importer, Package imported) {
		return importer.getPackageImport(imported, true);
	}

}
