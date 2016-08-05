package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Type;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;

public class ExpressionTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(ExpressionTests.class);
    }

    public ExpressionTests(String name) {
        super(name);
    }
    
    static String structure = "";

    static {
        structure += "model simple;\n";
        structure += "import base;\n";
        structure += "  class SimpleClass\n";
        structure += "    operation foo();\n";
        structure += "  end;\n";
        structure += "end.";
    }
    

    public void testIntegerBinaryOperands() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Integer, y : Integer;";
        source += "y := x + 1;";
        source += "y := x - 1;";
        source += "y := x * 2;";
        source += "y := x / 2;";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }
    
    public void testStringConcatenation() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : String;\n";
        source += "x := \"a\" + 1;\n";
        source += "x := (\"a\" + 1) + \"b\";\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testNotNull() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Boolean, y : String;\n";
        source += "x := ?y;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }


}
