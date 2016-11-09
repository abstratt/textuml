package com.abstratt.mdd.frontend.cli.standalone;

import static com.abstratt.mdd.frontend.cli.Helper.buildStatus;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.cli.CompilationDirectorCLI;
import com.abstratt.pluginutils.LogUtils;
import com.abstratt.standalone.StandaloneRegistryLoader;

public class StandaloneCompilationDirectorCLI {
	public static void main(String[] args) throws CoreException, IOException, URISyntaxException {
		ClassLoader classLoader = CompilationDirectorCLI.class.getClassLoader();
		StandaloneRegistryLoader registryLoader = new StandaloneRegistryLoader();
		IExtensionRegistry registry = registryLoader.createRegistry();
		registryLoader.loadAllContributions(registry, classLoader);
		if (System.getProperty("args.plugins") != null) {
			String pluginsDir = System.getProperty("args.plugins");
			StringTokenizer tokenizer = new StringTokenizer(pluginsDir, ",");
			while (tokenizer.hasMoreTokens()) {
				String current = tokenizer.nextToken();
				registryLoader.loadAllContributions(registry, new File(current));
			}
		}
		Arrays.stream(registry.getExtensionPoints()).forEach((xp) -> {
			System.out.println(xp.getUniqueIdentifier());
			Arrays.stream(xp.getExtensions()).forEach((ext) -> {
				System.out.println(">>> " + ext.getNamespaceIdentifier() + "." + ext.getSimpleIdentifier());	
			});
		});
		registryLoader.makeDefault(registry);
		EcorePlugin.ExtensionProcessor.process(null);
		registerURIMapping(URIConverter.URI_MAP, "pathmap://MDD_LIBRARIES/", "models/", "libraries/base.uml", classLoader);
		registerURIMapping(URIConverter.URI_MAP, "pathmap://MDD_LIBRARIES/", "models/", "libraries/mdd_types.uml", classLoader);
		registerURIMapping(URIConverter.URI_MAP, "pathmap://MDD_LIBRARIES/", "models/", "libraries/mdd_collections.uml", classLoader);
		registerURIMapping(URIConverter.URI_MAP, "pathmap://MDD_PROFILES/", "models/", "profiles/mdd_extensions.uml", classLoader);		
		UMLResourcesUtil.initGlobalRegistries();
		
		URIConverter.URI_MAP.forEach((key, value) -> System.out.println(MessageFormat.format("\t{0} -> \t{1}", key, value)));
		
		new CompilationDirectorCLI().doIt(null);
	}

	private static void registerURIMapping(Map<URI, URI> uriMap, String uriPrefix, String resourcePrefix, String sampleResourcePath, ClassLoader classLoader) throws CoreException, URISyntaxException {
		URL standaloneLocation = classLoader.getResource(resourcePrefix + sampleResourcePath);
		if (standaloneLocation == null) {
			LogUtils.log(buildStatus(IStatus.WARNING, "Could not find resource: " + resourcePrefix + sampleResourcePath, null));
			return;
		}
		LogUtils.log(buildStatus(IStatus.INFO, "Found resource: " + resourcePrefix + sampleResourcePath + " at " + standaloneLocation, null));
		java.net.URI mappedLocation = java.net.URI.create(uriPrefix).resolve(sampleResourcePath);
		uriMap.put(MDDUtil.fromJavaToEMF(mappedLocation), MDDUtil.fromJavaToEMF(standaloneLocation.toURI()));
	}

}
