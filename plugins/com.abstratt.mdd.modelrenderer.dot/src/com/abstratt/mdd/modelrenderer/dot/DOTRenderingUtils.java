package com.abstratt.mdd.modelrenderer.dot;

import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;

public class DOTRenderingUtils {
    public static void addAttribute(PrintWriter pw, String attribute, int value) {
        pw.println(attribute + " = " + value);
    }

    public static void addAttribute(PrintWriter pw, String attribute, String value) {
        pw.println(attribute + " = \"" + value + "\"");
    }

    public static void newLine(PrintWriter pw) {
        pw.print("\\n");
    }
    
	public static String escapeForDot(String labelText) {
		return StringUtils.replace(labelText, "\"", "\\\"");
	}


    
}
