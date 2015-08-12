package com.abstratt.mdd.core.tests.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTextUMLTests {
    public static Test suite() {
        TestSuite suite = new TestSuite(AllTextUMLTests.class.getName());
        suite.addTest(TextUMLCompilerTests.suite());
        suite.addTest(TextUMLFormatterTests.suite());
        return suite;
    }
}
