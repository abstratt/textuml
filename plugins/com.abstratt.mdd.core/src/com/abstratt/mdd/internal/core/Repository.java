/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Thipor Kong - running outside of Eclipse/OSGI
 *******************************************************************************/ 
package com.abstratt.mdd.internal.core;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLParserPool;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
import org.eclipse.emf.query.statements.FROM;
import org.eclipse.emf.query.statements.IQueryResult;
import org.eclipse.emf.query.statements.SELECT;
import org.eclipse.emf.query.statements.WHERE;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.ParameterableElement;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.TemplateParameter;
import org.eclipse.uml2.uml.TemplateSignature;
import org.eclipse.uml2.uml.TemplateableElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.IRepository.IElementVisitor.VisitorResult;
import com.abstratt.mdd.core.MDDCore;
import com.abstratt.mdd.core.NamedElementLookupCache;
import com.abstratt.mdd.core.RepositoryService;
import com.abstratt.mdd.core.isv.IModelWeaver;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.pluginutils.ISharedContextRunnable;
import com.abstratt.pluginutils.LogUtils;

/**
 * A model set groups together models that can refer to each other's elements.
 */
public class Repository implements IRepository {
	
	/** Reference to the repository being built in the current thread */
	private static ThreadLocal<IRepository> inProgressRepository = new ThreadLocal<IRepository>();

	private static final Map<String, Object> LOAD_OPTIONS = new HashMap<String, Object>();

	static {
		LOAD_OPTIONS.put(XMLResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE);
		LOAD_OPTIONS.put(XMLResource.OPTION_USE_PARSER_POOL, XMLParserPool.class.getName());
	}
	
	private NamedElementLookupCache lookup = new NamedElementLookupCache(this);

	protected static List<Package> getAllImportedPackages(Package package_,
					List<Package> allImportedPackages) {
		for (PackageImport packageImport : package_.getPackageImports())
			if (packageImport.getVisibility() == VisibilityKind.PUBLIC_LITERAL) {
				Package importedPackage = packageImport.getImportedPackage();
				if (importedPackage != null && allImportedPackages.add(importedPackage))
					getAllImportedPackages(importedPackage, allImportedPackages);
			}
		return allImportedPackages;
	}

	public static IRepository getInProgress() {
		IRepository inProgress = inProgressRepository.get();
        if (inProgress != null)
            return inProgress;
        if (RepositoryService.ENABLED)
            return RepositoryService.DEFAULT.getCurrentRepository();
        return null;
	}

	private URI baseURI;

	private ResourceSet resourceSet;

	private Set<Resource> systemResources = new HashSet<Resource>();

	private Properties properties;

	private IModelWeaver weaver;

	/**
	 * Creates a model set.
	 * 
	 * @param baseURI
	 * @throws CoreException
	 */
	public Repository(URI baseURI, boolean systemPackages) throws CoreException {
		Assert.isTrue(!baseURI.isRelative(), "Repository base URI must be absolute: " + baseURI);
		// it seems trailing slash causes cross-references to become relative URIs preserving parent paths (/common/parent/../sibling/foo.uml instead of ../sibling/foo.uml) 
		if (baseURI.hasTrailingPathSeparator())
			baseURI = baseURI.trimSegments(1);
		@SuppressWarnings("unused")
		UMLPackage umlPackage = UMLPackage.eINSTANCE;
		this.baseURI = baseURI;
		properties = MDDUtil.loadRepositoryProperties(this.baseURI);
		init(systemPackages);
		load();
	}

