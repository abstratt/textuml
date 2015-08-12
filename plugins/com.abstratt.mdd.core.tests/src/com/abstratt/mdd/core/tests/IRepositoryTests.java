package com.abstratt.mdd.core.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.MDDCore;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.pluginutils.ISharedContextRunnable;
import com.abstratt.resman.ResourceException;

public class IRepositoryTests extends AbstractRepositoryBuildingTests {

	public IRepositoryTests(String name) {
		super(name);
	}

	public void testCanSeeCreatedElements() throws CoreException, ResourceException {
		assertNull(getRepository().findNamedElement("package1", UMLPackage.Literals.PACKAGE, null));
		IRepository localRepo = MDDCore.createRepository(getRepositoryURI());
		localRepo.buildRepository(new ISharedContextRunnable<IRepository, Object>() {
			@Override
			public Object runInContext(IRepository context) {
				context.createPackage("package1");
				return null;
			}
		}, null);
		assertNotNull(getRepository().findNamedElement("package1", UMLPackage.Literals.PACKAGE, null));
	}

	public void testBasicPackages() {
		assertNotNull(getRepository().findPackage(IRepository.TYPES_NAMESPACE, Literals.PACKAGE));
		assertNotNull(getRepository().findPackage(IRepository.COLLECTIONS_NAMESPACE, Literals.PACKAGE));
		assertNotNull(getRepository().findPackage(IRepository.CONSOLE_NAMESPACE, Literals.PACKAGE));
		assertNotNull(getRepository().findPackage(IRepository.EXTENSIONS_NAMESPACE, Literals.PROFILE));
		assertNotNull(getRepository().findNamedElement(TypeUtils.makeTypeName("Object"), Literals.CLASS, null));
		assertNotNull(getRepository().findPackage("base", Literals.PACKAGE));
	}

	public void testCrossReferenceSameRepo() throws CoreException, IOException {
		Package package1 = getRepository().createPackage("package1");
		Package package2 = getRepository().createPackage("package2");
		package2.createPackageImport(package1);
		getRepository().save(null);
		File baseDir = new File(getRepository().getBaseURI().toFileString());
		assertEquals(2, listUMLFiles(getRepositoryDir()).size());
		List<String> expectedFileNames = Arrays.asList("package1.uml", "package2.uml");
		Collection<String> actualFileNames = listUMLFileNames(getRepositoryDir());
		assertEquals(new HashSet<String>(expectedFileNames), new HashSet<String>(actualFileNames));
		File package2File = new File(baseDir, "package2.uml");
		String packageContents = FileUtils.readFileToString(package2File);
		assertEquals(-1, packageContents.indexOf(".."));
		assertEquals(-1, packageContents.indexOf("file:"));
		assertTrue(packageContents.indexOf("\"package1.uml") > 0);
	}

	private Collection<File> listUMLFiles(IFileStore repoDir) throws CoreException {
		return FileUtils.listFiles(repoDir.toLocalFile(EFS.NONE, null), new WildcardFileFilter("*.uml"), null);
	}

	public void testCrossReferenceAcrossRepositories() throws CoreException, IOException {
		IFileStore repoDir1 = getBaseDir().getChild("repo1");
		IRepository repo1 = MDDCore.createRepository(MDDUtil.fromJavaToEMF(repoDir1.toURI()));
		repo1.createPackage("package1");
		repo1.save(null);
		repo1.dispose();

		repo1 = MDDCore.createRepository(repo1.getBaseURI());

		Properties repo2Properties = new Properties();
		repo2Properties.setProperty(IRepository.IMPORTED_PROJECTS, repo1.getBaseURI().toString());
		IFileStore repoDir2 = getBaseDir().getChild("repo2");
		saveSettings(repoDir2, repo2Properties);
		IRepository repo2 = MDDCore.createRepository(MDDUtil.fromJavaToEMF(repoDir2.toURI()));
		repo2.loadPackage(EcoreUtil.getURI(repo1.loadPackage("package1")));
		repo1.dispose();
		Package package2 = repo2.createPackage("package2");
		Package loadedPackage1 = repo2.findPackage("package1", null);
		assertNotNull(loadedPackage1);
		package2.createPackageImport(loadedPackage1);
		repo2.save(null);

		File baseDir = new File(repo2.getBaseURI().toFileString());
		assertEquals(Arrays.asList("package2.uml"), new ArrayList<String>(listUMLFileNames(repoDir2)));
		File package2File = new File(baseDir, "package2.uml");
		String packageContents = FileUtils.readFileToString(package2File);
		assertEquals(-1, packageContents.indexOf("file:"));
		assertTrue(packageContents.indexOf("\"../repo1/package1.uml") > 0);
	}

	private Collection<String> listUMLFileNames(IFileStore repoDir) throws CoreException {
		Collection<String> result = new HashSet<String>();
		for (File file : listUMLFiles(repoDir))
			result.add(file.getName());
		return result;
	}

	/**
	 * Searching for named elements would create spurious empty resources.
	 */
	public void testTicket166() throws CoreException {
		File baseDir = new File(getRepository().getBaseURI().toFileString());
		getRepository().save(null);
		assertTrue(baseDir.isDirectory());
		assertEquals("Unexpected: " + Arrays.asList(listUMLFileNames(getRepositoryDir())), 0,
		        listUMLFileNames(getRepositoryDir()).size());
		assertNull(getRepository().findNamedElement("foo::Bar", Literals.CLASS, null));
		getRepository().save(null);
		assertEquals(0, listUMLFileNames(getRepositoryDir()).size());
	}

	public static Test suite() {
		return new TestSuite(IRepositoryTests.class);
	}
}
