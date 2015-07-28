package com.abstratt.mdd.core.tests.harness;

import org.junit.Assert;

public class AssertHelper {
	public static String trim(String toTrim) {
		if (toTrim.length() == 0)
			return toTrim;
		StringBuilder result = new StringBuilder();
		int length = toTrim.length();
		boolean space = false;
		for (int i = 0; i < length; i++) {
			char current = toTrim.charAt(i);
			if (Character.isWhitespace(current)) {
				space = result.length() > 0;
				continue;
			}
			if (Character.isJavaIdentifierPart(current))
				if (space && Character.isJavaIdentifierPart(result.charAt(result.length()-1)))
					result.append(' ');
			result.append(current);
			space = false;
		}
		return result.toString();
	}

	public static boolean areEqual(String seq1, String seq2) {
		return trim(seq1).equals(trim(seq2));
	}
	
	public static void assertStringsEqual(String seq1, String seq2) {
        Assert.assertEquals(trim(seq1), trim(seq2));
    }
	
	public static void assertStringStartsAndEndsWith(String seq1, String seq2, String toTest) {
		assertStringStartsWith(seq1, toTest);
		assertStringEndsWith(seq2, toTest);
	}
	
	public static void assertStringStartsWith(String seq1, String seq2) {
        String expectedPrefix = trim(seq1);
		String actualString = trim(seq2);
		Assert.assertTrue(expectedPrefix.length() <= actualString.length());
		int minLength = expectedPrefix.length();
		Assert.assertNotEquals("Length not expected to be zero", 0, minLength);
		Assert.assertEquals(expectedPrefix, actualString.substring(0, minLength));
    }
	
	public static void assertStringEndsWith(String seq1, String seq2) {
        String expectedSuffix = trim(seq1);
		String actualString = trim(seq2);
		Assert.assertTrue(expectedSuffix.length() <= actualString.length());		
		int minLength = expectedSuffix.length();
		Assert.assertNotEquals("Length not expected to be zero", 0, minLength);
		Assert.assertEquals(expectedSuffix, actualString.substring(actualString.length() - expectedSuffix.length()));
    }
}
