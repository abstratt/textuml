package com.abstratt.mdd.frontend.core.builder;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.frontend.core.spi.IActivityBuilder;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.NamespaceTracker;

/**
 * Provides access to all services required to perform a build.
 */
public class UML2BuildContext {
	private boolean requiredLineInfo = false;

	public boolean isRequiredLineInfo() {
		return requiredLineInfo;
	}

	public void setRequiredLineInfo(boolean requiredLines) {
		this.requiredLineInfo = requiredLines;
	}

	private IBasicRepository repository;
	private IProblemTracker reporter;
	private IReferenceTracker referenceTracker;
	private IActivityBuilder activityBuilder;
	private NamespaceTracker namespaceTracker = new NamespaceTracker();

	public UML2BuildContext(IBasicRepository repository, IProblemTracker reporter, IReferenceTracker referenceTracker) {
		this(repository, reporter, referenceTracker, null);
	}

	public UML2BuildContext(IBasicRepository repository, IProblemTracker reporter, IReferenceTracker referenceTracker,
	        IActivityBuilder activityBuilder) {
		this.repository = repository;
		this.reporter = reporter;
		this.referenceTracker = referenceTracker;
		this.activityBuilder = activityBuilder;
	}

	public IActivityBuilder getActivityBuilder() {
		if (activityBuilder == null)
			throw new UnsupportedOperationException("Activity building not enabled for this context");
		return activityBuilder;
	}

	public IBasicRepository getRepository() {
		return repository;
	}

	public IProblemTracker getProblemTracker() {
		return reporter;
	}

	public IReferenceTracker getReferenceTracker() {
		return referenceTracker;
	}

	public NamespaceTracker getNamespaceTracker() {
		return namespaceTracker;
	}
}
