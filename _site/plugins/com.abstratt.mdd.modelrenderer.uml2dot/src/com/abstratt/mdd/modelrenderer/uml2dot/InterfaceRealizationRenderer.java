package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;

import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class InterfaceRealizationRenderer extends
		AbstractRelationshipRenderer<InterfaceRealization> {

	@Override
	protected boolean basicRenderObject(InterfaceRealization element, IndentedPrintWriter pw, IRenderingSession context) {
		final BehavioredClassifier implementor = element
				.getImplementingClassifier();
		final Interface contract = element.getContract();
		if (!shouldRender(context, implementor, contract))
			return true;
		context.render(contract, contract.eResource() != element.eResource());
		context.render(implementor, implementor.eResource() != element
				.eResource());
		pw.print("edge ");
		pw.println("[");
		pw.enterLevel();
		DOTRenderingUtils.addAttribute(pw, "arrowtail", "empty");
		DOTRenderingUtils.addAttribute(pw, "arrowhead", "none");
		DOTRenderingUtils.addAttribute(pw, "taillabel", "");
		DOTRenderingUtils.addAttribute(pw, "headlabel", "");
		DOTRenderingUtils.addAttribute(pw, "style", "dashed");
		pw.exitLevel();
		pw.println("]");
		pw.println(contract.getName() + ":port" + " -- "
				+ implementor.getName() + ":port");
		return true;
	}
}
