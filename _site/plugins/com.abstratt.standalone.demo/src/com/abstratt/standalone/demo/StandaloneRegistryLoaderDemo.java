/*******************************************************************************
 * Copyright (c) 2009 Abstratt Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) 
 * 
 *******************************************************************************/

package com.abstratt.standalone.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import com.abstratt.standalone.StandaloneRegistryLoader;

public class StandaloneRegistryLoaderDemo {
	public static void main(String[] args) throws CoreException, IOException {
		if (args.length < 1) {
			System.out.println("Need a plugins directory");
			return;
		}
		StandaloneRegistryLoader loader = new StandaloneRegistryLoader();
		IExtensionRegistry registry = loader.createRegistry();
		List<URL> loadedResources = loader.loadAllContributions(registry, StandaloneRegistryLoader.class
				.getClassLoader());
		for (URL url : loadedResources)
			System.out.println(url);
		List<File> loadedFiles = loader.loadAllContributions(registry, new File(args[0]));
		for (File file : loadedFiles)
			System.out.println(file);
		for (IExtensionPoint extPoint : registry.getExtensionPoints())
        { 
		  System.out.println("extPoint: " + extPoint.getUniqueIdentifier());
		  for (IExtension extension : extPoint.getExtensions())
          {
		    System.out.println("\textension: " + extension.getNamespaceIdentifier() + '.' + extension.getSimpleIdentifier());
          }
        }
		loader.makeDefault(registry);
		System.out.println("Extensions NOT processed");
	}

}