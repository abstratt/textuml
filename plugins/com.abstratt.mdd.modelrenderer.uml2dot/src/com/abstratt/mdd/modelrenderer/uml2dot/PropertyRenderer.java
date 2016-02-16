package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_FEATURE_VISIBILITY;

import org.eclipse.uml2.uml.LiteralNull;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowClassifierCompartmentForPackageOptions;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowMinimumVisibilityOptions;

public class PropertyRenderer implements IElementRenderer<Property> {
    public boolean renderObject(Property property, IndentedPrintWriter w, IRenderingSession context) {
        if (property.getName() == null)
            return false;
        if (RendererHelper.shouldSkip(context, property))
        	return false;
		if (property.isDerived() && !context.getSettings().getBoolean(UML2DOTPreferences.SHOW_DERIVED_ELEMENTS, true))
			return false;
		if (property.isStatic() && !context.getSettings().getBoolean(UML2DOTPreferences.SHOW_STATIC_FEATURES, true))
			return false;
        
        w.print("<TR><TD align=\"left\">");
        // TODO this is duplicated in ClassRenderer#renderNameAdornments
        if (context.getSettings().getBoolean(UML2DOTPreferences.SHOW_FEATURE_STEREOTYPES)
                && !property.getAppliedStereotypes().isEmpty()) {
            StringBuffer stereotypeList = new StringBuffer();
            for (Stereotype current : property.getAppliedStereotypes()) {
                stereotypeList.append(current.getName());
                stereotypeList.append(", ");
            }
            stereotypeList.delete(stereotypeList.length() - 2, stereotypeList.length());
            w.print(UML2DOTRenderingUtils.addGuillemots(stereotypeList.toString()));
        }
        if (context.getSettings().getBoolean(SHOW_FEATURE_VISIBILITY))
            w.print(UML2DOTRenderingUtils.renderVisibility(property.getVisibility()));
        if (property.isDerived())
            w.print('/');
        if (property.isStatic())
        	w.print("<u>");
        w.print(property.getName());
        if (property.isStatic())
        	w.print("</u>");
        if (property.getType() != null) {
            w.print(" : ");
            w.print(property.getType().getName());
            w.print(UML2DOTRenderingUtils.renderMultiplicity(property, true));
        }
        if (property.getDefaultValue() != null) {
            if (property.getDefaultValue() instanceof LiteralNull) {
                ValueSpecification defaultValue = property.getDefaultValue();
                String basicValue = defaultValue.stringValue();
                if ("String".equals(property.getType().getName()))
                    basicValue = "\"" + basicValue + "\"";
                w.print(" = " + basicValue);
            }
        } else if (property.getDefault() != null) {
            w.print(" = " + property.getDefault());
        }
        w.println("</TD></TR>");
        return true;
    }
}
