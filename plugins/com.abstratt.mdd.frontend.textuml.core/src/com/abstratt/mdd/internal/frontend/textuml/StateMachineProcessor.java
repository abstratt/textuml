package com.abstratt.mdd.internal.frontend.textuml;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.AnyReceiveEvent;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Vertex;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.frontend.core.NonInitialStatesMustBeNamed;
import com.abstratt.mdd.frontend.core.QueryOperationsMustBeSideEffectFree;
import com.abstratt.mdd.frontend.core.StateMachineMustHaveOneInitialState;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.DeferredReference;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker.Step;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAnyTransitionTrigger;
import com.abstratt.mdd.frontend.textuml.grammar.node.ABehaviorStateBehaviorDefinition;
import com.abstratt.mdd.frontend.textuml.grammar.node.ACallTransitionTrigger;
import com.abstratt.mdd.frontend.textuml.grammar.node.AEntryStateBehaviorModifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.AExitStateBehaviorModifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASignalTransitionTrigger;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStateBehavior;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStateDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStateMachineDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATransitionDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PStateBehavior;
import com.abstratt.mdd.frontend.textuml.grammar.node.PStateBehaviorModifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.PStateDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.PTransitionDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.PTransitionTrigger;

public class StateMachineProcessor extends AbstractProcessor<AStateMachineDecl, BehavioredClassifier> implements ProducingNodeProcessor<StateMachine, AStateMachineDecl> {

    private class TriggerProcessor<T extends PTransitionTrigger> implements NodeProcessor<T> {
        private Transition transition;
        private EClass sourceClass;
        private EClass eventClass;

        public TriggerProcessor(Transition transition, EClass sourceClass, EClass eventClass) {
            this.transition = transition;
            this.sourceClass = sourceClass;
            this.eventClass = eventClass;
        }

        @Override
        public void process(final T node) {
            Trigger trigger = transition.createTrigger(null);
            final Event event = (Event) namespaceTracker.currentPackage().createPackagedElement(null, eventClass);
            trigger.setEvent(event);

            // process triggers asynchronously as references may not be
            // resolvable yet
            referenceTracker.add(new IDeferredReference() {
                public void resolve(IBasicRepository repository) {
                    NamedElement source = null;
                    if (sourceClass != null) {
                        String sourceName = sourceMiner.getIdentifier(node);
                        source = repository.findNamedElement(sourceName, sourceClass, namespace);
                        if (source == null) {
                            problemBuilder.addProblem(new UnresolvedSymbol(sourceName), node);
                            return;
                        }
                    }

                    if (event instanceof CallEvent) {
                        Operation sourceOperation = (Operation) source;
                        QueryOperationsMustBeSideEffectFree.ensure(!sourceOperation.isQuery(), problemBuilder, node);
                        ((CallEvent) event).setOperation(sourceOperation);
                    } else if (event instanceof SignalEvent)
                        ((SignalEvent) event).setSignal((Signal) source);
                    else if (event instanceof AnyReceiveEvent)
                        ;
                    else
                        Assert.isTrue(false);
                }
            }, Step.GENERAL_RESOLUTION);
        }
    }

    private class StateProcessor implements NodeProcessor<AStateDecl> {
        @Override
        public void process(AStateDecl node) {
            String stateName = sourceMiner.getText(node.getIdentifier());
            Region region = (Region) namespaceTracker.currentNamespace(null);

            if (stateName != null && region.getSubvertex(stateName) != null) {
                problemBuilder.addProblem(new UnclassifiedProblem("A state with this name already exists"),
                        node.getState());
                return;
            }

            Vertex vertex;
            State asState = null;

            ModifierProcessor modifierProcessor = new ModifierProcessor((SCCTextUMLSourceMiner) sourceMiner);
            modifierProcessor.process(node.getStateModifierList());
            Set<Modifier> modifiers = modifierProcessor.getModifiers(true);

            boolean isInitial = modifiers.contains(Modifier.INITIAL);
            boolean isTerminate = modifiers.contains(Modifier.TERMINATE);
            if (isInitial && isTerminate) {
                problemBuilder.addProblem(new UnclassifiedProblem("Can't be both initial and terminate"),
                        node.getState());
                return;
            }
            if (isInitial || isTerminate) {
                Pseudostate pseudoState = (Pseudostate) region.createSubvertex(stateName,
                        UMLPackage.Literals.PSEUDOSTATE);
                pseudoState.setKind(isInitial ? PseudostateKind.INITIAL_LITERAL : PseudostateKind.TERMINATE_LITERAL);
                vertex = pseudoState;
            } else {
                vertex = asState = (State) region.createSubvertex(stateName, UMLPackage.Literals.STATE);
            }

            if (stateName == null && !isInitial) {
                problemBuilder.addProblem(new NonInitialStatesMustBeNamed(), node.getState());
                return;
            }

            if (!node.getStateBehavior().isEmpty()) {
                problemBuilder.ensure(asState != null, "Pseudo states can't have behavior", node.getState());
                for (PStateBehavior pStateBehavior : node.getStateBehavior()) {
                    final AStateBehavior aStateBehavior = (AStateBehavior) pStateBehavior;
                    problemBuilder.ensure(
                            aStateBehavior.getStateBehaviorDefinition() instanceof ABehaviorStateBehaviorDefinition,
                            "Only inline behavior definition supported at this time", pStateBehavior);
                    PStateBehaviorModifier modifier = aStateBehavior.getStateBehaviorModifier();
                    Activity activity = (Activity) EcoreUtil.create(UMLPackage.Literals.ACTIVITY);
                    if (modifier instanceof AEntryStateBehaviorModifier) {
                        problemBuilder.ensure(asState.getEntry() == null, "Only one entry activity allowed",
                                pStateBehavior);
                        asState.setEntry(activity);
                    } else if (modifier instanceof AExitStateBehaviorModifier) {
                        problemBuilder.ensure(asState.getExit() == null, "Only one exit activity allowed",
                                pStateBehavior);
                        asState.setExit(activity);
                    } else {
                        problemBuilder.ensure(asState.getDoActivity() == null, "Only one do activity allowed",
                                pStateBehavior);
                        asState.setDoActivity(activity);
                    }
                    behaviorGenerator.createBodyLater(aStateBehavior.getStateBehaviorDefinition(), activity);
                }
            }

            if (node.getTransitionDecl() != null)
                for (PTransitionDecl transitionDecl : node.getTransitionDecl())
                    new TransitionProcessor(vertex).process((ATransitionDecl) transitionDecl);
        }
    }