	/**
	 * (non-Javadoc)
	 * @see com.abstratt.mdd.core.IRepository#accept(com.abstratt.mdd.core.IRepository.IElementVisitor)
	 */
	public void accept(IElementVisitor visitor) {
		accept(visitor, resourceSet.getAllContents());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.abstratt.mdd.core.IRepository#accept(com.abstratt.mdd.core.IRepository.IElementVisitor, org.eclipse.uml2.uml.Element)
	 */
	public void accept(IElementVisitor visitor, Element root) {
		// visit the root itself
		if (visitor.visit(root) != VisitorResult.CONTINUE)
			return;
		// visit the children if warranted
		TreeIterator<?> allContents = root.eAllContents();
		accept(visitor, allContents);
	}

	private void accept(IElementVisitor visitor, TreeIterator<?> allContents) {
		while (allContents.hasNext()) {
			final Object current = allContents.next();
			if (current instanceof Element) {
				Element element = (Element) current;
				switch (visitor.visit(element)) {
				case STOP:
					return;
				case SKIP:
					allContents.prune();
				case CONTINUE:
				}
			}
		}
	}

	
	public Element findFirst(EObjectCondition condition) {
		for (Resource currentResource : resourceSet.getResources()) {
			if (systemResources.contains(currentResource))
				continue;
			IQueryResult partial = new SELECT(1, new FROM(currentResource.getContents()), new WHERE(condition)).execute();
			if (!partial.isEmpty())
				return (Element) partial.iterator().next();
		}
		return null;
	}
	
	@Override
	public List<Element> findAll(EObjectCondition condition, boolean internalOnly) {
		List<Element> result = new ArrayList<Element>();
		for (Resource currentResource : resourceSet.getResources()) {
			if (systemResources.contains(currentResource))
				continue;
			if (internalOnly && !currentResource.getURI().toString().startsWith(baseURI.toString()))
				continue;
			IQueryResult partial = new SELECT(new FROM(currentResource.getContents()), new WHERE(condition)).execute();
			for (EObject object : partial)
				result.add((Element) object);
		}
		return result;
	}

	@Override
	public <T extends Element> List<T> findInAnyPackage(
			EObjectCondition condition) {
		List<Package> startingPoints = new ArrayList<Package>();
		startingPoints.addAll(Arrays.asList(getTopLevelPackages(null)));
		for (Package top : getTopLevelPackages(null))
			getAllImportedPackages(top, startingPoints);
		return MDDUtil.findAllFrom(condition, startingPoints);
	}

	private boolean addPackage(Resource resource, Package newPackage) {
		return resource.getContents().add(newPackage);
	}

	private void addSystemPackage(Package systemPackage) {
		if (!systemResources.add(systemPackage.eResource()))
			// already added
			return;
		for (PackageImport current : systemPackage.getPackageImports())
			addSystemPackage(current.getImportedPackage());
	}
	
	private boolean isSystemPackage(Package toCheck) {
		return systemResources.contains(toCheck.eResource());
	}

	private void basicSaveResource(Resource resource) throws IOException {
		final URI uri = resource.getURI();
		BufferedOutputStream outputStream =
						new BufferedOutputStream(resourceSet.getURIConverter().createOutputStream(uri), 8192);
		try {
			resource.save(outputStream, null);
		} finally {
			outputStream.close();
		}
	}

	private URI computePackageURI(String packageName) {
		return baseURI.appendSegment(packageName).appendFileExtension("uml");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.IRepository#createPackage(java.lang.String)
	 */
	public Package createPackage(String packageName) {
		Assert.isNotNull(packageName);
		Package existing = findPackage(packageName, PACKAGE.getPackage());
		if (existing != null)
			// package already exists
			return existing;
		if (!MDDUtil.isQualifiedName(packageName))
			return createTopLevelPackage(packageName, null, null);
		String parentName = MDDUtil.removeLastSegment(packageName);
		Assert.isLegal(parentName != null, "Qualified name expected");
		Package parentNamespace = findPackage(parentName, PACKAGE.getPackage());
		Assert.isLegal(parentNamespace != null, "Parent namespace not found");
		return (Package) parentNamespace.createPackagedElement(MDDUtil.getLastSegment(packageName), PACKAGE
						.getPackage());
	}

	@Override
	public Package createTopLevelPackage(String qualifiedName,
			EClass packageClass) {
		return createTopLevelPackage(qualifiedName, packageClass, null);
	}
	
	public Package createTopLevelPackage(String packageName, EClass packageClass, URI packageURI) {
		Assert.isNotNull(packageName);
		if (packageClass == null)
			packageClass = UMLPackage.Literals.PACKAGE;
		Package existing = findPackage(packageName, packageClass);
		if (existing != null) {
			// package already exists
			if (packageURI != null) {
				URI existingURI = existing.eResource().getURI();
				Assert.isLegal(existingURI.equals(packageURI), "Wrong package location for " + packageName
								+ " (existing: " + existingURI + ", expected: " + packageURI + ")");
			}
			return existing;
		}
		if (packageURI == null)
			packageURI = computePackageURI(packageName);
		Resource resource = resourceSet.createResource(packageURI);
		Package newPackage = (Package) FACTORY.create(packageClass);
		addPackage(resource, newPackage);
		MDDUtil.markGenerated(newPackage);
		newPackage.setName(packageName);
		newPackage.setURI(getBaseURI().lastSegment()+'/'+packageName);
		if (Boolean.parseBoolean(getProperties().getProperty(ENABLE_EXTENSIONS, System.getProperty(ENABLE_EXTENSIONS, Boolean.FALSE.toString()) ))) {
			Profile extensions = (Profile) findPackage(EXTENSIONS_NAMESPACE, Literals.PROFILE);
			if (extensions != null && extensions.isDefined())
				newPackage.applyProfile(extensions);
		}
		if (getWeaver() != null)
			getWeaver().packageCreated(this, newPackage);
		return newPackage;
	}

	@Override
	public void dispose() {
		systemResources.clear();
		if (!isOpen())
			return;
		LogUtils.debug(MDDCore.PLUGIN_ID, "Disposing repository at " + getBaseURI());
		if (RepositoryService.ENABLED)
			// nothing to do
			;
		else
		    MDDUtil.unloadResources(resourceSet);
		resourceSet = null;
	}
	
	@Override
	public void close() {
		System.out.println("Closing "+ this.hashCode());
		// with resource management, we don't need to unload resources
		resourceSet = null;
		systemResources.clear();
	}
	
	@Override
	public boolean isOpen() {
		return resourceSet != null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (!isOpen())
			return;
		if (RepositoryService.ENABLED)
	    	LogUtils.logWarning(MDDCore.PLUGIN_ID, "Repository being finalized but still open: " + this.getBaseURI(), null);
		else
			dispose();
	}

	/*
	 * @see com.abstratt.mdd.core.IRepository#findNamedElement(String,
	 *      EClass, Package)
	 */
	public <T extends NamedElement> T findNamedElement(String name, EClass class_, Namespace namespace) {
		if (namespace == null) {
			T cached = lookup.findInCache(name, class_);
			if (cached != null)
				return cached;
		}
		final T found = internalFindNamedElement(name, class_, namespace, true, new HashSet<Namespace>());
		if (found != null)
			lookup.addToCache(namespace == null ? name : found.getQualifiedName(), found);
		return found;
	}
	
	@Override
	public NamedElementLookupCache getLookupCache() {
		return lookup;
	}
	
	@Override
	public String getBuild() {
		if (!isOpen())
			return "";
		for (Package package1 : getTopLevelPackages(null))
			if (!isSystemPackage(package1)) {
			    String timestamp = MDDUtil.getGeneratedTimestamp(package1);
			    if (timestamp != null)
			    	return timestamp;
		    }
		return null;
	}

	/*
	 * Does a best effort search in order to find the given object. Recurse to
	 * parent if cannot find the requested symbol.
	 */
	private <T extends NamedElement> T internalFindNamedElement(String name, EClass class_, Namespace namespace, boolean deep, Set<Namespace> visited) {
		Assert.isNotNull(name);
		if (namespace != null && !visited.add(namespace))
			// already visited, avoid infinite recursion
			return null;
		// tries to resolve the name regardless the contextual namespace
		NamedElement element = internalFindNamedElement(name, class_);
		if (element != null
						&& (namespace == null || isVisible(namespace, element, MDDUtil
										.isQualifiedName(name))))
			// element found or is fully qualified, no need to look further
			return (T) element;
		// no contextual namespace provided, there is nothing else we can do
		if (namespace == null) {
			if (MDDUtil.isQualifiedName(name)) {
				namespace = loadPackage(MDDUtil.getFirstSegment(name));
				return (T) ((namespace == null) ? null : findNamedElement(MDDUtil.removeFirstSegment(name), class_,
								namespace));
			} else
				return null;
		}
		if (namespace instanceof TemplateableElement) {
			TemplateableElement asTemplate = (TemplateableElement) namespace;
			if (asTemplate.isTemplate()) {
				TemplateSignature signature = asTemplate.getOwnedTemplateSignature();
				// TODO a template may not have a signature (descendant)
				if (signature != null)
					for (TemplateParameter parameter : signature.getParameters()) {
						final ParameterableElement parameteredElement = parameter.getParameteredElement();
						if (parameteredElement instanceof NamedElement
										&& name.equals(((NamedElement) parameteredElement).getName()))
							return (T) parameteredElement;
					}

			}
		}
		// uses the contextual namespace to derive a qualified name (assuming
		// 'name' is not fully qualified)
		if (namespace.getQualifiedName() != null) {
			final String fullyQualifiedName = MDDUtil.appendSegment(namespace.getQualifiedName(), name);
			element = internalFindNamedElement(fullyQualifiedName, class_);
			if (element != null && isVisible(namespace, element, MDDUtil.isQualifiedName(name)))
				// element found, no need to look further
				return (T) element;
            for (ElementImport elementImport : namespace.getElementImports())
                if (elementImport.getName().equals(name)
                                && isVisible(namespace, elementImport.getImportedElement(), elementImport
                                                .getVisibility() != VisibilityKind.PRIVATE_LITERAL))
                    return (T) elementImport.getImportedElement();

			if (deep) {
				// check imported packages now
				List<PackageImport> imports = namespace.getPackageImports();
				for (PackageImport packageImport : imports) {
					final Package importedPackage = packageImport.getImportedPackage();
					element =
									internalFindNamedElement(name, class_, importedPackage,
													packageImport.getVisibility() != VisibilityKind.PRIVATE_LITERAL, visited);
					if ((element != null && isVisible(namespace, element, false)))
						return (T) element;
				}
			}
		}
		// search the parent now
		if (namespace.getOwner() == null)
		    return null;
		return (T) internalFindNamedElement(name, class_, (Namespace) namespace.getOwner(), deep, visited);
	}

	/*
	 * @see com.abstratt.mdd.core.IRepository#findPackage(java.lang.String, org.eclipse.emf.ecore.EClass)
	 */
	public Package findPackage(String name, EClass packageClass) {
		if (packageClass == null)
			packageClass = PACKAGE.getPackage();
		final Package found = (Package) internalFindNamedElement(name, packageClass);
		return found;
	}
	
	public String resolveAlias(String qualifiedName) {
		return resolveAlias(qualifiedName, "");
	}
	
	public void makeAlias(String source, String target) {
		properties.setProperty(ALIASES + '.' + source, target);
	}
	
	private String resolveAlias(String toResolve, String tail) {
		if (hasAlias(toResolve))
			return getAlias(toResolve) + tail;
		int separatorIndex = toResolve.lastIndexOf(NamedElement.SEPARATOR);
		return separatorIndex < 0 ? (toResolve + tail) : (resolveAlias(toResolve.substring(0, separatorIndex),toResolve.substring(separatorIndex)) + tail); 
	}

	private String getAlias(String qualifiedName) {
		return properties.getProperty(ALIASES + '.' + qualifiedName);
	}
	
	private boolean hasAlias(String qualifiedName) {
		return properties.containsKey(ALIASES + '.' + qualifiedName);
	}



	private IFileStore getBaseStore() throws CoreException {
		return EFS.getStore(java.net.URI.create(baseURI.toString()));
	}

	public URI getBaseURI() {
		return baseURI;
	}

	public Operation getEntryPointOperation(Package package_) {
		for (NamedElement element : package_.getOwnedMembers()) {
			if (!(element instanceof Classifier))
				continue;
			Classifier classifier = (Classifier) element;
			for (Operation operation : classifier.getOperations())
				if (MDDExtensionUtils.isEntryPoint(operation))
					return operation;
		}
		return null;
	}

	private Package getPackage(URI packageURI, boolean onDemand) {
		if (packageURI.isRelative())
			// when resolving, making sure we have the trailing slash
			packageURI = packageURI.resolve(baseURI.appendSegment(""));
		Resource resource;
		try {
			resource = resourceSet.getResource(packageURI, true);
		} catch (WrappedException e) {
			if (!(e instanceof Diagnostic))
				throw e;
			return null;
		}
		if (resource == null)
			return null;
		if (!onDemand)
			try {
				resource.load(null, LOAD_OPTIONS);
			} catch (IOException e) {
				LogUtils.logError(MDDCore.PLUGIN_ID, "Error loading resource " + packageURI, e);
			}
		return (Package) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.PACKAGE);
	}

	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	public Package[] getTopLevelPackages(EClass packageClass) {
		if (packageClass == null)
			packageClass = PACKAGE.getPackage();
		List<Resource> results = resourceSet.getResources();
		List<Package> result = new ArrayList<Package>(results.size());
		for (Resource currentResource : results)
			for (EObject element : currentResource.getContents())
				if (packageClass.isInstance(element))
					result.add((Package) element);
		return result.toArray(new Package[result.size()]);
	}

	/**
	 * Overridable factory method for ResourceSet.
	 * Can be used to customize ResourceSet, e.g. for standalone uses.
	 * @return the ResourceSet
	 */
	protected ResourceSet newResourceSet() {
		return new ResourceSetImpl();
	}

	private void init(boolean systemPackages) {
		resourceSet = newResourceSet();
		resourceSet.eSetDeliver(false);
		if (systemPackages)
			loadSystemPackages();
	}

	/**
	 * Finds a named element given its fully qualified name and class.
	 * <p>
	 * Ensures the resource containing the element is loaded.
	 * </p>
	 * 
	 * @param qualifiedName
	 * @param eClass
	 * @return
	 */
	private NamedElement internalFindNamedElement(String qualifiedName, EClass eClass) {
		Assert.isNotNull(qualifiedName);
		// make sure we loaded the resource for the model potentially containing
		// the element to be found
		if (MDDUtil.isQualifiedName(qualifiedName)) {
			String firstSegment = MDDUtil.getFirstSegment(qualifiedName);
			Package package_ = loadPackage(firstSegment);
			if (package_ == null) {
				// try an alias
				String resolvedAlias = resolveAlias(firstSegment);
				if (firstSegment.equals(resolvedAlias))
					// no alias
					return null;
				package_ = loadPackage(resolvedAlias);
				if (package_ == null)
					// could not find the root package
					return null;
			}
		}
		Collection<NamedElement> found = internalFindNamedElements(qualifiedName, eClass);
		return found.isEmpty() ? null : found.iterator().next();
	}

	/**
	 * Straightforward name-based element lookup.
	 */
	private Collection<NamedElement> internalFindNamedElements(String qualifiedName, EClass eClass) {
		qualifiedName = resolveAlias(qualifiedName); 
		Collection<NamedElement> found = UMLUtil.findNamedElements(resourceSet, qualifiedName, false, eClass);
		return filterByQualifiedName(qualifiedName, found);
	}

	private Collection<NamedElement> filterByQualifiedName(String qualifiedName, Collection<NamedElement> toFilter) {
		List<NamedElement> matching = new ArrayList<NamedElement>(toFilter.size());
		for (NamedElement current : toFilter)
			if (qualifiedName.equals(current.getQualifiedName()))
				matching.add(current);			
		return matching;
	}

	private boolean isManaged(Resource resource) {
		URI resourceURI = resource.getURI();
		if (resourceURI.isRelative())
			resourceURI = resourceURI.resolve(baseURI);
		return resourceURI.toString().startsWith(baseURI.toString());
	}

	private boolean isSystemResource(Resource resource) {
		return systemResources.contains(resource);
	}

	private <T extends NamedElement> boolean isVisible(Namespace current, T element, boolean fullyQualified) {
		// TODO support protected and package visibility
		if (PACKAGE.getPackage().isInstance(element))
			return true;
		Package elementPackage = element.getNearestPackage();
		if (elementPackage == current.getNearestPackage())
			return true;
		if (fullyQualified)
			return element.getVisibility() == VisibilityKind.PUBLIC_LITERAL;
		List<Package> importedPackages = current.getImportedPackages();
		if (importedPackages.contains(elementPackage))
			return true;
		return current instanceof Package && ((Package) current).visibleMembers().contains(element);
	}

	private void load() throws CoreException {
		IFileStore baseStore = getBaseStore();
		loadFrom(baseStore);
	}

	private void loadFrom(IFileStore store) throws CoreException {
		if (!store.fetchInfo().exists())
			return;
		if (store.fetchInfo().isDirectory())
			for (IFileStore current : store.childStores(EFS.NONE, null))
				loadFrom(current);
		else if (store.getName().endsWith(".uml"))
			this.loadPackage(URI.createURI(store.toURI().toString()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.IRepository#loadPackage(java.lang.String)
	 */
	public Package loadPackage(String packageName) {
		Assert.isNotNull(packageName);
		// tries first to resolve to a built-in package
		Package builtIn = findPackage(packageName, PACKAGE.getPackage());
		if (builtIn != null)
			return builtIn;
		for (Package topPackage: getTopLevelPackages(null))
			for (Package imported : topPackage.getImportedPackages())
				if (imported.getName() != null && imported.getName().equals(packageName))
					return loadPackage(URI.createURI(imported.getURI()));
		return loadPackage(computePackageURI(packageName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.IRepository#loadPackage(org.eclipse.emf.common.util.URI)
	 */
	public Package loadPackage(URI packageURI) {
		// tries first to resolve to a built-in package
		String packageName = packageURI.trimFileExtension().lastSegment();
		if (packageName == null)
			return null;
		Package builtIn = findPackage(packageName, PACKAGE.getPackage());
		if (builtIn != null)
			return builtIn;
		return getPackage(packageURI, true);
	}

	private void loadSystemPackages() {
		systemResources.clear();
		IExtensionPoint xp = RegistryFactory.getRegistry().getExtensionPoint(MDDCore.PLUGIN_ID, "systemPackage");
		IConfigurationElement[] configElems = xp.getConfigurationElements();
		for (int i = 0; i < configElems.length; i++) {
			boolean autoLoad = !Boolean.FALSE.toString().equals(configElems[i].getAttribute("autoLoad"));
			if (!autoLoad)
				continue;
			String uriValue = configElems[i].getAttribute("uri");
			URI uri = URI.createURI(uriValue);
			String requirements = configElems[i].getAttribute("requires");
			if (requirements != null) {
				String[] propertyNames = requirements.split(",");
				if (propertyNames.length > 0) {
					boolean satisfied = false;
					for (String current : propertyNames) {
						if (Boolean.parseBoolean(properties.getProperty(current, System.getProperty(current)))) {
							satisfied = true;
							break;
						}
					}
					if (!satisfied)
						continue;
				}
			}
			try {
				boolean lazyLoad = !Boolean.FALSE.toString().equals(configElems[i].getAttribute("lazyLoad"));
				Package systemPackage = getPackage(uri, lazyLoad);
				if (systemPackage != null) {
					addSystemPackage(systemPackage);
					LogUtils.debug(MDDCore.PLUGIN_ID, "Loading system package: " + uri);
				}
			} catch (WrappedException e) {
				if (!(e instanceof Diagnostic))
					throw e;
				LogUtils.logError(MDDCore.PLUGIN_ID, "Unexpected  exception loading system package: " + uri, e);
			}
		}
	}

	public void removePackage(String name) {
		Assert.isNotNull(name);
		URI uri = computePackageURI(name);
		Package found = getPackage(uri, false);
		if (found == null)
			return;
		if (isSystemResource(found.eResource()))
			throw new IllegalArgumentException("Cannot remove system package: " + uri);
		Resource resource = resourceSet.getResource(uri, false);
		if (resource != null) {
			resource.unload();
			resourceSet.getResources().remove(resource);
		}
	}

	@Override
	public <R, E extends Throwable> R buildRepository(ISharedContextRunnable<IRepository, R> runnable, IProgressMonitor monitor) throws CoreException {
		IRepository previousRepository = inProgressRepository.get();
		Assert.isTrue(previousRepository == null, "Another repository being built in context");
		inProgressRepository.set(this);
		try {
			R result = runnable.runInContext(this);
			save(monitor);
			if (RepositoryService.ENABLED && RepositoryService.DEFAULT.isInSession())
				RepositoryService.DEFAULT.synchronizeCurrent();
			return result;
		} finally {
			inProgressRepository.remove();
		}
	}

	public void save(IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Saving repository", resourceSet.getResources().size());
		try {
			getBaseStore().mkdir(EFS.NONE, null);

			for (Resource resource : resourceSet.getResources()) {
				if (!isManaged(resource))
					continue;
				if (resource.getContents().isEmpty())
					// EMF bug 225939 - our ticket 166
					continue;
				// tries to ensure IDs are stable - does not work with method names made of symbols
				if (resource instanceof XMIResource) {
					for (TreeIterator<EObject> ti = resource.getAllContents(); ti.hasNext();) {
						EObject current = ti.next();
						String identifier = UML2Util.getXMIIdentifier((InternalEObject) current);
						((XMIResource) resource).setID(current, identifier);
					}
				}
			}

			for (Resource resource : resourceSet.getResources()) {
				monitor.worked(1);
				if (!isManaged(resource))
					continue;
				if (resource.getContents().isEmpty())
					// EMF bug 225939 - our ticket 166
					continue;
				try {
					basicSaveResource(resource);
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, MDDCore.PLUGIN_ID, 0, "Error saving resource '"
									+ resource.getURI() + "'", e));
				}
			}
		} finally {
			monitor.done();
		}
	}

	public void storeModels(OutputStream out) throws IOException {
		for (Resource current : resourceSet.getResources())
			if (isManaged(current))
				current.save(out, null);
	}

	@Override
	public String toString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			storeModels(baos);
			return new String(baos.toByteArray());
		} catch (IOException e) {
			return e.toString();
		}
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	private void initWeaver() {
		String weaverName = getProperties().getProperty(WEAVER);
		IExtensionPoint point = RegistryFactory.getRegistry().getExtensionPoint(MDDCore.PLUGIN_ID, "modelWeaver");
		IConfigurationElement[] elements = point.getConfigurationElements();
		for (IConfigurationElement current : elements)
			if (weaverName.equals(current.getAttribute("name"))) {
				try {
					weaver = (IModelWeaver) current.createExecutableExtension("class");
				} catch (CoreException e) {
					LogUtils.logError(MDDCore.PLUGIN_ID, "Could not instantiate weaver: " + weaverName, e);
				}
				return;
			}
	}
	
	public IModelWeaver getWeaver() {
		if (weaver != null || !getProperties().containsKey(WEAVER))
			return weaver;
		initWeaver();
        return weaver;
    }
}
