package com.abstratt.mdd.frontend.core.spi;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker.Step;

/**
 * A deferred reference allows cross-references to be resolved at a later
 * moment. This object has all the information necessary for resolving a
 * reference against a given repository. Subclasses define what behavior must be
 * performed during resolution time.
 * 
 * This implementation is comparable so it can be used in steps where order
 * matters.
 * 
 * @see Step#isOrdered()
 */
public abstract class DeferredReference<E extends NamedElement> implements IDeferredReference,
        Comparable<DeferredReference> {
	private static int seed = 0;
	/**
	 * Used as tie-breaker when sorting references to the same name.
	 */
	private int token = seed++;
	private Namespace currentNamespace;
	private String symbolName;
	private EClass symbolType;

	/**
	 * Creates a deferred reference.
	 * 
	 * @param symbolName
	 *            a symbol name, qualified or not
	 * @param symbolType
	 *            a symbol type
	 * @param currentNamespace
	 *            the current package, or <code>null</code>
	 */
	public DeferredReference(String symbolName, EClass symbolType, Namespace currentNamespace) {
		Assert.isNotNull(symbolName);
		Assert.isNotNull(symbolType);
		this.symbolName = symbolName;
		this.symbolType = symbolType;
		this.currentNamespace = currentNamespace;
	}

	public Namespace getCurrentNamespace() {
		return currentNamespace;
	}

	public String getSymbolName() {
		return symbolName;
	}

	public EClass getSymbolType() {
		return symbolType;
	}

	/**
	 * This operation is invoked against the element found during resolution
	 * time. If no object is found, <code>null</code> is passed in.
	 * <p>
	 * Clients must override.
	 * </p>
	 * 
	 * @param element
	 */
	protected abstract void onBind(E element);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.abstratt.mdd.core.frontend.spi.IDeferredReference#resolve(com.abstratt
	 * .mdd.core.IRepository, java.util.List)
	 */
	public final void resolve(IBasicRepository repository) {
		onBind(repository.<E> findNamedElement(symbolName, symbolType, currentNamespace));
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("reference to ").append(symbolName);
		if (symbolType != null)
			sb.append(" : ").append(symbolType.getName());
		if (currentNamespace != null)
			sb.append(" from ").append(currentNamespace.getName());
		return sb.toString();
	}

	public int compareTo(DeferredReference another) {
		if (this == another)
			// the only case this returns 0 - or else references to the same
			// name will clash in sorted sets/maps
			return 0;
		final int symbolOrder = symbolName.compareTo(((DeferredReference) another).symbolName);
		// never return 0
		return symbolOrder == 0 ? (this.token - another.token) : symbolOrder;
	}
}
