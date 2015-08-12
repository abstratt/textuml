package com.abstratt.mdd.frontend.core;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.Problem;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;

public class QueryOperationsMustBeSideEffectFree extends Problem {

	public QueryOperationsMustBeSideEffectFree() {
		super(Severity.WARNING);
	}

	@Override
	public String getMessage() {
		return "Query operations must be side-effect free";
	}

	public static <N> void ensure(boolean condition, ProblemBuilder<N> builder, N node) {
		if (!condition) {
			IProblem problem = new QueryOperationsMustBeSideEffectFree();
			builder.addProblem(problem, node);
		}
	}

}
