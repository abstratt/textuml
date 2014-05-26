package com.abstratt.mdd.core.tests.frontend.builder;

import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;

import com.abstratt.mdd.frontend.core.builder.PackageBuilder;
import com.abstratt.mdd.frontend.core.builder.UML2BuilderFactory;
import com.abstratt.mdd.frontend.core.builder.UML2ModelBuildDriver;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class ProfileTests extends AbstractElementBuilderTests {
	public ProfileTests(String name) {
		super(name);
	}
	
	public void testEmptyProfile() {
		PackageBuilder profileBuilder = new UML2BuilderFactory().newBuilder(UML2ProductKind.PROFILE);
		profileBuilder.name("myProfile");
		new UML2ModelBuildDriver().build(buildContext, profileBuilder);
		Profile product = (Profile) profileBuilder.getProduct();
		assertEquals("myProfile", product.getName());
		assertTrue(product.isDefined());
	}
	
	public void testProfileApplication() {
		PackageBuilder packageBuilder = new UML2BuilderFactory().newBuilder(UML2ProductKind.PACKAGE);
		packageBuilder.name("myPackage");
		packageBuilder.applyProfile("myProfile");

		PackageBuilder profileBuilder = new UML2BuilderFactory().newBuilder(UML2ProductKind.PROFILE);
		profileBuilder.name("myProfile");
		new UML2ModelBuildDriver().build(buildContext, packageBuilder, profileBuilder);
		Profile profile = (Profile) profileBuilder.getProduct();
		Package package_= (Package) packageBuilder.getProduct();

		assertTrue(package_.isProfileApplied(profile));
	}
}
