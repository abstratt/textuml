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

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.util.ClassifierUtils;
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
        source += "  role class AccountOwner end;\n";
        source += "  role class BranchManager end;\n";
        source += "  role class AccountManager end;\n";        
        source += "  role class SecurityOfficer end;\n";
        source += "\n";
        source += "  class Branch\n";
        source += "    reference manager : BranchManager;\n";
        source += "  end;\n";
        source += "\n";
        source += "  class BankAccount\n";
        source += "    allow BranchManager, AccountManager create, delete;\n";
        source += "    reference owner : AccountOwner;\n";
        source += "    reference branch : Branch;\n";
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
    
    public void testObjectPermission() throws CoreException {
    	parseAndCheck(source);
    	Stereotype accessStereotype = StereotypeUtils.findStereotype(MDDExtensionUtils.ACCESS_STEREOTYPE);
    	Class bankAccount = getClass("banking::BankAccount");
    	List<Constraint> accessConstraints = MDDExtensionUtils.findConstraints(bankAccount, MDDExtensionUtils.ACCESS_STEREOTYPE);
		assertEquals(1, accessConstraints.size());
    	Constraint permission = accessConstraints.get(0);
    	List<AccessCapability> allowed = ClassifierUtils.fromEnumerationLiterals(AccessCapability.class, (List<EEnumLiteral>) permission.getValue(accessStereotype, MDDExtensionUtils.ACCESS_ALLOWED));
    	assertEquals(2, allowed.size());
    	assertEquals(Arrays.asList(AccessCapability.Create, AccessCapability.Delete), allowed);
    	
    	List<Class> allowedRoles = (List<Class>) permission.getValue(accessStereotype, "roles");
    	assertEquals(2, allowedRoles.size());
    	assertEquals(Arrays.asList(getClass("banking::BranchManager"), getClass("banking::AccountManager")), allowedRoles);
    	
    	assertNull(permission.getSpecification());
    	
    }
    
    public void testAttributePermission() throws CoreException {
    	parseAndCheck(source);
    	Property balance = getProperty("banking::BankAccount::balance");
    	List<Constraint> accessConstraints = MDDExtensionUtils.findConstraints(balance, MDDExtensionUtils.ACCESS_STEREOTYPE);
		assertEquals(3, accessConstraints.size());
		
    	Constraint permission1 = accessConstraints.get(0);
    	assertNotNull(permission1.getSpecification());
    	
    }
    
    public void testOperationPermission() throws CoreException {
    	parseAndCheck(source);
    	Operation deposit = getOperation("banking::BankAccount::deposit");
    	List<Constraint> accessConstraints = MDDExtensionUtils.findConstraints(deposit, MDDExtensionUtils.ACCESS_STEREOTYPE);
		assertEquals(1, accessConstraints.size());
    	Constraint permission = accessConstraints.get(0);
    	assertNotNull(permission.getSpecification());
    }
    
}
