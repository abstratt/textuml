package com.abstratt.mdd.frontend.core.spi;

import com.abstratt.mdd.core.IBasicRepository;

/**
 * A deferred reference allows cross-references to be resolved at a later moment.
 * This object has all the information necessary for resolving a reference 
 * against a given repository. Implementation defines what behavior must be 
 * performed during resolution time.
 */
public interface IDeferredReference {

	/**
	 * Resolves this deferred reference against the given repository. 
	 * 
	 * @param repository
	 */
	public void resolve(IBasicRepository repository);
}