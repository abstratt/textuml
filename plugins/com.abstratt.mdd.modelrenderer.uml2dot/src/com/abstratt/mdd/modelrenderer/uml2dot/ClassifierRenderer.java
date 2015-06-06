package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowClassifierCompartmentForPackageOptions;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowClassifierCompartmentOptions;
import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;
import com.abstratt.modelrenderer.RenderingUtils;

public class ClassifierRenderer<T extends Classifier> implements IEObjectRenderer<T> {

	public boolean renderObject(T element, IndentedPrintWriter w,
			IRenderingSession context) {
		if (element.getName() == null || UML2DOTRenderingUtils.isTemplateInstance(element))
			return false;
		if (element.getVisibility() == VisibilityKind.PRIVATE_LITERAL)
		    return false;
		w.print('"' + element.getName() + "\" [");
		w.println("label=<");
		w.enterLevel();
		w
				.println("<TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\" cellborder=\"0\" port=\"port\">");
		w
				.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"3\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
		renderClassifierTypeAdornment(element, w, context);
		renderStereotypeAdornments(element, w, context);
		w.print("<TR><TD>");
		w.print(element.getName());
		w.println("</TD></TR>");
		if (!EcoreUtil.isAncestor(context.getRoot(), element)) {
			w.print("<TR><TD>");
			final Package nearestPackage = element.getNearestPackage();
				final String packageName = nearestPackage == null ? "?" : nearestPackage.getQualifiedName();
				w.print("(from " + packageName
						+ ")");
			w.print("</TD></TR>");
		}
		w.exitLevel();
		w.print("</TABLE></TD></TR>");

		boolean attributesEmpty = !context.getSettings().getBoolean(SHOW_ATTRIBUTES) || element.getAttributes().isEmpty();
		if (showCompartments(context, attributesEmpty)) {
			w.println("<TR><TD>");
			if (!attributesEmpty) {
				w.println("<TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" ALIGN=\"LEFT\">");
				RenderingUtils.renderAll(context, element.getAttributes());
				w.println("</TABLE>");
			} else
				w.println("<TABLE border=\"1\" cellborder=\"0\"><TR><TD> </TD></TR></TABLE>");
			w.println("</TD></TR>");
		}
		boolean operationsEmpty = !context.getSettings().getBoolean(SHOW_OPERATIONS) || element.getOperations().isEmpty();
		if (showCompartments(context, operationsEmpty)) {
			w.print("<TR><TD>");
			if (!operationsEmpty) {
				w.print("<TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" ALIGN=\"LEFT\">");
				RenderingUtils.renderAll(context, element.getOperations());
				w.print("</TABLE>");
			} else
				w.print("<TABLE border=\"1\" cellborder=\"0\"><TR><TD> </TD></TR></TABLE>");
			w.print("</TD></TR>");		
		}
		w.exitLevel();
		w.println("</TABLE>>];");
		w.enterLevel();
		renderRelationships(element, context);
		return true;
	}

    protected void renderClassifierTypeAdornment(T element, IndentedPrintWriter w, IRenderingSession session) {
        renderNameAdornments(Arrays.asList(getElementTypeName(element)), w, session);
    }

    private void renderStereotypeAdornments(T element, IndentedPrintWriter w, IRenderingSession session) {
		if (element.getAppliedStereotypes().isEmpty() || !session.getSettings().getBoolean(UML2DOTPreferences.SHOW_CLASSIFIER_STEREOTYPES))
		    return;
		List<String> stereotypeNames = element.getAppliedStereotypes().stream().map(it -> it.getName()).collect(Collectors.toList());
        renderNameAdornments(stereotypeNames, w, session);
	}
	
   protected void renderNameAdornments(List<String> markers, IndentedPrintWriter w, IRenderingSession session) {
        w.print("<TR><TD>");
        StringBuffer adornmentList = new StringBuffer();
        for (String marker : markers) {
            adornmentList.append(marker);
            adornmentList.append(", ");
        }
        adornmentList.delete(adornmentList.length() - 2, adornmentList.length());
        w.print(UML2DOTRenderingUtils.addGuillemots(adornmentList.toString()));
        w.println("</TD></TR>");
    }
	
	private String getElementTypeName(T element) {
		return StringUtils.uncapitalize(element.eClass().getName());
	}

	protected void renderRelationships(T element,
			IRenderingSession context) {
		List<Generalization> generalizations = element.getGeneralizations();
		RenderingUtils.renderAll(context, generalizations);
		EList<Association> associations = element.getAssociations();
		RenderingUtils.renderAll(context, associations);
		RenderingUtils.renderAll(context, element.getOwnedComments());
	}

	private boolean showCompartments(IRenderingSession context, boolean isEmpty) {
		ShowClassifierCompartmentOptions showCompartmentOption = context.getSettings().getSelection(ShowClassifierCompartmentOptions.class);
		switch (showCompartmentOption) {
		case Never:
			return false;
		case NotEmpty:
			if (isEmpty)
				return false;
		case Always:
		}
		ShowClassifierCompartmentForPackageOptions showCompartmentForPackageOption = context.getSettings().getSelection(ShowClassifierCompartmentForPackageOptions.class);
		switch (showCompartmentForPackageOption) {
		case Any:
			return true;
		case Immediate:
			EObject previousClassifier = context.getPrevious(UMLPackage.Literals.CLASSIFIER);
			if (previousClassifier != null && EcoreUtil.isAncestor(context.getRoot(), previousClassifier))
				return true;
		default: // Current
			return EcoreUtil
					.isAncestor(context.getRoot(), context.getCurrent());
		}
	}
}
