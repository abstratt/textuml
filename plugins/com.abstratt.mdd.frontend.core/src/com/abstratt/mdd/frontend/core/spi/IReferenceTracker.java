package com.abstratt.mdd.frontend.core.spi;

import com.abstratt.mdd.core.IBasicRepository;

public interface IReferenceTracker {
	enum Step {
		/** Package structuring */
		PACKAGE_STRUCTURE(true),
		/** Import packages, profile definitions. */
		PACKAGE_IMPORTS(false),
		/** type references, default resolution step */
		GENERAL_RESOLUTION(false),
		/** Use sparingly, for read-only validation of the structural model. */
		STRUCTURE_VALIDATION(false),
		/** Profile definitions */
		DEFINE_PROFILES(false),
		/** profile applications */
		PROFILE_APPLICATIONS(false),
		/** stereotype applications */
		STEREOTYPE_APPLICATIONS(false),
		/** Meant for activity compilation. */
		LAST(false),
		/** Validations that do not prevent a model from being built. */
		WARNINGS(false);
		private boolean ordered;
		/**
		 * Whether references in this stage are supposed to be comparable to each other.
		 */
		public boolean isOrdered() {
			return ordered;
		}
		private Step(boolean ordered) {
			this.ordered = ordered;
		}
	}
	public void add(IDeferredReference ref, Step step);

	/**
	 * Resolves all deferred references against the given repository in step
	 * order, collecting resolution errors in the given problem tracker.
	 * 
	 * @param repository
	 * @param problemTracker
	 */
	public void resolve(IBasicRepository repository,
			IProblemTracker problemTracker);
}
