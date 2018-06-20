package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils.addAttribute;
import static com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils.escapeForDot;
import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTRenderingUtils.getXMIID;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class CommentRenderer implements IElementRenderer<Comment> {

	
    public boolean renderObject(Comment element, IndentedPrintWriter out, IRenderingSession context) {
        if (!context.getSettings().getBoolean(UML2DOTPreferences.SHOW_COMMENTS))
            return false;
        List<Element> annotatedElements = element.getAnnotatedElements().stream().filter(it -> context.isRendered(it)).collect(Collectors.toList());
		if (annotatedElements.isEmpty())
        	return false;
        String commentText = generateCommentText(element);
        if (commentText == null)
        	return false;
        String commentNodeId = "comment_" + getXMIID(element);
		out.println('"' + commentNodeId + "\" [shape=note,label=\"" + escapeForDot(commentText) + "\"]");
        for (Element commented : annotatedElements) {
        	if (commented != element.getNearestPackage()) {
	            out.print("\"" + ((NamedElement) commented).getName() + "\":port" + " -- \"" + commentNodeId + "\"");
	            out.println("[");
	            out.runInNewLevel(() -> {
		            addAttribute(out, "head", "none");
		            addAttribute(out, "tail", "none");
		            addAttribute(out, "constraint", Boolean.TRUE.toString());
		            addAttribute(out, "arrowtail", "none");
		            addAttribute(out, "arrowhead", "none");
		            addAttribute(out, "style", "solid");
	            });
	            out.println("]");
        	}
        }
        return true;
    }

	private String generateCommentText(Comment element) {
		String body = element.getBody().trim().replaceAll("[\\n\\p{Space}]+", " ");
		int periodIndex = body.indexOf(".");
		int end = periodIndex == -1 ? body.length() : periodIndex;
		String wrapped = WordUtils.wrap(StringUtils.abbreviate(body.substring(0, end), 200), 30);
		return StringUtils.trimToNull(wrapped);
	}

}
