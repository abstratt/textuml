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
    
    /**
     * Computes the actual constraints in effect for each role/capability at the given context hierarchy.
     * 
     * @param allRoleClasses the role classes to consider (use a null element for anonymous users)
     * @param relevantCapabilities the capabilities to consider
     * @param accessConstraintContexts the constraint contexts, from wider to narrower (example: [class, operation])
     * @return a constraint per capability per role
     */
    static def Map<Classifier, Map<AccessCapability, Constraint>> computeConstraintsPerRoleClass(
    	Collection<Class> allRoleClasses, 
    	Collection<AccessCapability> relevantCapabilities, 
    	Iterable<NamedElement> accessConstraintContexts
    ) {
    	val canonicalCapabilities = AccessCapability.values.filter[relevantCapabilities.contains(it)].toSet
        val accessConstraintLayers = accessConstraintContexts.map[it.findConstraints.filter[access]]
        val Map<Classifier, Map<AccessCapability, Constraint>> result = newLinkedHashMap()
        val concreteRoleClasses = allRoleClasses.filter[!it.abstract]
        // loop through every layer (outer to inner), composing the final set of constraints per role
        accessConstraintLayers.forEach [ layer |
        	val Map<Classifier, Map<AccessCapability, Constraint>> layerConstraintsPerRole = 
        		newLinkedHashMap()
            layer.forEach [ constraint |
            	// note that multiple constraints may apply to the same role (but with different capabilities)
                val constraintAccessRoles = constraint.accessRoles
                val applicableRoles = if (constraintAccessRoles.empty) 
                	// the constraint specifies no roles, consider all roles
                	(allRoleClasses + #[null]) 
            	else 
            	    // consider only concrete roles - those specified in the constraint, and any subclasses
            		concreteRoleClasses.filter[specific | specific.isKindOfAnyOf(constraintAccessRoles, true)]
                // for each applicable role, compute the constraint per capability at this layer)
                // (note that multiple constraints may apply to a role/capability (due to role inheritance)
                applicableRoles.forEach [ roleClass |
                	// we want to keep track of all constraints in the current layer
                	// at the capability level, but preserve those already defined in the current layer
                    val previousConstraints = layerConstraintsPerRole.remove(roleClass)
                    val constraintsByCapabilities = if (previousConstraints == null) newLinkedHashMap() else previousConstraints
                    layerConstraintsPerRole.put(roleClass, constraintsByCapabilities)
                    val allowedCapabilities = constraint.allowedCapabilities
                    allowedCapabilities.forEach [ capability |
                    	if (canonicalCapabilities.contains(capability))
                    		constraintsByCapabilities.put(capability, constraint)
                    ]
                ]
            ]
            // now merge the constraints obtained for this layer with the ones found for outer layers
            // replacing any constraints we found before on a role/capability basis 
            layerConstraintsPerRole.forEach[roleClass, constraintsPerCapability |
            	val merged = result.computeIfAbsent(roleClass, [ newLinkedHashMap() ])
            	if (constraintsPerCapability.isEmpty) {
            		// an inner layer constraint may define no capabilities, overriding any previous layers
            		merged.clear
            	} else {
            		// let's combine the new capabilities with any previous ones
	            	constraintsPerCapability.forEach[capability, constraint |
	            		// replace any existing constraint defined by an outer layer
    	        		merged.put(capability, constraint)
        	    	]
            	}
            ]
        ]
        if (result.containsKey(null))
            if (result.get(null).empty)
                result.remove(null)
        // finally, apply implied capabilities
        result.forEach[roleClass, capabilityConstraints |
        	val explicitCapabilities = capabilityConstraints.keySet.toList
        	explicitCapabilities.forEach[ capability |
        		val explicitConstraint = capabilityConstraints.get(capability)
            	capability.getImplied(false).forEach[ implied |
                    capabilityConstraints.putIfAbsent(implied, explicitConstraint)
                ]	
        	]
        ]
        return result
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