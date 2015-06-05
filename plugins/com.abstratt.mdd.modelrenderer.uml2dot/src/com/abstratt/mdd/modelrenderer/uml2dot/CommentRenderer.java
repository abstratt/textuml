package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;
import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class CommentRenderer implements IEObjectRenderer<Comment> {

	public boolean renderObject(Comment element, IndentedPrintWriter out,
			IRenderingSession context) {
	    if (!context.getSettings().getBoolean(UML2DOTPreferences.SHOW_COMMENTS))
	        return false;
		String commentNodeId = "comment_" + UML2DOTRenderingUtils.getXMIID(element);
		out.println('"' + commentNodeId + "\" [shape=note,label=\"" + element.getBody() + "\"]");
		for (Element commented : element.getAnnotatedElements()) {
			out.print("\"" + ((NamedElement) commented).getName()
					+ "\":port" + " -- \"" +  commentNodeId + "\"");
			out.println("[");
			out.enterLevel();
			DOTRenderingUtils.addAttribute(out, "head", "none");
			DOTRenderingUtils.addAttribute(out, "tail", "none");
			DOTRenderingUtils.addAttribute(out, "constraint", Boolean.TRUE.toString());
			DOTRenderingUtils.addAttribute(out, "arrowtail", "none");
			DOTRenderingUtils.addAttribute(out, "arrowhead", "none");
			DOTRenderingUtils.addAttribute(out, "style", "solid");
			out.exitLevel();
			out.println("]");
		}
		return true;
	}

}
