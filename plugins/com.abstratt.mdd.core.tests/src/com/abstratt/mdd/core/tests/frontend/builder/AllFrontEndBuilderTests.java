package com.abstratt.mdd.core.tests.frontend.builder;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllFrontEndBuilderTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllFrontEndBuilderTests.class.getName());
		suite.addTestSuite(ActionBuilderTests.class);
		suite.addTestSuite(PackageBuilderTests.class);
		suite.addTestSuite(ClassifierBuilderTests.class);
		suite.addTestSuite(ConditionalBuilderSetTests.class);
		suite.addTestSuite(ProfileTests.class);
		suite.addTestSuite(StereotypeTests.class);
		return suite;

	}
}
