package com.abstratt.mdd.core.tests.frontend.textuml;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Property;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.frontend.core.UnknownType;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.WrongNumberOfRoles;

public class AssociationTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(AssociationTests.class);
    }

    public AssociationTests(String name) {
        super(name);
    }

    private String getSimpleModelSource() {
        String source = "";
        source += "model simple;\n";
        source += "  class Account\n";
        source += "  end;\n";
        source += "  class Client\n";
        source += "  end;\n";
        source += "end.";
        return source;
    }

    private void testMissingMemberEndType(String keyword) throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "class AccountChange\n";
        source += "  attribute account : Account;\n";
        source += "end;\n";
        source += keyword + " AccountAccountChange\n";
        source += "  role change : AccountChange;\n";
        source += "  /* intentional typo */\n";
        source += "  role AccountChange2.account;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = compile(getSimpleModelSource(), source);
        // expect error - end type is not known
        assertTrue(Arrays.asList(problems).toString(), problems.length == 2);
        assertTrue(Arrays.asList(problems).toString(), problems[0] instanceof WrongNumberOfRoles);
        assertTrue(Arrays.asList(problems).toString(), problems[1] instanceof UnresolvedSymbol);
    }

    public void testAssociationMissingMemberEndType() throws CoreException {
        testMissingMemberEndType("association");
    }

    public void testCompositionMissingMemberEndType() throws CoreException {
        testMissingMemberEndType("composition");
    }

    public void testAggregationMissingMemberEndType() throws CoreException {
        testMissingMemberEndType("aggregation");
    }

    private void testMemberEnd(String keyword, AggregationKind expected) throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "  class ClientWithAccountAttribute\n";
        source += "    attribute account : AccountWithClientAttribute;\n";
        source += "  end;\n";
        source += "  class AccountWithClientAttribute\n";
        source += "    attribute client : ClientWithAccountAttribute;\n";
        source += "  end;\n";
        source += keyword + " AccountClient\n";
        source += "  role ClientWithAccountAttribute.account;\n";
        source += "  role AccountWithClientAttribute.client;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        Class accountClass = (Class) getRepository().findNamedElement("simple::AccountWithClientAttribute",
                IRepository.PACKAGE.getClass_(), null);
        Class clientClass = (Class) getRepository().findNamedElement("simple::ClientWithAccountAttribute",
                IRepository.PACKAGE.getClass_(), null);
        final Association association = (Association) getRepository().findNamedElement("simple::AccountClient",
                IRepository.PACKAGE.getAssociation(), null);
        assertNotNull(association);
        Property accountEnd = association.getOwnedEnd("account", accountClass);
        assertNull(accountEnd);
        accountEnd = association.getMemberEnd("account", accountClass);
        assertNotNull(accountEnd);
        assertEquals(expected, accountEnd.getAggregation());
        Property clientEnd = association.getOwnedEnd("client", clientClass);
        assertNull(clientEnd);
        clientEnd = association.getMemberEnd("client", clientClass);
        assertNotNull(clientEnd);
        assertEquals(AggregationKind.NONE_LITERAL, clientEnd.getAggregation());
        assertEquals(1, ((LiteralUnlimitedNatural) accountEnd.getLowerValue()).getValue());
        assertEquals(1, ((LiteralUnlimitedNatural) accountEnd.getUpperValue()).getValue());
        assertEquals(1, ((LiteralUnlimitedNatural) clientEnd.getLowerValue()).getValue());
        assertEquals(1, ((LiteralUnlimitedNatural) clientEnd.getUpperValue()).getValue());
    }

    public void testMixedMemberEnd() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "  class Parent\n";
        source += "    attribute children : Child[*];\n";
        source += "  end;\n";
        source += "  class Child\n";
        source += "  end;\n";
        source += "  composition Hierarchy\n";
        source += "    role Parent.children;\n";
        source += "    role parent : Parent;\n";
        source += "  end;\n";
        source += "end.";
        parseAndCheck(source);
        Class parentClass = (Class) getRepository().findNamedElement("simple::Parent", IRepository.PACKAGE.getClass_(),
                null);
        Class childClass = (Class) getRepository().findNamedElement("simple::Child", IRepository.PACKAGE.getClass_(),
                null);
        final Association association = (Association) getRepository().findNamedElement("simple::Hierarchy",
                IRepository.PACKAGE.getAssociation(), null);
        Property parentEnd = association.getOwnedEnd("parent", parentClass);
        assertNotNull(parentEnd);
        assertEquals(AggregationKind.NONE_LITERAL, parentEnd.getAggregation());
        Property childrenEnd = association.getOwnedEnd("children", childClass);
        assertNull(childrenEnd);
        childrenEnd = association.getMemberEnd("children", childClass);
        assertNotNull(childrenEnd);
        assertEquals(AggregationKind.COMPOSITE_LITERAL, childrenEnd.getAggregation());
        assertEquals(0, ((LiteralInteger) childrenEnd.getLowerValue()).getValue());
        assertEquals(LiteralUnlimitedNatural.UNLIMITED,
                ((LiteralUnlimitedNatural) childrenEnd.getUpperValue()).getValue());
        assertEquals(1, ((LiteralUnlimitedNatural) parentEnd.getLowerValue()).getValue());
        assertEquals(1, ((LiteralUnlimitedNatural) parentEnd.getUpperValue()).getValue());
    }

    public void testAssociationMemberEnd() throws CoreException {
        testMemberEnd("association", AggregationKind.NONE_LITERAL);
    }

    public void testCompositionMemberEnd() throws CoreException {
        testMemberEnd("composition", AggregationKind.COMPOSITE_LITERAL);
    }

    public void testAggregationMemberEnd() throws CoreException {
        testMemberEnd("aggregation", AggregationKind.SHARED_LITERAL);
    }

    public void testMissingEndType() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "association AccountClient\n";
        source += "  navigable role account : Account;\n";
        source += "  navigable role client : Client2;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = compile(getSimpleModelSource(), source);
        // expect error - end type is not known
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
        assertTrue(problems[0] instanceof UnknownType);
    }

    public void testMissingRole() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "association AccountClient\n";
        source += "  role account : Account;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = compile(getSimpleModelSource(), source);
        // expect error - declared only one role
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
    }

    public void testSimpleAggregation() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "aggregation AccountClient\n";
        source += "  navigable role account : Account[*];\n";
        source += "  navigable role client : Client[1];\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(getSimpleModelSource(), source);
        final Association association = (Association) getRepository().findNamedElement("simple::AccountClient",
                IRepository.PACKAGE.getAssociation(), null);
        assertNotNull(association);
        Property accountEnd = association.getOwnedEnd("account", null);
        assertNotNull(accountEnd);
        assertEquals(AggregationKind.SHARED_LITERAL, accountEnd.getAggregation());
        Property clientEnd = association.getOwnedEnd("client", null);
        assertNotNull(clientEnd);
        assertEquals(AggregationKind.NONE_LITERAL, clientEnd.getAggregation());
    }

    public void testSimpleAssociation() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "association AccountClient\n";
        source += "  navigable role account : Account[*];\n";
        source += "  navigable role client : Client[1];\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(getSimpleModelSource(), source);
        Class accountClass = (Class) getRepository().findNamedElement("simple::Account",
                IRepository.PACKAGE.getClass_(), null);
        Class clientClass = (Class) getRepository().findNamedElement("simple::Client", IRepository.PACKAGE.getClass_(),
                null);
        final Association association = (Association) getRepository().findNamedElement("simple::AccountClient",
                IRepository.PACKAGE.getAssociation(), null);
        assertNotNull(association);
        Property accountEnd = association.getOwnedEnd("account", accountClass);
        assertNotNull(accountEnd);
        assertEquals(AggregationKind.NONE_LITERAL, accountEnd.getAggregation());
        Property clientEnd = association.getOwnedEnd("client", clientClass);
        assertNotNull(clientEnd);
        assertEquals(AggregationKind.NONE_LITERAL, clientEnd.getAggregation());
        assertEquals(0, ((LiteralInteger) accountEnd.getLowerValue()).getValue());
        assertEquals(LiteralUnlimitedNatural.UNLIMITED,
                ((LiteralUnlimitedNatural) accountEnd.getUpperValue()).getValue());
        assertEquals(1, ((LiteralInteger) clientEnd.getLowerValue()).getValue());
        assertEquals(1, ((LiteralUnlimitedNatural) clientEnd.getUpperValue()).getValue());
    }

    public void testAssociationShorthand() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "class A\n";
        source += "end;\n";
        source += "class C\n";
        source += "end;\n";
        source += "class B\n";
        source += "  reference a : A;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        Class classA = (Class) getRepository().findNamedElement("simple::A", IRepository.PACKAGE.getClass_(), null);
        Class classB = (Class) getRepository().findNamedElement("simple::B", IRepository.PACKAGE.getClass_(), null);

        Property propertyA = classB.getOwnedAttribute("a", null);
        assertNotNull(propertyA);

        Association association = propertyA.getAssociation();
        assertNotNull(association);

        assertTrue(association.getMemberEnds().contains(propertyA));
        assertFalse(association.getOwnedEnds().contains(propertyA));
        assertEquals(AggregationKind.NONE_LITERAL, propertyA.getAggregation());
        assertSame(classA, propertyA.getType());

        Property otherEnd = propertyA.getOtherEnd();
        assertTrue(association.getMemberEnds().contains(otherEnd));
        assertTrue(association.getOwnedEnds().contains(otherEnd));
        assertEquals(AggregationKind.NONE_LITERAL, otherEnd.getAggregation());
        assertSame(classB, otherEnd.getType());
    }

    public void testAggregationShorthand() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "class A\n";
        source += "end;\n";
        source += "class C\n";
        source += "end;\n";
        source += "class B\n";
        source += "  aggregation a : A;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        Class classA = (Class) getRepository().findNamedElement("simple::A", IRepository.PACKAGE.getClass_(), null);
        Class classB = (Class) getRepository().findNamedElement("simple::B", IRepository.PACKAGE.getClass_(), null);

        Property propertyA = classB.getOwnedAttribute("a", null);
        assertNotNull(propertyA);

        Association association = propertyA.getAssociation();
        assertNotNull(association);

        assertTrue(association.getMemberEnds().contains(propertyA));
        assertFalse(association.getOwnedEnds().contains(propertyA));
        assertEquals(AggregationKind.SHARED_LITERAL, propertyA.getAggregation());
        assertSame(classA, propertyA.getType());

        Property otherEnd = propertyA.getOtherEnd();
        assertTrue(association.getMemberEnds().contains(otherEnd));
        assertTrue(association.getOwnedEnds().contains(otherEnd));
        assertEquals(AggregationKind.NONE_LITERAL, otherEnd.getAggregation());
        assertSame(classB, otherEnd.getType());
    }

    public void testCompositionShorthand() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "class A\n";
        source += "end;\n";
        source += "class C\n";
        source += "end;\n";
        source += "class B\n";
        source += "  composition a : A;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        Class classA = (Class) getRepository().findNamedElement("simple::A", IRepository.PACKAGE.getClass_(), null);
        Class classB = (Class) getRepository().findNamedElement("simple::B", IRepository.PACKAGE.getClass_(), null);

        Property propertyA = classB.getOwnedAttribute("a", null);
        assertNotNull(propertyA);

        Association association = propertyA.getAssociation();
        assertNotNull(association);

        assertTrue(association.getMemberEnds().contains(propertyA));
        assertFalse(association.getOwnedEnds().contains(propertyA));
        assertEquals(AggregationKind.COMPOSITE_LITERAL, propertyA.getAggregation());
        assertSame(classA, propertyA.getType());

        Property otherEnd = propertyA.getOtherEnd();
        assertTrue(association.getMemberEnds().contains(otherEnd));
        assertTrue(association.getOwnedEnds().contains(otherEnd));
        assertEquals(AggregationKind.NONE_LITERAL, otherEnd.getAggregation());
        assertSame(classB, otherEnd.getType());
    }

    public void testSimpleComposition() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "composition AccountClient\n";
        source += "  navigable role account : Account[*];\n";
        source += "  navigable role client : Client[1];\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(getSimpleModelSource(), source);
        final Association association = getRepository().findNamedElement("simple::AccountClient",
                IRepository.PACKAGE.getAssociation(), null);
        assertNotNull(association);
        Property accountEnd = association.getOwnedEnd("account", null);
        assertNotNull(accountEnd);
        assertEquals(AggregationKind.COMPOSITE_LITERAL, accountEnd.getAggregation());
        Property clientEnd = association.getOwnedEnd("client", null);
        assertNotNull(clientEnd);
        assertEquals(AggregationKind.NONE_LITERAL, clientEnd.getAggregation());
    }

    public void testDerivedReference() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "class Class1\n";
        source += "  reference myClass2a : Class2[*];\n";
        source += "  reference myClass2b : Class2[*];\n";
        source += "  derived attribute allMyClass2 : Class2[*] := { self.myClass2a.union(self.myClass2b) };\n";
        source += "end;\n";
        source += "class Class2\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        Class class1 = getRepository().findNamedElement("simple::Class1", IRepository.PACKAGE.getClass_(), null);
        Class class2 = getRepository().findNamedElement("simple::Class2", IRepository.PACKAGE.getClass_(), null);
        assertNotNull(class1);
        assertNotNull(class2);
        Property derived = class1.getAttribute("allMyClass2", class2);
        assertTrue(derived.isDerived());
        assertTrue(derived.isMultivalued());
        assertNull(derived.getAssociation());
    }

    public void testDerivedAssociation() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import mdd_types;\n";
        source += "class Account\n";
        source += "    attribute balance : Double;\n";
        source += "end;\n";
        source += "abstract class Client\n";
        source += "end;\n";
        source += "class Person specializes Client\n";
        source += "    derived attribute totalWorth : Double := { self<-AccountClient->account.sum((a : Account) : Double { a.balance })};\n";
        source += "end;\n";
        source += "class Company specializes Client\n";
        source += "end;\n";
        source += "association AccountClient\n";
        source += "  navigable role account : Account[*];\n";
        source += "  navigable role client : Client;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
    }

}
