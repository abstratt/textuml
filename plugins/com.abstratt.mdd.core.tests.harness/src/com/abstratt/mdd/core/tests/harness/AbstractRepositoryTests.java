package com.abstratt.mdd.core.tests.harness;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.RepositoryService;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.resman.Resource;
import com.abstratt.resman.Task;

public class AbstractRepositoryTests extends TestCase {
	protected static final String TEXTUML_EXTENSION = "tuml";

	protected final IFileStore baseDir = computeBaseDir();
	private final IFileStore repositoryDir = baseDir.getChild(getName());

	public AbstractRepositoryTests(String name) {
		super(name);
	}

	public AbstractRepositoryTests() {
	}

	protected void originalRunTest() throws Throwable {
		super.runTest();
	}

	protected void runInContext(final Runnable runnable) {
		RepositoryService.DEFAULT.runTask(getRepositoryURI(), new Task<Object>() {
			@Override
			public Object run(Resource<?> resource) {
				runnable.run();
				return null;
			}
		});
	}

	protected IFileStore getBaseDir() {
		return baseDir;
	}

	protected URI getRepositoryURI() {
		return createRepositoryURI(getRepositoryDir());
	}

	protected IFileStore getRepositoryDir() {
		return repositoryDir;
	}

	protected URI createRepositoryURI(IFileStore dir) {
		return MDDUtil.fromJavaToEMF(dir.toURI());
	}

	protected IFileStore computeBaseDir() {
		final String tempDir = System.getProperty("java.io.tmpdir");
		IFileStore baseDir = EFS.getLocalFileSystem().getStore(new Path(tempDir)).getChild("tests");
		return baseDir;
	}

	public <T> T getFeature(Class<T> featureClass) {
		return RepositoryService.DEFAULT.getCurrentResource().getFeature(featureClass);
	}

	protected String memory(String... messages) {
		StringBuffer sb = new StringBuffer();
		for (String current : messages) {
			sb.append(current);
			sb.append(' ');
		}
		sb.append(toMB(Runtime.getRuntime().freeMemory()) + " / " + toMB(Runtime.getRuntime().totalMemory()));
		return sb.toString();
	}

	private static String toMB(long byteCount) {
		return byteCount / (1024 * 1024) + "MB";
	}

	protected Property getProperty(String propertyName) {
		return get(propertyName, UMLPackage.Literals.PROPERTY);
	}

	protected Operation getOperation(String operationName) {
		return get(operationName, UMLPackage.Literals.OPERATION);
	}

	protected Activity getActivity(String operationName) {
		Operation operation = get(operationName, UMLPackage.Literals.OPERATION);
		Activity activity = ActivityUtils.getActivity(operation);
		assertNotNull(operationName, activity);
		return activity;
	}

	protected org.eclipse.uml2.uml.Class getClass(String className) {
		return get(className, UMLPackage.Literals.CLASS);
	}

	protected <T extends NamedElement> T get(String name, EClass eClass) {
		T found = getRepository().findNamedElement(name, eClass, null);
		assertNotNull(eClass.getName() + " not found: " + name, found);
		return found;
	}

	public IRepository getRepository() {
		return getFeature(IRepository.class);
	}

	protected void saveSettings(IFileStore dir, Properties settings) throws IOException, CoreException {
		ByteArrayOutputStream rendered = new ByteArrayOutputStream();
		settings.store(rendered, null);
		dir.mkdir(EFS.NONE, null);
		FileUtils.writeByteArrayToFile(new File(dir.toLocalFile(EFS.NONE, null), IRepository.MDD_PROPERTIES),
		        rendered.toByteArray());
	}

	protected Properties createDefaultSettings() {
		Properties creationSettings = new Properties();
		creationSettings.setProperty(IRepository.ENABLE_EXTENSIONS, Boolean.TRUE.toString());
		creationSettings.setProperty(IRepository.ENABLE_LIBRARIES, Boolean.TRUE.toString());
		creationSettings.setProperty(IRepository.DEFAULT_LANGUAGE, getExtension());
		return creationSettings;
	}

	protected String getExtension() {
		return TEXTUML_EXTENSION;
	}
}
