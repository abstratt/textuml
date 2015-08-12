package com.abstratt.mdd.frontend.core;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;

import com.abstratt.mdd.core.Problem;

public class WrongMetaclass extends Problem {

    private String featureName;
    private String expectedMetaclass;
    private String actualMetaclass;

    public WrongMetaclass(ENamedElement featureName, EClass expectedMetaclass, EClass actualMetaclass) {
        super(Severity.ERROR);
        this.featureName = featureName.getName();
        this.expectedMetaclass = expectedMetaclass.getName();
        this.actualMetaclass = actualMetaclass.getName();
    }

    public String getMessage() {
        return "Feature '" + featureName + "' does not apply to metaclass: '" + actualMetaclass + "'. Expected: '"
                + expectedMetaclass + "'";
    }

}
