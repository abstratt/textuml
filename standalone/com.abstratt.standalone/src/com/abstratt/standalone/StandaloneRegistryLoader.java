/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    pwebster@ca.ibm.com - initial API and implementation
 *    Rafael Chaves (Abstratt Technologies) - clean-up 
 * 
 * Source: 
 *     http://dev.eclipse.org/newslists/news.eclipse.platform/msg67801.html
 *******************************************************************************/

package com.abstratt.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.ContributorFactorySimple;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.spi.IRegistryProvider;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.core.runtime.spi.RegistryStrategy;

/**
 * Takes an eclipse/plugins directory and loads an IExtensionRegistry with the
 * plugin.xml information. It'll even try and load the bundle id and main
 * localization information.
 * 
 * To run it you need 3 jars:
 * <ul>
 * <li>org.eclipse.equinox.supplement or org.eclipse.osgi</li>
 * <li>org.eclipse.equinox.common</li>
 * <li>org.eclipse.equinox.registry</li>
 * </ul>
 */
public class StandaloneRegistryLoader {

	private static final String LOCALIZATION_NAME = "plugin.properties";
	private static final String PLUGIN_XML_NAME = "plugin.xml";

	public static final String BUNDLE_SYMBOLICNAME = "Bundle-SymbolicName";
	public final static String BUNDLE_LOCALIZATION = "Bundle-Localization";

