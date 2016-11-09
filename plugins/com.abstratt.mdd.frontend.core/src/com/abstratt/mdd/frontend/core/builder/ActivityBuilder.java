package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.Class;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.core.builder.actions.StructuredActivityNodeBuilder;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;

public class ActivityBuilder extends DefaultParentBuilder<Activity> {

    private NameReference behavioralFeatureName;
    private OperationBuilder specification;

    public ActivityBuilder() {
        super(UML2ProductKind.ACTIVITY);
    }

    @Override
    protected Activity createProduct() {
        Class parentClass = (Class) getParent().as(ClassifierBuilder.class).getProduct();
        Activity activity = (Activity) parentClass.createNestedClassifier(null, getEClass());
        return activity;
    }

    public ActivityBuilder specification(OperationBuilder specification) {
        this.specification = specification;
        return this;
    }

    public ActivityBuilder specification(String specificationName) {
        this.behavioralFeatureName = new NameReference(specificationName, UML2ProductKind.OPERATION);
        return this;
    }

    public StructuredActivityNodeBuilder newBlock() {
        return (StructuredActivityNodeBuilder) newChildBuilder(UML2ProductKind.STRUCTURED_ACTIVITY_NODE);
    }

    @Override
    protected void enhance() {
        if (behavioralFeatureName != null)
            new ReferenceSetter<BehavioralFeature>(behavioralFeatureName, getParentProduct(), getContext()) {
                @Override
                protected void link(BehavioralFeature specification) {
                    setSpecification(specification);
                }
            };
        else if (specification != null)
            setSpecification(specification.getProduct());
        else
            abortScope(new UnclassifiedProblem("No specification set"));
        // build children as the last step
        getContext().getReferenceTracker().add(new IDeferredReference() {
            @Override
            public void resolve(IBasicRepository repository) {
                if (specification.getProduct() != null)
                    createActivityParameters(specification.getProduct());
                getContext().getActivityBuilder().createRootBlock(getProduct());
                try {
                    ActivityBuilder.super.enhance();
                } finally {
                    getContext().getActivityBuilder().closeRootBlock();
                }
            }
        }, IReferenceTracker.Step.LAST);
    }

    private void setSpecification(BehavioralFeature specification) {
        getProduct().setSpecification(specification);
        getProduct().setName(MDDUtil.getTokenFromQName(specification.getName()));
    }

    private void createActivityParameters(BehavioralFeature specification) {
        getProduct().getOwnedParameters().addAll(EcoreUtil.copyAll(specification.getOwnedParameters()));
    }
}
