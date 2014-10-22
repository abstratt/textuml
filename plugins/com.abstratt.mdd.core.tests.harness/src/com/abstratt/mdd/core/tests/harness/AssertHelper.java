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
}
