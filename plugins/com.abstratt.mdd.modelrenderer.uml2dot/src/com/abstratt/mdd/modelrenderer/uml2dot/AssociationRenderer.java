/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.*;

import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class AssociationRenderer extends AbstractRelationshipRenderer<Association> {

	@Override
	public boolean basicRenderObject(Association element, IndentedPrintWriter pw,
			IRenderingSession context) {
		if (!element.isBinary())
			// we humbly admit we can't handle n-ary associations
			return true;
		if (element.isDerived())
		    // until we support controlling whether derived associations are to be shown
		    return true;
		List<Property> ends = element.getMemberEnds();
		// source and target here are about the association direction
		// assumes source is the first end
		Property source = ends.get(0);
		Property target = ends.get(1);
		if (source.getType() == null || target.getType() == null)
			return true;
		// origin and destination here are only w.r.t. where are coming from in the rendering session
		Type origin = (Type) context.getPrevious(UMLPackage.Literals.TYPE);
		Type destination = (Type) (EcoreUtil.equals(ends.get(0), origin) ? ends.get(1).getType() : ends.get(0).getType()); 
		if (!shouldRender(context, origin, destination))
		    // don't render an association with a class that is not going to be shown
			return false;
		boolean aggregation = source.getAggregation() != AggregationKind.NONE_LITERAL
				|| target.getAggregation() != AggregationKind.NONE_LITERAL;
		boolean asymmetric = (source.isNavigable() ^ target.isNavigable() || source.getUpper() != target.getUpper())
				&& !context.getSettings().getBoolean(OMIT_CONSTRAINTS_FOR_NAVIGABILITY);
		if ((aggregation && target.getAggregation() == AggregationKind.NONE_LITERAL) ||
			 (asymmetric && (!target.isNavigable() || source.getUpper() < target.getUpper()))) {
			source = ends.get(1);
			target = ends.get(0); 
		}
		Type targetType = target.getType();
		Type sourceType = source.getType();
		context.render(targetType, targetType.eResource() != element
				.eResource());
		context.render(sourceType, sourceType.eResource() != element
				.eResource());
		pw.print("\"" + sourceType.getName() + "\":port -- " + "\"" + targetType.getName()
				+ "\":port "); 
		pw.println("[");
		pw.enterLevel();
		String edgeLabel = context.getSettings().getBoolean(SHOW_ASSOCIATION_NAME)
				&& element.getName() != null ? element.getName() : "";
		DOTRenderingUtils.addAttribute(pw, "label",
				edgeLabel);
		addEndAttributes(pw, "head", target, context);
		addEndAttributes(pw, "tail", source, context);
		DOTRenderingUtils.addAttribute(pw, "labeldistance", "1.7");
		DOTRenderingUtils.addAttribute(pw, "constraint", Boolean
				.toString(asymmetric || aggregation));
		DOTRenderingUtils.addAttribute(pw, "style", "solid");
		pw.exitLevel();
		pw.println("]");
		return true;
	}

	private void addEndAttributes(IndentedPrintWriter pw, String name,
			Property end, IRenderingSession context) {
		Property opposite = end.getOtherEnd();
		String arrow = end.isNavigable() & !opposite.isNavigable() ? "open"
				: "none";
		switch (opposite.getAggregation()) {
		case COMPOSITE_LITERAL:
			arrow = "diamond" + arrow;
			break;
		case SHARED_LITERAL:
			arrow = "ediamond" + arrow;
			break;
		default:
			arrow += "none";
		}
		if (context.getSettings().getBoolean(SHOW_ASSOCIATION_END_OWNERSHIP)
				&& !end.getAssociation().getOwnedEnds().contains(opposite))
			arrow = "dot" + arrow;
		DOTRenderingUtils.addAttribute(pw, "arrow" + name, arrow);
		String label = (context.getSettings().getBoolean(SHOW_ASSOCIATION_END_NAME)
				&& end.getName() != null ? end.getName() : "");
		if (context.getSettings().getBoolean(SHOW_ASSOCIATION_END_MULTIPLICITY))
			label += UML2DOTRenderingUtils.renderMultiplicity(end, false);
		DOTRenderingUtils.addAttribute(pw, name + "label", label);
	}

}
