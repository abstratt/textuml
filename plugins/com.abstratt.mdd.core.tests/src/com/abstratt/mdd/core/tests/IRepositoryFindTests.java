package com.abstratt.mdd.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
import org.eclipse.emf.query.ocl.conditions.BooleanOCLCondition;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.ecore.OCL;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;

public class IRepositoryFindTests extends AbstractRepositoryBuildingTests {

    public IRepositoryFindTests(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        String modelSource = "";
        modelSource += "model someModel;\n";
        modelSource += "apply someProfile;\n";
        modelSource += "[stereotype1] class Class1 end;\n";
        modelSource += "interface Interface1 end;\n";
        modelSource += "interface Interface2 specializes Interface1 end;\n";
        modelSource += "[stereotype2]class Class2 specializes Class1 implements Interface1 end;\n";
        modelSource += "[stereotype2(stringValue=\"foo\", intValue=20)]class Class3 specializes Class1 implements Interface2 end;\n";
        modelSource += "[stereotype2(stringValue=\"bar\", intValue=30)]class Class4 end;\n";
        modelSource += "[stereotype2(booleanValue=true)]class Class5 end;\n";
        modelSource += "[stereotype2(booleanValue=false)]class Class6 end;\n";
        modelSource += "end.";

        String profileSource = "";
        profileSource += "profile someProfile;\n";
        profileSource += "stereotype stereotype1 extends UML::Class end;\n";
        profileSource += "stereotype stereotype2 extends UML::Class\n";
        profileSource += "property intValue : PrimitiveTypes::Integer;\n";
        profileSource += "property stringValue : PrimitiveTypes::String;\n";
        profileSource += "property booleanValue : PrimitiveTypes::Boolean;\n";
        profileSource += "end;\n";
        profileSource += "end.";

        parseAndCheckInContext(new String[] { modelSource, profileSource });
    }

