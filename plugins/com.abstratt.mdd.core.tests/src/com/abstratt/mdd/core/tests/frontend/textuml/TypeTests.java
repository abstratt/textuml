package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.frontend.core.TypeMismatch;

public class TypeTests extends AbstractRepositoryBuildingTests {

	static String structure = "";

	static {
		structure += "model tests;\n";
		structure += "import base;\n";
		structure += "class Struct\n";
		structure += "  attribute attrib1 :  Integer;\n";
		structure += "  attribute attrib2 :  Boolean;\n";
		structure += "  attribute attrib3 :  any;\n";
		structure += "  operation op1();\n";
		structure += "end;\n";
		structure += "class SubStruct1 specializes Struct\n";
		structure += "end;\n";
		structure += "class SubStruct2 specializes Struct\n";
		structure += "end;\n";
		structure += "end.";
	}

	public static Test suite() {
		return new TestSuite(TypeTests.class);
	}

	public TypeTests(String name) {
		super(name);
	}

	public void testAssignAnyToBoolean() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib3 := true;\n";
		behavior += "  self.attrib2 := self.attrib3;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignAnyToClass() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var source : any, target : Struct;\n";
		behavior += "  source := null;\n";
		behavior += "  target := source;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignAnyToInteger() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib3 := 10;\n";
		behavior += "  self.attrib1 := self.attrib3;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignBooleanToAny() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib3 := true;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignBooleanToBoolean() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib2 := true;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignBooleanToInteger() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib1 := true;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		IProblem[] result = compile(structure, behavior);
		FixtureHelper.assertTrue(result, result.length == 1);
		assertTrue(result[0].toString(), result[0] instanceof TypeMismatch);
	}

	public void testAssignClassToAny() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var source : Struct, target : any;\n";
		behavior += "  source := null;\n";
		behavior += "  target := source;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignClassToClass() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var sub1 : SubStruct1, sub2 : SubStruct1;\n";
		behavior += "  sub1 := null;\n";
		behavior += "  sub2 := sub1;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignIntegerToAny() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib3 := 10;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignIntegerToBoolean() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib2 := 20;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		IProblem[] result = compile(structure, behavior);
		FixtureHelper.assertTrue(result, result.length == 1);
		assertTrue(result[0].toString(), result[0] instanceof TypeMismatch);
		assertEquals(4, result[0].getAttribute(IProblem.LINE_NUMBER));
	}

	public void testAssignIntegerToInteger() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib1 := 10;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignSubClass1ToSubClass2() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var source : SubStruct1, target : SubStruct2;\n";
		behavior += "  source := null;\n";
		behavior += "  target := source;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		IProblem[] result = compile(structure, behavior);
		FixtureHelper.assertTrue(result, result.length == 1);
		assertTrue(result[0].toString(), result[0] instanceof TypeMismatch);
		assertEquals(6, result[0].getAttribute(IProblem.LINE_NUMBER));
	}

	public void testAssignSubClassToSuperClass() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var sub : SubStruct1, super : Struct;\n";
		behavior += "  sub := null;\n";
		behavior += "  super := sub;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignSuperClassToSubClass() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var sub : SubStruct1, super : Struct;\n";
		behavior += "  super := null;\n";
		behavior += "  sub := super;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		IProblem[] result = compile(structure, behavior);
		FixtureHelper.assertTrue(result, result.length == 1);
		assertTrue(result[0].toString(), result[0] instanceof TypeMismatch);
		assertEquals(6, result[0].getAttribute(IProblem.LINE_NUMBER));
	}

	public void testCastBooleanToInteger() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  self.attrib1 := (true as Integer);\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignCompatibleTupleToAnonymousTupleAndViceVersa() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "datatype MyTuple\n";
		behavior += "  attribute aString : String;\n";
		behavior += "  attribute anInteger : Integer;\n";
		behavior += "end;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var source : { :String, :Integer}, target : MyTuple;\n";
		behavior += "  target := source;\n";
		behavior += "  source := target;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignCompatibleTupleLiteralToAnonymousTuple() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "datatype MyTuple\n";
		behavior += "  attribute aString : String;\n";
		behavior += "  attribute anInteger : Integer;\n";
		behavior += "end;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var target : MyTuple;\n";
		behavior += "  target := {aString := \"value\", anInteger := 10};\n";
		behavior += "end;\n";
		behavior += "end.\n";
		parseAndCheck(structure, behavior);
	}

	public void testAssignIncompatibleAnonymousTuples() throws CoreException {
		String behavior = "model tests;\n";
		behavior += "operation Struct.op1;\n";
		behavior += "begin\n";
		behavior += "  var source : {aString : String, anInteger : Integer}, target : {anotherString : String, anInteger : Integer};\n";
		behavior += "  target := source;\n";
		behavior += "end;\n";
		behavior += "end.\n";
		IProblem[] errors = parse(structure, behavior);
		FixtureHelper.assertTrue(errors, errors.length == 1);
		FixtureHelper.assertTrue(errors, errors[0] instanceof TypeMismatch);
	}
}
