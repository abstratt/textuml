package com.abstratt.mdd.core.tests.frontend.textuml;

import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.ParameterEffectKind;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.FeatureUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.core.CannotModifyADerivedAttribute;
import com.abstratt.mdd.frontend.core.CannotSpecializeClassifier;
import com.abstratt.mdd.frontend.core.DuplicateSymbol;
import com.abstratt.mdd.frontend.core.IdsShouldBeRequiredSingle;
import com.abstratt.mdd.frontend.core.MissingDefaultValue;
import com.abstratt.mdd.frontend.core.ReadSelfFromStaticContext;
import com.abstratt.mdd.frontend.core.TypeMismatch;

public class ClassifierTests extends AbstractRepositoryBuildingTests {

	public static Test suite() {
		return new TestSuite(ClassifierTests.class);
	}

	public ClassifierTests(String name) {
		super(name);
	}

	public void testClassAttributes() throws CoreException {
		testAttributes("class", IRepository.PACKAGE.getClass_());
	}

	public void testDatatypeAttributes() throws CoreException {
		testAttributes("datatype", IRepository.PACKAGE.getDataType());
	}

	public void testInterfaceAttributes() throws CoreException {
		testAttributes("interface", IRepository.PACKAGE.getInterface());
	}

	public void testEnumerationAttributes() throws CoreException {
		testAttributes("enumeration", IRepository.PACKAGE.getEnumeration());
	}

	private void testAttributes(String classifierKeyword, EClass metaClass) throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "$classifier SomeClassifier\n";
		source += "attribute attrib1 : Integer;\n";
		source += "public attribute attrib2 : Integer;\n";
		source += "private attribute attrib3 : Integer;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source.replace("$classifier", classifierKeyword));

		Type integerType = (Type) getRepository()
		        .findNamedElement("base::Integer", IRepository.PACKAGE.getType(), null);
		assertNotNull(integerType);

