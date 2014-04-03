package com.abstratt.mdd.core.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

public class StateMachineUtils {
	
	public static Vertex getVertex(StateMachine stateMachine, String name) {
		return stateMachine.getRegions().get(0).getSubvertex(name);
	}

	public static Vertex getInitialVertex(StateMachine stateMachine) {
		EList<Vertex> subvertices = stateMachine.getRegions().get(0).getSubvertices();
		for (Vertex vertex : subvertices)
			if (vertex instanceof Pseudostate && ((Pseudostate) vertex).getKind() == PseudostateKind.INITIAL_LITERAL)
				return vertex;
		for (Vertex vertex : subvertices)
			if (vertex.getIncomings().isEmpty())
				return vertex;
		return null;
	}

	public static List<Property> findStateProperties(Classifier classifier) {
		List<Property> stateProperties = new LinkedList<Property>();
		for (Property property : classifier.getAllAttributes())
			if (property.getType() instanceof StateMachine)
				stateProperties.add(property);
		return stateProperties;
	}

	public static Map<Operation, List<Vertex>> findStateSpecificOperations(BehavioredClassifier classifier) {
		Map<Operation, List<Vertex>> result = new HashMap<Operation, List<Vertex>>();
		EList<Behavior> behaviors = classifier.getOwnedBehaviors();
		for (Behavior behavior : behaviors)
			if (behavior instanceof StateMachine)
				for (Vertex state : ((StateMachine) behavior).getRegions().get(0).getSubvertices())
					for (Transition transition : state.getOutgoings())
						for (Trigger trigger : transition.getTriggers()) 
							if (trigger.getEvent() instanceof CallEvent) {
								Operation triggerOperation = ((CallEvent) trigger.getEvent()).getOperation();
								List<Vertex> supportedStates = result.get(triggerOperation);
								if (supportedStates == null)
									result.put(triggerOperation, supportedStates = new LinkedList<Vertex>());
								supportedStates.add(state);
							}
		return result;
	}

	public static List<Vertex> getVertices(StateMachine umlEnum) {
		return umlEnum.getRegions().get(0).getSubvertices();
	}
}
