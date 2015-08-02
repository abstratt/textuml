package com.abstratt.mdd.core.tests.textuml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;

import com.abstratt.mdd.frontend.textuml.grammar.analysis.AnalysisAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.Start;
import com.abstratt.mdd.internal.frontend.textuml.core.TextUMLCompiler;
import com.abstratt.mdd.internal.frontend.textuml.core.TextUMLFormatter;

public class TextUMLFormatterTests extends TestCase {
	
	private static final String INDENTATION = "    ";

	private static final String LINE_ENDING = System.getProperty("line.separator");

	public TextUMLFormatterTests(String name) {
		super(name);
	}

	public void testEmptyModel() throws CoreException {
		String toFormat = "model    bank;        \n\t   end.   ";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}
	
	public void testEscaping() throws CoreException {
		String toFormat = "model \\model; class \\class attribute \\attribute : Integer; end; end.";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model \\model;<LE><LE>class \\class<LE><LE><TAB>attribute \\attribute : Integer;<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}
	
	public void testClassWithAttributes() throws CoreException {
		String toFormat = "model    bank; class Foo attribute a1 : Integer; end;   end.   ";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>attribute a1 : Integer;<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}
	
	public void testIfElse() throws CoreException {
		String toFormat = "model bank;class Foo operation a();begin if(condition)then obj1.method() else obj2.method();end;end;end.";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>operation a();<LE><TAB>begin<LE><TAB><TAB>if (condition) then<LE><TAB><TAB><TAB>obj1.method()<LE><TAB><TAB>else<LE><TAB><TAB><TAB>obj2.method();<LE><TAB>end;<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}
	
	public void testIfElseif() throws CoreException {
		String toFormat = "model bank;class Foo operation a();begin if(condition1)then obj1.method() elseif(condition2)then obj2.method();end;end;end.";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>operation a();<LE><TAB>begin<LE><TAB><TAB>if (condition1) then<LE><TAB><TAB><TAB>obj1.method()<LE><TAB><TAB>elseif (condition2) then<LE><TAB><TAB><TAB>obj2.method();<LE><TAB>end;<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}
	
	public void testIfElseifElse() throws CoreException {
		String toFormat = "model bank;class Foo operation a();begin if(condition1)then obj1.method() elseif(condition2)then obj2.method() else obj3.method();end;end;end.";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>operation a();<LE><TAB>begin<LE><TAB><TAB>if (condition1) then<LE><TAB><TAB><TAB>obj1.method()<LE><TAB><TAB>elseif (condition2) then<LE><TAB><TAB><TAB>obj2.method()<LE><TAB><TAB>else<LE><TAB><TAB><TAB>obj3.method();<LE><TAB>end;<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}
	
	public void testIfNoElse() throws CoreException {
		String toFormat = "model bank;class Foo operation a();begin if(condition)then obj1.method();end;end;end.";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>operation a();<LE><TAB>begin<LE><TAB><TAB>if (condition) then<LE><TAB><TAB><TAB>obj1.method();<LE><TAB>end;<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}
	
	public void testWordyBlock() throws CoreException {
		String toFormat = "model    bank; class Foo operation a(); begin a := 1; return a+1; end; end;   end.   ";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>operation a();<LE><TAB>begin<LE><TAB><TAB>a := 1;<LE><TAB><TAB>return a + 1;<LE><TAB>end;<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}

	public void testSimpleBlock() throws CoreException {
		String toFormat = "model    bank; class Foo attribute  a : Integer := { a := 1; return a+1; }; end;   end.   ";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>attribute a : Integer := {<LE><TAB><TAB>a := 1;<LE><TAB><TAB>return a + 1;<LE><TAB>};<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}
	
	public void testStateMachine() throws CoreException {
		String toFormat = "model bank; class Foo statemachine SM state s1 transition to s2; end; state s2 end; state s3 entry{};exit{}; end; end; end;  end.   ";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>statemachine SM<LE><LE><TAB><TAB>state s1<LE><TAB><TAB><TAB>transition to s2;<LE><TAB><TAB>end;<LE><LE><TAB><TAB>state s2 end;<LE><LE><TAB><TAB>state s3<LE><TAB><TAB><TAB>entry {<LE><TAB><TAB><TAB>};<LE><TAB><TAB><TAB>exit {<LE><TAB><TAB><TAB>};<LE><TAB><TAB>end;<LE><LE><TAB>end;<LE><LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}

	// disabled, this is a bug with identifiers that embed digits that needs fixing 
	public void _testClassSpecializes() throws CoreException {
		String toFormat = "model bank; class C1    specializes    C2 end; end.";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class C1 specializes C2<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}

	
	public void testOperationParameters() throws CoreException {
		String toFormat = "model bank; class Foo operation op1([s] out a1 : Boolean) [v] : Integer; begin end; end;   end.   ";
		Start parseTree = new TextUMLCompiler().parse(toFormat);
		checkFormatting("model bank;<LE><LE>class Foo<LE><LE><TAB>operation op1([s] out a1 : Boolean) [v] : Integer;<LE><TAB>begin<LE><TAB>end;<LE>end;<LE><LE>end.", new TextUMLFormatter().format(
				parseTree, new AnalysisAdapter()));
	}

	
	public void checkFormatting(String expected, String actual) {
		assertEquals(replaceTags(expected, LINE_ENDING, INDENTATION), actual);
	}
	
	private String replaceTags(String original, String lineEnding, String indentation) {
		return original.replaceAll("<LE>", lineEnding).replaceAll("<TAB>", indentation);
	}
	
	public void testUnformat() {
		String formatted = "\n\n\nabc123 456asd*bcdf/fdhjgfdr  : foo  \t\t+aaa eee";
		String unformatted = "abc123 456asd*bcdf/fdhjgfdr:foo+aaa eee";
		assertEquals(unformatted, unformat(formatted));
	}

	public static Test suite() {
		return new TestSuite(TextUMLFormatterTests.class);
	}

	private String unformat(String formatted) {
		StringBuffer output = new StringBuffer(formatted.length());
		boolean needSpace = false;
		boolean foundWord = false;
		for (int i = 0; i < formatted.length(); i++) {
			char current = formatted.charAt(i);
			if (Character.isWhitespace(current)) {
				if (foundWord)
					 needSpace = true;
				foundWord = false;
				continue;
			}
			if (Character.isLetter(current)) {
				foundWord = true;
				if (needSpace)
					output.append(' ');
			} else if (Character.isDigit(current)) {
				foundWord = foundWord && Character.isLetterOrDigit(current);
				if (needSpace)
					output.append(' ');
			} else
				foundWord = false;
			output.append(current);
			needSpace = false;
		}
		return output.toString();
	}
}
