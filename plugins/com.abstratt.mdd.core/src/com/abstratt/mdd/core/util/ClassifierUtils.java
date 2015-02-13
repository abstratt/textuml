package com.abstratt.mdd.core.util;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.IRepository;

public class ClassifierUtils {
    public static List<Classifier> findAllSpecifics(IRepository repository, final Classifier general, final boolean includeAbstract) {
		List<Classifier> specifics = repository.findInAnyPackage(new EObjectCondition() {
			@Override
			public boolean isSatisfied(EObject object) {
				if (object instanceof Classifier) {
					Classifier classifier = (Classifier) object;
					return (includeAbstract || !classifier.isAbstract()) && classifier.conformsTo(general);
				}
				return false;
			}
		});
		return specifics;
    }
    
    public static Classifier findClassifier(IRepository repository, String className) {
    	return repository.findNamedElement(className, UMLPackage.Literals.CLASSIFIER, null);
    }
    
    public static <C extends Classifier> C createClassifier(Namespace namespace, String name, EClass eClass) {
        namespace = NamedElementUtils.findNearestNamespace(namespace, UMLPackage.Literals.PACKAGE, UMLPackage.Literals.CLASS);
        if (namespace instanceof Package)
            return (C) ((Package) namespace).createOwnedType(name, eClass);
        else
            return (C) ((Class) namespace).createNestedClassifier(name, eClass);

    }
}
