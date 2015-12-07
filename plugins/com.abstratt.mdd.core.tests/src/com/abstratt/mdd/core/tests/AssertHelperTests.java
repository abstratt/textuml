package com.abstratt.mdd.core.tests;

import com.abstratt.mdd.core.tests.harness.AssertHelper;

import junit.framework.TestCase;

public class AssertHelperTests extends TestCase {
    public void testComplexExpressions() {
        TestCase.assertEquals("(4+5.47)", AssertHelper.trim("( 4 + 5.47 )"));
        TestCase.assertEquals("4+(5.47)*2", AssertHelper.trim("4 +(5.47) * 2"));
        TestCase.assertEquals("int sum=0;for(Integer e:listOfInts){sum=e+1;}",
                AssertHelper.trim("int sum = 0;\nfor ( Integer e : listOfInts ) {\n\tsum = e + 1 ;\n}"));
    }

    public void testOneWord() {
        TestCase.assertEquals("foo", AssertHelper.trim("foo"));
        TestCase.assertEquals("foo", AssertHelper.trim("foo "));
        TestCase.assertEquals("foo", AssertHelper.trim(" foo"));
        TestCase.assertEquals("foo", AssertHelper.trim(" foo "));
    }

    public void testOneWordWithMultipleSpaces() {
        TestCase.assertEquals("foo", AssertHelper.trim("foo  "));
        TestCase.assertEquals("foo", AssertHelper.trim("  foo"));
        TestCase.assertEquals("foo", AssertHelper.trim("  foo  "));
    }

    public void testSingleChar() {
        TestCase.assertEquals("", AssertHelper.trim(""));
        TestCase.assertEquals("a", AssertHelper.trim("a"));
        TestCase.assertEquals("a", AssertHelper.trim("a "));
        TestCase.assertEquals("a", AssertHelper.trim(" a "));
    }

    public void testTwoWord() {
        TestCase.assertEquals("foo bar", AssertHelper.trim("foo bar"));
        TestCase.assertEquals("foo bar", AssertHelper.trim("foo bar "));
        TestCase.assertEquals("foo bar", AssertHelper.trim(" foo bar"));
        TestCase.assertEquals("foo bar", AssertHelper.trim(" foo bar "));
    }

    public void testTwoWordWithMultipleSpaces() {
        TestCase.assertEquals("foo bar", AssertHelper.trim("foo  bar  "));
        TestCase.assertEquals("foo bar", AssertHelper.trim("  foo  bar"));
        TestCase.assertEquals("foo bar", AssertHelper.trim("  foo  bar  "));
    }

}
