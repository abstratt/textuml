package com.abstratt.mdd.core.util

import java.util.Collection
import java.util.Map
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Classifier
import org.eclipse.uml2.uml.Constraint
import org.eclipse.uml2.uml.NamedElement

import static extension com.abstratt.mdd.core.util.ConstraintUtils.*
import static extension com.abstratt.mdd.core.util.MDDExtensionUtils.*
import static extension com.abstratt.mdd.core.util.ClassifierUtils.*
import java.util.List
import java.util.stream.Stream
import java.util.Set
import java.util.LinkedHashSet

class AccessControlUtils {
    static def Map<Classifier, Map<AccessCapability, Constraint>> computeConstraintsPerRoleClass(Collection<Class> allRoleClasses, Collection<NamedElement> accessConstraintContexts) {
        return computeConstraintsPerRoleClass(allRoleClasses, AccessCapability.values.toList, accessConstraintContexts)
    }
    static def Map<Classifier, Map<AccessCapability, Constraint>> computeConstraintsPerRoleClass(Collection<Class> allRoleClasses, Collection<AccessCapability> relevantCapabilities, Iterable<NamedElement> accessConstraintContexts) {
        val accessConstraintLayers = accessConstraintContexts.map[it.findConstraints.filter[access]]
        val Map<Classifier, Map<AccessCapability, Constraint>> constraintsPerRole = newLinkedHashMap()
        val concreteRoleClasses = allRoleClasses.filter[!it.abstract]
        accessConstraintLayers.forEach [ layer |
            layer.forEach [ constraint |
                val constraintAccessRoles = constraint.accessRoles
                val applicableRoles = if (constraintAccessRoles.empty) (allRoleClasses + #[null]) else concreteRoleClasses.filter[specific | specific.isKindOfAnyOf(constraintAccessRoles, true)]
                applicableRoles.forEach [ roleClass |
                    val previousConstraints = constraintsPerRole.remove(roleClass)
                    val constraintsByCapabilities = if (previousConstraints == null) newLinkedHashMap() else previousConstraints
                    constraintsPerRole.put(roleClass, constraintsByCapabilities)
                    val allowedCapabilities = constraint.allowedCapabilities
                    allowedCapabilities.filter[relevantCapabilities.contains(it)].forEach [ capability |
                        constraintsByCapabilities.put(capability, constraint)
                        capability.getImplied(false).forEach[ implied |
                            constraintsByCapabilities.putIfAbsent(implied, constraint)
                        ]
                    ]
                ]
            ]
        ]
        if (constraintsPerRole.containsKey(null) && constraintsPerRole.get(null).empty)
            constraintsPerRole.remove(null)
        return constraintsPerRole
    }
    
        
    def static boolean isAllTautologies(Map<? extends Classifier, Map<AccessCapability, Constraint>> constraintsPerRole, AccessCapability requiredCapability) {
        val allTautologies = !constraintsPerRole.empty && constraintsPerRole.values.forall[it.get(requiredCapability)?.tautology]
        return allTautologies
    }
    
    def static boolean hasAnyAccessConstraints(List<NamedElement> scopes) {
        return !scopes.stream().allMatch[it | findAccessConstraints(it).isEmpty()];
    }
    /**
     * Finds the first access constraint in the given scopes that match the required capability and/or role class.
     *    
     * @param scopes
     * @param capability optional capability to match
     * @param roleClass optional role class to match
     * @return the constraint found, or null
     */
    def static Constraint findAccessConstraint(List<? extends NamedElement> scopes, AccessCapability capability, Class roleClass) {
        return scopes.stream().map[it | findAccessConstraint(it, capability, roleClass)].filter[it | it != null].findFirst().orElse(null);
    }
    def static Constraint findAccessConstraint(NamedElement scope, AccessCapability capability, Class roleClass) {
        val List<Constraint> accessConstraints = findAccessConstraints(scope);
        return chooseAccessConstraint(accessConstraints, capability, roleClass);
    }
    
    def static Constraint chooseAccessConstraint(List<Constraint> accessConstraints, AccessCapability capability, Class roleClass) {
        val Stream<Constraint> matching = accessConstraints.stream().filter[it | 
            val List<AccessCapability> allowedCapabilities = getAllowedCapabilities(it);
            val List<Classifier> accessRoles = getAccessRoles(it);
            return (allowedCapabilities.isEmpty() || capability == null || allowedCapabilities.contains(capability)) && (accessRoles.isEmpty() || roleClass == null || accessRoles.stream().anyMatch[r | ClassifierUtils.isKindOf(roleClass, r)]);
        ];
        return matching.reduce[a, b |
            if (roleClass != null) {
                val List<Classifier> accessRolesInA = getAccessRoles(a);
                val List<Classifier> accessRolesInB = getAccessRoles(b);
                val boolean explicitRoleClassInA = accessRolesInA.contains(roleClass);
                val boolean explicitRoleClassInB = accessRolesInB.contains(roleClass);
                if (explicitRoleClassInA != explicitRoleClassInB)
                    // choose where the role appeared explicitly
                    return if (explicitRoleClassInA) a else b;
            }
            if (capability != null) {
                val List<AccessCapability> capabilitiesInA = getAllowedCapabilities(a);
                val boolean explicitCapabilityInA = capabilitiesInA.contains(capability);
                val List<AccessCapability> capabilitiesInB = getAllowedCapabilities(b);
                val boolean explicitCapabilityInB = capabilitiesInB.contains(capability);
                if (explicitCapabilityInA != explicitCapabilityInB)
                    // choose where the capability appeared explicitly
                    return if (explicitCapabilityInA) a else b;
            }
            // choose the last one otherwise
            return b;
        ].orElse(null);
    }   
    
    def static Set<AccessCapability> allImplied(Set<AccessCapability> base) {
        val Set<AccessCapability> result = new LinkedHashSet(base);
        base.forEach[result.addAll(it.getImplied(false))];
        return result;
    }
    
}