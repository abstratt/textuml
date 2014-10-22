package com.abstratt.mdd.frontend.internal.core;

import org.eclipse.core.filesystem.IFileStore;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;

public class LocalProblemTracker implements IProblemTracker {
	private IProblemTracker baseTracker;
	private IFileStore source;

	public LocalProblemTracker(IProblemTracker baseTracker, IFileStore source) {
		super();
		this.source = source;
		this.baseTracker = baseTracker;
	}

	public void add(IProblem toReport) {
		toReport.setAttribute(IProblem.FILE_NAME, source);
		baseTracker.add(toReport);
	}

	public IProblem[] getAllProblems() {
		return baseTracker.getAllProblems();
	}

	public boolean hasProblems(Severity severity) {
		return baseTracker.hasProblems(severity);
	}
}
