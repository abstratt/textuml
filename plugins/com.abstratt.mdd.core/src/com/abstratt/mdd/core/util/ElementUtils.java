package com.abstratt.mdd.core.util;

import java.util.List;

import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;

public class ElementUtils {
    public static String getComments(Element element) {
        List<Comment> comments = element.getOwnedComments();
        return comments.isEmpty() ? "" : comments.get(0).getBody().trim();
    }
}
