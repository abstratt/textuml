package com.abstratt.mdd.core.tests;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.pluginutils.ISharedContextRunnable;

public class IRepositoryAliasingTests extends AbstractRepositoryBuildingTests {

	private static final ISharedContextRunnable<IRepository, Object> EMPTY_BUILDER = new ISharedContextRunnable<IRepository, Object>() {
		@Override
		public Object runInContext(IRepository context) {
			return null;
		}
	};

	public IRepositoryAliasingTests(String name) {
		super(name);
	}
	
	@Override
	protected Properties createDefaultSettings() {
		return new Properties();
	}

	public void testNoBasePackage() throws CoreException {
		assertNull(getRepository().findPackage("base", Literals.PACKAGE));
		assertNull(getRepository().findPackage(IRepository.EXTENSIONS_NAMESPACE, Literals.PROFILE));
		assertNull(getRepository().findNamedElement("base::Object", Literals.CLASS, null));
	}

	public void testAliasing() {
		getRepository().makeAlias("sourcePackage", "targetPackage");
		getRepository().makeAlias("sourcePackage::sourceClass2", "targetPackage::targetClass2");
		assertEquals("targetPackage", getRepository().resolveAlias("sourcePackage"));
		assertEquals("targetPackage::sourceClass1", getRepository().resolveAlias("sourcePackage::sourceClass1"));
		assertEquals("anotherPackage", getRepository().resolveAlias("anotherPackage"));
		assertEquals("anotherPackage::anotherClass", getRepository().resolveAlias("anotherPackage::anotherClass"));
	}
	
	public void testBasePackageAliasing() throws CoreException {
		String modelSource = "";
		modelSource += "model myBase;\n";
		modelSource += "class Object end;\n";
		modelSource += "class Integer end;\n";
		modelSource += "end.";
		
		assertNull(getRepository().findPackage("base", Literals.PACKAGE));
		
		parseAndCheck(modelSource);
		getRepository().makeAlias("mdd_types", "myBase");
		
		Package basePackage = getRepository().findPackage("mdd_types", Literals.PACKAGE);
		assertNotNull(basePackage);
		assertEquals("myBase", basePackage.getQualifiedName());
		NamedElement baseObject = getRepository().findNamedElement("mdd_types::Object", Literals.CLASS, null);
		assertNotNull(baseObject);
		assertEquals("myBase::Object", baseObject.getQualifiedName());
		NamedElement baseInteger = getRepository().findNamedElement("mdd_types::Integer", Literals.CLASS, null);
		assertNotNull(baseInteger);
		assertEquals("myBase::Integer", baseInteger.getQualifiedName());
	}
	
	public void testBaseClassAliasing() throws CoreException {
		String modelSource = "";
		modelSource += "model myBase;\n";
		modelSource += "class MyObject end;\n";
		modelSource += "class MyInteger end;\n";
		modelSource += "end.";
		
		assertNull(getRepository().findPackage("mdd_types", Literals.PACKAGE));
		
		parseAndCheck(modelSource);
		getRepository().makeAlias("mdd_types", "myBase");
		getRepository().makeAlias("mdd_types::Object", "myBase::MyObject");
		getRepository().makeAlias("mdd_types::Integer", "myBase::MyInteger");
		
		Package basePackage = getRepository().findPackage("mdd_types", Literals.PACKAGE);
		assertNotNull(basePackage);
		assertEquals("myBase", basePackage.getQualifiedName());
		NamedElement baseObject = getRepository().findNamedElement("mdd_types::Object", Literals.CLASS, null);
		assertNotNull(baseObject);
		assertEquals("myBase::MyObject", baseObject.getQualifiedName());
		NamedElement baseInteger = getRepository().findNamedElement("mdd_types::Integer", Literals.CLASS, null);
		assertNotNull(baseInteger);
		assertEquals("myBase::MyInteger", baseInteger.getQualifiedName());
	}
	
	public static Test suite() {
		return new TestSuite(IRepositoryAliasingTests.class);
	}
}
