package com.abstratt.resman.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllResManTests {
    public static Test suite() {
        TestSuite suite = new TestSuite(AllResManTests.class.getName());
        suite.addTest(ResourceManagerTests.suite());
        return suite;
    }

}
