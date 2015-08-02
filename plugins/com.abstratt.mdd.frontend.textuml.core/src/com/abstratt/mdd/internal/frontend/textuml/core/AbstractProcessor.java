package com.abstratt.mdd.internal.frontend.textuml.core;

import org.eclipse.uml2.uml.Namespace;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.ISourceMiner;
import com.abstratt.mdd.frontend.core.spi.NamespaceTracker;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;

public abstract class AbstractProcessor<N, S extends Namespace> extends DepthFirstAdapter implements
		NodeProcessor<N> {

	protected S namespace;
	protected SourceCompilationContext<Node> sourceContext;
	protected NamespaceTracker namespaceTracker;
	protected IReferenceTracker referenceTracker;
	protected ISourceMiner<Node> sourceMiner;
	protected ProblemBuilder<Node> problemBuilder;
	protected IRepository repository;

	public AbstractProcessor(SourceCompilationContext<Node> sourceContext,
			S namespace) {
		this.sourceContext = sourceContext;
		this.namespace = namespace;
		this.namespaceTracker = sourceContext.getNamespaceTracker();
		this.referenceTracker = sourceContext.getReferenceTracker();
		this.sourceMiner = sourceContext.getSourceMiner();
		this.problemBuilder = sourceContext.getProblemBuilder();
		this.repository = sourceContext.getContext().getRepository();
	}
}
