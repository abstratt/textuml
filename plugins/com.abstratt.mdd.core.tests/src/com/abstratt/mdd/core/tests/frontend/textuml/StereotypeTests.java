/*******************************************************************************
 * Copyright (c) 2006, 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Vladimir Sosnin - #2796613, #2798455
 *******************************************************************************/ 
package com.abstratt.mdd.core.tests.frontend.textuml;

 import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.MDDCore;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.frontend.core.NotAConcreteClassifier;
import com.abstratt.mdd.frontend.core.NotAMetaclass;

public class StereotypeTests extends AbstractRepositoryBuildingTests {

	public static Test suite() {
		return new TestSuite(StereotypeTests.class);
	}

	public StereotypeTests(String name) {
		super(name);
	}

	public void testAttributeStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype my_stereotype extends UML::Property end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "import base;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "class SomeClass\n";
		modelSource += "  [my_stereotype] attribute someAttribute : Integer;\n";
		modelSource += "end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);
		Class class_ =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClassifier(), null);
		Property attribute = class_.getAttribute("someAttribute", null);
		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype);
		assertTrue(attribute.isStereotypeApplied(stereotype));
	}
	
	public void testParameterStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype my_stereotype1 extends UML::Parameter end;\n";
		profileSource += "stereotype my_stereotype2 extends UML::Parameter end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "import base;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "class SomeClass\n";
		modelSource += "  operation someOperation([my_stereotype1] out someParam : Integer) [my_stereotype2] : Boolean;\n";
		modelSource += "end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);
		Operation operation =
			(Operation) getRepository().findNamedElement("someModel::SomeClass::someOperation",
							IRepository.PACKAGE.getOperation(), null);
		Parameter param = operation.getOwnedParameter("someParam", null);
		assertEquals(ParameterDirectionKind.OUT_LITERAL, param.getDirection());
		Stereotype stereotype1 =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype1",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype1);
		assertTrue(param.isStereotypeApplied(stereotype1));
		
		Parameter returnParameter = operation.getReturnResult();
		Stereotype stereotype2 =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype2",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype2);
		assertTrue(returnParameter.isStereotypeApplied(stereotype2));

	}


	public void testDependencyStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype my_dep_stereotype extends UML::Dependency end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "import base;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "class SomeClass\n";
		modelSource += "  [my_dep_stereotype] dependency Integer;\n";
		modelSource += "end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);
		Class class_ =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClassifier(), null);
		Dependency dependency = class_.getClientDependency(null);
		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("someProfile::my_dep_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype);
		assertTrue(dependency.isStereotypeApplied(stereotype));
	}

	public void testAssociationRoleStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype my_stereotype extends UML::Property end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "import base;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "class SomeClass\n";
		modelSource += "  attribute someAttribute : Integer;\n";
		modelSource += "end;\n";
		modelSource += "class AnotherClass\n";
		modelSource += "  attribute anotherAttribute : Integer;\n";
		modelSource += "end;\n";
		modelSource += "association SomeAssoc\n";
		modelSource += "  [my_stereotype] role anotherAttribute : AnotherClass;\n";
		modelSource += "  [my_stereotype] navigable role SomeClass.someAttribute;\n";
		modelSource += "end;\n";
		modelSource += "end.\n";
		IProblem [] problems = parse(profileSource, modelSource);
		assertTrue(problems.length == 1 );
		assertTrue(problems[0].getSeverity() == IProblem.Severity.WARNING);
		Class class_ =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClassifier(), null);
		Property attribute = class_.getAttribute("someAttribute", null);
		Association assoc = (Association) getRepository().findNamedElement("someModel::SomeAssoc",
				IRepository.PACKAGE.getAssociation(), null);
		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype);
		assertNotNull(assoc);
		Property anotherEnd = assoc.getOwnedEnd("anotherAttribute", null);
		assertNotNull(anotherEnd);
		assertTrue(anotherEnd.isStereotypeApplied(stereotype));
		assertFalse(attribute.isStereotypeApplied(stereotype));
	}

	public void testBasicStereotype() throws CoreException {
		String source = "";
		source += "profile someProfile;\n";
		source += "import UML;\n";
		source += "stereotype my_stereotype extends Class end;\n";
		source += "end.\n";
		parseAndCheck(source);
		Profile thisProfile = (Profile) getRepository().findPackage("someProfile", IRepository.PACKAGE.getProfile());
		assertTrue(thisProfile.isDefined());
		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype);
		assertTrue(thisProfile.getOwnedStereotypes().contains(stereotype));
	}
	
	public void testExtensions() throws CoreException {
		String source = "";
		source += "profile testExtensions;\n";
		source += "import UML;\n";
		source += "stereotype my_stereotype extends Class, Operation required end;\n";
		source += "end.\n";
		parseAndCheck(source);
		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("testExtensions::my_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		Class classMetaclass = getRepository().findNamedElement("UML::Class", Literals.CLASS, null);
		Class operationMetaclass = getRepository().findNamedElement("UML::Operation", Literals.CLASS, null);
		
		EList<Class> extendedMetaclasses = stereotype.getExtendedMetaclasses();
		assertEquals(2, extendedMetaclasses.size());
		assertEquals(classMetaclass, extendedMetaclasses.get(0));
		assertEquals(operationMetaclass, extendedMetaclasses.get(1));

		Extension operationExtension = operationMetaclass.getExtension("Operation_my_stereotype");
		assertNotNull(operationExtension);
		assertEquals(stereotype, operationExtension.getStereotype());
		assertTrue(operationExtension.isRequired());

		Extension classExtension = classMetaclass.getExtension("Class_my_stereotype");
		assertNotNull(classExtension);
		assertEquals(stereotype, classExtension.getStereotype());
		assertFalse(classExtension.isRequired());
	}

	public void testClassStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype my_stereotype extends UML::Class end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "[my_stereotype] class SomeClass end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);
		Class class_ =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClassifier(), null);
		assertNotNull(class_);
		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype);
		assertTrue(class_.isStereotypeApplied(stereotype));
	}
	
	public void testAbstractStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "abstract stereotype my_stereotype extends UML::Class end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "[my_stereotype] class SomeClass end;\n";
		modelSource += "end.\n";
		IProblem[] problems = parse(profileSource, modelSource);
		assertEquals(1, problems.length);
		FixtureHelper.assertTrue(problems, problems[0] instanceof NotAConcreteClassifier);
	}

	public void testClassStereotypeWithProperties() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "import UML;\n";
		profileSource += "stereotype my_stereotype extends Class\n";
		profileSource += "property prop1 : PrimitiveTypes::Integer;\n";
		profileSource += "property prop2 : PrimitiveTypes::Boolean;\n";
		profileSource += "end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "[my_stereotype] class SomeClass end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);

		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype);
		Class someClass =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClass_(), null);
		assertNotNull(someClass);
		assertTrue(someClass.isStereotypeApplied(stereotype));
		assertNotNull(stereotype.getAttribute("prop1", null));
		assertNotNull(stereotype.getAttribute("prop2", null));
		assertNull(stereotype.getAttribute("prop3", null));
	}

	public void testOperationStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype my_stereotype extends UML::Operation end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "class SomeClass\n";
		modelSource += "  [my_stereotype] operation someOperation();\n";
		modelSource += "  begin\n";
		modelSource += "  end;\n";
		modelSource += "end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);
		Class class_ =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClassifier(), null);
		Operation operation = class_.getOperation("someOperation", null, null);
		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype);
		assertTrue(operation.isStereotypeApplied(stereotype));
	}
	
	public void testSpecializationStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype class_annotation extends UML::Class end;\n";
		profileSource += "stereotype generalization_annotation1 extends UML::Generalization end;\n";
		profileSource += "stereotype generalization_annotation2 extends UML::Generalization end;\n";
		profileSource += "stereotype generalization_annotation3 extends UML::Generalization end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "class SuperClass1\n";
		modelSource += "end;\n";
		modelSource += "class SuperClass2\n";
		modelSource += "end;\n";
		modelSource += "class SomeClass\n";
		modelSource += "  specializes\n";
		modelSource += "    [generalization_annotation1]SuperClass1,\n";
		modelSource += "    [generalization_annotation2,generalization_annotation3]SuperClass2\n";
		modelSource += "end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);
		Class superClass1 = getRepository().findNamedElement("someModel::SuperClass1",
						IRepository.PACKAGE.getClassifier(), null);
		Class superClass2 = getRepository().findNamedElement("someModel::SuperClass2",
						IRepository.PACKAGE.getClassifier(), null);
		Class someClass =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClassifier(), null);
		Stereotype stereotype1 =
			(Stereotype) getRepository().findNamedElement("someProfile::generalization_annotation1",
							IRepository.PACKAGE.getStereotype(), null);
		Stereotype stereotype2 =
			(Stereotype) getRepository().findNamedElement("someProfile::generalization_annotation2",
							IRepository.PACKAGE.getStereotype(), null);
		Stereotype stereotype3 =
			(Stereotype) getRepository().findNamedElement("someProfile::generalization_annotation3",
							IRepository.PACKAGE.getStereotype(), null);

		assertNotNull(stereotype1);
		assertNotNull(stereotype2);
		assertNotNull(stereotype3);
		// generalization of SuperClass1
		assertTrue(someClass.getGeneralization(superClass1).isStereotypeApplied(stereotype1));
		assertFalse(someClass.getGeneralization(superClass1).isStereotypeApplied(stereotype2));
		assertFalse(someClass.getGeneralization(superClass1).isStereotypeApplied(stereotype3));
		// generalization of SuperClass2
		assertFalse(someClass.getGeneralization(superClass2).isStereotypeApplied(stereotype1));
		assertTrue(someClass.getGeneralization(superClass2).isStereotypeApplied(stereotype2));
		assertTrue(someClass.getGeneralization(superClass2).isStereotypeApplied(stereotype3));
	}
	
	public void testRealizationStereotypeApplication() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype class_annotation extends UML::Class end;\n";
		profileSource += "stereotype realization_annotation1 extends UML::InterfaceRealization end;\n";
		profileSource += "stereotype realization_annotation2 extends UML::InterfaceRealization end;\n";
		profileSource += "stereotype realization_annotation3 extends UML::InterfaceRealization end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "interface Interface1\n";
		modelSource += "end;\n";
		modelSource += "interface Interface2\n";
		modelSource += "end;\n";
		modelSource += "class SomeClass\n";
		modelSource += "  implements\n";
		modelSource += "    [realization_annotation1]Interface1,\n";
		modelSource += "    [realization_annotation2,realization_annotation3]Interface2\n";
		modelSource += "end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);
		Interface interface1 = getRepository().findNamedElement("someModel::Interface1",
						IRepository.PACKAGE.getClassifier(), null);
		Interface interface2 = getRepository().findNamedElement("someModel::Interface2",
						IRepository.PACKAGE.getClassifier(), null);
		Class someClass =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClassifier(), null);
		Stereotype stereotype1 =
			(Stereotype) getRepository().findNamedElement("someProfile::realization_annotation1",
							IRepository.PACKAGE.getStereotype(), null);
		Stereotype stereotype2 =
			(Stereotype) getRepository().findNamedElement("someProfile::realization_annotation2",
							IRepository.PACKAGE.getStereotype(), null);
		Stereotype stereotype3 =
			(Stereotype) getRepository().findNamedElement("someProfile::realization_annotation3",
							IRepository.PACKAGE.getStereotype(), null);

		assertNotNull(stereotype1);
		assertNotNull(stereotype2);
		assertNotNull(stereotype3);
		// realization of SuperClass1
		assertTrue(someClass.getInterfaceRealization(null, interface1).isStereotypeApplied(stereotype1));
		assertFalse(someClass.getInterfaceRealization(null, interface1).isStereotypeApplied(stereotype2));
		assertFalse(someClass.getInterfaceRealization(null, interface1).isStereotypeApplied(stereotype3));
		// realization of SuperClass2
		assertFalse(someClass.getInterfaceRealization(null, interface2).isStereotypeApplied(stereotype1));
		assertTrue(someClass.getInterfaceRealization(null, interface2).isStereotypeApplied(stereotype2));
		assertTrue(someClass.getInterfaceRealization(null, interface2).isStereotypeApplied(stereotype3));
	}

	public void testStereotypeApplicationPersistence() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "stereotype my_stereotype extends UML::Class end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "[my_stereotype] class SomeClass end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);

		getRepository().save(null);
		getRepository().dispose();
		//RepositoryService.DEFAULT.synchronizeCurrent();

		IRepository newRepo = MDDCore.createRepository(getRepository().getBaseURI());
		Class class_ = (Class) newRepo.findNamedElement("someModel::SomeClass", IRepository.PACKAGE.getClassifier(), null);
		assertNotNull(class_);
		Stereotype stereotype =
						(Stereotype) newRepo.findNamedElement("someProfile::my_stereotype", IRepository.PACKAGE
										.getStereotype(), null);
		assertNotNull(stereotype);
		assertTrue(class_.isStereotypeApplied(stereotype));
		newRepo.dispose();
	}

	public void testStereotypeExtendsNonMetaclass() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "class Foo end;\n";
		profileSource += "stereotype my_stereotype extends Foo end;\n";
		profileSource += "end.\n";
		IProblem[] result = compile(profileSource);
		assertTrue(Arrays.asList(result).toString(), result.length == 1);
		assertTrue(result[0].toString(), result[0] instanceof NotAMetaclass);
	}

	public void testStereotypeSpecialization() throws CoreException {
		String source = "";
		source += "profile someProfile;\n";
		source += "import UML;\n";
		source += "stereotype super_stereotype extends Class end;\n";
		source += "stereotype sub_stereotype specializes super_stereotype end;\n";
		source += "end.\n";
		parseAndCheck(source);
		final Stereotype superClass =
						(Stereotype) getRepository().findNamedElement("someProfile::super_stereotype",
										IRepository.PACKAGE.getClass_(), null);
		final Stereotype subClass =
						(Stereotype) getRepository().findNamedElement("someProfile::sub_stereotype",
										IRepository.PACKAGE.getClass_(), null);
		assertNotNull(superClass);
		assertNotNull(subClass);
		assertTrue(subClass.getSuperClasses().contains(superClass));
	}

	public void testStereotypeWithPropertiesSettingValues() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "import UML;\n";
		profileSource += "enumeration Values VALUE1; VALUE2; VALUE3; end;\n";
		profileSource += "stereotype my_stereotype extends Class\n";
		profileSource += "property prop1 : PrimitiveTypes::String;\n";
		profileSource += "property prop2 : PrimitiveTypes::Boolean;\n";
		profileSource += "property prop3 : PrimitiveTypes::Integer;\n";
		profileSource += "property prop4 : Values;\n";		
		profileSource += "end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "[my_stereotype(prop1=\"value1\",prop2=true,prop3=45,prop4=VALUE2)]\n";
		modelSource += "class SomeClass end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);

		Stereotype stereotype =
						(Stereotype) getRepository().findNamedElement("someProfile::my_stereotype",
										IRepository.PACKAGE.getStereotype(), null);
		assertNotNull(stereotype);
		Class someClass =
						(Class) getRepository().findNamedElement("someModel::SomeClass",
										IRepository.PACKAGE.getClass_(), null);
		assertEquals("value1", someClass.getValue(stereotype, "prop1"));
		assertEquals(true, someClass.getValue(stereotype, "prop2"));
		assertEquals(45, someClass.getValue(stereotype, "prop3"));
		
		Enumeration valuesEnum =
			(Enumeration) getRepository().findNamedElement("someProfile::Values",
							IRepository.PACKAGE.getEnumeration(), null);
		final EnumerationLiteral expectedLiteral = valuesEnum.getOwnedLiteral("VALUE2");
		assertNotNull(expectedLiteral);
		assertEquals(expectedLiteral, someClass.getValue(stereotype, "prop4"));
	}
	
	/**
	 * See bug 2796613.
	 */
	public void testStereotypeWithInheritedPropertiesSettingValues() throws CoreException {
		String profileSource = "";
		profileSource += "profile someProfile;\n";
		profileSource += "import UML;\n";
		profileSource += "stereotype super_stereotype extends Class\n";		
		profileSource += "property prop1 : PrimitiveTypes::String;\n";
		profileSource += "property prop2 : PrimitiveTypes::Boolean;\n";
		profileSource += "end;\n";
		profileSource += "stereotype sub_stereotype specializes super_stereotype\n";
		profileSource += "property prop2 : PrimitiveTypes::Integer;\n";
		profileSource += "property prop3 : PrimitiveTypes::Integer;\n";
		profileSource += "end;\n";
		profileSource += "end.\n";

		String modelSource = "";
		modelSource += "model someModel;\n";
		modelSource += "apply someProfile;\n";
		modelSource += "[super_stereotype(prop1=\"value1\",prop2=true)]\n";
		modelSource += "class Class1 end;\n";
		modelSource += "[sub_stereotype(prop1=\"value2\",prop2=40,prop3=45)]\n";
		modelSource += "class Class2 end;\n";
		modelSource += "end.\n";
		parseAndCheck(profileSource, modelSource);

		final Stereotype superStereotype =
			(Stereotype) getRepository().findNamedElement("someProfile::super_stereotype",
							IRepository.PACKAGE.getClass_(), null);
		final Stereotype subStereotype =
			(Stereotype) getRepository().findNamedElement("someProfile::sub_stereotype",
							IRepository.PACKAGE.getClass_(), null);
		Class class1 =
						(Class) getRepository().findNamedElement("someModel::Class1",
										IRepository.PACKAGE.getClass_(), null);
		assertTrue(class1.isStereotypeApplied(superStereotype));
		assertEquals("value1", class1.getValue(superStereotype, "prop1"));
		assertEquals(true, class1.getValue(superStereotype, "prop2"));		
		Class class2 =
			(Class) getRepository().findNamedElement("someModel::Class2",
							IRepository.PACKAGE.getClass_(), null);
		assertTrue(class2.isStereotypeApplied(subStereotype));
		assertEquals("value2", class2.getValue(subStereotype, "prop1"));
		// substereotype-only property
		assertEquals(45, class2.getValue(subStereotype, "prop3"));
		// overridden property
		assertEquals(40, class2.getValue(subStereotype, "prop2"));
	}
}
