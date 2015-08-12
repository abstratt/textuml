package com.abstratt.mdd.frontend.core.builder.actions;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.ReadSelfAction;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.core.ReadSelfFromStaticContext;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;

public class ReadSelfActionBuilder extends ActionBuilder<ReadSelfAction> {
	public ReadSelfActionBuilder() {
		super(UML2ProductKind.READ_SELF_ACTION);
	}

	@Override
	public void enhanceAction() {
		Activity currentActivity = getContext().getActivityBuilder().getCurrentActivity();
		while (MDDExtensionUtils.isClosure(currentActivity)) {
			ActivityNode rootNode = MDDExtensionUtils.getClosureContext(currentActivity);
			currentActivity = MDDUtil.getNearest(rootNode, Literals.ACTIVITY);
		}
		final BehavioralFeature operation = currentActivity.getSpecification();
		if (operation != null && operation.isStatic()) {
			getContext().getProblemTracker().add(new ReadSelfFromStaticContext());
			throw new AbortedStatementCompilationException();
		}
		getProduct().createResult(null, (Classifier) currentActivity.getNamespace());
	}

	@Override
	protected boolean isProducer() {
		return true;
	}
}
