package com.abstratt.mdd.frontend.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Stereotype;

import com.abstratt.mdd.frontend.core.IProblem;
import com.abstratt.mdd.frontend.core.InternalProblem;
import com.abstratt.mdd.frontend.core.UnclassifiedProblem;
import com.abstratt.mdd.frontend.core.spi.AbortedCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;

public abstract class ElementBuilder<E extends Element> implements IElementBuilder<E> {
	private String comment;
	private E element;
	private UML2ProductKind kind;
	private NameReference lastReference;
	protected IParentBuilder<?> parent;
	private List<NameReference> stereotypesApplied = new ArrayList<NameReference>();
	private ConditionalBuilderSet conditionalSet;
	private List<ElementBuilder<?>> dependencies = new ArrayList<ElementBuilder<?>>();
	private Integer lastLine;

	public ElementBuilder(UML2ProductKind kind) {
		this.kind = kind;
	}

	protected ElementBuilder() {
	}
	
	protected void addDependency(ElementBuilder<?> dependency) {
		dependencies.add(dependency);
	}

	protected void applyComment() {
		if (comment != null) {
			Comment newComment = getProduct().createOwnedComment();
			newComment.setBody(comment);
			newComment.getAnnotatedElements().add(getProduct());
		}
	}
	
	public ElementBuilder<E> applyStereotype(String stereotypeName) {
		this.stereotypesApplied.add(reference(stereotypeName,
				UML2ProductKind.STEREOTYPE));
		return this;
	}
	
	public <T extends ElementBuilder<? extends Element>> T as(Class<T> type) {
		if (type.isInstance(this))
			return (T) this;
		if (getParent() != null)
			return getParent().as(type);
		throw new ClassCastException(this + " as " + type);
	}
	
	public void build() {
		// just to validate
		getContext();
		if (!canBuild())
			// not chosen
			return;
		if (getProduct() == null) {
			E created = createProduct();
			setProduct(created);
		}
		enhance();
	}

	private boolean canBuild() {
		boolean dependencySatisfied = true;
		if (!dependencies.isEmpty())
			for (ElementBuilder<?> dependency : dependencies)
				if (dependencySatisfied = dependency.canBuild())
					// at least one dependency ought to be satisfied 
					break;
		return dependencySatisfied && (conditionalSet == null || conditionalSet.getChosenBuilder() == this);
	}

	public ElementBuilder<E> comment(String comment) {
		this.comment = comment;
		return this;
	}

	protected abstract E createProduct();

	protected void enhance() {
		applyComment();
		applyStereotypes();
	}

	private void applyStereotypes() {
		for (NameReference stereotypeName : this.stereotypesApplied)
			new ReferenceSetter<Stereotype>(stereotypeName, getParentProduct(),
					getContext(), IReferenceTracker.Step.STEREOTYPE_APPLICATIONS) {
				@Override
				protected void link(Stereotype stereotype) {
					if (!getProduct().isStereotypeApplicable(stereotype))
						getContext().getProblemTracker().add(
								new UnclassifiedProblem("Stereotype not applicable"));
					else
						getProduct().applyStereotype(stereotype);
				}
			};
	}

	protected UML2BuildContext getContext() {
		return UML2ModelBuildDriver.getContext();
	}

	protected EClass getEClass() {
		if (kind == null)
			throw new UnsupportedOperationException("");
		return getKind().getMetaClass();
	}

	public final UML2ProductKind getKind() {
		return kind;
	}

	public IParentBuilder<? extends Namespace> getParent() {
		return (IParentBuilder<? extends Namespace>) parent;
	}
	
	protected Namespace getParentProduct() {
		return parent == null ? null : getParent().getProduct();
	}

	public E getProduct() {
		return element;
	}

	public ElementBuilder<E> line(int line) {
		this.lastLine = line;
		return this;
	}

	public ElementBuilder<E> location(String location) {
		lastReference.setLocation(location);
		return this;
	}

	public ElementBuilder<E> propertyValue(String stereotypeName,
			String propertyName, Object value) {
		return this;
	}

	protected NameReference reference(String name, UML2ProductKind kind) {
		lastReference = new NameReference(name, kind);
		if (lastLine == null && getContext().isRequiredLineInfo())
			abortCompilation(new InternalProblem("No line number set in reference to " + name + ":" + kind.name() + "  from " + toUserString()));
		lastReference.setLine(lastLine);
		return lastReference;
	}

	protected String toUserString() {
		return getUserName() + lastLine == null ? "" : (":" + lastLine);
	}

	protected String getUserName() {
		return this.getKind().name();
	}

	public void setParent(IParentBuilder<?> parent) {
		this.parent = parent;
	}

	private void setProduct(E element) {
		this.element = element;
	}
	
	protected void abortStatement(IProblem problem) {
		collectProblem(problem);
		throw new AbortedStatementCompilationException();
	}

	protected void abortScope(IProblem problem) {
		collectProblem(problem);
		throw new AbortedScopeCompilationException();
	}
	
	protected void abortCompilation(IProblem problem) {
		collectProblem(problem);
		throw new AbortedCompilationException();
	}


	private void collectProblem(IProblem problem) {
		if (problem.getAttribute(IProblem.LINE_NUMBER) == null)
			problem.setAttribute(IProblem.LINE_NUMBER, lastLine);
		getContext().getProblemTracker().add(problem);
	}

	public void setConditionalSet(ConditionalBuilderSet conditionalBuilderSet) {
		this.conditionalSet = conditionalBuilderSet;
	}

	@Override
	public String toString() {
		return toUserString();
	}
}
