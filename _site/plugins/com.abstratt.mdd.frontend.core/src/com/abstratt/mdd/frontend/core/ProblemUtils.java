package com.abstratt.mdd.frontend.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import com.abstratt.mdd.frontend.core.IProblem.Severity;

public class ProblemUtils {
	public static <T extends IProblem> List<T> filterProblems(
			Iterable<IProblem> problems, Class<? extends IProblem> problemClass) {
		Assert.isLegal(IProblem.class.isAssignableFrom(problemClass));
		List<T> filtered = new ArrayList<T>();
		for (IProblem problem : problems)
			if (problemClass.isInstance(problem))
				filtered.add((T) problem);
		return filtered;
	}

	public static <T extends IProblem> List<T> filterProblems(
			Iterable<IProblem> problems, Severity minimumSeverity) {
		List<T> filtered = new ArrayList<T>();
		for (IProblem problem : problems)
			if (problem.getSeverity().compareTo(minimumSeverity) >= 0)
				filtered.add((T) problem);
		return filtered;
	}

	public static boolean hasProblems(Iterable<IProblem> problems,
			Severity minimumSeverity) {
		for (IProblem current : problems)
			if (current.getSeverity().compareTo(minimumSeverity) >= 0)
				return true;
		return false;
	}
}
