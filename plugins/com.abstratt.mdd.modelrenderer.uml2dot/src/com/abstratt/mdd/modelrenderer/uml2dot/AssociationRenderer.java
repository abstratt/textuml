/**
 * Copyright (c) Abstratt Technologies 2007. All rights reserved.
 */
package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.*;

import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.dot.DOTRenderingUtils;

public class AssociationRenderer extends AbstractRelationshipRenderer<Association> {

    @Override
    public boolean basicRenderObject(Association element, IndentedPrintWriter pw, IRenderingSession<Element> context) {
        if (!element.isBinary())
            // we humbly admit we can't handle n-ary associations
            return false;
        if (RendererHelper.shouldSkip(context, element))
        	return false;
		if (element.isDerived() && !context.getSettings().getBoolean(UML2DOTPreferences.SHOW_DERIVED_ELEMENTS, false))
			return false;        
        List<Property> ends = element.getMemberEnds();
        // source and target here are about the association direction
        // assumes source is the first end
        Property source = ends.get(0);
        Property target = ends.get(1);
        if (source.getType() == null || target.getType() == null)
            return false;
        // origin and destination here are only w.r.t. where are coming from in
        // the rendering session
        Type origin = (Type) context.getPrevious(UMLPackage.Literals.TYPE);
        Type destination = (ends.get(0).getType() == origin) ? ends.get(1).getType() : ends.get(0)
                .getType();
        boolean renderedOrigin = context.isRendered(origin);
        boolean renderedDestination = context.isRendered(destination);
        // if at least one of the members is not going to be rendered, do not render the association
        if (!renderedOrigin || !renderedDestination)
        	return false;
        if (!shouldRender(context, origin, destination))
            // don't render an association with a class that is not going to be
            // shown
            return false;
        boolean aggregation = source.getAggregation() != AggregationKind.NONE_LITERAL
                || target.getAggregation() != AggregationKind.NONE_LITERAL;
        boolean asymmetric = (source.isNavigable() ^ target.isNavigable() || source.getUpper() != target.getUpper())
                && !context.getSettings().getBoolean(OMIT_CONSTRAINTS_FOR_NAVIGABILITY);
        if ((aggregation && target.getAggregation() == AggregationKind.NONE_LITERAL)
                || (asymmetric && (!target.isNavigable() || source.getUpper() < target.getUpper()))) {
            source = ends.get(1);
            target = ends.get(0);
        }
        Type targetType = target.getType();
        Type sourceType = source.getType();
        context.render(targetType, targetType.eResource() != element.eResource());
        context.render(sourceType, sourceType.eResource() != element.eResource());
        pw.print("\"" + sourceType.getName() + "\":port -- " + "\"" + targetType.getName() + "\":port ");
        pw.println("[");
        Property finalTarget = target;
        Property finalSource = source;
        pw.runInNewLevel(() -> {
	        String edgeLabel = context.getSettings().getBoolean(SHOW_ASSOCIATION_NAME) && element.getName() != null ? element
	                .getName() : "";
	        DOTRenderingUtils.addAttribute(pw, "label", edgeLabel);
	        addEndAttributes(pw, "head", finalTarget, context);
	        addEndAttributes(pw, "tail", finalSource, context);
	        DOTRenderingUtils.addAttribute(pw, "labeldistance", "1.7");
	        DOTRenderingUtils.addAttribute(pw, "constraint", Boolean.toString(asymmetric || aggregation));
	        DOTRenderingUtils.addAttribute(pw, "style", "solid");
        });
        pw.println("]");
        return true;
    }

    private void addEndAttributes(IndentedPrintWriter pw, String name, Property end, IRenderingSession<Element> context) {
        Property opposite = end.getOtherEnd();
        String arrow = end.isNavigable() & !opposite.isNavigable() ? "open" : "none";
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
        String label = (context.getSettings().getBoolean(SHOW_ASSOCIATION_END_NAME) && end.getName() != null ? end
                .getName() : "");
        if (context.getSettings().getBoolean(SHOW_ASSOCIATION_END_MULTIPLICITY))
            label += UML2DOTRenderingUtils.renderMultiplicity(end, false);
        DOTRenderingUtils.addAttribute(pw, name + "label", label);
    }

}
