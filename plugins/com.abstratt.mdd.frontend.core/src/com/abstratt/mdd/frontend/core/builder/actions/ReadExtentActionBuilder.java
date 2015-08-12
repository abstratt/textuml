package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.ReadExtentAction;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class ReadExtentActionBuilder extends ActionBuilder<ReadExtentAction> {
    private String classifierName;

    public ReadExtentActionBuilder() {
        super(UML2ProductKind.READ_EXTENT_ACTION);
    }

    public ReadExtentActionBuilder classifier(String classifierName) {
        this.classifierName = classifierName;
        return this;
    }

    @Override
    public void enhanceAction() {
        getProduct().setClassifier(this.<Classifier> findNamedElement(classifierName, Literals.CLASSIFIER));
        final OutputPin result = getProduct().createResult(null, getProduct().getClassifier());
        result.setUpperValue(MDDUtil.createLiteralUnlimitedNatural(getProduct().getNearestPackage(),
                LiteralUnlimitedNatural.UNLIMITED));
        result.setIsUnique(true);
    }

    @Override
    protected boolean isProducer() {
        return true;
    }
}
