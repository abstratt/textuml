package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.TemplateableElement;
import org.eclipse.uml2.uml.VisibilityKind;

public class UML2DOTRenderingUtils {

    public static String ID = UML2DOTRenderingUtils.class.getPackage().getName();

    public static String addGuillemots(String original) {
        return "&laquo;" + original + "&raquo;";
    }

    public static String renderMultiplicity(MultiplicityElement multiple, boolean brackets) {
        if (multiple.lowerBound() == multiple.upperBound()) {
            if (multiple.upperBound() == -1)
                return wrapInBrackets("*", brackets);
            else if (multiple.upperBound() != 1) {
                return wrapInBrackets(Integer.toString(multiple.upperBound()), brackets);
            }
            return "";
        }
        StringBuffer interval = new StringBuffer();
        interval.append(multiple.lowerBound());
        interval.append("..");
        interval.append(multiple.upperBound() == -1 ? "*" : multiple.upperBound());
        return wrapInBrackets(interval.toString(), brackets);
    }

    public static String renderVisibility(VisibilityKind visibility) {
        switch (visibility) {
        case PACKAGE_LITERAL:
            return "~";
        case PRIVATE_LITERAL:
            return "-";
        case PROTECTED_LITERAL:
            return "#";
        case PUBLIC_LITERAL:
            return "+";
        }
        return "";
    }

    private static String wrapInBrackets(String original, boolean useBrackets) {
        return useBrackets ? "[" + original + "]" : " " + original;
    }

    public static String getXMIID(Element element) {
        return ((XMLResource) element.eResource()).getID(element);
    }

    public static boolean isTemplateInstance(TemplateableElement t) {
        return !t.getTemplateBindings().isEmpty();
    }
}
