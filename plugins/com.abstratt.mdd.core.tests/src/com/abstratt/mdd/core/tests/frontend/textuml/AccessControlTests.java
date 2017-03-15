package com.abstratt.mdd.core.tests.frontend.textuml;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.junit.Assert;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.OneRoleAllowedForConstraintWithCondition;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.core.util.AccessCapability;
import com.abstratt.mdd.core.util.AccessControlUtils;
import com.abstratt.mdd.core.util.ClassifierUtils;
import com.abstratt.mdd.core.util.ConstraintUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
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
        source += "  class BranchOfficer specializes Employee\n";
        source += "    attribute branch : Branch;\n";        
        source += "  end;\n";
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
        source += "    allow BranchManager, AccountManager create, delete, call;\n";
        source += "    allow Teller extent;\n";
        source += "    allow Teller read { self.branch == (System#user() as Teller).branch };\n";
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
        source += "    operation harmlessAction() allow call;\n";
        source += "    static operation open(accountOwner : AccountOwner) : BankAccount\n";
        source += "        allow BranchOfficer static call; \n";        
        source += "  end;\n";
        source += "  class AccountApplication\n";
        source += "      allow Employee all;\n";
        source += "      allow create;\n";
        source += "      attribute applicationName : String;\n";
        source += "      attribute branch : Branch;\n";        
        source += "      operation accept()\n";
        source += "        allow BranchOfficer { (System#user() as BranchOfficer).branch == self.branch }; \n";
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
    
    public void testConstraintsPerRole_BankAccount() throws CoreException {
    	parseAndCheck(source);
    	Class bankAccount = getClass("banking::BankAccount");
    	Class branchManager = getClass("banking::BranchManager");
    	Class accountManager = getClass("banking::AccountManager");
    	Class teller = getClass("banking::Teller");
    	
    	Map<Classifier, Map<AccessCapability, Constraint>> computed = AccessControlUtils.computeConstraintsPerRoleClass(Arrays.asList(branchManager, teller, accountManager), Arrays.asList(AccessCapability.values()), Arrays.asList(bankAccount));
    	assertEquals(3, computed.size());
    	assertSameClasses(Arrays.asList(branchManager, accountManager, teller), computed.keySet());
    	
    	Map<AccessCapability, Constraint> branchManagerConstraints = computed.get(branchManager);
    	Map<AccessCapability, Constraint> accountManagerConstraints = computed.get(accountManager);
    	Map<AccessCapability, Constraint> tellerConstraints = computed.get(teller);
    	
    	assertEquals(branchManagerConstraints.keySet(), new LinkedHashSet<>(Arrays.asList(AccessCapability.Call, AccessCapability.Delete, AccessCapability.Create)));
    	assertEquals(accountManagerConstraints.keySet(), new LinkedHashSet<>(Arrays.asList(AccessCapability.Call, AccessCapability.Delete, AccessCapability.Create)));
    	assertEquals(tellerConstraints.keySet(), new LinkedHashSet<>(Arrays.asList(AccessCapability.Read, AccessCapability.List)));
    }
    
    public void testConstraintsPerRole_AccountApplication() throws CoreException {
    	parseAndCheck(source);
    	Class bankAccount = getClass("banking::AccountApplication");
    	Class branchManager = getClass("banking::BranchManager");
    	Class accountManager = getClass("banking::AccountManager");
    	Class teller = getClass("banking::Teller");
    	
    	Map<Classifier, Map<AccessCapability, Constraint>> computed = AccessControlUtils.computeConstraintsPerRoleClass(Arrays.asList(branchManager, teller, accountManager), Arrays.asList(AccessCapability.values()), Arrays.asList(bankAccount));
    	assertSameClasses(Arrays.asList(branchManager, accountManager, teller, null), computed.keySet());
    	
    	Map<AccessCapability, Constraint> branchManagerConstraints = computed.get(branchManager);
    	Map<AccessCapability, Constraint> accountManagerConstraints = computed.get(accountManager);
    	Map<AccessCapability, Constraint> tellerConstraints = computed.get(teller);
    	Map<AccessCapability, Constraint> anonymousConstraints = computed.get(null);
    	
    	Set<AccessCapability> allCapabilities = new LinkedHashSet<>(Arrays.asList(AccessCapability.values()));
    	
    	assertEquals(allCapabilities, branchManagerConstraints.keySet());
    	assertEquals(allCapabilities, accountManagerConstraints.keySet());
    	assertEquals(allCapabilities, tellerConstraints.keySet());
    	assertEquals(Collections.singleton(AccessCapability.Create), anonymousConstraints.keySet());
    }
    
    
    public void testConstraintsPerRole_harmlessAction() throws CoreException {
    	parseAndCheck(source);
    	Class bankAccount = getClass("banking::BankAccount");
    	Operation harmlessAction = getOperation("banking::BankAccount::harmlessAction");
    	
    	Class branchManager = getClass("banking::BranchManager");
    	Class accountManager = getClass("banking::AccountManager");
    	Class teller = getClass("banking::Teller");
    	
    	Map<Classifier, Map<AccessCapability, Constraint>> computed = AccessControlUtils.computeConstraintsPerRoleClass(Arrays.asList(branchManager, teller, accountManager), Arrays.asList(AccessCapability.Call), Arrays.asList(bankAccount, harmlessAction));
		assertEquals(4, computed.size());
    	assertSameClasses(Arrays.asList(branchManager, accountManager, teller, null), computed.keySet());
    	
    	assertEquals(Collections.singleton(AccessCapability.Call), computed.get(null).keySet());
    	assertEquals(Collections.singleton(AccessCapability.Call), computed.get(branchManager).keySet());
    	assertEquals(Collections.singleton(AccessCapability.Call), computed.get(accountManager).keySet());
    	assertEquals(Collections.singleton(AccessCapability.Call), computed.get(teller).keySet());
    }

    
    private void assertSameClasses(Collection<? extends Type> expected, Collection<? extends Type> actual) {
    	Function<Collection<? extends Type>, Set<String>> sampler = classes -> classes.stream().map(it -> it == null ? null : it.getName()).collect(Collectors.toSet());
    	assertEquals(sampler.apply(expected), sampler.apply(actual));
	}

	public void testObjectPermission() throws CoreException {
    	parseAndCheck(source);
    	Stereotype accessStereotype = StereotypeUtils.findStereotype(MDDExtensionUtils.ACCESS_STEREOTYPE);
    	Class bankAccount = getClass("banking::BankAccount");
    	List<Constraint> accessConstraints = ConstraintUtils.findConstraints(bankAccount, MDDExtensionUtils.ACCESS_STEREOTYPE);
		assertEquals(3, accessConstraints.size());
    	Constraint permission1 = accessConstraints.get(0);
    	List<AccessCapability> allowed1 = MDDExtensionUtils.getAllowedCapabilities(permission1);
    	assertEquals(new LinkedHashSet<>(Arrays.asList(AccessCapability.Call, AccessCapability.Create, AccessCapability.Delete)), new LinkedHashSet<>(allowed1));
    	
    	List<Class> allowedRoles1 = (List<Class>) permission1.getValue(accessStereotype, "roles");
    	assertEquals(2, allowedRoles1.size());
    	assertSameClasses(Arrays.asList(getClassifier("banking::BranchManager"), getClassifier("banking::AccountManager")), allowedRoles1);
    	
    	assertTrue(ConstraintUtils.isTautology(permission1));
    	
    	Constraint permission2 = accessConstraints.get(1);

    	List<AccessCapability> allowed2 = MDDExtensionUtils.getAllowedCapabilities(permission2);
    	assertEquals(1, allowed2.size());
    	assertEquals(Arrays.asList(AccessCapability.List), allowed2);
    	
    	List<Class> allowedRoles2 = (List<Class>) permission2.getValue(accessStereotype, "roles");
    	assertEquals(1, allowedRoles2.size());
    	assertSameClasses(Arrays.asList(getClassifier("banking::Teller")), allowedRoles2);
    	
    	assertTrue(ConstraintUtils.isTautology(permission2));

    	Constraint permission3 = accessConstraints.get(2);

    	List<AccessCapability> allowed3 = MDDExtensionUtils.getAllowedCapabilities(permission3);
    	assertEquals(1, allowed3.size());
    	assertEquals(Arrays.asList(AccessCapability.Read), allowed3);
    	
    	List<Class> allowedRoles3 = (List<Class>) permission3.getValue(accessStereotype, "roles");
    	assertEquals(1, allowedRoles3.size());
    	assertSameClasses(Arrays.asList(getClassifier("banking::Teller")), allowedRoles3);
    	
    	assertFalse(ConstraintUtils.isTautology(permission3));

    }

	public void testObjectPermission_Anonymous() throws CoreException {
    	parseAndCheck(source);
    	Class accountApplication = getClass("banking::AccountApplication");
    	Constraint constraint = AccessControlUtils.findAccessConstraint(accountApplication, AccessCapability.Create, null);
    	assertNotNull(constraint);
    	assertEquals(Arrays.asList(AccessCapability.Create), MDDExtensionUtils.getAllowedCapabilities(constraint));
    	assertEquals(Collections.emptyList(), MDDExtensionUtils.getAccessRoles(constraint));
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
    
    public void testStaticOperationPermission() throws CoreException {
    	parseAndCheck(source);
    	Operation deposit = getOperation("banking::BankAccount::open");
    	List<Constraint> accessConstraints = ConstraintUtils.findConstraints(deposit, MDDExtensionUtils.ACCESS_STEREOTYPE);
		assertEquals(1, accessConstraints.size());
    	Constraint permission = accessConstraints.get(0);
    	assertNotNull(permission.getSpecification());
    	assertTrue(ConstraintUtils.isTautology(permission));
    	List<Classifier> accessRoles = MDDExtensionUtils.getAccessRoles(permission);
    	Assert.assertEquals(Arrays.asList(getClass("banking::BranchOfficer")), accessRoles);
    	Assert.assertEquals(Arrays.asList(AccessCapability.StaticCall), MDDExtensionUtils.getAllowedCapabilities(permission));
    }
    
    public void testRoleClassSpecializesUser() throws CoreException {
    	parseAndCheck(source);
    	Class appUser = getClass("banking::ApplicationUser");
    	Class systemUser = getClass("mdd_types::SystemUser");
    	
    	assertTrue(ClassifierUtils.isKindOf(appUser, systemUser));
    }
    
}
