package com.abstratt.mdd.core.tests.frontend.builder;

import org.eclipse.uml2.uml.Package;

import com.abstratt.mdd.frontend.core.builder.ClassifierBuilder;
import com.abstratt.mdd.frontend.core.builder.PackageBuilder;
import com.abstratt.mdd.frontend.core.builder.UML2BuilderFactory;
import com.abstratt.mdd.frontend.core.builder.UML2ModelBuildDriver;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;

public class PackageBuilderTests extends AbstractElementBuilderTests {
	public PackageBuilderTests(String name) {
		super(name);
	}
	
	public void testEmptyPackage() {
		PackageBuilder packageBuilder = new UML2BuilderFactory().newBuilder(UML2ProductKind.PACKAGE);
		packageBuilder.name("myPackage");
		new UML2ModelBuildDriver().build(buildContext, packageBuilder);
		Package product = packageBuilder.getProduct();
		assertEquals("myPackage", product.getName());
	}

	public void testPackageWithClass() {
		PackageBuilder packageBuilder = new UML2BuilderFactory().newBuilder(UML2ProductKind.PACKAGE);
		packageBuilder.name("myPackage");
		ClassifierBuilder classBuilder = packageBuilder.newClassifier(UML2ProductKind.CLASS);
		classBuilder.name("myClass");
		new UML2ModelBuildDriver().build(buildContext, packageBuilder);
		assertNotNull(packageBuilder.getProduct());
		assertNotNull(classBuilder.getProduct());
		assertEquals("myClass", classBuilder.getProduct().getName());
		assertNotNull(classBuilder.getProduct().getNearestPackage());
		assertSame(classBuilder.getProduct().getNearestPackage(), packageBuilder.getProduct());
		assertEquals("myPackage::myClass", classBuilder.getProduct().getQualifiedName());
	}
}
