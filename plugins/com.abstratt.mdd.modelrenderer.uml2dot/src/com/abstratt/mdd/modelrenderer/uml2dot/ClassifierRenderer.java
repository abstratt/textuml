package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_ATTRIBUTES;
import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_OPERATIONS;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.core.util.FeatureUtils;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.RenderingUtils;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowClassifierCompartmentForPackageOptions;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowClassifierCompartmentOptions;

public class ClassifierRenderer<T extends Classifier> implements IElementRenderer<T> {

    public boolean renderObject(T element, IndentedPrintWriter w, IRenderingSession context) {
        if (element.getName() == null || UML2DOTRenderingUtils.isTemplateInstance(element))
            return false;
        if (element.getVisibility() == VisibilityKind.PRIVATE_LITERAL)
            return false;
        w.print('"' + element.getName() + "\" [");
        w.println("label=<");
        w.runInNewLevel(() -> {
	        w.println("<TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\" cellborder=\"0\" port=\"port\">");
	        w.runInNewLevel(() -> {
		        w.println("<TR><TD>");
		        w.runInNewLevel(() -> {
			        w.println("<TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"3\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
			        renderClassifierTypeAdornment(element, w, context);
			        renderStereotypeAdornments(element, w, context);
			        w.runInNewLevel(() -> {
				        w.print("<TR><TD>");
				        if (element.isAbstract())
				        	w.println("<i>");
				        w.print(element.getName());
				        if (element.isAbstract())
				        	w.println("</i>");
				        w.println("</TD></TR>");
			        });
			        if (!EcoreUtil.isAncestor(context.getRoot(), element)) {
			        	w.runInNewLevel(() -> {
			        		w.print("<TR><TD>");
			        		final Package nearestPackage = element.getNearestPackage();
			        		final String packageName = nearestPackage == null ? "?" : nearestPackage.getQualifiedName();
			        		w.print("(from " + packageName + ")");
			        		w.print("</TD></TR>");
			        	});
			        }
			        w.println("</TABLE>");
		        });
		        w.println("</TD></TR>");
		        boolean attributesEmpty = !context.getSettings().getBoolean(SHOW_ATTRIBUTES)
		                || element.getAttributes().isEmpty();
		        if (showCompartments(context, attributesEmpty)) {
		        	w.println("<TR><TD>");
		        	w.runInNewLevel(() -> {
			            if (!attributesEmpty) {
			                w.println("<TABLE border=\"1\" cellborder=\"0\" CELLPADDING=\"0\" CELLSPACING=\"5\" ALIGN=\"LEFT\">");
			                w.runInNewLevel(() -> {
				                boolean renderedAny = RenderingUtils.renderAll(context, element.getAttributes());
				                if (!renderedAny) {
				                	w.runInNewLevel(() -> {
				                		w.println("<TR><TD> </TD></TR>");
				                	});
				                }
			                });
			                w.println("</TABLE>");
			            } else
			                w.println("<TABLE border=\"1\" cellborder=\"0\"><TR><TD> </TD></TR></TABLE>");
		            });
		        	w.println("</TD></TR>");
		        }
		        List<? extends BehavioralFeature> behavioralFeatures = getBehavioralFeatures(element);
		        boolean operationsEmpty = !context.getSettings().getBoolean(SHOW_OPERATIONS) || behavioralFeatures.isEmpty();
		        if (showCompartments(context, operationsEmpty)) {
		            w.println("<TR><TD>");
		            w.runInNewLevel(() -> {
			            if (!operationsEmpty) {
			                w.println("<TABLE border=\"1\" cellborder=\"0\">");
			                boolean renderedAny = RenderingUtils.renderAll(context, behavioralFeatures);
			                if (!renderedAny)
			                	w.runInNewLevel(() -> {
			                		w.println("<TR><TD> </TD></TR>");
			                	});
			                w.println("</TABLE>");
			            } else {
			                w.println("<TABLE border=\"1\" cellborder=\"0\">");
			                w.runInNewLevel(() -> {
			                	w.println("<TR><TD> </TD></TR>");
			                });
			                w.println("</TABLE>");
			            }
		            });
		            w.println("</TD></TR>");
		        }
	        });
	        w.println("</TABLE>");
        });
        w.println(">];");
        w.runInNewLevel(() -> {
        	renderRelationships(element, context);
        });
        return true;
    }

    protected List<? extends BehavioralFeature> getBehavioralFeatures(T element) {
        return FeatureUtils.getBehavioralFeatures(element);
    }

    protected void renderClassifierTypeAdornment(T element, IndentedPrintWriter w, IRenderingSession session) {
        renderNameAdornments(Arrays.asList(getElementTypeName(element)), w, session);
    }

    private void renderStereotypeAdornments(T element, IndentedPrintWriter w, IRenderingSession session) {
        if (element.getAppliedStereotypes().isEmpty()
                || !session.getSettings().getBoolean(UML2DOTPreferences.SHOW_CLASSIFIER_STEREOTYPES))
            return;
        List<String> stereotypeNames = element.getAppliedStereotypes().stream().map(it -> it.getName())
                .collect(Collectors.toList());
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

    protected void renderRelationships(T element, IRenderingSession context) {
        List<Generalization> generalizations = element.getGeneralizations();
        RenderingUtils.renderAll(context, generalizations);
        EList<Association> associations = element.getAssociations();
        RenderingUtils.renderAll(context, associations);
        RenderingUtils.renderAll(context, ElementUtils.getComments(element));
    }

    private boolean showCompartments(IRenderingSession<Element> context, boolean isEmpty) {
        ShowClassifierCompartmentOptions showCompartmentOption = context.getSettings().getSelection(
                ShowClassifierCompartmentOptions.class);
        switch (showCompartmentOption) {
        case Never:
            return false;
        case NotEmpty:
            if (isEmpty)
                return false;
        case Always:
        }
        ShowClassifierCompartmentForPackageOptions showCompartmentForPackageOption = context.getSettings()
                .getSelection(ShowClassifierCompartmentForPackageOptions.class);
        EObject previousClassifier = context.getPrevious(UMLPackage.Literals.CLASSIFIER);
        switch (showCompartmentForPackageOption) {
        case Any:
            return true;
        case Immediate:
            if (previousClassifier != null && EcoreUtil.isAncestor(context.getRoot(), previousClassifier))
                return true;
            break;
        case Local:
            if (ElementUtils.sameRepository(context.getRoot(), context.getCurrent()))
                return true;
            break;
        case Current:
            break;
        }
        boolean underRootPackage = EcoreUtil.isAncestor(context.getRoot(), context.getCurrent());
        return underRootPackage;
    }
}
