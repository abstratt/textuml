package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

public class StateMachineUtils {
	
    public static BehavioredClassifier getStateMachineContext(StateMachine stateMachine) {
        return stateMachine.getContext();
    }
    
	public static Vertex getVertex(StateMachine stateMachine, String name) {
		return stateMachine.getRegions().get(0).getSubvertex(name);
	}

	public static Vertex getInitialVertex(StateMachine stateMachine) {
		EList<Vertex> subvertices = stateMachine.getRegions().get(0).getSubvertices();
		for (Vertex vertex : subvertices)
			if (isMarkedInitial(vertex))
				return vertex;
		// fall back to first orphan vertex 
		for (Vertex vertex : subvertices)
			if (vertex.getIncomings().isEmpty())
				return vertex;
		return null;
	}
	
	public static boolean isMarkedInitial(Vertex vertex) {
	    return vertex instanceof Pseudostate && ((Pseudostate) vertex).getKind() == PseudostateKind.INITIAL_LITERAL;
	}
	
	public static boolean isInitial(Vertex vertex) {
        return isMarkedInitial(vertex) || getInitialVertex(vertex.containingStateMachine()) == vertex;
    }

	public static List<Property> findStateProperties(Classifier classifier) {
		List<Property> stateProperties = new LinkedList<Property>();
		for (Property property : classifier.getAllAttributes())
			if (property.getType() instanceof StateMachine)
				stateProperties.add(property);
		return stateProperties;
	}
	
	public static Map<Event, List<Trigger>> findTriggersPerEvent(StateMachine stateMachine) {
	    Map<Event, List<Trigger>> triggersPerEvent = new LinkedHashMap<Event, List<Trigger>>();
        for (Vertex state : stateMachine.getRegions().get(0).getSubvertices())
            collectTriggersPerEvent(state, triggersPerEvent);
        return triggersPerEvent;
    }
	
	public static Collection<Trigger> findTriggersForCalling(StateMachine stateMachine, Operation operation) {
	    Collection<Trigger> result = new ArrayList<Trigger>();
        for (Vertex state : stateMachine.getRegions().get(0).getSubvertices())
            for (Transition transition : state.getOutgoings())
                for (Trigger trigger : transition.getTriggers()) {
                    Event event = trigger.getEvent();
                    if (event instanceof CallEvent && ((CallEvent) event).getOperation() == operation)
                        result.add(trigger);
                }
        return result;
	}
	
	public static Map<Event, List<Trigger>> findTriggersPerEvent(Vertex state) {
        Map<Event, List<Trigger>> triggersPerEvent = new LinkedHashMap<Event, List<Trigger>>();
        collectTriggersPerEvent(state, triggersPerEvent);
        return triggersPerEvent;
    }

    private static void collectTriggersPerEvent(Vertex state, Map<Event, List<Trigger>> triggersPerEvent) {
        for (Transition transition : state.getOutgoings())
            for (Trigger trigger : transition.getTriggers()) {
                Event keyEvent = null;
                for (Event existingEvent : triggersPerEvent.keySet()) 
                    if (EcoreUtil.equals(existingEvent, trigger.getEvent())) {
                        keyEvent = existingEvent;
                        break;
                    }
                if (keyEvent == null)
                    keyEvent = trigger.getEvent();
                List<Trigger> triggers = triggersPerEvent.get(keyEvent);
                if (triggers == null)
                    triggersPerEvent.put(keyEvent, triggers = new ArrayList<Trigger>());
                triggers.add(trigger);
            }
    }

	public static Map<Operation, List<Vertex>> findStateSpecificOperations(BehavioredClassifier classifier) {
		Map<Operation, List<Vertex>> result = new LinkedHashMap<Operation, List<Vertex>>();
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
