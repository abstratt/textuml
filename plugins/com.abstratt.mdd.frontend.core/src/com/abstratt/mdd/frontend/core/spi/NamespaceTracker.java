package com.abstratt.mdd.frontend.core.spi;

import java.util.Stack;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;

import com.abstratt.mdd.core.IRepository;

/**
 * Er... tracks namespaces?
 */
public class NamespaceTracker {
	private Stack<Namespace> namespaces = new Stack<Namespace>();

	/**
	 * Returns the closest containing namespace that is an instance of the given
	 * type. If the type is <code>null</code>, returns the closest containing
	 * namespace regardless its type.
	 */
	public Namespace currentNamespace(EClass namespaceType) {
		if (namespaceType == null)
			return namespaces.isEmpty() ? null : (Namespace) namespaces.peek();
		for (int i = namespaces.size() - 1; i >= 0; i--) {
			Namespace current = namespaces.get(i);
			if (namespaceType.isInstance(current))
				return current;
		}
		return null;
	}

	/**
	 * Returns the closest containing package. Convenience method, packages is
	 * the kind of namespace most frequently referred to.
	 */
	public Package currentPackage() {
		return (Package) currentNamespace(IRepository.PACKAGE.getPackage());
	}

	public void enterNamespace(Namespace toEnter) {
		namespaces.push(toEnter);
	}

	public Namespace leaveNamespace() {
		Namespace left = namespaces.pop();
		return left;
	}
}
