package com.abstratt.mdd.core.isv;

import com.abstratt.mdd.core.IRepository;

/**
 * Things one can do from a model weaver:
 * <ul>
 * <li>add new imported packages to packages just created to enhance the symbol
 * lookup
 * <li>apply a profile to a package
 * <li>apply stereotypes to classes, operations and other elements
 *
 */
public interface IModelWeaver {
	public void packageCreated(IRepository repository, org.eclipse.uml2.uml.Package created);

	public void repositoryComplete(IRepository repository);
}
