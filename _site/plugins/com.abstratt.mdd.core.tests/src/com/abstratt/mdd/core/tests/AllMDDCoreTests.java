package com.abstratt.mdd.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllMDDCoreTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllMDDCoreTests.class.getName());
		suite.addTest(NameTests.suite());
		suite.addTest(IRepositoryTests.suite());
		suite.addTest(IRepositoryAliasingTests.suite());
		suite.addTest(IRepositoryFindTests.suite());
		return suite;
	}
}
