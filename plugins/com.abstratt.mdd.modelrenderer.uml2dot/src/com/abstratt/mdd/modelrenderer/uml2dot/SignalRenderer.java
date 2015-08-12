package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_SIGNALS;

import org.eclipse.uml2.uml.Signal;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class SignalRenderer extends ClassifierRenderer<Signal> {
	public boolean renderObject(Signal element, IndentedPrintWriter w, IRenderingSession context) {
		if (!context.getSettings().getBoolean(SHOW_SIGNALS))
			return false;
		return super.renderObject(element, w, context);
	}
}