    public class TransitionProcessor implements NodeProcessor<ATransitionDecl> {

        private Vertex source;

        public TransitionProcessor(Vertex vertex) {
            this.source = vertex;
        }

        @Override
        public void process(final ATransitionDecl node) {
            final String destinationName = sourceMiner.getIdentifier(node.getDestination());
            final Transition transition = source.getContainer().createTransition(null);
            source.getOutgoings().add(transition);
            referenceTracker.add(new DeferredReference<Vertex>(destinationName, UMLPackage.Literals.VERTEX,
                    namespaceTracker.currentNamespace(null)) {
                @Override
                protected void onBind(Vertex target) {
                    if (target == null) {
                        problemBuilder.addProblem(new UnresolvedSymbol(destinationName), node.getDestination());
                        return;
                    }
                    transition.setTarget(target);
                }
            }, IReferenceTracker.Step.GENERAL_RESOLUTION);
            if (node.getTransitionTriggers() != null)
                node.getTransitionTriggers().apply(new DepthFirstAdapter() {
                    @Override
                    public void caseACallTransitionTrigger(ACallTransitionTrigger node) {
                        new TriggerProcessor<PTransitionTrigger>(transition, UMLPackage.Literals.OPERATION,
                                UMLPackage.Literals.CALL_EVENT).process(node);
                    }

                    @Override
                    public void caseASignalTransitionTrigger(ASignalTransitionTrigger node) {
                        new TriggerProcessor<PTransitionTrigger>(transition, UMLPackage.Literals.SIGNAL,
                                UMLPackage.Literals.SIGNAL_EVENT).process(node);
                    }

                    @Override
                    public void caseAAnyTransitionTrigger(AAnyTransitionTrigger node) {
                        new TriggerProcessor<PTransitionTrigger>(transition, null,
                                UMLPackage.Literals.ANY_RECEIVE_EVENT).process(node);
                    }
                });
            if (node.getTransitionGuard() != null)
                behaviorGenerator.createConstraintBehaviorLater(source.containingStateMachine(),
                        transition.createGuard(null), node.getTransitionGuard(), Collections.<Parameter> emptyList());
            if (node.getTransitionEffect() != null)
                behaviorGenerator.createBodyLater(node.getTransitionEffect(),
                        (Activity) transition.createEffect(null, UMLPackage.Literals.ACTIVITY));
        }
    }

    private BehaviorGenerator behaviorGenerator;

    public StateMachineProcessor(SourceCompilationContext<Node> compilationContext,
            BehavioredClassifier contextClassifier) {
        super(compilationContext, contextClassifier);
        this.behaviorGenerator = new BehaviorGenerator(compilationContext);
    }

    @Override
    public StateMachine processAndProduce(AStateMachineDecl node) {
        String smName = sourceMiner.getText(node.getIdentifier());
        StateMachine sm = (StateMachine) namespace.createOwnedBehavior(smName, UMLPackage.Literals.STATE_MACHINE);
        Region region = sm.createRegion(smName);
        namespaceTracker.enterNamespace(region);
        try {
            if (node.getStateDecl() != null)
                for (PStateDecl stateDecl : node.getStateDecl())
                    new StateProcessor().process((AStateDecl) stateDecl);
            if (region.getSubvertices().isEmpty()) {
                problemBuilder.addProblem(new UnclassifiedProblem("Can't have a state machine without states"),
                        node.getStatemachine());
                return null;
            }
            int initialCount = 0;
            for (Vertex vertex : region.getSubvertices()) {
                if (vertex instanceof Pseudostate
                        && ((Pseudostate) vertex).getKind() == PseudostateKind.INITIAL_LITERAL)
                    initialCount++;
            }
            if (initialCount == 0) {
                problemBuilder.addProblem(new StateMachineMustHaveOneInitialState(), node.getStatemachine());
                return null;
            }
            if (initialCount > 1) {
                problemBuilder.addProblem(new StateMachineMustHaveOneInitialState(), node.getStatemachine());
                return null;
            }
        } finally {
            namespaceTracker.leaveNamespace();
        }
        return sm;
    }

}
