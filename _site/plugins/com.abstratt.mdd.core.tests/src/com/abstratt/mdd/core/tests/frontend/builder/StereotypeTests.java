package com.abstratt.mdd.core.tests.frontend.builder;

import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Stereotype;

import com.abstratt.mdd.frontend.core.IProblem.Severity;
import com.abstratt.mdd.frontend.core.builder.ClassifierBuilder;
import com.abstratt.mdd.frontend.core.builder.PackageBuilder;
import com.abstratt.mdd.frontend.core.builder.UML2BuilderFactory;
import com.abstratt.mdd.frontend.core.builder.UML2ModelBuildDriver;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class StereotypeTests extends AbstractElementBuilderTests {
	public StereotypeTests(String name) {
		super(name);
	}

	public void testStereotypeApplication() {
		// builds a package with a class, applying a stereotype
		PackageBuilder packageBuilder = new UML2BuilderFactory()
				.newBuilder(UML2ProductKind.PACKAGE);
		packageBuilder.name("myPackage");
		packageBuilder.applyProfile("myProfile");
		ClassifierBuilder classBuilder = packageBuilder
				.newClassifier(UML2ProductKind.CLASS);
		classBuilder.name("MyClass").applyStereotype("Stereotype1");

		// builds a profile with a stereotype
		PackageBuilder profileBuilder = new UML2BuilderFactory()
				.newBuilder(UML2ProductKind.PROFILE);
		profileBuilder.name("myProfile");
		ClassifierBuilder stereotypeBuilder = profileBuilder
				.newClassifier(UML2ProductKind.STEREOTYPE);
		stereotypeBuilder.name("Stereotype1");
		stereotypeBuilder.extend("uml::Class", false);

		// builds the model from the given package builders
		new UML2ModelBuildDriver().build(buildContext, packageBuilder,
				profileBuilder);
		
		assertFalse(buildContext.getProblemTracker().hasProblems(Severity.ERROR));

		Classifier targetClass = classBuilder.getProduct();
		Stereotype stereotype = (Stereotype) stereotypeBuilder.getProduct();
		assertTrue(targetClass.isStereotypeApplied(stereotype));
	}

	public void testStandaloneClass() {
		ClassifierBuilder classBuilder = new UML2BuilderFactory()
				.newBuilder(UML2ProductKind.CLASS);
		classBuilder.name("myClass");
		new UML2ModelBuildDriver().build(buildContext, classBuilder);
		Classifier product = classBuilder.getProduct();
		assertEquals("myClass", product.getName());
		assertNull(product.getNearestPackage());
	}

	public void testSpecialize() {
		PackageBuilder packageBuilder = new UML2BuilderFactory()
				.newBuilder(UML2ProductKind.PACKAGE);
		packageBuilder.name("mypackage");
		ClassifierBuilder subClassBuilder = packageBuilder.newClassifier(
				UML2ProductKind.CLASS).specialize("myBaseClass");
		subClassBuilder.name("myClass");
		ClassifierBuilder superClassBuilder = packageBuilder
				.newClassifier(UML2ProductKind.CLASS);
		superClassBuilder.name("myBaseClass");

		new UML2ModelBuildDriver().build(buildContext, packageBuilder);
		assertNotNull(subClassBuilder.getProduct());
		assertNotNull(superClassBuilder.getProduct());
		assertTrue(subClassBuilder.getProduct().getGenerals().contains(
				superClassBuilder.getProduct()));
	}

	public void testImplement() {
		PackageBuilder packageBuilder = new UML2BuilderFactory()
				.newBuilder(UML2ProductKind.PACKAGE);
		packageBuilder.name("mypackage");
		ClassifierBuilder classBuilder = packageBuilder.newClassifier(
				UML2ProductKind.CLASS).implement("myInterface");
		classBuilder.name("myClass");
		ClassifierBuilder interfaceBuilder = packageBuilder
				.newClassifier(UML2ProductKind.INTERFACE);
		interfaceBuilder.name("myInterface");

		new UML2ModelBuildDriver().build(buildContext, packageBuilder);
		assertNotNull(classBuilder.getProduct());
		assertNotNull(interfaceBuilder.getProduct());
		assertTrue(((BehavioredClassifier) classBuilder.getProduct())
				.getAllImplementedInterfaces().contains(
						interfaceBuilder.getProduct()));
	}

}
