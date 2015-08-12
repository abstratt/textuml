package com.abstratt.mdd.core.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.abstratt.mdd.core.Name;

public class NameTests extends TestCase {
	public NameTests(String testCase) {
		super(testCase);
	}

	public void testBasic() {
		String[] segments = { "this", "is", "a", "test" };
		Name name = new Name("this::is::a::test");
		assertEquals(name.toString(), segments.length, name.segmentCount());
		for (int i = 0; i < segments.length; i++)
			assertEquals(segments[i], name.segment(i));
	}

	public void testEscaped() {
		String[] segments = { "this", "is::a", "test" };
		Name name = new Name("this::is\\::a::test");
		assertEquals(segments.length, name.segmentCount());
		for (int i = 0; i < segments.length; i++)
			assertEquals(segments[i], name.segment(i));
	}

	public static Test suite() {
		return new TestSuite(NameTests.class);
	}
}
