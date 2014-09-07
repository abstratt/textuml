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

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.query.ocl.internal.OCLPlugin;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.internal.core.Repository;
import com.abstratt.standalone.StandaloneRegistryLoader;

public class StandaloneUtil {
	public interface RepositoryFactory {
		IRepository createRepository(URI baseURI, boolean includeBase, Properties creationSettings) throws CoreException;
	}

	public static RepositoryFactory setup() {
		setupRegistry();
		setupOCL();
		setupResources();
		return new StandaloneRepositoryFactory();
	}
	
	private static void setupOCL() {
		new OCLPlugin.Implementation() {
			@Override
			public String getSymbolicName() {
				return OCLPlugin.class.getName();
			}
		};
	}

	private static void setupRegistry() {
		try {
			StandaloneRegistryLoader loader = new StandaloneRegistryLoader();
			IExtensionRegistry registry = loader.createRegistry();
			loader.loadAllContributions(registry, Thread.currentThread().getContextClassLoader());
			loader.makeDefault(registry);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private static void setupResources() {
		EPackage.Registry.INSTANCE.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);

		Map<String, Object> extMappings = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extMappings.put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		extMappings.put(null, new ResourceFactoryImpl());
	}
	
	private static class StandaloneRepositoryFactory implements RepositoryFactory {
		@Override
		public IRepository createRepository(URI baseURI, boolean includeBase, Properties creationSettings) throws CoreException {
			return new Repository(baseURI, includeBase, creationSettings) {
				@Override
				protected ResourceSet newResourceSet() {
					ResourceSet rs = super.newResourceSet();
					
					Map<URI, URI> uriMap = rs.getURIConverter().getURIMap();
					String[][] mappings = new String[][] {
							{ "pathmap://MDD_LIBRARIES/", "models/libraries/", "base.uml" },
							{ "pathmap://MDD_PROFILES/", "models/profiles/", "mdd_extensions.uml" },
							{ "pathmap://UML_LIBRARIES/", "libraries/", "UMLPrimitiveTypes.library.uml" },
							{ "pathmap://UML_METAMODELS/", "metamodels/", "UML.metamodel.uml" },
							{ "pathmap://UML_PROFILES/", "profiles/", "StandardL2.profile.uml" }, };
					for (String[] mapping : mappings) {
						ClassLoader cl = Thread.currentThread().getContextClassLoader();
						String url = cl.getResource(mapping[1] + mapping[2]).toString();
						String toUri = url.substring(0, url.length() - mapping[2].length());
			
						uriMap.put(URI.createURI(mapping[0]), URI.createURI(toUri));
					}
	
					UMLResourcesUtil.init(rs);
					return rs;
				}
			};
		}
	};
}
