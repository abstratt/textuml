/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.isv.IModelWeaver;
import com.abstratt.pluginutils.ISharedContextRunnable;

/**
 * A repository is a place that gathers multiple models and profiles together.
 * 
 * <p>
 * A repository provides services for model creation, storage, load and lookup.
 * </p>
 */
public interface IRepository extends IBasicRepository {
    public interface IElementVisitor {
        enum VisitorResult {
            CONTINUE, SKIP, STOP
        }

        public VisitorResult visit(Element element);
    }

    public final String MDD_PROPERTIES = "mdd.properties";

    public UMLFactory FACTORY = UMLFactory.eINSTANCE;

    public String TYPES_NAMESPACE = "mdd_types";

    public String COLLECTIONS_NAMESPACE = "mdd_collections";

    public String EXTENSIONS_NAMESPACE = "mdd_extensions";

    public String CONSOLE_NAMESPACE = "mdd_console";

    public UMLPackage PACKAGE = UMLPackage.eINSTANCE;

    public String WEAVER = "mdd.modelWeaver";

    public String EXTEND_BASE_OBJECT = "mdd.extendBaseObject";

    public String ENABLE_EXTENSIONS = "mdd.enableExtensions";

    public String ALIASES = "mdd.aliases";

    public String ENABLE_LIBRARIES = "mdd.enableLibraries";

    public String ENABLE_TYPES = "mdd.enableTypes";

    public String ENABLE_COLLECTIONS = "mdd.enableCollections";

    public String DEFAULT_LANGUAGE = "mdd.defaultLanguage";

    public String IMPORTED_PROJECTS = "mdd.importedProjects";
    
    public String LOADED_PACKAGES = "mdd.loadedPackages";

    public String TARGET_ENGINE = "mdd.target.engine";

    public String TESTS_ENABLED = "mdd.enableTests";

    public String LIBRARY_PROJECT = "mdd.isLibrary";

    public String APPLICATION_NAME = "mdd.application.name";
    
    public String APPLICATION_TITLE = "mdd.application.title";

    /**
     * Accepts the given visitor.
     * 
     * @param visitor
     */
    public void accept(IElementVisitor visitor);

    /**
     * Accepts the given visitor.
     * 
     * @param visitor
     */
    public void accept(IElementVisitor visitor, Element root);

    /**
     * Returns the first element in the repository to satisfy the given
     * condition.
     * 
     * @param condition
     *            condition an element must satisfy
     * @return the first element to satisfy the condition, or <code>null</code>
     *         if none
     */
    public Element findFirst(EObjectCondition condition);

    /**
     * Returns the all elements in the repository that satisfy the given
     * condition.
     * 
     * @param condition
     *            condition an element must satisfy
     * @param internalOnly
     *            whether only elements of the repository should be considered
     * @return all elements that satisfy the condition, never <code>null</code>
     */
    public <T extends Element> List<T> findAll(EObjectCondition condition, boolean internalOnly);

    /**
     * Creates a new package from the given qualified name. If a package with
     * the given qualified name already exists, just returns it.
     * 
     * @param qualifiedName
     * @return the package found or created
     */
    public Package createPackage(String qualifiedName);
    
    public void addTopLevelPackage(Package toAdd, String name, URI resourceURI);

    /**
     * Creates a new top-level package from the given qualified name. If a
     * package with the given qualified name already exists, just returns it.
     * 
     * @param name
     * @param packageClass
     *            the package type, or <code>null<code>
     * @param resourceURI
     *            the resource location, or <code>null</code> if the default
     *            location should be used
     * @return
     */
    public Package createTopLevelPackage(String qualifiedName, EClass packageClass, URI resourceURI);

    /**
     * Disposes this repository instance. Afterwards, any attempts to use it
     * will fail.
     */
    public void dispose();

    /**
     * Whether this repository is open. A repository is open until it is
     * disposed.
     * 
     * @return true if repository is open for business, false otherwise
     */
    public boolean isOpen();

    /**
     * Returns the package with the given qualified name and package type. This
     * is a convenience method, equivalent to: <code>
     * {@link #findNamedElement(qualifiedName, packageClass, scope)}
     * </code>
     * 
     * @param qualifiedName
     *            the qualified name for the package
     * @param packageClass
     *            the package type or <code>null</code> if any
     * @return the package found, or <code>null</code> if none
     * @see #findNamedElement(String, EClass, Namespace)
     */
    public Package findPackage(String qualifiedName, EClass packageClass);

    /**
     * Returns the base URI for this repository. The base URI is the base all
     * relative URIs are resolved against.
     * 
     * @return the base URI
     */
    public URI getBaseURI();

    /**
     * Returns the operation in the given package that is an entry point, or
     * <code>null</code> if none is found. If multiple entry point operations
     * exist, one of them will be returned.
     * 
     * @param package_
     *            the package where to look for entry point operations
     * @return an operation, or <code>null</code>
     */
    public Operation getEntryPointOperation(Package package_);

    /**
     * Returns all top-level packages of the given type. If the package class is
     * not provided, returns all top-level packages. .
     * 
     * @param packageClass
     * @return an array of top level packages, never <code>null</code>
     */
    public Package[] getTopLevelPackages(EClass packageClass);
    
    public default Package[] getOwnPackages(EClass packageClass) {
    	return Arrays.stream(getTopLevelPackages(packageClass)).filter(it -> isOwnPackage(it)).toArray(size -> new Package[size]);
    }

    /**
     * Loads an existing package with the given qualified name. If a package
     * with the given qualified name cannot be found, returns <code>null</code>.
     * Only packages under this repository base location can be found.
     * 
     * @param name
     *            the qualified name of a package
     * @return the package found, or <code>null</code>
     */
    public Package loadPackage(String name);

    /**
     * Loads an existing package from the given URI. If a package with the given
     * URI cannot be found, returns <code>null</code>.
     * 
     * @param packageURI
     *            the URI where to find a package
     * @return the package found, or <code>null</code>
     */
    public Package loadPackage(URI packageURI);

    /**
     * Removes a package with the given qualified name. Does nothing if a
     * package with the given name does not exist.
     * 
     * @param name
     *            the qualified name for a package
     */
    public void removePackage(String name);

    /**
     * Runs the given builder runnable having this repository as the current
     * in-progress repository.
     * 
     * @param runnable
     *            a runnable that builds this repository up
     * @throws CoreException
     * @see MDDCore#getInProgressRepository()
     */
    public <R, E extends Throwable> R buildRepository(ISharedContextRunnable<IRepository, R> runnable,
            IProgressMonitor monitor) throws CoreException;

    /**
     * Saves this repository. This will save all models and profiles contained
     * in this repository.
     * 
     * @param monitor
     *            a progress monitor, or <code>null</code>
     * @throws IOException
     *             if an error occurs while saving
     * @throws CoreException
     */
    public void save(IProgressMonitor monitor) throws CoreException;

    public String resolveAlias(String toResolve);

    public void makeAlias(String source, String target);

    public Properties getProperties();

    public IModelWeaver getWeaver();

    <T extends Element> List<T> findInAnyPackage(EObjectCondition condition);

    public void close();

    public NamedElementLookupCache getLookupCache();

    public String getBuild();

	boolean isSystemPackage(Package toCheck);
	
	boolean isOwnPackage(Package toCheck);
}
