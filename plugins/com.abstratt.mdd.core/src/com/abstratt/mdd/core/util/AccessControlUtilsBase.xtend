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


class AccessControlUtilsBase {
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
}