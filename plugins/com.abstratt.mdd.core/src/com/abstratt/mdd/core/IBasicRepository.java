package com.abstratt.mdd.core;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;

public interface IBasicRepository {
    /**
     * Finds a named element in this repository starting from the given starting
     * point. Returns the element with the given (potentially) qualified
     * name/type in the model being built taking the given package as the
     * starting point.
     * 
     * @param qualifiedName
     *            the qualified name for the element
     * @param class_
     *            its EMF type, or null for NamedElement
     * @param scope
     *            the current scope, or <code>null</code>
     * @return
     */
    public <T extends NamedElement> T findNamedElement(String qualifiedName, EClass class_, Namespace scope);

    /**
     * Creates a new top-level package from the given qualified name. If a
     * package with the given qualified name already exists, just returns it.
     * 
     * @param name
     * @param packageClass
     *            the package type, or <code>null<code>
     * @return
     */
    public Package createTopLevelPackage(String qualifiedName, EClass packageClass);
}
