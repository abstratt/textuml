package com.abstratt.mdd.core.util;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.UMLPackage;

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
}
