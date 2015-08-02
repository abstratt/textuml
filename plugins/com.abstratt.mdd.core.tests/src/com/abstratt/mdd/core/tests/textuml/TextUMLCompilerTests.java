package com.abstratt.mdd.core.tests.textuml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.internal.frontend.textuml.TextUMLCompiler;
import com.abstratt.mdd.internal.frontend.textuml.node.AStart;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.Start;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;

public class TextUMLCompilerTests extends TestCase {
	
	public TextUMLCompilerTests(String name) {
		super(name);
	}
	
	private static String toParse = ""; 
	
	static {
		toParse += "model mymodel;\n";
		toParse += "package package1;\n";
		toParse += "    class class11\n";
		toParse += "        attribute attribute111 : integer;\n";
		toParse += "    end;\n";
		toParse += "    class class12\n";
		toParse += "    end;\n";
		toParse += "end;\n";
		toParse += "package package2;\n";
		toParse += "    class class21\n";
		toParse += "    end;\n";
		toParse += "end;\n";
		toParse += "end.\n";
	}
	
	public void testFindNamespace() {
		TextUMLCompiler compiler = new TextUMLCompiler();
		assertEquals("mymodel", compiler.findNamespace(toParse, 1, 1));
		assertEquals("mymodel", compiler.findNamespace(toParse, 1, 100));
		assertEquals("mymodel::package1", compiler.findNamespace(toParse, 2, 1));
		assertEquals("mymodel::package1", compiler.findNamespace(toParse, 2, 100));
		assertEquals("mymodel::package1", compiler.findNamespace(toParse, 3, 1));
		assertEquals("mymodel::package1::class11", compiler.findNamespace(toParse, 3, 5));
		assertEquals("mymodel::package1::class11", compiler.findNamespace(toParse, 3, 100));
		assertEquals("mymodel::package1::class11", compiler.findNamespace(toParse, 4, 15));
		assertEquals("mymodel::package1::class11", compiler.findNamespace(toParse, 5, 1));
		assertEquals("mymodel::package1::class11", compiler.findNamespace(toParse, 5, 100));
		assertEquals("mymodel::package1::class12", compiler.findNamespace(toParse, 6, 100));
		assertEquals("mymodel::package1::class12", compiler.findNamespace(toParse, 7, 100));
		assertEquals("mymodel::package2::class21", compiler.findNamespace(toParse, 10, 100));
	}



	public void testFindToken() {
		TextUMLCompiler compiler = new TextUMLCompiler();
		assertEquals("model", compiler.findTokenAt(toParse, 1, 1).getText());
		assertEquals(";", compiler.findTokenAt(toParse, 1, 100).getText());
		assertEquals("package", compiler.findTokenAt(toParse, 2, 1).getText());
		assertEquals(";", compiler.findTokenAt(toParse, 2, 100).getText());
		assertEquals(";", compiler.findTokenAt(toParse, 3, 1).getText());
		assertEquals("class", compiler.findTokenAt(toParse, 3, 5).getText());
		assertEquals("class11", compiler.findTokenAt(toParse, 3, 100).getText());
		assertEquals("attribute", compiler.findTokenAt(toParse, 4, 15).getText());
		assertEquals(";", compiler.findTokenAt(toParse, 5, 1).getText());
		assertEquals(";", compiler.findTokenAt(toParse, 5, 100).getText());
		assertEquals("class12", compiler.findTokenAt(toParse, 6, 100).getText());
		assertEquals("end", compiler.findTokenAt(toParse, 13, 1).getText());
		assertEquals(".", compiler.findTokenAt(toParse, 13, 4).getText());
		assertEquals(".", compiler.findTokenAt(toParse, 13, 5).getText());
		assertEquals(".", compiler.findTokenAt(toParse, 13, 100).getText());
	}

	
	public void testParse() {
		Start parsed = new TextUMLCompiler().parse(toParse);
		assertNotNull(parsed);
		ASTNode<Token, Node> tree = ASTNode.<Token, Node> buildTree(parsed.getPStart());
		assertTrue(tree.getBaseNode() instanceof AStart);
	}
	
	public void testFindModelName() {
		String modelName = new TextUMLCompiler().findModelName(toParse);
		assertEquals("mymodel", modelName);
	}
	
	public static Test suite() {
		return new TestSuite(TextUMLCompilerTests.class);
	}
}
