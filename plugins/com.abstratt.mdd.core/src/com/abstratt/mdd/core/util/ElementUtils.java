package com.abstratt.mdd.core.util;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;

public class ElementUtils {
    public static String getComments(Element element) {
        List<Comment> comments = element.getOwnedComments();
        return comments.isEmpty() ? "" : comments.get(0).getBody().trim();
    }
    public static boolean sameRepository(Element elementA, Element elementB) {
        URI resourceALocation = elementA.eResource().getURI();
        URI resourceBLocation = elementB.eResource().getURI();
        if (resourceALocation.isHierarchical() && resourceBLocation.isHierarchical())
            if (URI.createURI("..").resolve(resourceALocation).equals(URI.createURI("..").resolve(resourceBLocation)))
                return true;
        return resourceALocation.equals(resourceBLocation);
    }
}
