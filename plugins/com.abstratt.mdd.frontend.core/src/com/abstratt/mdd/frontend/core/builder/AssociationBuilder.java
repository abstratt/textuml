package com.abstratt.mdd.frontend.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Package;

import com.abstratt.mdd.frontend.core.WrongNumberOfRoles;

public class AssociationBuilder extends DefaultParentBuilder<Association> {

    private PropertyBuilder childRole;
    private PropertyBuilder parentRole;
    private List<PropertyBuilder> memberEnds = new ArrayList<PropertyBuilder>();

    public AssociationBuilder(UML2ProductKind kind) {
        super(kind);
    }

    public PropertyBuilder newChildRole(PropertyBuilder memberEnd) {
        return childRole = memberEnd;
    }

    public PropertyBuilder newChildRole() {
        return childRole = newOwnedEnd();
    }

    public PropertyBuilder newParentRole(PropertyBuilder memberEnd) {
        return parentRole = memberEnd;
    }

    public PropertyBuilder newParentRole() {
        return parentRole = newOwnedEnd();
    }

    public PropertyBuilder newOwnedEnd() {
        return newChildBuilder(UML2ProductKind.PROPERTY);
    }

    public PropertyBuilder newMemberEnd(PropertyBuilder memberEnd) {
        memberEnds.add(memberEnd);
        return memberEnd;
    }

    @Override
    protected Association createProduct() {
        if (childBuilders.size() + memberEnds.size() != 2)
            abortScope(new WrongNumberOfRoles(2, childBuilders.size()));
        return (Association) ((Package) getParentProduct()).createOwnedType(getName(), getEClass());
    }

    @Override
    protected void enhance() {
        super.enhance();
        for (PropertyBuilder memberEnd : memberEnds)
            memberEnd.getProduct().setAssociation(getProduct());
    }
}
