package com.abstratt.mdd.internal.frontend.textuml;

import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;

import com.abstratt.mdd.frontend.textuml.grammar.node.TModelComment;

public class CommentUtils {
    public static String collectCommentBody(TModelComment node) {
        String commentText = node.getText().substring(2);
        commentText = commentText.substring(0, commentText.length() - 2);
        return commentText;
    }

    public static void applyComment(TModelComment commentToken, Element commented) {
        if (commentToken != null) {
            Comment newComment = commented.createOwnedComment();
            newComment.setBody(CommentUtils.collectCommentBody(commentToken));
            newComment.getAnnotatedElements().add(commented);
        }
    }

}
