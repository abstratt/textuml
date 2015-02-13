package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllFrontEndTextUMLTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllFrontEndTextUMLTests.class.getName());
		suite.addTest(ActivityTests.suite());
		suite.addTest(ReceptionTests.suite());
		suite.addTest(SignalTests.suite());
		suite.addTest(AssociationTests.suite());
		suite.addTest(ClassifierTests.suite());
		suite.addTest(CommentTests.suite());
		suite.addTest(ComponentTests.suite());
		suite.addTest(CollectionTests.suite());
		suite.addTest(FunctionTests.suite());
		suite.addTest(LookupTests.suite());
		suite.addTest(MultiplicityTests.suite());
		suite.addTest(PackageTests.suite());
		suite.addTest(SignatureTests.suite());
		suite.addTest(StateMachineTests.suite());
		suite.addTest(StereotypeTests.suite());
		suite.addTest(StringTests.suite());
		suite.addTest(TemplateTests.suite());
		suite.addTest(TypeTests.suite());
		suite.addTest(WildcardTypeTests.suite());
		return suite;
	}
}
