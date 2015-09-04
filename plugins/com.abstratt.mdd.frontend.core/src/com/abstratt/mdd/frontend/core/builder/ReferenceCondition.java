package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;

public class ReferenceCondition implements BuildCondition {

    private NameReference reference;

    public ReferenceCondition(NameReference reference) {
        this.reference = reference;
    }

    @Override
    public boolean isSatisfied(UML2BuildContext buildContext) {
        Namespace scope = buildContext.getNamespaceTracker().currentNamespace(null);
        NamedElement found = buildContext.getRepository().findNamedElement(reference.getName(),
                reference.getElementType().getMetaClass(), scope);
        return found != null;
    }
}
