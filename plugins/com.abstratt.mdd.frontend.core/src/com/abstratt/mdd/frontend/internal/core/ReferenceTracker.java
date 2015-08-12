/**
 * 
 */
package com.abstratt.mdd.frontend.internal.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.MDDCore;
import com.abstratt.mdd.frontend.core.InternalProblem;
import com.abstratt.mdd.frontend.core.spi.AbortedCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.pluginutils.LogUtils;

public class ReferenceTracker implements IReferenceTracker {
	private static class Stage implements Comparable<Stage> {
		private Collection<IDeferredReference> references;
		private Step sequence;

		public Stage(Step sequence) {
			this.sequence = sequence;
			this.references = sequence.isOrdered() ? Collections
			        .synchronizedSortedSet(new TreeSet<IDeferredReference>()) : Collections
			        .synchronizedList(new LinkedList<IDeferredReference>());
		}

		public int compareTo(Stage another) {
			return sequence.compareTo(another.sequence);
		}

		@Override
		public boolean equals(Object another) {
			return compareTo((Stage) another) == 0;
		}

		@Override
		public int hashCode() {
			return sequence.hashCode();
		}

		@Override
		public String toString() {
			return sequence.name() + "(" + references.size() + ")";
		}

		public IDeferredReference nextReference() {
			if (references.isEmpty())
				return null;
			Iterator<IDeferredReference> iterator = references.iterator();
			IDeferredReference next = iterator.next();
			iterator.remove();
			return next;
		}

		public Step getSequence() {
			return sequence;
		}
	}

	private SortedSet<Stage> stages = Collections.synchronizedSortedSet(new TreeSet<Stage>());
	private IBasicRepository repository;

	@Override
	public void add(IDeferredReference ref, Step step) {
		if (repository != null && step.compareTo(firstStage().getSequence()) < 0) {
			// resolve right away
			ref.resolve(this.repository);
			return;
		}
		Stage stage = getStage(step);
		stage.references.add(ref);
	}

	private Stage getStage(Step step) {
		for (Stage stage : stages)
			if (stage.sequence.compareTo(step) == 0)
				return stage;
		Stage newStage = new Stage(step);
		stages.add(newStage);
		return newStage;
	}

	@Override
	public void resolve(IBasicRepository repository, IProblemTracker problemTracker) {
		if (this.repository != null)
			throw new IllegalStateException("Already resolving");
		this.repository = repository;
		try {
			while (!stages.isEmpty()) {
				Stage currentStage = firstStage();
				for (IDeferredReference ref; (ref = currentStage.nextReference()) != null;) {
					try {
						ref.resolve(repository);
					} catch (AbortedStatementCompilationException e) {
						// continue with next symbol
					} catch (AbortedCompilationException e) {
						throw e;
					} catch (RuntimeException e) {
						final InternalProblem toReport = new InternalProblem(e);
						problemTracker.add(toReport);
						LogUtils.logError(MDDCore.PLUGIN_ID, "Unexpected exception while compiling", e);
					}
					// if a new reference has been added by the current
					// reference to a previous stage,
					// abort, as we need to resolve it first
					if (firstStage().compareTo(currentStage) < 0)
						break;
				}
				// once we are done with the current stage, remove it from the
				// list (but might pop-up back later)
				if (currentStage.references.isEmpty())
					stages.remove(currentStage);
			}
		} finally {
			this.repository = null;
		}
	}

	private Stage firstStage() {
		return stages.isEmpty() ? null : stages.first();
	}
}