package com.abstratt.mdd.target.base

import com.abstratt.mdd.core.IRepository
import java.util.Collection
import org.eclipse.uml2.uml.Action
import org.eclipse.uml2.uml.CallOperationAction
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.NamedElement
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.ReadLinkAction
import org.eclipse.uml2.uml.ReadStructuralFeatureAction

import static extension com.abstratt.kirra.mdd.core.KirraHelper.*
import static extension com.abstratt.mdd.core.util.ActivityUtils.*
import com.abstratt.kirra.mdd.core.KirraHelper

abstract class AbstractGenerator {
    protected IRepository repository

    protected String applicationName

    protected Iterable<Class> entities
    
    protected Collection<Package> appPackages
    
    new(IRepository repository) {
        this.repository = repository
        if (repository != null) {
            this.appPackages = repository.getTopLevelPackages(null).applicationPackages
            this.entities = appPackages.entities.filter[topLevel]
            this.applicationName = KirraHelper.getApplicationName(repository)
        }
    }
    
    def String toJavaPackage(Package package_) {
        package_.qualifiedName.replace(NamedElement.SEPARATOR, ".")
    }
    
    
    def boolean isCollectionOperation(Action toCheck) {
        if (toCheck instanceof CallOperationAction)
            return toCheck.target != null && toCheck.target.multivalued
        return false
    }
    
    def boolean isOperation(Action toCheck, String typeName, String... operationNames) {
        if (toCheck instanceof CallOperationAction)
        	return operationNames.contains(toCheck.operation.name)
        return false
    } 
    
     def boolean isPlainCollectionOperation(Action action) {
        if (!action.collectionOperation)
            return false
        val asCallAction = action as CallOperationAction
        val sourceAction = asCallAction.target.sourceAction
        if (sourceAction instanceof ReadLinkAction || sourceAction instanceof ReadStructuralFeatureAction) {
            return true
        }
        return sourceAction.collectionOperation && sourceAction.plainCollectionOperation 
    }
    
    def static <I> CharSequence generateMany(Iterable<I> items, (I)=>CharSequence mapper) {
        return items.generateMany(mapper, '\n')
    }

    def static <I> CharSequence generateMany(Iterable<I> items, (I)=>CharSequence mapper, String separator) {
        return items.map[mapper.apply(it)].join(separator)
    }
}