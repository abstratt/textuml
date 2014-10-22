package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.NamedElement;

public class NamedElementUtils {
    public static <NE extends NamedElement> List<String> getNames(List<NE> namedElements) {
        List<String> enumValues = new ArrayList<String>(namedElements.size());
        for (NE literal : namedElements)
            enumValues.add(literal.getName());
        return enumValues;
    }
}
