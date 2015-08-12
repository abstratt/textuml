package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;

public class StringTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(StringTests.class);
    }

    public StringTests(String name) {
        super(name);
    }

    public void testStringConcatenation() throws CoreException {
        String model = "";
        model += "model simple;\n";
        model += "import base;\n";
        model += "class StringTests\n";
        model += "    operation concatFooBar() : String;\n";
        model += "    begin\n";
        model += "        return \"foo\".add(\"bar\");\n";
        model += "    end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

    public void testStringConcatenationWithOperator() throws CoreException {
        String model = "";
        model += "model simple;\n";
        model += "import base;\n";
        model += "class StringTests\n";
        model += "    operation concatFooBar() : String;\n";
        model += "    begin\n";
        model += "        return \"foo\"+\"bar\";\n";
        model += "    end;\n";
        model += "end;\n";
        model += "end.\n";
        parseAndCheck(model);
    }

}
