package com.abstratt.mdd.frontend.core.spi;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.Step;

public interface IReferenceTracker {
	
	interface IStepListener {
		default void before(Step step) {}
		default void after(Step step) {}
	}
	
    public default void defer(Step step, IDeferredReference ref) {
        add(ref, step);
    }

    public void add(IDeferredReference ref, Step step);

    /**
     * Resolves all deferred references against the given repository in step
     * order, collecting resolution errors in the given problem tracker.
     * 
     * @param repository
     * @param problemTracker
     * @param stepListener optional
     */
    public void resolve(IBasicRepository repository, IProblemTracker problemTracker, IStepListener stepListener);
}