		Classifier classifier = (Classifier) getRepository().findNamedElement("someModel::SomeClassifier", metaClass,
		        null);
		assertNotNull(classifier);
		Property property = classifier.getAttribute("attrib1", integerType);
		assertNotNull(property);
	}

	public void testAttributeInitialization() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "enumeration MyEnum literal VALUE1; literal VALUE2; literal VALUE3; end;\n";
		source += "class SomeClassifier\n";
		source += "attribute attrib1 : Integer := 10;\n";
		source += "attribute attrib2 : Boolean := true;\n";
		source += "attribute attrib3 : String := \"foo\";\n";
		source += "attribute attrib4 : MyEnum := VALUE2;\n";
		source += "attribute attrib5 : Date := { Date#today() };\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);

		Type integerType = (Type) getRepository()
		        .findNamedElement("base::Integer", IRepository.PACKAGE.getType(), null);
		assertNotNull(integerType);

		Classifier classifier = (Classifier) getRepository().findNamedElement("someModel::SomeClassifier",
		        Literals.CLASS, null);

		Property attr1 = classifier.getAttribute("attrib1", null);
		ValueSpecification attr1DefaultValue = attr1.getDefaultValue();
		assertNotNull(attr1DefaultValue);
		assertTrue(MDDExtensionUtils.isBasicValue(attr1DefaultValue));
		assertEquals(10L, MDDExtensionUtils.getBasicValue(attr1DefaultValue));

		Property attr2 = classifier.getAttribute("attrib2", null);
		ValueSpecification attr2DefaultValue = attr2.getDefaultValue();
		assertNotNull(attr2DefaultValue);
		assertTrue(MDDExtensionUtils.isBasicValue(attr2DefaultValue));
		assertEquals(true, MDDExtensionUtils.getBasicValue(attr2DefaultValue));

		Property attr3 = classifier.getAttribute("attrib3", null);
		ValueSpecification attr3DefaultValue = attr3.getDefaultValue();
		assertNotNull(attr3DefaultValue);
		assertTrue(attr3DefaultValue instanceof LiteralString);
		assertEquals("foo", MDDExtensionUtils.getBasicValue(attr3DefaultValue));

		Property attr4 = classifier.getAttribute("attrib4", null);
		ValueSpecification attr4DefaultValue = attr4.getDefaultValue();
		assertNotNull(attr4DefaultValue);
		assertTrue(attr4DefaultValue instanceof InstanceValue);
		assertTrue(((InstanceValue) attr4DefaultValue).getInstance() instanceof EnumerationLiteral);
		assertEquals("VALUE2", ((EnumerationLiteral) ((InstanceValue) attr4DefaultValue).getInstance()).getName());

		Property attr5 = classifier.getAttribute("attrib5", null);
		ValueSpecification attr5DefaultValue = attr5.getDefaultValue();
		assertNotNull(attr5DefaultValue);
		assertTrue(ActivityUtils.isBehaviorReference(attr5DefaultValue));
	}

	public void testDerivedAttribute() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClassifier\n";
		source += "attribute attrib1 : Integer;\n";
		source += "derived attribute attrib2 : Integer := { self.attrib1 * 2 };\n";
		source += "derived attribute attrib3 : Boolean := { self.attrib1 > 0 };\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);

		Type integerType = (Type) getRepository()
		        .findNamedElement("base::Integer", IRepository.PACKAGE.getType(), null);
		assertNotNull(integerType);

		Classifier classifier = (Classifier) getRepository().findNamedElement("someModel::SomeClassifier",
		        Literals.CLASS, null);

		Property attr1 = classifier.getAttribute("attrib1", null);
		Assert.assertFalse(attr1.isDerived());

		Property attr2 = classifier.getAttribute("attrib2", null);
		Assert.assertTrue(attr2.isDerived());

		Property attr3 = classifier.getAttribute("attrib3", null);
		Assert.assertTrue(attr3.isDerived());

		Assert.assertNotNull(attr2.getDefaultValue());
		Assert.assertTrue(ActivityUtils.isBehaviorReference(attr2.getDefaultValue()));

		Assert.assertNotNull(attr3.getDefaultValue());
		Assert.assertTrue(ActivityUtils.isBehaviorReference(attr3.getDefaultValue()));
	}

	public void testIdAttribute() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClassifier1\n";
		source += "id attribute attrib1 : Integer;\n";
		source += "end;\n";
		source += "class SomeClassifier2\n";
		source += "id attribute attrib2 : Integer[0,1];\n";
		source += "end;\n";
		source += "end.";
		String[] sources = { source };

		IProblem[] result = parse(sources);
		IdsShouldBeRequiredSingle error = assertExpectedProblem(IdsShouldBeRequiredSingle.class, result);
		assertEquals(Severity.WARNING, error.getSeverity());
		assertEquals(Integer.valueOf(7), result[0].getAttribute(IProblem.LINE_NUMBER));

		Property attr1 = getProperty("someModel::SomeClassifier1::attrib1");
		Assert.assertTrue(attr1.isID());

		Property attr2 = getProperty("someModel::SomeClassifier2::attrib2");
		Assert.assertTrue(attr2.isID());
	}

	public void testDerivedAttributeMissingInitialization() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClassifier\n";
		source += "derived attribute attrib : Integer;\n";
		source += "end;\n";
		source += "end.";
		IProblem[] problems = parse(source);
		Assert.assertEquals(1, problems.length);
		Assert.assertTrue(problems[0] instanceof MissingDefaultValue);
		Assert.assertEquals((Integer) 4, problems[0].getAttribute(IProblem.LINE_NUMBER));
	}

	public void testSourceInfo() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClassifier\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Classifier classifier = (Classifier) getRepository().findNamedElement("someModel::SomeClassifier",
		        Literals.CLASS, null);
		EAnnotation unitInfo = classifier.getEAnnotation(MDDUtil.UNIT);
		assertNotNull(unitInfo);
		assertNotNull(unitInfo.getDetails());
		assertEquals("foo0." + fixtureHelper.getExtension(), unitInfo.getDetails().get("name"));
	}

	public void testWriteDerivedAttribute() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClassifier\n";
		source += "derived attribute attrib : Integer := 45;\n";
		source += "operation invalidOp();\n";
		source += "begin\n";
		source += "    self.attrib := 1;\n";
		source += "end;\n";
		source += "end;\n";
		source += "end.";
		IProblem[] problems = parse(source);
		Assert.assertEquals(1, problems.length);
		Assert.assertTrue(problems[0].toString(), problems[0] instanceof CannotModifyADerivedAttribute);
		Assert.assertEquals((Integer) 7, problems[0].getAttribute(IProblem.LINE_NUMBER));
	}

	public void testBasicClass() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "class SomeClass\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final Class class_ = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertNotNull(class_);
	}

	public void testDuplicateClass() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "class MyDuplicateClass\n";
		source += "end;\n";
		source += "class MyDuplicateClass\n";
		source += "end;\n";
		source += "end.";
		IProblem[] problems = compile(source);
		FixtureHelper.assertTrue(problems, problems.length == 1);
		FixtureHelper.assertTrue(problems, problems[0] instanceof DuplicateSymbol);
		assertEquals(4, problems[0].getAttribute(IProblem.LINE_NUMBER));
		assertEquals("MyDuplicateClass", ((DuplicateSymbol) problems[0]).getSymbol());
		assertEquals("Class", ((DuplicateSymbol) problems[0]).getMetaClass());
	}

	public void testAbstractClass() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "abstract class SomeClass\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final Class class_ = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertTrue(class_.isAbstract());
	}

	public void testBasicInterface() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "interface SomeInterface\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final Interface interface_ = (Interface) getRepository().findNamedElement("someModel::SomeInterface",
		        IRepository.PACKAGE.getInterface(), null);
		assertNotNull(interface_);
	}

	public void testClassSpecializes() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "class SuperClass\n";
		source += "end;\n";
		source += "class SubClass specializes SuperClass\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final Class superClass = (Class) getRepository().findNamedElement("someModel::SuperClass",
		        IRepository.PACKAGE.getClass_(), null);
		final Class subClass = (Class) getRepository().findNamedElement("someModel::SubClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertNotNull(superClass);
		assertNotNull(subClass);
		assertTrue(subClass.getSuperClasses().contains(superClass));
	}

	public void testClassImplements() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "interface SomeInterface\n";
		source += "end;\n";
		source += "class SomeClass implements SomeInterface\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final Class someClass = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		final Interface someInterface = (Interface) getRepository().findNamedElement("someModel::SomeInterface",
		        IRepository.PACKAGE.getInterface(), null);
		assertNotNull(someClass);
		assertNotNull(someInterface);
		assertTrue(someClass.getImplementedInterfaces().contains(someInterface));
	}

	public void testConstants() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "static readonly attribute CONST1 : Integer := 10;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Type integerType = (Type) getRepository()
		        .findNamedElement("base::Integer", IRepository.PACKAGE.getType(), null);
		assertNotNull(integerType);
		Class class_ = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertNotNull(class_);
		Property property = class_.getAttribute("CONST1", integerType);
		assertNotNull(property);
		assertTrue(property.isReadOnly());
		ValueSpecification defaultValue = property.getDefaultValue();
		assertNotNull(defaultValue);
		assertTrue(MDDExtensionUtils.isBasicValue(defaultValue));
		assertEquals(10L, MDDExtensionUtils.getBasicValue(defaultValue));
	}

	public void testConstantMismatch() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "static readonly attribute CONST1 : Integer := true;\n";
		source += "end;\n";
		source += "end.";
		IProblem[] problems = compile(source);
		FixtureHelper.assertTrue(problems, problems.length == 1);
		FixtureHelper.assertTrue(problems, problems[0] instanceof TypeMismatch);
		assertEquals(4, problems[0].getAttribute(IProblem.LINE_NUMBER));
	}

	public void testInterfaceSpecializes() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "interface SuperInterface\n";
		source += "end;\n";
		source += "interface SubInterface specializes SuperInterface\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final Interface superInterface = (Interface) getRepository().findNamedElement("someModel::SuperInterface",
		        IRepository.PACKAGE.getInterface(), null);
		final Interface subInterface = (Interface) getRepository().findNamedElement("someModel::SubInterface",
		        IRepository.PACKAGE.getInterface(), null);
		assertNotNull(superInterface);
		assertNotNull(subInterface);
		assertNotNull(subInterface.getGeneralization(superInterface));
	}

	public void testDataType() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "datatype ADataType\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final DataType aDataType = (DataType) getRepository().findNamedElement("someModel::ADataType",
		        IRepository.PACKAGE.getDataType(), null);
		assertNotNull(aDataType);
	}

	public void testDataTypeSpecializesDataType() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "datatype ADataType\n";
		source += "end;\n";
		source += "datatype AnotherDataType specializes ADataType\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
	}

	public void testDataTypeSpecializesClass() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "class AClass\n";
		source += "end;\n";
		source += "datatype ADataType specializes AClass\n";
		source += "end;\n";
		source += "end.";
		IProblem[] problems = compile(source);
		FixtureHelper.assertTrue(problems, problems.length == 1);
		FixtureHelper.assertTrue(problems, problems[0] instanceof CannotSpecializeClassifier);
		assertEquals(4, problems[0].getAttribute(IProblem.LINE_NUMBER));
	}

	public void testPrimitiveType() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "primitive Primitive1;\n";
		source += "end.";
		parseAndCheck(source);
		PrimitiveType found = (PrimitiveType) getRepository().findNamedElement("someModel::Primitive1",
		        IRepository.PACKAGE.getPrimitiveType(), null);
		assertNotNull(found);
	}

	public void testActor() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "actor Actor1 end;\n";
		source += "actor Actor2 end;\n";
		source += "actor Actor3 specializes Actor1, Actor2 end;\n";
		source += "end.";
		parseAndCheck(source);
		Actor actor1 = (Actor) getRepository().findNamedElement("someModel::Actor1", IRepository.PACKAGE.getActor(),
		        null);
		Actor actor2 = (Actor) getRepository().findNamedElement("someModel::Actor2", IRepository.PACKAGE.getActor(),
		        null);
		Actor actor3 = (Actor) getRepository().findNamedElement("someModel::Actor3", IRepository.PACKAGE.getActor(),
		        null);
		assertNotNull(actor1);
		assertNotNull(actor2);
		assertNotNull(actor3);
		assertEquals(Arrays.asList(actor1, actor2), actor3.getGenerals());
	}

	public void testOperations() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "operation op1(par1 : Boolean) : Integer;\n";
		source += "protected abstract operation op2();\n";
		source += "package static operation op3();\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Type integerType = (Type) getRepository()
		        .findNamedElement("base::Integer", IRepository.PACKAGE.getType(), null);
		assertNotNull(integerType);
		Type booleanType = (Type) getRepository()
		        .findNamedElement("base::Boolean", IRepository.PACKAGE.getType(), null);
		assertNotNull(booleanType);
		Class class_ = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertNotNull(class_);

		Operation op1 = class_.getOperation("op1", null, null);
		assertNotNull(op1);
		assertSame(op1.getType(), integerType);
		assertEquals(2, op1.getOwnedParameters().size());
		Parameter par1 = op1.getOwnedParameter("par1", booleanType);
		assertNotNull(par1);
		assertSame(ParameterDirectionKind.IN_LITERAL, par1.getDirection());
		Parameter result = op1.getOwnedParameter(null, integerType);
		assertNotNull(result);
		assertSame(ParameterDirectionKind.RETURN_LITERAL, result.getDirection());
		assertSame(VisibilityKind.PUBLIC_LITERAL, op1.getVisibility());

		Operation op2 = class_.getOperation("op2", null, null);
		assertNotNull(op2);
		assertTrue(op2.isAbstract());
		assertFalse(op2.isStatic());
		assertSame(VisibilityKind.PROTECTED_LITERAL, op2.getVisibility());

		Operation op3 = class_.getOperation("op3", null, null);
		assertNotNull(op3);
		assertTrue(op3.isStatic());
		assertFalse(op3.isAbstract());
		assertSame(VisibilityKind.PACKAGE_LITERAL, op3.getVisibility());
	}

	public void testOperationWithBody() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "operation op1(par1 : Boolean);\n";
		source += "begin\n";
		source += "end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class_ = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertNotNull(class_);
		Operation operation = class_.getOperation("op1", null, null);
		assertNotNull(operation);
		assertEquals(1, operation.getMethods().size());
	}

	public void testOperationWithPreConditions() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "operation op1(par1 : Boolean)\n";
		source += "precondition condition1 { true }\n";
		source += "precondition { true }\n";
		source += "precondition condition3 (par1) { not par1 };\n";
		source += "begin\n";
		source += "end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class_ = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertNotNull(class_);
		Operation operation = class_.getOperation("op1", null, null);
		assertNotNull(operation);
		assertEquals(1, operation.getMethods().size());
		assertEquals(3, operation.getPreconditions().size());

		assertEquals("condition1", operation.getPreconditions().get(0).getName());
		assertNull(operation.getPreconditions().get(1).getName());
		assertEquals("condition3", operation.getPreconditions().get(2).getName());

		Assert.assertNotNull(operation.getPreconditions().get(0).getSpecification());

		int[] expectedParameterCount = { 1, 1, 2 };
		int index = 0;
		for (Constraint constraint : operation.getPreconditions())
			checkConstraint(constraint, expectedParameterCount[index++]);
	}

	private void checkConstraint(Constraint constraint, int expectedParameters) {
		assertNotNull(constraint.getSpecification());
		assertTrue(ActivityUtils.isBehaviorReference(constraint.getSpecification()));
		Activity activity = (Activity) ActivityUtils.resolveBehaviorReference(constraint.getSpecification());
		Parameter returnParameter = FeatureUtils.findReturnParameter(activity.getOwnedParameters());
		assertNotNull(returnParameter);
		assertEquals("mdd_types::Boolean", returnParameter.getType().getQualifiedName());
		assertEquals(expectedParameters, activity.getOwnedParameters().size());
	}

	public void testAttributeWithInvariants() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "attribute attr1: Boolean\n";
		source += "invariant condition1 { true }\n";
		source += "invariant { true }\n";
		source += "invariant condition3 { not self.attr1 };\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class_ = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertNotNull(class_);
		Property property = class_.getOwnedAttribute("attr1", null);
		assertNotNull(property);
		assertEquals(3, class_.getOwnedRules().size());

		assertEquals("condition1", class_.getOwnedRules().get(0).getName());
		assertNull(class_.getOwnedRules().get(1).getName());
		assertEquals("condition3", class_.getOwnedRules().get(2).getName());

		Assert.assertNotNull(class_.getOwnedRules().get(0).getSpecification());

		for (Constraint constraint : class_.getOwnedRules()) {
			// expect only the return parameter
			checkConstraint(constraint, 1);
			assertEquals(Arrays.asList(property), constraint.getConstrainedElements());
		}
	}

	public void testClassWithInvariants() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "invariant condition1 { true };\n";
		source += "invariant { true };\n";
		source += "invariant condition3 { not self.attr1 };\n";
		source += "access condition4 { false };\n";
		source += "attribute attr1: Boolean;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class class_ = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		assertNotNull(class_);
		assertEquals(4, class_.getOwnedRules().size());

		assertEquals("condition1", class_.getOwnedRules().get(0).getName());
		assertNull(class_.getOwnedRules().get(1).getName());
		assertEquals("condition3", class_.getOwnedRules().get(2).getName());
		assertEquals("condition4", class_.getOwnedRules().get(3).getName());

		Assert.assertNotNull(class_.getOwnedRules().get(0).getSpecification());

		for (Constraint constraint : class_.getOwnedRules()) {
			// expect only the return parameter
			checkConstraint(constraint, 1);
			assertEquals(Arrays.asList(class_), constraint.getConstrainedElements());
		}
	}

	public void testOperationParametersWithDefaultValue() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "enumeration MyEnum literal VALUE1; literal VALUE2; literal VALUE3; end;\n";
		source += "class SomeClassifier\n";
		source += "operation op1(param1 : Boolean := true, param2 : Integer := 2, param3 : MyEnum := VALUE2, param4 : Date := { Date#today() });\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);

		Type integerType = (Type) getRepository()
		        .findNamedElement("base::Integer", IRepository.PACKAGE.getType(), null);
		assertNotNull(integerType);

		Type booleanType = (Type) getRepository()
		        .findNamedElement("base::Boolean", IRepository.PACKAGE.getType(), null);
		assertNotNull(booleanType);

		Classifier classifier = (Classifier) getRepository().findNamedElement("someModel::SomeClassifier",
		        Literals.CLASS, null);
		Operation operation = classifier.getOperation("op1", null, null);
		assertNotNull(operation);

		Parameter param1 = operation.getOwnedParameter("param1", null);
		ValueSpecification param1DefaultValue = param1.getDefaultValue();
		assertNotNull(param1DefaultValue);
		assertTrue(MDDExtensionUtils.isBasicValue(param1DefaultValue));
		assertEquals(true, MDDExtensionUtils.getBasicValue(param1DefaultValue));

		Parameter param2 = operation.getOwnedParameter("param2", null);
		ValueSpecification param2DefaultValue = param2.getDefaultValue();
		assertNotNull(param2DefaultValue);
		assertTrue(MDDExtensionUtils.isBasicValue(param2DefaultValue));
		assertEquals(2L, MDDExtensionUtils.getBasicValue(param2DefaultValue));

		Parameter param3 = operation.getOwnedParameter("param3", null);
		ValueSpecification param3DefaultValue = param3.getDefaultValue();
		assertNotNull(param3DefaultValue);
		assertTrue(param3DefaultValue instanceof InstanceValue);
		assertTrue(((InstanceValue) param3DefaultValue).getInstance() instanceof EnumerationLiteral);
		assertEquals("VALUE2", ((EnumerationLiteral) ((InstanceValue) param3DefaultValue).getInstance()).getName());

		Parameter param4 = operation.getOwnedParameter("param4", null);
		ValueSpecification param4DefaultValue = param4.getDefaultValue();
		assertNotNull(param4DefaultValue);
		assertTrue(ActivityUtils.isBehaviorReference(param4DefaultValue));
		Activity param4Expression = (Activity) ActivityUtils.resolveBehaviorReference(param4DefaultValue);
		assertTrue(param4Expression.isReadOnly());
		Parameter returnParameter = FeatureUtils.findReturnParameter(param4Expression.getOwnedParameters());
		assertNotNull(returnParameter);
		assertEquals("Date", returnParameter.getType().getName());
	}

	public void testRaisedExceptions() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeException1 end;\n";
		source += "class SomeException2 end;\n";
		source += "class SomeClass\n";
		source += "operation op1(par1 : Boolean) : Integer raises SomeException1, SomeException2;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Class someClass = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		Operation operation = someClass.getOperation("op1", null, null);
		assertNotNull(operation);
		assertNotNull(operation.getRaisedException("SomeException1"));
		assertNotNull(operation.getRaisedException("SomeException2"));
	}

	public void testReadSelf() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "class SomeClass\n";
		source += "operation op1() : SomeClass;\n";
		source += "begin\n";
		source += "return self;\n";
		source += "end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
	}

	public void testReadSelfFromStatic() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "class SomeClass\n";
		source += "static operation op1() : SomeClass;\n";
		source += "begin\n";
		source += "return self;\n";
		source += "end;\n";
		source += "end;\n";
		source += "end.";
		IProblem[] problems = compile(source);
		assertExpectedProblem(ReadSelfFromStaticContext.class, problems);
	}

	public void testClassDependency() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "interface SomeInterface\n";
		source += "end;\n";
		source += "class SomeClass\n";
		source += "dependency SomeInterface;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final Class someClass = (Class) getRepository().findNamedElement("someModel::SomeClass",
		        IRepository.PACKAGE.getClass_(), null);
		final Interface someInterface = (Interface) getRepository().findNamedElement("someModel::SomeInterface",
		        IRepository.PACKAGE.getInterface(), null);
		assertNotNull(someClass);
		assertNotNull(someInterface);
		assertTrue(someClass.getClientDependencies().get(0).getSuppliers().contains(someInterface));
	}

	public void testReadOnlyModifier() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "readonly attribute att1 : Integer;\n";
		source += "attribute att2 : Integer;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		final Property attr1 = (Property) getRepository().findNamedElement("someModel::SomeClass::att1",
		        UMLPackage.Literals.PROPERTY, null);
		final Property attr2 = (Property) getRepository().findNamedElement("someModel::SomeClass::att2",
		        UMLPackage.Literals.PROPERTY, null);
		assertTrue(attr1.isReadOnly());
		assertFalse(attr2.isReadOnly());
	}

	/**
	 * In the presence of multiple conflicting modifiers, the last one wins.
	 */
	public void testParameterModifiers() throws CoreException {
		String source = "";
		source += "model someModel;\n";
		source += "import base;\n";
		source += "class SomeClass\n";
		source += "operation op1(create read in out param1 : Integer);\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(source);
		Operation op1 = (Operation) getRepository().findNamedElement("someModel::SomeClass::op1",
		        UMLPackage.Literals.OPERATION, null);
		Parameter param1 = op1.getOwnedParameter("param1", null);
		assertNotNull(param1);
		assertEquals(ParameterDirectionKind.OUT_LITERAL, param1.getDirection());
		assertEquals(ParameterEffectKind.READ_LITERAL, param1.getEffect());
	}

}
