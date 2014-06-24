package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_STRUCTURAL_FEATURE_VISIBILITY;

import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.ValueSpecification;

import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public class PropertyRenderer implements IEObjectRenderer<Property> {
	public boolean renderObject(Property property, IndentedPrintWriter w, IRenderingSession context) {
		if (property.getName() == null)
			return false;
		w.print("<TR><TD align=\"left\">");
		// TODO this is duplicated in ClassRenderer#renderNameAdornments
		if (context.getSettings().getBoolean(UML2DOTPreferences.SHOW_FEATURE_STEREOTYPES) && !property.getAppliedStereotypes().isEmpty()) {
			StringBuffer stereotypeList = new StringBuffer();
			for (Stereotype current : property.getAppliedStereotypes()) {
				stereotypeList.append(current.getName());
				stereotypeList.append(", ");
			}
			stereotypeList.delete(stereotypeList.length() - 2, stereotypeList.length());
			w.print(UML2DOTRenderingUtils.addGuillemots(stereotypeList.toString()));
		}
		if (context.getSettings().getBoolean(SHOW_STRUCTURAL_FEATURE_VISIBILITY))
			w.print(UML2DOTRenderingUtils.renderVisibility(property.getVisibility()));
		if (property.isDerived())
			w.print('/');
		w.print(property.getName());
		if (property.getType() != null) {
			w.print(" : ");
			w.print(property.getType().getName());
			w.print(UML2DOTRenderingUtils.renderMultiplicity(property, true));
		}
		if (property.getDefaultValue() != null) {
			ValueSpecification defaultValue = property.getDefaultValue();
			String basicValue = defaultValue.stringValue();
			if ("String".equals(property.getType().getName())) 
				basicValue = "\"" + basicValue + "\"";
			w.print(" = " + basicValue);
		} else if (property.getDefault() != null) {
			w.print(" = " + property.getDefault());			
		}
		w.println("</TD></TR>");
		return true;
	}
}
