package com.abstratt.mdd.core.tests.frontend.textuml;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.ValueSpecificationAction;
import org.eclipse.uml2.uml.Variable;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.NotInAssociation;
import com.abstratt.mdd.frontend.core.ReturnStatementRequired;
import com.abstratt.mdd.frontend.core.ReturnValueNotExpected;
import com.abstratt.mdd.frontend.core.ReturnValueRequired;
import com.abstratt.mdd.frontend.core.TypeMismatch;
import com.abstratt.mdd.frontend.core.UnknownAttribute;
import com.abstratt.mdd.frontend.core.UnknownOperation;
import com.abstratt.mdd.frontend.core.UnknownRole;

public class ActivityTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(ActivityTests.class);
    }

    public ActivityTests(String name) {
        super(name);
    }

    static String structure = "";

    static {
        structure += "model simple;\n";
        structure += "import base;\n";
        structure += "  class SomeException end;\n";
        structure += "  class SimpleClass\n";
        structure += "    operation foo();\n";
        structure += "    operation realOp() : Double;\n";
        structure += "    operation fred();\n";
        structure += "    operation bar(arg1 : Integer, arg2 : Boolean);\n";
        structure += "    operation zar(arg1 : Integer) : Integer raises SomeException;\n";
        structure += "    attribute zoo : Integer;\n";
        structure += "    attribute fred : Integer;\n";
        structure += "    attribute realValue : Double;\n";
        structure += "    attribute optionalRealValue : Double[0,1];\n";
        structure += "    static attribute staticZoo : Integer;\n";
        structure += "    readonly static attribute K : Integer := 42;\n";
        structure += "  end;\n";
        structure += "end.";
    }

    public void testBasicStructure() throws CoreException {
        parseAndCheck(structure);
    }

    public void testCallExternalOperation() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "[mdd_extensions::External]\n";
        source += "class SimpleExternal\n";
        source += "static operation simpleExternalOperation();\n";
        source += "end;\n";
        source += "operation SimpleClass.bar;\n";
        source += "begin\n";
        source += "SimpleExternal#simpleExternalOperation();";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testCallOperation() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.bar;\n";
        source += "begin\n";
        source += "self.fred();";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testCallOperationOnMultipleObject() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "class MyClass\n";
        source += "operation op1();\n";
        source += "begin\n";
        source += "end;\n";
        source += "static operation op2();\n";
        source += "begin\n";
        source += "    MyClass extent.op1();";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = parse(source);
        assertEquals(1, problems.length);
        UnknownOperation unknownOperation = assertExpectedProblem(UnknownOperation.class, problems);
        assertEquals("simple::Set<MyClass>", unknownOperation.getClassifier());
    }

    public void testCallOperationWithArguments() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.bar;\n";
        source += "begin\n";
        source += "self.zar(10);";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testReferenceToContextualVariable() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.bar;\n";
        source += "begin\n";
        source += "var closure : {(:Integer) : Boolean}, param : Integer;\n";
        source += "param := 15;\n";
        source += "closure := (value : Integer) : Boolean { return value > param; };";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = parse(structure, source);
        FixtureHelper.assertCompilationSuccessful(problems);
    }

    public void testCreateObject() throws CoreException {
        String source2 = "";
        source2 += "package simple::pack1;\n";
        source2 += "  class SimpleClass2\n";
        source2 += "    attribute zoo : Integer;\n";
        source2 += "  end;\n";
        source2 += "end.\n";
        String source;
        source = "model simple;\n";
        source += "import pack1;\n";
        source += "operation SimpleClass.bar;\n";
        source += "begin\n";
        source += "var ref : SimpleClass2, value : Integer;";
        source += "ref := new SimpleClass2;";
        source += "value := ref.zoo;";
        source += "value := new SimpleClass2.zoo;";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source, source2);
    }
    
    public void testCreateObject_constructor() throws CoreException {
        String source2 = "";
        source2 += "package simple::pack1;\n";
        source2 += "  class SimpleClass2\n";
        source2 += "    attribute zoo : Integer;\n";
        source2 += "    constructor createSimple(value : Integer);\n";
        source2 += "  end;\n";
        source2 += "end.\n";
        String source;
        source = "model simple;\n";
        source += "import pack1;\n";
        source += "operation SimpleClass.bar;\n";
        source += "begin\n";
        source += "var ref : SimpleClass2, value : Integer;";
        source += "ref := new SimpleClass2.createSimple(1);";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source, source2);
    }


    public void testDebugInfo() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "import base;\n";
        source += "class MyClass\n";
        source += "operation foo() : Integer;\n";
        source += "begin\n";
        source += "return 1;\n";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        Operation operation = (Operation) getRepository().findNamedElement("simple::MyClass::foo",
                IRepository.PACKAGE.getOperation(), null);
        assertNotNull(operation);
        StructuredActivityNode rootNode = ActivityUtils.getRootAction(operation);
        ValueSpecificationAction found = null;
        for (TreeIterator<Element> i = UMLUtil.getAllContents(rootNode, false, false); i.hasNext();) {
            Element next = i.next();
            if (next instanceof ValueSpecificationAction)
                found = (ValueSpecificationAction) next;
        }
        assertNotNull(found);
        assertTrue(MDDExtensionUtils.isDebuggable(found));
        Integer lineNumber = MDDExtensionUtils.getLineNumber(found);
        assertNotNull(lineNumber);
        assertEquals(6, lineNumber.intValue());
    }

    public void testEmptyBody() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }
    
    public void testTransactionalBlocks() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "  begin\n";
        source += "  (* Step 1*)\n";
        source += "  end;\n";
        source += "  begin\n";
        source += "  (* Step 2*)\n";
        source += "  end;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }


    public void testCannotReturnWithoutReturnType() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "class MyClass\n";
        source += "operation foo();\n";
        source += "begin\n";
        source += "    return null;";
        source += "end;\n";
        source += "end;";
        source += "end.";
        IProblem[] problems = parse(source);
        assertExpectedProblem(ReturnValueNotExpected.class, problems);
    }

    public void testReturnRequiresAValue() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "import base;\n";
        source += "class MyClass\n";
        source += "operation foo() : Boolean;\n";
        source += "begin\n";
        source += "    return;";
        source += "end;\n";
        source += "end;";
        source += "end.";
        IProblem[] problems = parse(source);
        assertExpectedProblem(ReturnValueRequired.class, problems);
    }

    public void testReturnTypeRequiresAReturn() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "import base;\n";
        source += "class MyClass\n";
        source += "operation foo() : Boolean;\n";
        source += "begin\n";
        source += "end;\n";
        source += "end;";
        source += "end.";
        IProblem[] problems = parse(source);
        assertExpectedProblem(ReturnStatementRequired.class, problems);
    }

    public void testIf() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Integer;\n";
        source += "x := 10;\n";
        source += "if (x > 20) then\n";
        source += "self.foo();\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testIfElse() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Integer;\n";
        source += "x := 10;\n";
        source += "if x > 20 then\n";
        source += "self.foo()\n";
        source += "else\n";
        source += "self.fred();\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testIntegerLiteralValue() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "import base;\n";
        source += "class MyClass\n";
        source += "operation returnsInteger() : Integer;\n";
        source += "begin\n";
        source += "return 1;\n";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
    }

    public void testRealLiteralValue() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "import base;\n";
        source += "class MyClass\n";
        source += "operation returnsDouble() : Double;\n";
        source += "begin\n";
        source += "return 3.14;\n";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
    }

    public void testOperationRaisingDeclaredException() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.zar;\n";
        source += "begin\n";
        source += "  raise new SomeException;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testOperationRaisingUnDeclaredException() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.fred;\n";
        source += "begin\n";
        source += "  raise new SomeException;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = compile(structure, source);
        // expect a warning: raised exception type not declared for the current
        // operation
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
        assertEquals(problems[0].toString(), IProblem.Severity.WARNING, problems[0].getSeverity());
    }

    public void testReadAttribute() throws CoreException {
        String source = "";
        source = "model simple;\n";
        source += "import base;\n";
        source += "class MyClass\n";
        source += "attribute booleanAttr : Boolean;\n";
        source += "query returnsBoolean() : Boolean;\n";
        source += "begin\n";
        source += "return self.booleanAttr;";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
    }

    public void testMultipleResult() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "import base;\n";
        source += "class MyClass\n";
        source += "operation op1() : Integer[*];\n";
        source += "begin\n";
        source += "return 1;\n";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        Operation operation = (Operation) getRepository().findNamedElement("simple::MyClass::op1",
                IRepository.PACKAGE.getOperation(), null);
        Activity activity = ActivityUtils.getActivity(operation);
        assertNotNull(activity);
        StructuredActivityNode bodyNode = ActivityUtils.getBodyNode(activity);
        assertNotNull(bodyNode);
        Variable returnValue = ActivityUtils.findVariable(bodyNode, "");
        assertNotNull(returnValue);
        assertEquals("Integer", returnValue.getType().getName());
        assertEquals(0, returnValue.getLower());
        assertEquals(LiteralUnlimitedNatural.UNLIMITED, returnValue.getUpper());
    }

    public void testReadAttributeOnMultipleObject() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "primitive Integer;\n";
        source += "class RelatedClass\n";
        source += "attribute relatedAttr1 : Integer;\n";
        source += "end;\n";
        source += "class MyClass\n";
        source += "reference related : RelatedClass[*];\n";
        source += "operation op1() : Integer;\n";
        source += "begin\n";
        source += "    return self->related.relatedAttr1;";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = parse(source);
        UnknownAttribute unknownAttribute = assertExpectedProblem(UnknownAttribute.class, problems);
        assertEquals("Set<RelatedClass>", unknownAttribute.getClassifier());
    }

    public void testTraverseRelationshipOnMultipleObjectImplicitRole() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "primitive Integer;\n";
        source += "class RelatedClass1\n";
        source += "reference related2 : RelatedClass2;\n";
        source += "end;\n";
        source += "class RelatedClass2\n";
        source += "attribute relatedAttr2 : Integer;\n";
        source += "end;\n";
        source += "class MyClass\n";
        source += "reference related1 : RelatedClass1[*];\n";
        source += "operation op1() : RelatedClass2;\n";
        source += "begin\n";
        source += "    return self->related1->related2;";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = parse(source);
        assertExpectedProblem(UnknownRole.class, problems);
    }

    public void testTraverseRelationshipOnMultipleObject() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "primitive Integer;\n";
        source += "class RelatedClass1\n";
        source += "attribute related2 : RelatedClass2;\n";
        source += "end;\n";
        source += "class RelatedClass2\n";
        source += "attribute relatedAttr2 : Integer;\n";
        source += "end;\n";
        source += "association Assoc1\n";
        source += "role RelatedClass1.related2;\n";
        source += "role related1 : RelatedClass1;\n";
        source += "end;\n";
        source += "association Assoc2\n";
        source += "role MyClass.related1;\n";
        source += "role my : MyClass;\n";
        source += "end;\n";
        source += "class MyClass\n";
        source += "reference related1 : RelatedClass1[*];\n";
        source += "operation op1() : RelatedClass2;\n";
        source += "begin\n";
        source += "    return self<-Assoc2->related1<-Assoc1->related2;";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = parse(source);
        assertExpectedProblem(NotInAssociation.class, problems);
    }

    public void testReadClassAttribute() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.fred;\n";
        source += "begin\n";
        source += "  var local : Integer;\n";
        source += "  local := SimpleClass#staticZoo;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testReadConstant() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.fred;\n";
        source += "begin\n";
        source += "  var local : Integer;\n";
        source += "  local := SimpleClass#K;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testRepeat() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.zar;\n";
        source += "begin\n";
        source += "var total : Integer;\n";
        source += "total := arg1;\n";
        source += "repeat\n";
        source += "arg1 := arg1 - 1;\n";
        source += "total := total + arg1;\n";
        source += "until arg1 = 0;\n";
        source += "return total;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testSubtractExpression() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Integer, y : Integer;";
        source += "y := x - 1;";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testVarDecl() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Integer;";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testVariableAssignment() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Integer;";
        source += "x := 1;";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testWhile() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.zar;\n";
        source += "begin\n";
        source += "var total : Integer;\n";
        source += "total := arg1;\n";
        source += "while arg1 > 0 do\n";
        source += "begin\n";
        source += "arg1 := arg1 - 1;\n";
        source += "total := total + arg1;\n";
        source += "end;\n";
        source += "return total;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testWriteAttribute() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.bar;\n";
        source += "begin\n";
        source += "self.zoo := 5;";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }
    //TODO-RC in preparation for validating required vs optional values
    public void _testWriteRequiredAttributeWithOptionalValue() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.bar;\n";
        source += "begin\n";
        source += "self.realValue := self.optionalRealValue;";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = parse(structure, source);
        assertEquals(1, problems.length);
        TypeMismatch typeMismatch = assertExpectedProblem(TypeMismatch.class, problems);
    }


    public void testWriteClassAttribute() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.fred;\n";
        source += "begin\n";
        source += "  SimpleClass#staticZoo := 10;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = compile(structure, source);
        assertEquals(Arrays.asList(problems).toString(), 0, problems.length);
    }

    public void testWriteConstant() throws CoreException {
        // TODO for now we allow this
        // "The semantics is undefined for adding a new value for a structural
        // feature with
        // isReadonly=true after initialization of the owning object."

        // String source;
        // source = "model simple;\n";
        // source += "operation SimpleClass.fred;\n";
        // source += "begin\n";
        // source += "  var local : Integer;\n";
        // source += "  SimpleClass#K := 10;\n";
        // source += "end;\n";
        // source += "end.";
        // IProblem[] problems = compile(structure, source);
        // // can't assign to a constant
        // assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
    }

    public void testTryCatch() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "import base;\n";
        source += "class MyClass\n";
        source += "query returnsInteger() : Integer;\n";
        source += "begin\n";
        source += "  try \n";
        source += "      raise 1;\n";
        source += "  catch (b : Integer)\n";
        source += "      return 1;\n";
        source += "  end;\n";
        source += "end;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = compile(source);
        assertEquals(Arrays.asList(problems).toString(), 0, problems.length);
    }

    public void testTryFinally() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "  try \n";
        source += "      raise 1;\n";
        source += "  finally\n";
        source += "      return 1;\n";
        source += "  end;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] problems = compile(structure, source);
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
        // warning expected as the method does not declare it raises Integer
        // objects as exceptions
        assertEquals(problems[0].getMessage(), IProblem.Severity.WARNING, problems[0].getSeverity());
    }

    public void testLinkUnlink() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class A attribute bs : B[*]; end;\n";
        source += "  class B attribute a : A; end;\n";
        source += "  association AB role A.bs; role B.a; end;\n";
        source += "  class SimpleClass\n";
        source += "    operation linkOp(a : A, b : B); begin link AB (a := a, bs := b); end;\n";
        source += "    operation unlinkOp(a : A, b : B); begin unlink AB (a := a, bs := b); end;\n";
        source += "  end;\n";
        source += "end.";
        parseAndCheck(source);
    }
}
