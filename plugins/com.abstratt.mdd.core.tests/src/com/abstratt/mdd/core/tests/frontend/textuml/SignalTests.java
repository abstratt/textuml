package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.frontend.core.IProblem;
import com.abstratt.mdd.frontend.core.MissingRequiredArgument;

public class SignalTests extends AbstractRepositoryBuildingTests {

	public static Test suite() {
		return new TestSuite(SignalTests.class);
	}

	public SignalTests(String name) {
		super(name);
	}
	
	public void testSend() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "  signal SimpleSignal end;\n";
		source += "  class SimpleClass\n";
		source += "    operation op1();\n";
		source += "    begin\n";
		source += "        send SimpleSignal() to self;";
		source += "    end;\n";
		source += "  end;\n";
		source += "end.";
		parseAndCheck(source);
	}
	
	public void testSend_MissingArgument() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "  signal SimpleSignal\n";
		source += "      attribute attr1 : String;\n";
		source += "  end;\n";
		source += "  class SimpleClass\n";
		source += "    operation op1();\n";
		source += "    begin\n";
		source += "        send SimpleSignal() to self;";
		source += "    end;\n";
		source += "  end;\n";
		source += "end.";
		IProblem[] result = parse(source);
		assertEquals(1, result.length);
		assertSame(result[0].toString(), MissingRequiredArgument.class, result[0].getClass());
	}

	public void testSend_WithArgument() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "  signal SimpleSignal\n";
		source += "      attribute attr1 : String;\n";
		source += "      attribute attr2 : Boolean;\n";
		source += "      attribute attr3 : Integer[0,1];\n";
		source += "  end;\n";
		source += "  class SimpleClass\n";
		source += "    operation op1();\n";
		source += "    begin\n";
		source += "        send SimpleSignal(attr1 := \"aaa\", attr2 := true) to self;";
		source += "    end;\n";
		source += "  end;\n";
		source += "end.";
		parseAndCheck(source);
	}
}
