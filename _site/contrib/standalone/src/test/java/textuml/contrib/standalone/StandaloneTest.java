/*******************************************************************************
 * Copyright (c) 2013 Thipor Kong
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thipor Kong - running outside of Eclipse/OSGI
 *******************************************************************************/ 
package textuml.contrib.standalone;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class StandaloneTest {
	private static StandaloneUtil.RepositoryFactory repositoryFactory;
	
	public static Test suite() {
		repositoryFactory = StandaloneUtil.setup();
		System.setProperty(AbstractRepositoryTests.RepositoryFactory.class.getName(), StandaloneTestRepositoryFactory.class.getName());
		
		TestSuite suite = new TestSuite(StandaloneTest.class.getName());
		suite.addTest(com.abstratt.mdd.core.tests.AllCoreTests.suite());
		return suite;
	}
	
	public static class StandaloneTestRepositoryFactory implements AbstractRepositoryTests.RepositoryFactory {
		@Override
		public IRepository createRepository(URI baseURI, boolean includeBase, Properties creationSettings) throws CoreException {
			return repositoryFactory.createRepository(baseURI, includeBase, creationSettings);
		}
	}
}
