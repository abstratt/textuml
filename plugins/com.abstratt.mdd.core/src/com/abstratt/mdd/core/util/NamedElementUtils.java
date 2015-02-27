package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;

public class NamedElementUtils {
    public static <NE extends NamedElement> List<String> getNames(List<NE> namedElements) {
        List<String> enumValues = new ArrayList<String>(namedElements.size());
        for (NE literal : namedElements)
            enumValues.add(literal.getName());
        return enumValues;
    }
    
    public static void collectParents(List<Namespace> collected, NamedElement current, Collection<EClass> classes) {
        Namespace namespace = current.getNamespace();
        if (namespace == null)
            return;
        if (classes.stream().anyMatch((eClass) -> eClass.isInstance(namespace)))
            collected.add(namespace);
        collectParents(collected, namespace, classes);
    }
    
    public static NamedElement findNearest(NamedElement current, Predicate<NamedElement> check) {
        if (check.test(current))
            return current;
        Namespace namespace = current.getNamespace();
        return (namespace == null) ? null : findNearest(namespace, check); 
    }

    
    public static boolean isWithin(NamedElement toCheck, Namespace potentialParent) {
        if (toCheck == null)
            return false;
        if (potentialParent == toCheck)
            return true;
        return isWithin(toCheck.getNamespace(), potentialParent); 
    }
    
    public static Namespace findNearestNamespace(NamedElement element, EClass... classes) {
        Namespace namespace = element instanceof Namespace ? (Namespace) element : element.getNamespace();
        while (namespace != null) {
            for (EClass clazz : classes)
                if (clazz.isInstance(namespace))
                    return namespace;
            namespace = namespace.getNamespace();
        }
        throw new IllegalArgumentException("Could not find a namespace of the given types");
    }
}
