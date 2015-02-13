package com.abstratt.mdd.core.tests.frontend.textuml;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.TemplateBinding;
import org.eclipse.uml2.uml.TemplateParameter;
import org.eclipse.uml2.uml.TemplateParameterSubstitution;
import org.eclipse.uml2.uml.TemplateSignature;
import org.eclipse.uml2.uml.TemplateableElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Variable;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.FeatureUtils;
import com.abstratt.mdd.core.util.StructuralFeatureUtils;
import com.abstratt.mdd.core.util.TemplateUtils;
import com.abstratt.mdd.core.util.UML2Compatibility;
import com.abstratt.mdd.frontend.core.UnboundTemplate;

public class TemplateTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(TemplateTests.class);
    }

    public TemplateTests(String name) {
        super(name);
    }

    private void checkTemplateBinding(Classifier boundClassifier, final String templateClassName,
            final String templateBindingParameterTypeName) {
        assertNotNull(boundClassifier);
        Classifier templateClass = (Classifier) getRepository().findNamedElement(templateClassName, IRepository.PACKAGE.getClassifier(),
                null);
        Classifier templateParameterClassifier = (Classifier) getRepository().findNamedElement(templateBindingParameterTypeName,
                IRepository.PACKAGE.getClassifier(), null);
        final TemplateBinding templateBinding = boundClassifier.getTemplateBinding(templateClass.getOwnedTemplateSignature());
        assertNotNull(templateBinding);
        List<TemplateParameterSubstitution> substitutions = templateBinding.getParameterSubstitutions();
        assertEquals(1, substitutions.size());
        final TemplateParameter templateParameter = templateClass.getOwnedTemplateSignature().getOwnedParameters().get(0);
        assertSame(templateParameter, substitutions.get(0).getFormal());
        assertSame(templateParameterClassifier, UML2Compatibility.getActualParameter(substitutions.get(0)));
        assertSame(boundClassifier, boundClassifier);
    }

    public void testAccessToAttributeOfTemplateType() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "attribute attr1 : T;\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "operation op1();\n";
        model += "begin\n";
        model += "var f : Bar<Zoo>, x : Zoo;\n";
        model += "x := f.attr1;\n";
        model += "end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testBasicTemplateDeclaration() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Bar<T>\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier barClass = (Classifier) getRepository().findNamedElement("test::Bar", IRepository.PACKAGE.getClassifier(), null);
        assertNotNull(barClass);
        assertTrue(barClass.isTemplate());
        TemplateSignature signature = barClass.getOwnedTemplateSignature();
        assertNotNull(signature);
        List<TemplateParameter> parameters = signature.getOwnedParameters();
        assertNotNull(parameters);
        assertEquals(1, parameters.size());
        assertTrue(parameters.get(0).getParameteredElement() instanceof Class);
        assertEquals("T", ((Class) parameters.get(0).getParameteredElement()).getName());
    }

    public void testBindingOfTemplateWithOperation() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "  operation op1(a : T);\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "  attribute boz : Bar<Zoo>;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier zooClass = (Classifier) getRepository().findNamedElement("test::Zoo", IRepository.PACKAGE.getClassifier(), null);
        assertNotNull(zooClass);
        Classifier fooClass = (Classifier) getRepository().findNamedElement("test::Foo", IRepository.PACKAGE.getClassifier(), null);
        assertNotNull(fooClass);
        Property bozProperty = fooClass.getAttribute("boz", null);
        Classifier bozType = (Classifier) bozProperty.getType();
        assertNotNull(bozType);
        Operation boundOp1 = StructuralFeatureUtils.findOperation(getRepository(), bozType, "op1", null);
        assertNotNull(boundOp1);
        assertEquals("T", boundOp1.getOwnedParameter("a", null).getType().getName());
    }

    public void testCallOperationOnSuperTemplateType() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "operation op1(t : T) : T;\n";
        model += "end;\n";
        model += "class Bar2<V> specializes Bar<V>\n";
        model += "operation op2(v : V) : V;\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "operation op2();\n";
        model += "begin\n";
        model += "var f : Bar2<Zoo>, x : Zoo;\n";
        model += "x := f.op2(x);\n";
        model += "x := f.op1(x);\n";
        model += "end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testCallOperationOnTemplateType() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "operation op1() : T;\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "operation op2();\n";
        model += "begin\n";
        model += "var f : Bar<Zoo>, x : Zoo;\n";
        model += "x := f.op1();\n";
        model += "end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testCallOperationReturningMultipleOnTemplateType() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "operation op1() : T[*];\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "operation op2();\n";
        model += "begin\n";
        model += "var f : Bar<Zoo>, x : Zoo[*];\n";
        model += "x := f.op1();\n";
        model += "end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testCallOperationTakingSignatureOnSubTemplateType() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "import base;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "operation op1(p : {( : T) : Boolean}) : T;\n";
        model += "end;\n";
        model += "class Bar2<Z> specializes Bar<Z>\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "operation op2();\n";
        model += "begin\n";
        model += "var f : Bar2<Zoo>, x : Zoo;\n";
        model += "x := f.op1((x : Zoo) : Boolean  { return true; });\n";
        model += "end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testCallOperationTakingSignatureOnTemplateType() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "import base;\n";
        model += "class Zoo specializes Object\n";
        model += "end;\n";
        model += "class Bar<T> specializes Object\n";
        model += "operation op1(p : {( : T) : String}) : T;\n";
        model += "end;\n";
        model += "class Foo specializes Object\n";
        model += "operation op2();\n";
        model += "begin\n";
        model += "var f : Bar<Zoo>, x : Zoo;\n";
        model += "x := f.op1((x : Zoo) : String  { return x.toString(); });\n";
        model += "end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testTemplateBindingAsAttributeType() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "  attribute attr1 : Bar<Zoo>;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier fooType = (Classifier) getRepository().findNamedElement("test::Foo", IRepository.PACKAGE.getClassifier(), null);
        Property fooAttr1 = fooType.getAttribute("attr1", null);
        assertNotNull(fooAttr1);
        checkTemplateBinding((Classifier) fooAttr1.getType(), "test::Bar", "test::Zoo");
    }

    public void testTemplateBindingAsLocalVar() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "  operation my_op1();\n";
        model += "  begin\n";
        model += "    var f : Bar<Zoo>;\n";
        model += "  end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier fooType = (Classifier) getRepository().findNamedElement("test::Foo", IRepository.PACKAGE.getClassifier(), null);
        Operation myOp1 = fooType.getOperation("my_op1", null, null);
        assertNotNull(myOp1);
        StructuredActivityNode mainNode = ActivityUtils.getRootAction(myOp1);
        assertNotNull(mainNode);
        StructuredActivityNode firstChild = (StructuredActivityNode) mainNode.getContainedNode(null, false,
                UMLPackage.Literals.STRUCTURED_ACTIVITY_NODE);
        Variable variable = firstChild.getVariable("f", null);
        assertNotNull(variable);
        checkTemplateBinding((Classifier) variable.getType(), "test::Bar", "test::Zoo");
    }

    public void testTemplateBindingAsParameterType() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "  operation my_op1(par1 : Bar<Zoo>);\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier fooType = (Classifier) getRepository().findNamedElement("test::Foo", IRepository.PACKAGE.getClassifier(), null);
        Operation myOp1 = fooType.getOperation("my_op1", null, null);
        assertNotNull(myOp1);
        Parameter par1 = myOp1.getOwnedParameter("par1", null);
        assertNotNull(par1);
        checkTemplateBinding((Classifier) par1.getType(), "test::Bar", "test::Zoo");
    }

    public void testTemplateBindingThroughInheritance() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "  class Zoo\n";
        model += "  end;\n";
        model += "  class Bar<T>\n";
        model += "    operation op1() : T;\n";
        model += "  end;\n";
        model += "  class BarOfZoo specializes Bar<Zoo>\n";
        model += "    operation op2() : Zoo;\n";
        model += "    begin\n";
        model += "      return self.op1();\n";
        model += "    end;\n";
        model += "  end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testTemplateDeclarationWithAttribute() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Bar<T>\n";
        model += "attribute attr1 : T;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier barClass = (Classifier) getRepository().findNamedElement("test::Bar", IRepository.PACKAGE.getClassifier(), null);
        Classifier parameterType = (Classifier) barClass.getOwnedTemplateSignature().getParameters().get(0).getParameteredElement();
        assertNotNull(parameterType);
        Property attribute = barClass.getAttribute("attr1", null);
        assertNotNull(attribute);
        assertSame(parameterType, attribute.getType());
    }

    public void testTemplateDeclarationWithLocalVar() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Bar<T>\n";
        model += "  operation op1();\n";
        model += "  begin\n";
        model += "    var x : T;\n";
        model += "  end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier barClass = (Classifier) getRepository().findNamedElement("test::Bar", IRepository.PACKAGE.getClassifier(), null);
        Classifier parameterType = (Classifier) barClass.getOwnedTemplateSignature().getParameters().get(0).getParameteredElement();
        assertNotNull(parameterType);
        Operation operation = barClass.getOperation("op1", null, null);
        assertNotNull(operation);
        StructuredActivityNode block = ActivityUtils.getRootAction(operation);
        StructuredActivityNode firstChild = (StructuredActivityNode) block.getContainedNode(null, false,
                UMLPackage.Literals.STRUCTURED_ACTIVITY_NODE);
        Variable xVar = ActivityUtils.findVariable(firstChild, "x");
        assertNotNull(xVar.getType());
        assertNotNull(((TemplateableElement) xVar.getType()).isTemplate());
    }

    public void testTemplateDeclarationWithOperation() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "class Bar<T>\n";
        model += "operation op1(a : T);\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier barClass = (Classifier) getRepository().findNamedElement("test::Bar", IRepository.PACKAGE.getClassifier(), null);
        Classifier parameterType = (Classifier) barClass.getOwnedTemplateSignature().getParameters().get(0).getParameteredElement();
        assertNotNull(parameterType);
        Operation operation = barClass.getOperation("op1", null, null);
        assertNotNull(operation);
        assertNotNull(operation.getOwnedParameter("a", parameterType));
    }

    public void testTemplateParameterInheritance() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "  class Bar<T>\n";
        model += "  end;\n";
        model += "  class Bar2<W> specializes Bar<W>\n";
        model += "    operation op2() : W;\n";
        model += "  end;\n";
        model += "  class Bar3<U> specializes Bar<U>\n";
        model += "    operation op2() : U;\n";
        model += "  end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Classifier barClass = (Classifier) getRepository().findNamedElement("test::Bar", IRepository.PACKAGE.getClassifier(), null);
        assertNotNull(barClass);
        Classifier bar2Class = (Classifier) getRepository().findNamedElement("test::Bar2", IRepository.PACKAGE.getClassifier(), null);
        assertNotNull(bar2Class);
        assertTrue("Bar2 is not a subclass of Bar", bar2Class.allParents().contains(barClass));
    }

    public void testTemplateSubclassWithAdditionalParameters() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "  class Bar<T>\n";
        model += "  end;\n";
        model += "  class Bar2<Z,T> specializes Bar<T>\n";
        model += "    operation op2() : T;\n";
        model += "    operation op3() : Z;\n";
        model += "  end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testTemplateUsageWithoutParameters() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "  class Bar<T>\n";
        model += "  end;\n";
        model += "  class Foo \n";
        model += "    operation op2() : Bar;\n";
        model += "  end;\n";
        model += "end.\n";
        IProblem[] problems = compile(model);
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
        assertTrue(Arrays.asList(problems).toString(), problems[0] instanceof UnboundTemplate);
    }

    public void testApplySubstitution() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "import base;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "operation op1(t : T) : T;\n";
        model += "end;\n";
        model += "class Bar2<V> specializes Bar<V>\n";
        model += "operation op2(v : V) : V;\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "attribute attr1 : Bar2<Zoo>;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        IRepository repo = getRepository();
        Property attr1 = (Property) repo.findNamedElement("test::Foo::attr1", UMLPackage.Literals.PROPERTY, null);
        Operation op1 = (Operation) repo.findNamedElement("test::Bar::op1", UMLPackage.Literals.OPERATION, null);
        org.eclipse.uml2.uml.Class foo = (org.eclipse.uml2.uml.Class) repo.findNamedElement("test::Foo", UMLPackage.Literals.CLASS, null);
        org.eclipse.uml2.uml.Class zoo = (org.eclipse.uml2.uml.Class) repo.findNamedElement("test::Zoo", UMLPackage.Literals.CLASS, null);
        assertNotNull(foo);
        assertNotNull(zoo);
        assertNotNull(attr1);
        assertNotNull(op1);
        assertNotNull(op1.getType());
        assertTrue(op1.getType().isTemplateParameter());
        // tests TemplateUtils.applySubstitution
        assertSame(zoo, TemplateUtils.applySubstitution((Classifier) attr1.getType(), op1.getType()));
    }

    public void testIsFullyResolved() throws CoreException {
        String model = "";
        model += "model test;\n";
        model += "import base;\n";
        model += "class Zoo\n";
        model += "end;\n";
        model += "class Bar<T>\n";
        model += "attribute attr3 : T;\n";
        model += "attribute attr4 : Bar<T>;\n";
        model += "end;\n";
        model += "class Foo\n";
        model += "attribute attr1 : Zoo;\n";
        model += "attribute attr2 : Bar<Zoo>;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
        Property attr1 = getProperty("test::Foo::attr1");
        Property attr2 = getProperty("test::Foo::attr2");
        Property attr3 = getProperty("test::Bar::attr3");
        Property attr4 = getProperty("test::Bar::attr4");
        
        assertFalse(TemplateUtils.isFullyResolvedTemplateInstance((Classifier) attr3.getType()));
        assertFalse(TemplateUtils.isFullyResolvedTemplateInstance((Classifier) attr4.getType()));
        assertTrue(TemplateUtils.isFullyResolvedTemplateInstance((Classifier) attr1.getType()));
        assertTrue(TemplateUtils.isFullyResolvedTemplateInstance((Classifier) attr2.getType()));
    }

}
