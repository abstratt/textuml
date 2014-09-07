package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.util.MDDUtil;

public class CommentTests extends AbstractRepositoryBuildingTests {

	public static Test suite() {
		return new TestSuite(CommentTests.class);
	}

	public CommentTests(String name) {
		super(name);
	}

	public void testComments() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "(* Class comment *)\n";
		source += "class AClass\n";
		source += "(* Attribute comment *)\n";
		source += "attribute attrib1 : Integer\n";
		source += "(* Attribute inv comment *)\n";
		source += "invariant attrib1Inv { false };\n";
		source += "(* Operation comment *)\n";
		source += "operation op1() : Integer\n";
		source += "(* Precondition 1 comment *)\n";
		source += "precondition pre1 { false };\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		
		assertEquals("Class comment", MDDUtil.getDescription(get("someModel::AClass", Literals.CLASS)));
		assertEquals("Attribute comment", MDDUtil.getDescription(get("someModel::AClass::attrib1", Literals.PROPERTY)));
		assertEquals("Attribute inv comment", MDDUtil.getDescription(get("someModel::AClass::attrib1Inv", Literals.CONSTRAINT)));
		assertEquals("Operation comment", MDDUtil.getDescription(get("someModel::AClass::op1", Literals.OPERATION)));
		assertEquals("Precondition 1 comment", MDDUtil.getDescription(get("someModel::AClass::op1::pre1", Literals.CONSTRAINT)));

	}
}
