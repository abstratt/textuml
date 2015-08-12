package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;

public class MultiplicityTests extends AbstractRepositoryBuildingTests {

	public MultiplicityTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(MultiplicityTests.class);
	}

	public void testFrom0To1() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "class Class1\n";
		source += "  attribute attr1 : Integer[0,1];\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class1 = getRepository().findNamedElement("simple::Class1", UMLPackage.Literals.CLASS, null);
		assertNotNull(class1);
		Property attr1 = class1.getAttribute("attr1", null);
		assertNotNull(attr1);
		assertEquals(0, attr1.getLower());
		assertEquals(1, attr1.getUpper());

		ValueSpecification lowerValue = attr1.getLowerValue();
		assertNotNull(lowerValue);
		assertTrue(lowerValue instanceof LiteralInteger);
		assertEquals(0, lowerValue.integerValue());

		ValueSpecification upperValue = attr1.getUpperValue();
		assertNotNull(upperValue);
		assertTrue(upperValue instanceof LiteralUnlimitedNatural);
		assertEquals(1, ((LiteralUnlimitedNatural) upperValue).unlimitedValue());
	}

	public void testFrom1To1ShortForm() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "class Class1\n";
		source += "  attribute attr1 : Integer[1];\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class1 = getRepository().findNamedElement("simple::Class1", UMLPackage.Literals.CLASS, null);
		assertNotNull(class1);
		Property attr1 = class1.getAttribute("attr1", null);
		assertNotNull(attr1);
		assertEquals(1, attr1.getLower());
		assertEquals(1, attr1.getUpper());

		ValueSpecification lowerValue = attr1.getLowerValue();
		assertNotNull(lowerValue);
		assertTrue(lowerValue instanceof LiteralInteger);
		assertEquals(1, lowerValue.integerValue());

		ValueSpecification upperValue = attr1.getUpperValue();
		assertNotNull(upperValue);
		assertTrue(upperValue instanceof LiteralUnlimitedNatural);
		assertEquals(1, ((LiteralUnlimitedNatural) upperValue).unlimitedValue());
	}

	public void testFrom0ToUnlimitedShortForm() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "class Class1\n";
		source += "  attribute attr1 : Integer[*];\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class1 = getRepository().findNamedElement("simple::Class1", UMLPackage.Literals.CLASS, null);
		assertNotNull(class1);
		Property attr1 = class1.getAttribute("attr1", null);
		assertNotNull(attr1);
		assertEquals(0, attr1.getLower());
		assertEquals(LiteralUnlimitedNatural.UNLIMITED, attr1.getUpper());

		ValueSpecification lowerValue = attr1.getLowerValue();
		assertNotNull(lowerValue);
		assertTrue(lowerValue instanceof LiteralInteger);
		assertEquals(0, lowerValue.integerValue());

		ValueSpecification upperValue = attr1.getUpperValue();
		assertNotNull(upperValue);
		assertTrue(upperValue instanceof LiteralUnlimitedNatural);
		assertEquals(LiteralUnlimitedNatural.UNLIMITED, ((LiteralUnlimitedNatural) upperValue).unlimitedValue());
	}

	public void testFrom1To1() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "class Class1\n";
		source += "  attribute attr1 : Integer[1,1];\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class1 = getRepository().findNamedElement("simple::Class1", UMLPackage.Literals.CLASS, null);
		assertNotNull(class1);
		Property attr1 = class1.getAttribute("attr1", null);
		assertNotNull(attr1);
		assertEquals(1, attr1.getLower());
		assertEquals(1, attr1.getUpper());

		ValueSpecification lowerValue = attr1.getLowerValue();
		assertNotNull(lowerValue);
		assertTrue(lowerValue instanceof LiteralInteger);
		assertEquals(1, lowerValue.integerValue());

		ValueSpecification upperValue = attr1.getUpperValue();
		assertNotNull(upperValue);
		assertTrue(upperValue instanceof LiteralUnlimitedNatural);
		assertEquals(1, ((LiteralUnlimitedNatural) upperValue).unlimitedValue());
	}

	public void testFrom1ToUnlimited() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "class Class1\n";
		source += "  attribute attr1 : Integer[1,*];\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class1 = getRepository().findNamedElement("simple::Class1", UMLPackage.Literals.CLASS, null);
		assertNotNull(class1);
		Property attr1 = class1.getAttribute("attr1", null);
		assertNotNull(attr1);
		assertEquals(1, attr1.getLower());
		assertEquals(LiteralUnlimitedNatural.UNLIMITED, attr1.getUpper());

		ValueSpecification lowerValue = attr1.getLowerValue();
		assertNotNull(lowerValue);
		assertTrue(lowerValue instanceof LiteralInteger);
		assertEquals(1, lowerValue.integerValue());

		ValueSpecification upperValue = attr1.getUpperValue();
		assertNotNull(upperValue);
		assertTrue(upperValue instanceof LiteralUnlimitedNatural);
		assertEquals(LiteralUnlimitedNatural.UNLIMITED, ((LiteralUnlimitedNatural) upperValue).unlimitedValue());
	}

	public void testFrom0ToUnlimited() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "class Class1\n";
		source += "  attribute attr1 : Integer[0,*];\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class1 = getRepository().findNamedElement("simple::Class1", UMLPackage.Literals.CLASS, null);
		assertNotNull(class1);
		Property attr1 = class1.getAttribute("attr1", null);
		assertNotNull(attr1);
		assertEquals(0, attr1.getLower());
		assertEquals(LiteralUnlimitedNatural.UNLIMITED, attr1.getUpper());

		ValueSpecification lowerValue = attr1.getLowerValue();
		assertNotNull(lowerValue);
		assertTrue(lowerValue instanceof LiteralInteger);
		assertEquals(0, lowerValue.integerValue());

		ValueSpecification upperValue = attr1.getUpperValue();
		assertNotNull(upperValue);
		assertTrue(upperValue instanceof LiteralUnlimitedNatural);
		assertEquals(LiteralUnlimitedNatural.UNLIMITED, ((LiteralUnlimitedNatural) upperValue).unlimitedValue());
	}
}
