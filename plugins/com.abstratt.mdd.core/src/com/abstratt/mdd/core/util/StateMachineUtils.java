package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.Vertex;

public class StateMachineUtils {

    public static BehavioredClassifier getStateMachineContext(StateMachine stateMachine) {
        return stateMachine.getContext();
    }

    public static boolean isVertexLiteral(ValueSpecification specification) {
		return MDDExtensionUtils.isVertexLiteral(specification);
	}
	
	public static Vertex resolveVertexLiteral(ValueSpecification specification) {
		return MDDExtensionUtils.resolveVertexLiteral(specification);
	}
	
	public static Vertex createPseudoState(Region region, PseudostateKind kind) {
        Pseudostate pseudoState = (Pseudostate) region.createSubvertex(null,
                UMLPackage.Literals.PSEUDOSTATE);
        pseudoState.setKind(kind);
		return pseudoState;
	}
	
	public static void connectVertices(List<Vertex> sources, List<Vertex> destinations) {
		sources.forEach((source) -> {
			destinations.forEach((destination) -> {
				Transition transition = source.getContainer().createTransition(null);
	            source.getOutgoings().add(transition);
	            transition.setTarget(destination);
			});
		});
	}
	

    public static Vertex getInitialVertex(StateMachine stateMachine) {
        EList<Vertex> subvertices = stateMachine.getRegions().get(0).getSubvertices();
        for (Vertex vertex : subvertices)
            if (isPseudoState(vertex, PseudostateKind.INITIAL_LITERAL)) {
                Optional<Vertex> found = vertex.getOutgoings().stream().findAny().map(t -> t.getTarget());
                if (found.isPresent())
                	return found.get();
            }
        // fall back to first orphan vertex
        for (Vertex vertex : subvertices)
            if (vertex.getIncomings().isEmpty())
                return vertex;
        return null;
    }
    
    public static boolean isPseudoState(Vertex vertex, PseudostateKind kind) {
    	return vertex instanceof Pseudostate && ((Pseudostate) vertex).getKind() == kind;
    }

    public static boolean isMarkedInitial(Vertex vertex) {
    	return vertex.getIncomings().stream().anyMatch(v -> isPseudoState(v.getSource(), PseudostateKind.INITIAL_LITERAL));
    }
    
    public static boolean isMarkedTerminate(Vertex vertex) {
    	return vertex.getOutgoings().size() == 1 && isPseudoState(vertex.getOutgoings().get(0).getTarget(), PseudostateKind.TERMINATE_LITERAL);
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
    
    public static Collection<Vertex> findStatesForCalling(StateMachine stateMachine, Operation operation) {
        Collection<Vertex> result = new LinkedHashSet<>();
        nextState: for (Vertex state : stateMachine.getRegions().get(0).getSubvertices())
            for (Transition transition : state.getOutgoings())
                for (Trigger trigger : transition.getTriggers()) {
                    Event event = trigger.getEvent();
                    if (event instanceof CallEvent && ((CallEvent) event).getOperation() == operation) {
                        result.add(state);
                        continue nextState;
                    }
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
                for (Vertex state : getStates((StateMachine) behavior))
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

	public static List<State> getStates(StateMachine stateMachine) {
		return streamStates(stateMachine).collect(Collectors.toList());
	}

	public static State getState(StateMachine stateMachine, String stateName) {
		return streamStates(stateMachine)
				.filter(v -> stateName.equals(v.getName()))
				.findAny()
				.orElse(null);
	}

	private static Stream<State> streamStates(StateMachine stateMachine) {
		return stateMachine.getRegions().stream()
				.flatMap(r -> r.getSubvertices().stream())
				.filter(v -> v instanceof State)
				.map(v -> (State) v);
	}
}
