package com.abstratt.mdd.core.util;

import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.NamedElement;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;

import static com.abstratt.mdd.core.util.MDDExtensionUtils.*;

public class AccessControlUtils {
	/**
	 * Finds the first access constraint in the given scopes that match the required capability and/or role class.
	 *    
	 * @param scopes
	 * @param capability optional capability to match
	 * @param roleClass optional role class to match
	 * @return the constraint found, or null
	 */
	public static Constraint findAccessConstraint(List<NamedElement> scopes, AccessCapability capability, Class roleClass) {
		return scopes.stream().map(it -> findAccessConstraint(it, capability, roleClass)).filter(it -> it != null).findFirst().orElse(null);
	}
	public static Constraint findAccessConstraint(NamedElement scope, AccessCapability capability, Class roleClass) {
		List<Constraint> accessConstraints = findAccessConstraints(scope);
		Stream<Constraint> matching = accessConstraints.stream().filter((it) -> {
			List<AccessCapability> allowedCapabilities = getAllowedCapabilities(it);
			List<Classifier> accessRoles = getAccessRoles(it);
			return (allowedCapabilities.isEmpty() || capability == null || allowedCapabilities.contains(capability)) && (accessRoles.isEmpty() || roleClass == null || accessRoles.stream().anyMatch(r -> ClassifierUtils.isKindOf(roleClass, r)));
		});
		return matching.reduce((a, b) -> {
			if (roleClass != null) {
				List<Classifier> accessRolesInA = getAccessRoles(a);
				List<Classifier> accessRolesInB = getAccessRoles(b);
				boolean explicitRoleClassInA = accessRolesInA.contains(roleClass);
				boolean explicitRoleClassInB = accessRolesInB.contains(roleClass);
				if (explicitRoleClassInA != explicitRoleClassInB)
					// choose where the role appeared explicitly
					return explicitRoleClassInA ? a : b;
			}
			if (capability != null) {
				List<AccessCapability> capabilitiesInA = getAllowedCapabilities(a);
				boolean explicitCapabilityInA = capabilitiesInA.contains(capability);
				List<AccessCapability> capabilitiesInB = getAllowedCapabilities(b);
				boolean explicitCapabilityInB = capabilitiesInB.contains(capability);
				if (explicitCapabilityInA != explicitCapabilityInB)
					// choose where the capability appeared explicitly
					return explicitCapabilityInA ? a : b;
			}
			// choose the last one otherwise
			return b;
		}).orElse(null);
	}
	
	public static Set<AccessCapability> allImplied(Set<AccessCapability> base) {
		Set<AccessCapability> result = new LinkedHashSet<>(base);
		base.forEach(it -> result.addAll(it.getImplied(false)));
		return result;
	}
}
