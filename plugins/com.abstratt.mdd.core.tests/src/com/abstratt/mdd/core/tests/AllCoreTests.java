package com.abstratt.mdd.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCoreTests {
    public static String PLUGIN_ID = AllCoreTests.class.getPackage().getName();

    public static Test suite() {
        TestSuite suite = new TestSuite(AllCoreTests.class.getName());
        suite.addTest(com.abstratt.mdd.core.tests.AllMDDCoreTests.suite());
        suite.addTest(com.abstratt.mdd.core.tests.frontend.textuml.AllFrontEndTextUMLTests.suite());
        suite.addTest(com.abstratt.mdd.core.tests.textuml.AllTextUMLTests.suite());
        // commented out for releasing, will be back in 1.8 or later
        // suite.addTest(com.abstratt.mdd.core.tests.frontend.builder.AllFrontEndBuilderTests.suite());
        return suite;
    }
}
