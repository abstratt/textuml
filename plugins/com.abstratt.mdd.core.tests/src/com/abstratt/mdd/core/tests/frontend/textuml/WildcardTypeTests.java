package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Variable;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.FeatureUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;

public class WildcardTypeTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(WildcardTypeTests.class);
    }

    public WildcardTypeTests(String name) {
        super(name);
    }

    public void testOperationWildcardTypeInResult() throws CoreException {
        String model = "";
        model += "model tests;\n";
        model += "import base;\n";
        model += "class MyClass1\n";
        model += "  operation <T> op1() : T;\n";
        model += "end;\n";
        model += "end.";
        parseAndCheck(model);

        Operation op1 = getOperation("tests::MyClass1::op1");
        Type wildcardType = op1.getReturnResult().getType();
        assertTrue(MDDExtensionUtils.isWildcardTypeContext(op1));
        assertTrue(MDDExtensionUtils.isWildcardType(wildcardType));

        assertSame(op1, MDDExtensionUtils.getWildcardTypeContext(wildcardType));
        assertTrue(MDDExtensionUtils.getWildcardTypes(op1).contains(wildcardType));
    }
    
    public void testOperationWildcardTypeVisibleOnlyInOperation() throws CoreException {
        String model = "";
        model += "model tests;\n";
        model += "import base;\n";
        model += "class MyClass1\n";
        model += "  operation <T> op1() : T;\n";
        model += "  operation op2() : T;\n";
        model += "end;\n";
        model += "end.";
        IProblem[] results = parse(model);
        FixtureHelper.assertTrue(results, results.length == 1);
        assertEquals(5, results[0].getAttribute(IProblem.LINE_NUMBER));
    }

    public void testOperationWildcardTypeInClosureParameter() throws CoreException {
        String model = "";
        model += "model tests;\n";
        model += "import base;\n";
        model += "class MyClass1\n";
        model += "  operation <T> op1(par1 : {() : T});\n";
        model += "end;\n";
        model += "end.";
        parseAndCheck(model);

        Operation op1 = getOperation("tests::MyClass1::op1");

        Parameter par1 = FeatureUtils.getInputParameters(op1.getOwnedParameters()).get(0);
        Parameter signatureResult = FeatureUtils.getReturnParameter(MDDExtensionUtils.getSignatureParameters(par1.getType()));

        assertTrue(MDDExtensionUtils.isWildcardTypeContext(op1));
        assertTrue(MDDExtensionUtils.isWildcardType(signatureResult.getType()));
        assertSame(op1, MDDExtensionUtils.getWildcardTypeContext(signatureResult.getType()));
    }

    public void testOperationWildcardTypeReplacedInResult() throws CoreException {
        String model = "";
        model += "model tests;\n";
        model += "import base;\n";
        model += "class MyClass1\n";
        model += "  static operation <T> op1(par1 : T) : T;\n";
        model += "end;\n";
        model += "class MyClass2\n";
        model += "  operation op2();\n";
        model += "  begin\n";
        model += "      var local;\n";
        model += "      local := MyClass1#op1(1);\n";
        model += "  end;\n";
        model += "end;\n";
        model += "end.";
        parseAndCheck(model);

        Operation op2 = getOperation("tests::MyClass2::op2");
        StructuredActivityNode firstChild = (StructuredActivityNode) ActivityUtils.getRootAction(op2).getContainedNode(null, false, UMLPackage.Literals.STRUCTURED_ACTIVITY_NODE);
        Variable localVar = ActivityUtils.findVariable(firstChild, "local");
        Class integerType = getClass("mdd_types::Integer");
        assertSame(integerType, localVar.getType());
    }

    public void testOperationWildcardTypeInClosureReplaced() throws CoreException {
        String model = "";
        model += "model tests;\n";
        model += "import base;\n";
        model += "class MyClass1\n";
        model += "  static operation <T1, T2> op1(par1 : {(a : T1) : T2}) : T1;\n";
        model += "  operation op2();\n";
        model += "  begin\n";
        model += "      var local;\n";
        model += "      local := MyClass1#op1((a : Boolean) : Integer { 1 });\n";
        model += "  end;\n";
        model += "end;\n";
        model += "end.";
        parseAndCheck(model);
        
        Operation op2 = getOperation("tests::MyClass1::op2");
        StructuredActivityNode firstChild = (StructuredActivityNode) ActivityUtils.getRootAction(op2).getContainedNode(null, false, UMLPackage.Literals.STRUCTURED_ACTIVITY_NODE);
        Variable localVar = ActivityUtils.findVariable(firstChild, "local");
        Class integerType = getClass("mdd_types::Boolean");
        assertSame(integerType, localVar.getType());
    }
    
    public void testInstanceOperationWildcardTypeInClosureReplaced() throws CoreException {
        String model = "";
        model += "model tests;\n";
        model += "import base;\n";
        model += "class MyClass1\n";
        model += "  operation <T1, T2> op1(par1 : {(a : T1) : T2}) : T1;\n";
        model += "  operation op2();\n";
        model += "  begin\n";
        model += "      var local;\n";
        model += "      local := self.op1((a : Boolean) : Integer { 1 });\n";
        model += "  end;\n";
        model += "end;\n";
        model += "end.";
        parseAndCheck(model);
        
        Operation op2 = getOperation("tests::MyClass1::op2");
        StructuredActivityNode firstChild = (StructuredActivityNode) ActivityUtils.getRootAction(op2).getContainedNode(null, false, UMLPackage.Literals.STRUCTURED_ACTIVITY_NODE);
        Variable localVar = ActivityUtils.findVariable(firstChild, "local");
        Class integerType = getClass("mdd_types::Boolean");
        assertSame(integerType, localVar.getType());
    }
}
