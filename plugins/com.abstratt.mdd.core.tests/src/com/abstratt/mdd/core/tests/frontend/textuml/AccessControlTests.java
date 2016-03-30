package com.abstratt.mdd.core.tests.frontend.textuml;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.OneRoleAllowedForConstraintWithCondition;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.core.util.ClassifierUtils;
import com.abstratt.mdd.core.util.ConstraintUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils.AccessCapability;
import com.abstratt.mdd.core.util.StereotypeUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AccessControlTests extends AbstractRepositoryBuildingTests {
	
	private static String source;
	
	static {
        source = "";
        source += "model banking;\n";
        source += "import base;\n";
        source += "apply mdd_extensions;\n";
        source += "  class Person\n";
        source += "      attribute name : String;\n";
        source += "  end;\n";
        source += "  abstract role class ApplicationUser specializes Person end;\n";
        source += "  class AccountOwner specializes ApplicationUser\n";
        source += "      reference accounts : BankAccount[*] opposite owner;\n";
        source += "  end;\n";
        source += "  abstract class Employee specializes ApplicationUser end;\n";
        source += "  class BranchOfficer specializes Employee end;\n";
        source += "  class AccountManager specializes Employee end;\n";
        source += "  class BranchManager specializes BranchOfficer end;\n";
        source += "  class Teller specializes BranchOfficer end;\n";
        source += "  class SecurityOfficer specializes Employee end;\n";
        source += "\n";
        source += "  class Branch\n";
        source += "    reference manager : BranchManager;\n";
        source += "  end;\n";
        source += "\n";
        source += "  class BankAccount\n";
        source += "    allow BranchManager, AccountManager create, delete;\n";
        source += "    attribute owner : AccountOwner;\n";
        source += "    attribute branch : Branch;\n";
        source += "    attribute balance : Double\n";
        source += "        allow AccountOwner read { System#user() == self.owner } \n";
        source += "        allow BranchManager read { System#user() == self.branch.manager } \n";
        source += "        allow SecurityOfficer read;\n";        
        source += "    operation withdraw(amount : Double)\n";
        source += "        allow AccountOwner { System#user() == self.owner }; \n";
        source += "    operation deposit(amount : Double)\n";
        source += "        allow AccountOwner { System#user() == self.owner }; \n";
        source += "  end;\n";
        source += "end.";        

        System.out.println(source);
	}

    public static Test suite() {
        return new TestSuite(AccessControlTests.class);
    }

    public AccessControlTests(String name) {
        super(name);
    }
    
    public void testSmokeTest() throws CoreException {
        parseAndCheck(source);
    }
    
    public void testOnlyOneRoleClassWithCondition() throws CoreException {
    	String source = "";
        source = "";
        source += "model banking;\n";
        source += "import base;\n";
        source += "apply mdd_extensions;\n";
        source += "  role class Employee\n";
        source += "      attribute name : String;\n";
        source += "  end;\n";        
        source += "  class Approver specializes Employee end;\n";
        source += "\n";
        source += "  class Expense\n";
        source += "    allow Approver, Employee read { true };\n";
        source += "  end;\n";
        source += "end.";
        IProblem[] errors = parse(source);
        FixtureHelper.assertTrue(errors, errors.length == 1);
        FixtureHelper.assertTrue(errors, errors[0] instanceof OneRoleAllowedForConstraintWithCondition);
    }
    
    public void testObjectPermission() throws CoreException {
    	parseAndCheck(source);
    	Stereotype accessStereotype = StereotypeUtils.findStereotype(MDDExtensionUtils.ACCESS_STEREOTYPE);
    	Class bankAccount = getClass("banking::BankAccount");
    	List<Constraint> accessConstraints = ConstraintUtils.findConstraints(bankAccount, MDDExtensionUtils.ACCESS_STEREOTYPE);
		assertEquals(1, accessConstraints.size());
    	Constraint permission = accessConstraints.get(0);
    	List<AccessCapability> allowed = MDDExtensionUtils.getAllowedCapabilities(permission);
    	assertEquals(2, allowed.size());
    	assertEquals(Arrays.asList(AccessCapability.Create, AccessCapability.Delete), allowed);
    	
    	List<Class> allowedRoles = (List<Class>) permission.getValue(accessStereotype, "roles");
    	assertEquals(2, allowedRoles.size());
    	assertEquals(Arrays.asList(getClassifier("banking::BranchManager"), getClassifier("banking::AccountManager")), allowedRoles);
    	
    	assertTrue(ConstraintUtils.isTautology(permission));
    }
    
    public void testAttributePermission() throws CoreException {
    	parseAndCheck(source);
    	Property balance = getProperty("banking::BankAccount::balance");
    	List<Constraint> accessConstraints = ConstraintUtils.findConstraints(balance, MDDExtensionUtils.ACCESS_STEREOTYPE);
		assertEquals(3, accessConstraints.size());
		
    	Constraint permission1 = accessConstraints.get(0);
    	assertNotNull(permission1.getSpecification());
    	
    }
    
    public void testOperationPermission() throws CoreException {
    	parseAndCheck(source);
    	Operation deposit = getOperation("banking::BankAccount::deposit");
    	List<Constraint> accessConstraints = ConstraintUtils.findConstraints(deposit, MDDExtensionUtils.ACCESS_STEREOTYPE);
		assertEquals(1, accessConstraints.size());
    	Constraint permission = accessConstraints.get(0);
    	assertNotNull(permission.getSpecification());
    }
    
    public void testRoleClassSpecializesUser() throws CoreException {
    	parseAndCheck(source);
    	Class appUser = getClass("banking::ApplicationUser");
    	Class systemUser = getClass("mdd_types::SystemUser");
    	
    	assertTrue(ClassifierUtils.isKindOf(appUser, systemUser));
    }
    
}
