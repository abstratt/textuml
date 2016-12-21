package com.abstratt.mdd.core.util;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;

public class ElementUtils {
	public static List<Comment> getComments(Element element) {
		return UML2Util.getInverseReferences(element).stream()
			.filter(it -> it.getEStructuralFeature() == UMLPackage.Literals.COMMENT__ANNOTATED_ELEMENT)
			.map(it -> (Comment) it.getEObject())
			.collect(Collectors.toList());
    }
    public static String getCommentText(Element element) {
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
