package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;

import com.abstratt.mdd.frontend.core.IProblem;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.DeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker.Step;

public abstract class ReferenceSetter<N extends NamedElement> extends DeferredReference<N> {

	private UML2BuildContext context;
	private NameReference nameReference;

	public ReferenceSetter(NameReference reference,
			Namespace currentNamespace, UML2BuildContext context) {
		this(reference, currentNamespace, context, Step.GENERAL_RESOLUTION);
	}
	
	public ReferenceSetter(NameReference reference,
			Namespace currentNamespace, UML2BuildContext context, IReferenceTracker.Step step) {
		super(reference.getName(), reference.getElementType().getMetaClass(), currentNamespace);
		this.context = context;
		this.nameReference = reference;
		context.getReferenceTracker().add(this, step);
	}

	@Override
	protected void onBind(N element) {
		if (element == null) {
			UnresolvedSymbol problem = new UnresolvedSymbol(getSymbolName());
			problem.setAttribute(IProblem.LINE_NUMBER, nameReference.getLine());
			problem.setAttribute(IProblem.FILE_NAME, nameReference.getLocation());
			context.getProblemTracker().add(problem);
			return;
		}
		link((N) element);
	}
	
	protected abstract void link(N found);
}
