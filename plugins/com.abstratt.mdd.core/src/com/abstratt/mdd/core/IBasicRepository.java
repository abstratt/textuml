package com.abstratt.mdd.core;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

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
    public default Package findPackage(String qualifiedName, EClass packageClass) {
        if (packageClass == null)
            packageClass = UMLPackage.Literals.PACKAGE;
        final Package found = (Package) findNamedElement(qualifiedName, packageClass, null);
        return found;
    }    

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
