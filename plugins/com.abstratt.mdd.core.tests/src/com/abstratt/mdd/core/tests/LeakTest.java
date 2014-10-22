package com.abstratt.mdd.core.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.MDDCore;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.core.util.StereotypeUtils;
import com.abstratt.mdd.frontend.core.ICompilationDirector;
import com.abstratt.mdd.frontend.core.LocationContext;
import com.abstratt.mdd.frontend.internal.core.CompilationDirector;

public class LeakTest extends AbstractRepositoryBuildingTests{
	public LeakTest(String name) {
		super(name);
	}
	
	@Override
	protected void runTest() throws Throwable {
		originalRunTest();
	}
	
	@Override
	public void setUp() {
	}
	
	@Override
	protected void tearDown() {
	}
	
	public void _testLeak3() throws Exception {
		Properties creationSettings = new Properties();
		creationSettings.setProperty(IRepository.ENABLE_EXTENSIONS, Boolean.TRUE.toString());
		creationSettings.setProperty(IRepository.ENABLE_LIBRARIES, Boolean.TRUE.toString());
		IRepository repo = MDDCore.createRepository(URI.createURI("file:///C:/Users/rafael/AppData/Local/Temp/kirra/perf2"));
		org.eclipse.uml2.uml.Class employeeClass = repo.findNamedElement("expenses::Employee", Literals.CLASS, null);
		Operation activeEmployeesOperation = employeeClass.getOperation("activeEmployees", null, null, true);
		assertEquals(1, activeEmployeesOperation.getMethods().size());
		Behavior method = activeEmployeesOperation.getMethods().get(0);
		Behavior closure = method.getOwnedBehaviors().get(0);
		assertEquals(1, closure.getStereotypeApplications().size());
		assertTrue(StereotypeUtils.hasStereotype(closure, "mdd_extensions::Closure"));
		repo.dispose();
		showMemory("after test");
	}
	
	public void testLeak2() throws Exception {
		ICompilationDirector director = CompilationDirector.getInstance();
		
		IFileStore sourceRoot = EFS.getStore(java.net.URI.create("file:///C:/Users/rafael/AppData/Local/Temp/kirra/perf2/src"));
		IFileStore output = sourceRoot.getParent();
		LocationContext context = new LocationContext(output);
		context.addSourcePath(sourceRoot, output);
		IFileStore[] allChildren = sourceRoot.childStores(EFS.NONE, null);
		List<IFileStore> source = new ArrayList<IFileStore>();
		for (IFileStore iFileStore : allChildren)
			if (iFileStore.getName().endsWith("tuml"))
				source.add(iFileStore);
		IProblem[] problems = director.compile(source.toArray(new IFileStore[0]), null, context, ICompilationDirector.FULL_BUILD | ICompilationDirector.CLEAN, null);
		FixtureHelper.assertCompilationSuccessful(problems);
		showMemory("after test");
	}
	
	public void _testLeak() throws CoreException {
		String profile;
		profile= "profile simpleprofile;\n";
		profile+= "stereotype Entity extends uml::Class\n";
		profile+= "end;\n";
		profile+= "end.";

		String source;
		source = "model simple;\n";
		source += "import mdd_types;\n";
		source += "apply simpleprofile;\n";
		source += "[Entity]class SimpleClass\n";
		source += "derived attribute K : Integer := () : Integer { return 10 };\n";
		source += "end;\n";
		source += "[Entity]class Category attribute name : String;end;\n";
		source += "enumeration ExpenseStatus RECORDED, APPROVED, SUBMITTED, REJECTED end;\n";
		source += "composition EmployeeExpenses   role Employee.expenses;  role Expense.employee; end;\n";
		source += "[Entity]class Expense      reference category : Category; attribute status : ExpenseStatus := SUBMITTED; attribute amount : Double; attribute date : Date; attribute employee : Employee;end;\n";
		source += "[Entity]class Employee    attribute name : String;    attribute expenses : Expense[*];end;\n";
		source += "end.";
		parseAndCheck(source, profile);
		showMemory("after test");
	}

	private void showMemory(String string) {
		System.gc();
		final int MEGABYTE = (1024 * 1024);
		System.out.println(string + " - Available memory: "
				+ Runtime.getRuntime().freeMemory() / MEGABYTE + "MB / "
				+ Runtime.getRuntime().totalMemory() / MEGABYTE + "MB");
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(LeakTest.class.getName());
		for (int i = 0; i < 5000; i++) {
			final TestSuite testSuite = new TestSuite("TestSuite" + i);
			suite.addTest(testSuite);
			testSuite.addTestSuite(LeakTest.class);
		}
		return suite;
	}
}