    public void testTaggedValueMatching() {
        assertIncluded(
                "someModel::Class3",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'intValue') = 20"));
        assertIncluded(
                "someModel::Class3",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'stringValue') = 'foo'"));
        assertNotIncluded(
                "someModel::Class3",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'intValue') = 30"));
        assertNotIncluded(
                "someModel::Class3",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'stringValue') = 'bar'"));

        assertIncluded(
                "someModel::Class4",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'intValue') = 30"));
        assertIncluded(
                "someModel::Class4",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'stringValue') = 'bar'"));
        assertNotIncluded(
                "someModel::Class4",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'intValue') = 20"));
        assertNotIncluded(
                "someModel::Class4",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'stringValue') = 'foo'"));

        assertIncluded(
                "someModel::Class5",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'booleanValue') = true"));

        assertNotIncluded(
                "someModel::Class3",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'booleanValue') = true"));
        assertNotIncluded(
                "someModel::Class4",
                Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null and self.getValue(self.getAppliedStereotype('someProfile::stereotype2'), 'booleanValue') = true"));
    }

    // public void testEClassMatching() {
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // EClassMatcher(Literals.CLASS));
    // assertNotIncluded("someModel::Class1", Literals.CLASS, new
    // EClassMatcher(Literals.INTERFACE));
    // assertIncluded("someModel::Interface1", Literals.INTERFACE, new
    // EClassMatcher(Literals.INTERFACE));
    // assertNotIncluded("someModel::Interface1", Literals.INTERFACE, new
    // EClassMatcher(Literals.CLASS));
    // assertIncluded("someModel::Class2", Literals.CLASS, new
    // EClassMatcher(Literals.CLASS));
    // assertNotIncluded("someModel::Class2", Literals.CLASS, new
    // EClassMatcher(Literals.INTERFACE));
    // }

    private EObjectCondition createMatcher(String expression) {
        OCL ocl = OCL.newInstance();
        try {
            return new BooleanOCLCondition<EClassifier, EClass, EObject>(ocl.getEnvironment(), expression, null);
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

    }

    public void testEClassMatching() {
        assertIncluded("someModel::Class1", Literals.CLASS, createMatcher("self.oclIsTypeOf(Class)"));
        assertNotIncluded("someModel::Class1", Literals.CLASS, createMatcher("self.oclIsTypeOf(Interface)"));
        assertIncluded("someModel::Interface1", Literals.INTERFACE, createMatcher("self.oclIsTypeOf(Interface)"));
        assertNotIncluded("someModel::Interface1", Literals.INTERFACE, createMatcher("self.oclIsTypeOf(Class)"));
        assertIncluded("someModel::Class2", Literals.CLASS, createMatcher("self.oclIsTypeOf(Class)"));
        assertNotIncluded("someModel::Class2", Literals.CLASS, createMatcher("self.oclIsTypeOf(Interface)"));
    }

    //
    // public void testNameMatching() {
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // NameMatcher("Class1"));
    // assertNotIncluded("someModel::Class1", Literals.CLASS, new
    // NameMatcher("Class2"));
    // assertIncluded("someModel::Class2", Literals.CLASS, new
    // NameMatcher("Class2"));
    // assertNotIncluded("someModel::Class2", Literals.CLASS, new
    // NameMatcher("Class1"));
    // }
    //

    public void testSuperTypeMatching() {
        assertIncluded("someModel::Class1", Literals.CLASS, createMatcher("self.oclIsKindOf(Class)"));
        assertIncluded("someModel::Class1", Literals.CLASS, createMatcher("self.oclIsKindOf(Classifier)"));
        assertIncluded("someModel::Class1", Literals.CLASS, createMatcher("self.oclIsKindOf(Type)"));
        assertIncluded("someModel::Class1", Literals.CLASS, createMatcher("self.oclIsKindOf(NamedElement)"));
        assertIncluded("someModel::Class1", Literals.CLASS, createMatcher("self.oclIsKindOf(Element)"));
        assertNotIncluded("someModel::Class1", Literals.CLASS, createMatcher("self.oclIsKindOf(Interface)"));
    }

    // public void testSuperTypeMatching() {
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // AssignableFromMatcher(Literals.CLASS));
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // AssignableFromMatcher(Literals.CLASSIFIER));
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // AssignableFromMatcher(Literals.TYPE));
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // AssignableFromMatcher(Literals.NAMED_ELEMENT));
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // AssignableFromMatcher(Literals.ELEMENT));
    // assertNotIncluded("someModel::Class1", Literals.CLASS, new
    // AssignableFromMatcher(Literals.INTERFACE));
    // }
    //

    public void testCompositeMatching() {
        assertIncluded("someModel::Class1", Literals.CLASS,
                createMatcher("self.oclIsTypeOf(Class) or self.oclIsTypeOf(Interface)"));
        assertNotIncluded("someModel::Class1", Literals.CLASS,
                createMatcher("self.oclIsTypeOf(Class) and self.oclIsTypeOf(Interface)"));
        assertIncluded("someModel::Interface1", Literals.INTERFACE,
                createMatcher("self.oclIsTypeOf(Class) or self.oclIsTypeOf(Interface)"));
        assertNotIncluded("someModel::Interface1", Literals.INTERFACE,
                createMatcher("self.oclIsTypeOf(Class) and self.oclIsTypeOf(Interface)"));
    }

    // public void testCompositeMatching() {
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // DisjunctionMatcher(new EClassMatcher(Literals.CLASS), new
    // EClassMatcher(Literals.INTERFACE)));
    // assertNotIncluded("someModel::Class1", Literals.CLASS, new
    // ConjunctionMatcher(new EClassMatcher(Literals.CLASS), new
    // EClassMatcher(Literals.INTERFACE)));
    // assertIncluded("someModel::Interface1", Literals.INTERFACE, new
    // DisjunctionMatcher(new EClassMatcher(Literals.CLASS), new
    // EClassMatcher(Literals.INTERFACE)));
    // assertNotIncluded("someModel::Interface1", Literals.INTERFACE, new
    // ConjunctionMatcher(new EClassMatcher(Literals.CLASS), new
    // EClassMatcher(Literals.INTERFACE)));
    // }
    //

    public void testReferenceMatching() {
        assertIncluded("someModel::Class2", Literals.CLASS, createMatcher("not self.generalization->isEmpty()"));

        assertIncluded("someModel::Class2", Literals.CLASS,
                createMatcher("self.generalization.general.name->includes('Class1')"));
        assertIncluded("someModel::Class3", Literals.CLASS,
                createMatcher("self.generalization.general.name->includes('Class1')"));

        // OCL bug 251808 - unexpected ambiguity between Class#general and
        // Classifier#general - need to use ECore binding
        assertIncluded("someModel::Class2", Literals.CLASS, createMatcher("self.general.name->includes('Class1')"));
        assertIncluded("someModel::Class3", Literals.CLASS, createMatcher("self.general.name->includes('Class1')"));

        assertIncluded("someModel::Class2", Literals.CLASS, createMatcher("self.superClass.name->includes('Class1')"));
        assertIncluded("someModel::Class3", Literals.CLASS, createMatcher("self.superClass.name->includes('Class1')"));

        assertIncluded("someModel::Class2", Literals.CLASS, createMatcher("not self.interfaceRealization->isEmpty()"));

        assertIncluded("someModel::Class2", Literals.CLASS,
                createMatcher("self.interfaceRealization.contract.name->includes('Interface1')"));
        assertNotIncluded("someModel::Class3", Literals.CLASS,
                createMatcher("self.interfaceRealization.contract.name->includes('Interface1')"));
        assertIncluded("someModel::Class3", Literals.CLASS,
                createMatcher("self.interfaceRealization.contract.name->includes('Interface2')"));
    }

    // public void testReferenceMatching() {
    // assertIncluded("someModel::Class2", Literals.CLASS, new
    // ReferenceMatcher(new EClassMatcher(Literals.GENERALIZATION),
    // Literals.CLASSIFIER__GENERALIZATION.getName()));
    //
    // assertIncluded("someModel::Class2", Literals.CLASS, new
    // ReferenceMatcher(new ReferenceMatcher(new NameMatcher("Class1"),
    // Literals.GENERALIZATION__GENERAL.getName()),
    // Literals.CLASSIFIER__GENERALIZATION.getName()));
    // assertIncluded("someModel::Class3", Literals.CLASS, new
    // ReferenceMatcher(new ReferenceMatcher(new NameMatcher("Class1"),
    // Literals.GENERALIZATION__GENERAL.getName()),
    // Literals.CLASSIFIER__GENERALIZATION.getName()));
    // assertIncluded("someModel::Class2", Literals.CLASS, new
    // ReferenceMatcher(new NameMatcher("Class1"),
    // Literals.CLASSIFIER__GENERAL.getName()));
    // assertIncluded("someModel::Class3", Literals.CLASS, new
    // ReferenceMatcher(new NameMatcher("Class1"),
    // Literals.CLASSIFIER__GENERAL.getName()));
    // assertIncluded("someModel::Class2", Literals.CLASS, new
    // ReferenceMatcher(new NameMatcher("Class1"),
    // Literals.CLASS__SUPER_CLASS.getName()));
    // assertIncluded("someModel::Class3", Literals.CLASS, new
    // ReferenceMatcher(new NameMatcher("Class1"),
    // Literals.CLASS__SUPER_CLASS.getName()));
    //
    // assertIncluded("someModel::Class2", Literals.CLASS, new
    // ReferenceMatcher(new ReferenceMatcher(new NameMatcher("Interface1"),
    // Literals.INTERFACE_REALIZATION__CONTRACT.getName()),
    // Literals.BEHAVIORED_CLASSIFIER__INTERFACE_REALIZATION.getName()));
    // assertNotIncluded("someModel::Class3", Literals.CLASS, new
    // ReferenceMatcher(new ReferenceMatcher(new NameMatcher("Interface1"),
    // Literals.INTERFACE_REALIZATION__CONTRACT.getName()),
    // Literals.BEHAVIORED_CLASSIFIER__INTERFACE_REALIZATION.getName()));
    // assertIncluded("someModel::Class3", Literals.CLASS, new
    // ReferenceMatcher(new ReferenceMatcher(new NameMatcher("Interface2"),
    // Literals.INTERFACE_REALIZATION__CONTRACT.getName()),
    // Literals.BEHAVIORED_CLASSIFIER__INTERFACE_REALIZATION.getName()));
    // }
    //

    public void testStereotypeApplicationMatching() {
        Stereotype stereotype1 = getRepository()
                .findNamedElement("someProfile::stereotype1", Literals.STEREOTYPE, null);
        Stereotype stereotype2 = getRepository()
                .findNamedElement("someProfile::stereotype2", Literals.STEREOTYPE, null);
        assertNotNull(stereotype1);
        assertNotNull(stereotype2);

        assertIncluded("someModel::Class1", Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype1') <> null"));
        assertNotIncluded("someModel::Class1", Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null"));

        assertIncluded("someModel::Class2", Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null"));
        assertNotIncluded("someModel::Class2", Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype1') <> null"));

        assertIncluded("someModel::Class3", Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype2') <> null"));
        assertNotIncluded("someModel::Class3", Literals.CLASS,
                createMatcher("self.getAppliedStereotype('someProfile::stereotype1') <> null"));
    }

    // public void testStereotypeApplicationMatching() {
    // Stereotype stereotype1 =
    // getRepository().findNamedElement("someProfile::stereotype1",
    // Literals.STEREOTYPE);
    // Stereotype stereotype2 =
    // getRepository().findNamedElement("someProfile::stereotype2",
    // Literals.STEREOTYPE);
    // assertNotNull(stereotype1);
    // assertNotNull(stereotype2);
    //
    // assertIncluded("someModel::Class1", Literals.CLASS, new
    // StereotypeApplicationMatcher(stereotype1));
    // assertNotIncluded("someModel::Class1", Literals.CLASS, new
    // StereotypeApplicationMatcher(stereotype2));
    //
    // assertIncluded("someModel::Class2", Literals.CLASS, new
    // StereotypeApplicationMatcher(stereotype2));
    // assertNotIncluded("someModel::Class2", Literals.CLASS, new
    // StereotypeApplicationMatcher(stereotype1));
    //
    // assertIncluded("someModel::Class3", Literals.CLASS, new
    // StereotypeApplicationMatcher(stereotype2));
    // assertNotIncluded("someModel::Class3", Literals.CLASS, new
    // StereotypeApplicationMatcher(stereotype1));
    // }

    //
    // public void testTaggedValueMatching() {
    // assertNotIncluded("someModel::Class1", Literals.CLASS, new
    // TaggedValueMatcher("someProfile::stereotype2", "value", 20));
    // assertNotIncluded("someModel::Class2", Literals.CLASS, new
    // TaggedValueMatcher("someProfile::stereotype2", "value", 20));
    // assertIncluded("someModel::Class3", Literals.CLASS, new
    // TaggedValueMatcher("someProfile::stereotype2", "value", "20"));
    // assertNotIncluded("someModel::Class3", Literals.CLASS, new
    // TaggedValueMatcher("someProfile::stereotype2", "value", "30"));
    // }

    // self instanceOf UML::Class and self.interfaceRealization.contract.name =
    // 'Interface1"

    private void assertIncluded(String qualifiedName, EClass eClass, EObjectCondition matcher) {
        NamedElement expected = getRepository().findNamedElement(qualifiedName, eClass, null);
        assertNotNull(expected);
        assertTrue(matcher.isSatisfied(expected));
        assertTrue(getRepository().findAll(matcher, false).contains(expected));
    }

    private void assertNotIncluded(String qualifiedName, EClass eClass, EObjectCondition matcher) {
        NamedElement notExpected = getRepository().findNamedElement(qualifiedName, eClass, null);
        assertNotNull(notExpected);
        assertFalse(matcher.isSatisfied(notExpected));
        assertFalse(getRepository().findAll(matcher, false).contains(notExpected));
    }

    public static Test suite() {
        return new TestSuite(IRepositoryFindTests.class);
    }

}
