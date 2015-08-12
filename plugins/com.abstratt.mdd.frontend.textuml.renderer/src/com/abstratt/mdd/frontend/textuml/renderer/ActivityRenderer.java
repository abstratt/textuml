package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.StructuredActivityNode;

import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class ActivityRenderer implements IEObjectRenderer<Activity> {

	@Override
	public boolean renderObject(Activity element, IndentedPrintWriter out, IRenderingSession session) {
		StructuredActivityNode rootAction = ActivityUtils.getRootAction(element);
		if (rootAction != null) {
			session.render(rootAction);
		}
		return true;
	}

}
