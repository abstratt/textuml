package com.abstratt.mdd.core.tests.frontend.builder;


import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.StrictProblemTracker;
import com.abstratt.mdd.frontend.core.FrontEnd;
import com.abstratt.mdd.frontend.core.builder.UML2BuildContext;
import com.abstratt.mdd.frontend.core.spi.IActivityBuilder;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.internal.core.ReferenceTracker;

public abstract class AbstractElementBuilderTests extends AbstractRepositoryBuildingTests {
	protected UML2BuildContext buildContext;

	public AbstractElementBuilderTests(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		buildContext = createBuildContext();
	}

	protected UML2BuildContext createBuildContext() {
		return new UML2BuildContext(getRepository(), createProblemTracker(), createReferenceTracker(), createActivityBuilder());
	}

	protected IActivityBuilder createActivityBuilder() {
		return FrontEnd.newActivityBuilder(getRepository());
	}

	protected IReferenceTracker createReferenceTracker() {
		return new ReferenceTracker();
	}

	protected IProblemTracker createProblemTracker() {
		return new StrictProblemTracker();
	}
}
