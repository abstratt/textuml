/**
 * 
 */
package com.abstratt.mdd.core.tests.harness;

import junit.framework.Assert;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;

public class StrictProblemTracker implements IProblemTracker {

	@Override
	public void add(IProblem toReport) {
		Assert.fail(toReport.toString());
	}

	@Override
	public IProblem[] getAllProblems() {
		return new IProblem[0];
	}

	@Override
	public boolean hasProblems(Severity minimumSeverity) {
		return false;
	}

}