package com.abstratt.mdd.core.tests.frontend.textuml;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecificationAction;
import org.eclipse.uml2.uml.Vertex;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.StateMachineUtils;
import com.abstratt.mdd.frontend.core.StatesMustBeNamed;
import com.abstratt.mdd.frontend.core.StateMachineMustHaveOneInitialState;

public class StateMachineTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(StateMachineTests.class);
    }

    public StateMachineTests(String name) {
        super(name);
    }

    public void testStateLiteral() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    attribute status : Status;\n";
        source += "    statemachine Status\n";
        source += "      initial state State1 end;\n";
        source += "      state State2 end;\n";
        source += "    end;\n";
        source += "    operation isInState() : Boolean;\n";
        source += "    begin\n";
        source += "        return self.status == Status#State2;\n";
        source += "    end;\n";
        source += "  end;\n";
        source += "end.";
        parseAndCheck(source);
        StateMachine statusSM = (StateMachine) getRepository().findNamedElement("simple::SimpleClass::Status",
                UMLPackage.Literals.STATE_MACHINE, null);
        Vertex state2 = StateMachineUtils.getState(statusSM, "State2");
        Operation statusCheckingOperation = (Operation) getRepository().findNamedElement(
                "simple::SimpleClass::isInState", UMLPackage.Literals.OPERATION, null);
        assertNotNull(statusCheckingOperation);
        StructuredActivityNode rootAction = ActivityUtils.getRootAction(statusCheckingOperation);
        ValueSpecificationAction expressionAction = (ValueSpecificationAction) ActivityUtils.findNode(rootAction,
                new MDDUtil.EClassMatcher(Literals.VALUE_SPECIFICATION_ACTION));
        assertTrue(MDDExtensionUtils.isVertexLiteral(expressionAction.getValue()));
        assertEquals(state2.getName(), MDDExtensionUtils.resolveVertexLiteral(expressionAction.getValue()).getName());
        assertEquals(statusSM, expressionAction.getValue().getType());
    }

    public void testNaming() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    attribute status : Status;\n";
        source += "    statemachine Status\n";
        source += "      initial state First end;\n";
        source += "      state Second end;\n";
        source += "      state Third end;\n";
        source += "      terminate state Last end;\n";
        source += "    end;\n";
        source += "  end;\n";
        source += "end.";
        parseAndCheck(source);
        StateMachine statusSM = (StateMachine) getRepository().findNamedElement("simple::SimpleClass::Status",
                UMLPackage.Literals.STATE_MACHINE, null);
        List<State> vertices = StateMachineUtils.getStates(statusSM);
        assertEquals("First", vertices.get(0).getName());
        assertEquals("Second", vertices.get(1).getName());
        assertEquals("Third", vertices.get(2).getName());
        assertEquals("Last", vertices.get(3).getName());
    }
    
    public void testStateAttribute() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    attribute status : Status;\n";
        source += "    statemachine Status\n";
        source += "      initial state First end;\n";
        source += "    end;\n";
        source += "  end;\n";
        source += "end.";
        parseAndCheck(source);
        StateMachine statusSM = (StateMachine) getRepository().findNamedElement("simple::SimpleClass::Status",
                UMLPackage.Literals.STATE_MACHINE, null);
        Property statusProperty = (Property) getRepository().findNamedElement("simple::SimpleClass::status",
                UMLPackage.Literals.PROPERTY, null);
        assertNotNull(statusProperty);
        assertEquals(statusSM, statusProperty.getType());
    }
    
    public void testUnnamedState() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    statemachine Status\n";
        source += "      state end;\n";
        source += "    end;\n";
        source += "  end;\n";
        source += "end.";
        List<IProblem> problems = Arrays.asList(parse(source));
        assertTrue(problems.toString(), problems.stream().anyMatch(p -> p instanceof StatesMustBeNamed));
    }

    public void testStateMachineAsOperationArgument() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    statemachine Status\n";
        source += "      initial state State1 end;\n";
        source += "    end;\n";
        source += "    operation op1();\n";
        source += "    begin\n";
        source += "        self.op2(Status#State1);\n";
        source += "    end;\n";
        source += "    operation op2(s : Status);\n";
        source += "    begin\n";
        source += "    end;\n";
        source += "  end;\n";
        source += "end.";
        parseAndCheck(source);
    }

    public void testParseTransitions() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    operation op1();\n";
        source += "    operation op2();\n";
        source += "    operation op3();\n";
        source += "    statemachine Status\n";
        source += "      initial state First\n";
        source += "          transition to Second;\n";
        source += "      end;\n";
        source += "      state Second\n";
        source += "          transition on call(op1) to Second;\n";
        source += "          transition on call(op1),call(op2) to Final;\n";
        source += "          transition on call(op3) to Third;\n";
        source += "      end;\n";
        source += "      state Third\n";
        source += "          transition to Final;\n";
        source += "      end;\n";
        source += "      terminate state Final\n";
        source += "      end;\n";
        source += "    end;\n";
        source += "  end;\n";
        source += "end.";
        parseAndCheck(source);
        StateMachine sm1 = (StateMachine) getRepository().findNamedElement("simple::SimpleClass::Status",
                UMLPackage.Literals.STATE_MACHINE, null);
        Operation op1 = getRepository().findNamedElement("simple::SimpleClass::op1", UMLPackage.Literals.OPERATION,
                null);
        Operation op2 = getRepository().findNamedElement("simple::SimpleClass::op2", UMLPackage.Literals.OPERATION,
                null);
        Operation op3 = getRepository().findNamedElement("simple::SimpleClass::op3", UMLPackage.Literals.OPERATION,
                null);

        assertEquals(1, sm1.getRegions().size());
        List<State> states = StateMachineUtils.getStates(sm1);
        assertEquals(4, states.size());

        assertEquals(1, states.get(0).getOutgoings().size());
        assertEquals(states.get(1), states.get(0).getOutgoings().get(0).getTarget());
        assertTrue(StateMachineUtils.isMarkedInitial(states.get(0)));

        assertEquals(3, states.get(1).getOutgoings().size());
        assertEquals(states.get(1), states.get(1).getOutgoings().get(0).getTarget());
        assertEquals(states.get(2), states.get(1).getOutgoings().get(2).getTarget());
        assertEquals(states.get(3), states.get(1).getOutgoings().get(1).getTarget());

        EList<Trigger> transition1Triggers = states.get(1).getOutgoings().get(0).getTriggers();
        assertEquals(1, transition1Triggers.size());
        assertTrue(transition1Triggers.get(0).getEvent() instanceof CallEvent);
        assertSame(op1, ((CallEvent) transition1Triggers.get(0).getEvent()).getOperation());

        EList<Trigger> transition2Triggers = states.get(1).getOutgoings().get(1).getTriggers();
        assertEquals(2, transition2Triggers.size());
        assertTrue(transition2Triggers.get(0).getEvent() instanceof CallEvent);
        assertSame(op1, ((CallEvent) transition2Triggers.get(0).getEvent()).getOperation());
        assertTrue(transition2Triggers.get(1).getEvent() instanceof CallEvent);
        assertSame(op2, ((CallEvent) transition2Triggers.get(1).getEvent()).getOperation());

        EList<Trigger> transition3Triggers = states.get(1).getOutgoings().get(2).getTriggers();
        assertTrue(transition3Triggers.get(0).getEvent() instanceof CallEvent);
        assertSame(op3, ((CallEvent) transition3Triggers.get(0).getEvent()).getOperation());

        assertEquals(1, states.get(2).getOutgoings().size());
        assertEquals(states.get(3), states.get(2).getOutgoings().get(0).getTarget());
        assertTrue(states.get(2) instanceof State);

        assertEquals(1, states.get(3).getOutgoings().size());
        assertTrue(StateMachineUtils.isMarkedTerminate(states.get(3)));
    }

    public void testStateMachineBehavior() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    statemachine Status\n";
        source += "      initial state S0\n";
        source += "          transition on any to S1;\n";
        source += "      end;\n";
        source += "      state S1\n";
        source += "          entry {\n";
        source += "          };\n";
        source += "          do {\n";
        source += "          };\n";
        source += "          exit {\n";
        source += "          };\n";
        source += "          transition on any to S1 when { true } do { };\n";
        source += "      end;\n";
        source += "    end;\n";
        source += "  end;\n";
        source += "end.";
        parseAndCheck(source);
        org.eclipse.uml2.uml.Class c1 = getClass("simple::SimpleClass");
        StateMachine sm1 = get("simple::SimpleClass::Status", UMLPackage.Literals.STATE_MACHINE);
        List<State> vertices = StateMachineUtils.getStates(sm1);
        assertEquals(2, vertices.size());
        State s1 = StateMachineUtils.getState(sm1, "S1");
        assertNotNull(s1.getEntry());
        assertSame(c1, ActivityUtils.getBehaviorContext(s1.getEntry()));
        assertNotNull(s1.getDoActivity());
        assertSame(c1, ActivityUtils.getBehaviorContext(s1.getDoActivity()));
        assertNotNull(s1.getExit());
        assertSame(c1, ActivityUtils.getBehaviorContext(s1.getExit()));
        assertEquals(1, s1.getOutgoings().size());
        Transition t = s1.getOutgoings().get(0);
        assertNotNull(t.getGuard());
        assertNotNull(t.getEffect());
        assertSame(c1, ActivityUtils.getBehaviorContext(t.getEffect()));
    }

}