	private static final class StandaloneRegistryStrategy extends
			RegistryStrategy {
		private StandaloneRegistryStrategy(File[] storageDirs,
				boolean[] cacheReadOnly) {
			super(storageDirs, cacheReadOnly);
		}

		@Override
		public String translate(String key, ResourceBundle resources) {
			try {
				return super.translate(key, resources);
			} catch (MissingResourceException e) {
				// too bad, stay with original key
			}
			return key;
		}

		@Override
		public Object createExecutableExtension(
				RegistryContributor contributor, String className,
				String overridenContributorName) throws CoreException {
			try {
				return Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, contributor
						.getName(), "Error creating executable extension", e));
			}
		}
	}

	private static class BundleInfo {
		InputStream pluginxml = null;
		ResourceBundle resources = null;
		IContributor contributor = null;
	}

	/**
	 * @see RegistryFactory#setDefaultRegistryProvider(IRegistryProvider)
	 * @param defaultRegistry
	 * @throws CoreException
	 */
	public void makeDefault(final IExtensionRegistry defaultRegistry)
			throws CoreException {
		RegistryFactory.setDefaultRegistryProvider(new IRegistryProvider() {

			public IExtensionRegistry getRegistry() {
				return defaultRegistry;
			}
		});
	}

	public IExtensionRegistry createRegistry() {
		return RegistryFactory.createRegistry(new StandaloneRegistryStrategy(
				null, null), null, null);
	}

	public List<URL> loadAllContributions(IExtensionRegistry registry,
			ClassLoader loader) throws IOException {
		Enumeration<URL> resources = loader.getResources(PLUGIN_XML_NAME);
		List<URL> loaded = new ArrayList<URL>();
		while (resources.hasMoreElements()) {
			final URL nextManifest = resources.nextElement();
			if (loadContributions(registry, nextManifest))
				loaded.add(nextManifest);
		}
		return loaded;
	}

	public boolean loadContributions(IExtensionRegistry registry, URL url)
			throws IOException {
		BundleInfo in = getPluginInfo(url);
		if (in == null)
			return false;
		if (registry.getExtensions(in.contributor).length != 0
				|| registry.getExtensionPoints(in.contributor).length != 0)
			return false;
		try {
			registry.addContribution(in.pluginxml, in.contributor, false,
					in.contributor.getName(), in.resources, null);
			System.out.println("Loading " + url);
			return true;
		} finally {
			in.pluginxml.close();
		}
	}

	public List<File> loadAllContributions(IExtensionRegistry registry,
			File directory) throws IOException {
		File[] files = directory.listFiles();
		if (files == null)
			return Collections.emptyList();
		List<File> loaded = new ArrayList<File>(files.length);
		for (int i = 0; i < files.length; i++)
			if (loadContributions(registry, files[i]))
				loaded.add(files[i]);
		return loaded;
	}

	public boolean loadContributions(IExtensionRegistry registry, File current)
			throws IOException {
		BundleInfo in = getPluginInfo(current);
		if (in == null)
			return false;
		if (registry.getExtensions(in.contributor).length > 0
				|| registry.getExtensionPoints(in.contributor).length > 0)
			return false;
		try {
			System.out.println("Loading " + current);
			registry.addContribution(in.pluginxml, in.contributor, false,
					in.contributor.getName(), in.resources, null);
			return true;
		} finally {
			in.pluginxml.close();
		}
	}

	private BundleInfo getPluginInfo(URL url) throws IOException {
		if ("jar".equals(url.getProtocol()))
			// unwrap the jar URL
			url = new URL(url.getPath());
		String path = url.getPath();
		if (File.separatorChar != '/')
			path = path.replace(File.separatorChar, '/');
		if (path.indexOf('!') >= 0)
			path = path.substring(0, path.indexOf('!'));
		File file = new File(path);
		if (file.getName().equals(PLUGIN_XML_NAME))
			// an exploded JAR (cached configuration)
			return getPluginInfo(file.getParentFile());
		String propName = LOCALIZATION_NAME;
		JarFile jf = new JarFile(file);
		JarEntry entry = jf.getJarEntry(PLUGIN_XML_NAME);
		if (entry == null)
			return null;
		BundleInfo b = new BundleInfo();
		b.pluginxml = jf.getInputStream(entry);
		b.contributor = ContributorFactorySimple.createContributor(jf
				.getManifest().getMainAttributes()
				.getValue(BUNDLE_SYMBOLICNAME).split(";")[0].trim());
		String baseName = jf.getManifest().getMainAttributes().getValue(
				BUNDLE_LOCALIZATION);
		if (baseName != null)
			propName = baseName + ".properties";
		JarEntry prop = jf.getJarEntry(propName);
		if (prop != null) {
			InputStream translationStream = jf.getInputStream(prop);
			try {
				b.resources = new PropertyResourceBundle(translationStream);
			} finally {
				translationStream.close();
			}
		}
		return b;
	}

	private BundleInfo getPluginInfo(File file) throws IOException {
		String propName = LOCALIZATION_NAME;
		if (file.isDirectory()) {
			File xml = new File(file, PLUGIN_XML_NAME);
			if (xml.exists()) {
				BundleInfo b = new BundleInfo();
				b.pluginxml = new FileInputStream(xml);
				b.contributor = ContributorFactorySimple.createContributor(file
						.getName().split("_")[0]);
				File cont = new File(file, "META-INF/MANIFEST.MF");
				if (cont.exists()) {
					FileInputStream manifestStream = new FileInputStream(cont);
					try {
						Manifest m = new Manifest(manifestStream);
						ContributorFactorySimple.createContributor(m
								.getMainAttributes().getValue(
										BUNDLE_SYMBOLICNAME).split(";")[0]
								.trim());
						String baseName = m.getMainAttributes().getValue(
								BUNDLE_LOCALIZATION);
						if (baseName != null)
							propName = baseName + ".properties";
					} finally {
						manifestStream.close();
					}
				}
				File prop = new File(file, propName);
				if (prop.exists()) {
					FileInputStream translationStream = new FileInputStream(
							prop);
					try {
						b.resources = new PropertyResourceBundle(
								translationStream);
					} finally {
						translationStream.close();
					}
				}
				return b;
			}
			return null;
		}
		if (file.getName().endsWith("jar")) {
			JarFile jf = new JarFile(file);
			JarEntry entry = jf.getJarEntry(PLUGIN_XML_NAME);
			if (entry != null) {
				BundleInfo b = new BundleInfo();
				b.pluginxml = jf.getInputStream(entry);
				b.contributor = ContributorFactorySimple.createContributor(jf
						.getManifest().getMainAttributes().getValue(
								BUNDLE_SYMBOLICNAME).split(";")[0].trim());
				String baseName = jf.getManifest().getMainAttributes()
						.getValue(BUNDLE_LOCALIZATION);
				if (baseName != null)
					propName = baseName + ".properties";
				JarEntry prop = jf.getJarEntry(propName);
				if (prop != null) {
					InputStream translationStream = jf.getInputStream(prop);
					try {
						b.resources = new PropertyResourceBundle(
								translationStream);
					} finally {
						translationStream.close();
					}
				}
				return b;
			}
		}
		return null;
	}
}