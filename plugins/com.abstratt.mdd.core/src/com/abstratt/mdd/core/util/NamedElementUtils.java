package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.List;

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
