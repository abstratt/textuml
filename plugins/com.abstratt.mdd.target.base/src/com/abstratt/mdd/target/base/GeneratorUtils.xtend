package com.abstratt.mdd.target.base

import java.util.Collection
import org.eclipse.uml2.uml.UMLPackage
import org.eclipse.uml2.uml.Type
import java.util.List
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.Class
import org.eclipse.emf.ecore.EClass
import org.eclipse.uml2.uml.Enumeration
import org.eclipse.uml2.uml.StateMachine

class GeneratorUtils {
    def static <I> CharSequence generateMany(Iterable<I> items, (I)=>CharSequence mapper) {
        items.generateMany(mapper, '\n')
    }

    def static <I> CharSequence generateMany(Iterable<I> items, (I)=>CharSequence mapper, String separator) {
        return items.generateMany(separator, mapper)
    }
    def static <I> CharSequence generateMany(Iterable<I> items, String separator, (I)=>CharSequence mapper) {
        return items.generateMany(true, separator, mapper)
    }
    def static <I> CharSequence generateMany(Iterable<I> items, boolean trim, String separator, (I)=>CharSequence mapper) {
    	val toString = if (trim) [ it.toString.trim ] else [ it.toString.trim ] 
        return items.map[toString.apply(mapper.apply(it))].join(separator)
    }
	
	def static List<Enumeration> getEnumerations(Collection<Package> appPackages) {
		return getTypes(appPackages, UMLPackage.Literals.ENUMERATION).map[it as Enumeration]
	}
	
	def static List<StateMachine> getStateMachines(Collection<Package> appPackages) {
		return getTypes(appPackages, UMLPackage.Literals.CLASS).map[it as Class].map[it.ownedBehaviors.filter(StateMachine)].flatten.toList
	}
	
    def static <T extends Type>  List<T> getTypes(Collection<Package> packages, EClass type) {
        val List<T> result = newArrayList()
        for (Package current : packages) {
            for (Type it : current.getOwnedTypes())
                if (it.eClass == type)
                    result.add(it as T)
            result.addAll(getTypes(current.getNestedPackages(), type))
        }
        return result
    }
	
    
}