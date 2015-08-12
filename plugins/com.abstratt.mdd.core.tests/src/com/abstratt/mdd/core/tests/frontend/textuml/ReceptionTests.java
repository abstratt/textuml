package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;

public class ReceptionTests extends AbstractRepositoryBuildingTests {

	public static Test suite() {
		return new TestSuite(ReceptionTests.class);
	}

	public ReceptionTests(String name) {
		super(name);
	}

	public void testReception() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "  signal SimpleSignal end;\n";
		source += "  class SimpleClass\n";
		source += "    reception simpleReception(s:SimpleSignal);\n";
		source += "    begin end;\n";
		source += "  end;\n";
		source += "end.";
		parseAndCheck(source);
		Class simpleClass = getClass("simple::SimpleClass");
		Signal simpleSignal = get("simple::SimpleSignal", UMLPackage.Literals.SIGNAL);
		Reception reception = simpleClass.getOwnedReception("simpleReception", null, null);
		assertEquals(1, reception.getOwnedParameters().size());
		assertSame(simpleSignal, reception.getOwnedParameters().get(0).getType());
		assertSame(simpleSignal, reception.getSignal());
		assertEquals(1, reception.getMethods().size());
	}

}
