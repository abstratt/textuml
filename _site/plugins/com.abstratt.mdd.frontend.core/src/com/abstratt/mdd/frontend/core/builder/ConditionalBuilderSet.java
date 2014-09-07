package com.abstratt.mdd.frontend.core.builder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ConditionalBuilderSet {

	private LinkedHashMap<BuildCondition, ElementBuilder<?>> conditionedBuilders = new LinkedHashMap<BuildCondition, ElementBuilder<?>>();
	private ElementBuilder<?> chosenBuilder;
	private boolean choiceMade;

	public void addOption(NameReference reference, ElementBuilder<?> builder) {
		addOption(new ReferenceCondition(reference), builder);
	}
	
	public void addOption(BuildCondition condition, ElementBuilder<?> builder) {
		if (choiceMade)
			throw new IllegalStateException();
		builder.setConditionalSet(this);
		conditionedBuilders.put(condition, builder);
	}

	
	public ElementBuilder<?> getChosenBuilder() {
		if (choiceMade)
			return chosenBuilder;
		choiceMade = true;
		for (Entry<BuildCondition, ElementBuilder<?>> entry : conditionedBuilders.entrySet()) {
			BuildCondition condition = entry.getKey();
			ElementBuilder<?> builder = entry.getValue();
			if (condition.isSatisfied(builder.getContext()))
				return chosenBuilder = builder;
		}
		// none satisfied, let first builder fail
		Entry<BuildCondition, ElementBuilder<?>> first = conditionedBuilders.entrySet().iterator().next();
		return chosenBuilder = first.getValue();
	}

	public boolean isChoiceMade() {
		return choiceMade;
	}

	public Map<BuildCondition, ElementBuilder<?>> getOptions() {
		return conditionedBuilders;
	}

}
