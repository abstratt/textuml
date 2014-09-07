package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowClassifierCompartmentForPackageOptions;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowClassifierCompartmentOptions;
import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class ClassifierRenderer<T extends Classifier> implements IEObjectRenderer<T> {

	public boolean renderObject(T element, IndentedPrintWriter w,
			IRenderingSession context) {
		if (element.getName() == null || UML2DOTRenderingUtils.isTemplateInstance(element))
			return false;
		w.print('"' + element.getName() + "\" [");
		w.println("label=<");
		w.enterLevel();
		w
				.println("<TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\" cellborder=\"0\" port=\"port\">");
		w
				.println("<TR><TD><TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"3\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
		renderNameAdornments(element, w, context);
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

		if (showCompartments(context, element.getAttributes().isEmpty())) {
			w.println("<TR><TD>");
			if (!element.getAttributes().isEmpty()) {
				w.println("<TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" ALIGN=\"LEFT\">");
				context.renderAll(element.getAttributes());
				w.println("</TABLE>");
			} else
				w.println("<TABLE border=\"1\" cellborder=\"0\"><TR><TD> </TD></TR></TABLE>");
			w.println("</TD></TR>");
		}
		if (showCompartments(context, element.getOperations().isEmpty())) {
			w.print("<TR><TD>");
			if (!element.getOperations().isEmpty()) {
				w.print("<TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" ALIGN=\"LEFT\">");
				context.renderAll(element.getOperations());
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

	protected void renderNameAdornments(T element, IndentedPrintWriter w, IRenderingSession session) {
		if (element.getAppliedStereotypes().isEmpty() || !session.getSettings().getBoolean(UML2DOTPreferences.SHOW_CLASSIFIER_STEREOTYPES))
		    return;
		w.print("<TR><TD>");
		StringBuffer stereotypeList = new StringBuffer();
		for (Stereotype current : element.getAppliedStereotypes()) {
			stereotypeList.append(current.getName());
			stereotypeList.append(", ");
		}
		stereotypeList.delete(stereotypeList.length() - 2, stereotypeList.length());
		w.print(UML2DOTRenderingUtils.addGuillemots(stereotypeList.toString()));
		w.println("</TD></TR>");
	}
	
	private String getElementTypeName(T element) {
		return element.eClass().getInstanceClassName();
	}

	protected void renderRelationships(T element,
			IRenderingSession context) {
		List<Generalization> generalizations = element.getGeneralizations();
		context.renderAll(generalizations);
		EList<Association> associations = element.getAssociations();
		context.renderAll(associations);
		context.renderAll(element.getOwnedComments());
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
