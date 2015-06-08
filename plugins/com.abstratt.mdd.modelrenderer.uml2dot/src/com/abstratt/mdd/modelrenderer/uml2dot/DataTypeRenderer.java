package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.SHOW_DATATYPES;

import org.eclipse.uml2.uml.DataType;

import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class DataTypeRenderer extends ClassifierRenderer<DataType> {
    public boolean renderObject(DataType element, IndentedPrintWriter w, IRenderingSession context) {
        if (!context.getSettings().getBoolean(SHOW_DATATYPES))
             return false;
        return super.renderObject(element, w, context);
    }
}
