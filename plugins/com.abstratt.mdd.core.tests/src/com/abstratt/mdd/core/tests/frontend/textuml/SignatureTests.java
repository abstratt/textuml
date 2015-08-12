package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Type;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.frontend.core.TypeMismatch;

public class SignatureTests extends AbstractRepositoryBuildingTests {

	public static Test suite() {
		return new TestSuite(SignatureTests.class);
	}

	public SignatureTests(String name) {
		super(name);
	}

	public void testFunctionAsParameter() throws CoreException {
		String model = "model tests;\n";
		model += "import base;\n";
		model += "class Test\n";
		model += "operation op1(filter: {(: Integer) : Boolean});\n";
		model += "begin\n";
		model += "end;\n";
		model += "end;\n";
		model += "end.\n";
		parseAndCheck(model);
		Type integerType = (Type) getRepository()
		        .findNamedElement("base::Integer", IRepository.PACKAGE.getType(), null);
		assertNotNull(integerType);
		Type booleanType = (Type) getRepository()
		        .findNamedElement("base::Boolean", IRepository.PACKAGE.getType(), null);
		assertNotNull(booleanType);
		Operation op = (Operation) getRepository().findNamedElement("tests::Test::op1",
		        IRepository.PACKAGE.getOperation(), null);
		assertNotNull(op);
		assertEquals(1, op.getOwnedParameters().size());
		Parameter opParam = op.getOwnedParameters().get(0);
		Type paramType = opParam.getType();
		assertTrue("found: " + paramType, MDDExtensionUtils.isSignature(paramType));
		assertEquals(2, MDDExtensionUtils.getSignatureParameters(paramType).size());
		Parameter signatureParam = MDDExtensionUtils.getSignatureParameters(paramType).get(0);
		assertNull(signatureParam.getName());
		assertEquals(ParameterDirectionKind.IN_LITERAL, signatureParam.getDirection());
		assertSame(integerType, signatureParam.getType());
		Parameter signatureReturn = MDDExtensionUtils.getSignatureParameters(paramType).get(1);
		assertNull(signatureReturn.getName());
		assertEquals(ParameterDirectionKind.RETURN_LITERAL, signatureReturn.getDirection());
		assertSame(booleanType, signatureReturn.getType());
	}

	public void testSignatureBehaviorCompatibility() throws CoreException {
		String model = "model tests;\n";
		model += "import mdd_types;\n";
		model += "class Test\n";
		model += "operation op1();\n";
		model += "begin\n";
		model += "  var filter: {(: mdd_types::Integer) : mdd_types::Boolean};\n";
		model += "  filter := (value : Integer) : Boolean { value > 10 };\n";
		model += "end;\n";
		model += "end;\n";
		model += "end.\n";
		parseAndCheck(model);
	}

	public void testSignatureAsAttributeType() throws CoreException {
		String model = "model tests;\n";
		model += "class Test\n";
		model += "attribute filter: {(: mdd_types::Integer) : mdd_types::Boolean};\n";
		model += "end;\n";
		model += "end.\n";
		parseAndCheck(model);
	}

	public void testSignatureBehaviorIncompatibility() throws CoreException {
		String model = "model tests;\n";
		model += "import mdd_types;\n";
		model += "class Test\n";
		model += "operation op1();\n";
		model += "begin\n";
		model += "  var filter: {(: Integer) : Integer};\n";
		model += "  filter := (value : Integer) : Boolean { value > 10 };\n";
		model += "end;\n";
		model += "end;\n";
		model += "end.\n";
		IProblem[] result = compile(model);
		FixtureHelper.assertTrue(result, result.length == 1);
		assertTrue(result[0] instanceof TypeMismatch);
	}
}
