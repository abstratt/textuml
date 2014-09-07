package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Type;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;

public class FunctionTests extends AbstractRepositoryBuildingTests {

	public static Test suite() {
		return new TestSuite(FunctionTests.class);
	}

	public FunctionTests(String name) {
		super(name);
	}

	public void testBasicFunction() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "function someFunction(par1 : Integer,par2 : Integer) : Boolean;\n";
		source += "end.";
		parseAndCheck(source);
		final FunctionBehavior function =
						(FunctionBehavior) getRepository().findNamedElement("someModel::someFunction",
										IRepository.PACKAGE.getFunctionBehavior(), null);
		final Type booleanType =
						(Type) getRepository().findNamedElement("base::Boolean", IRepository.PACKAGE.getType(), null);
		final Type integerType =
						(Type) getRepository().findNamedElement("base::Integer", IRepository.PACKAGE.getType(), null);
		assertNotNull(function);
		assertNotNull(function.getOwnedParameter("par1", integerType));
		assertNotNull(function.getOwnedParameter("par2", integerType));
		assertNotNull(function.getOwnedParameter(null, booleanType));
	}

	public void testFunctionAssignment() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "  class SimpleClass\n";
		source += "    operation op1();\n";
		source += "    begin\n";
		source += "      var f : {(p : Integer) : Boolean};\n";
		source += "      f := (q : Integer) : Boolean { return q > 0; };\n";
		source += "    end;\n";
		source += "  end;\n";
		source += "end.";
		parseAndCheck(source);
	}

	//TODO temporarily disabled during refactor to remove metamodel extensions	
//	public void testFunctionCall() throws CoreException {
//		String source = "";
//		source += "model someModel;\n";
//		source += "import base;\n";
//		source += "  class SimpleClass\n";
//		source += "    operation op1(f : {(p : Integer) : Boolean});\n";
//		source += "    begin\n";
//		source += "      var result : Boolean;\n";
//		source += "      result := f(10);\n";
//		source += "      Console#writeln(result.toString());\n";
//		source += "    end;\n";
//		source += "  end;\n";
//		source += "end.";
//		parseAndCheck(source);
//	}

}
