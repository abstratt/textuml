package com.abstratt.mdd.core.tests.textuml;

public class TextUMLTestUtils {
    public static String replaceTags(String original, String lineEnding, String indentation) {
        return original.replaceAll("<LE>", lineEnding).replaceAll("<TAB>", indentation);
    }
}
