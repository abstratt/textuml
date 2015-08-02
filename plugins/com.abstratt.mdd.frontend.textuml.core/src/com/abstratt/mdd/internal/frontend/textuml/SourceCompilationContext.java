package com.abstratt.mdd.internal.frontend.textuml;

import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.ISourceMiner;
import com.abstratt.mdd.frontend.core.spi.NamespaceTracker;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;

public class SourceCompilationContext<N> {
    private CompilationContext context;
    private NamespaceTracker namespaceTracker;
    private ISourceMiner<N> sourceMiner;
    private ProblemBuilder<N> problemBuilder;

    public CompilationContext getContext() {
		return context;
	}
	public NamespaceTracker getNamespaceTracker() {
		return namespaceTracker;
	}
	public ISourceMiner<N> getSourceMiner() {
		return sourceMiner;
	}
	public ProblemBuilder<N> getProblemBuilder() {
		return problemBuilder;
	}
	public SourceCompilationContext(CompilationContext context,
			NamespaceTracker namespaceTracker,
			ISourceMiner<N> sourceMiner,
			ProblemBuilder<N> problemBuilder) {
		this.context = context;
		this.namespaceTracker = namespaceTracker;
		this.sourceMiner = sourceMiner;
		this.problemBuilder = problemBuilder;
	}
	public IReferenceTracker getReferenceTracker() {
		return context.getReferenceTracker();
	}
}
