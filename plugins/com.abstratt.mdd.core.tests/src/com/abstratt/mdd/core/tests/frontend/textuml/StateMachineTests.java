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
import com.abstratt.mdd.frontend.core.NonInitialStatesMustBeNamed;
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
        Vertex state2 = StateMachineUtils.getVertex(statusSM, "State2");
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
        source += "      initial state end;\n";
        source += "      state Second end;\n";
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
        assertEquals(1, statusSM.getRegions().size());
        List<Vertex> vertices = statusSM.getRegions().get(0).getSubvertices();
        assertNull(vertices.get(0).getName());
        assertEquals("Second", vertices.get(1).getName());
    }

    public void testUnnamedNonInitialState() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    statemachine Status\n";
        source += "      state end;\n";
        source += "    end;\n";
        source += "  end;\n";
        source += "end.";
        IProblem[] problems = parse(source);
        assertEquals(Arrays.asList(problems).toString(), 2, problems.length);
        assertTrue(problems[0].toString(), problems[0] instanceof StateMachineMustHaveOneInitialState);
        assertTrue(problems[1].toString(), problems[1] instanceof NonInitialStatesMustBeNamed);
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
        List<Vertex> vertices = sm1.getRegions().get(0).getSubvertices();
        assertEquals(4, vertices.size());

        assertEquals(1, vertices.get(0).getOutgoings().size());
        assertEquals(vertices.get(1), vertices.get(0).getOutgoings().get(0).getTarget());
        assertTrue(vertices.get(0) instanceof Pseudostate);
        assertTrue(((Pseudostate) vertices.get(0)).getKind() == PseudostateKind.INITIAL_LITERAL);

        assertEquals(3, vertices.get(1).getOutgoings().size());
        assertEquals(vertices.get(1), vertices.get(1).getOutgoings().get(0).getTarget());
        assertEquals(vertices.get(2), vertices.get(1).getOutgoings().get(2).getTarget());
        assertEquals(vertices.get(3), vertices.get(1).getOutgoings().get(1).getTarget());
        assertTrue(vertices.get(1) instanceof State);

        EList<Trigger> transition1Triggers = vertices.get(1).getOutgoings().get(0).getTriggers();
        assertEquals(1, transition1Triggers.size());
        assertTrue(transition1Triggers.get(0).getEvent() instanceof CallEvent);
        assertSame(op1, ((CallEvent) transition1Triggers.get(0).getEvent()).getOperation());

        EList<Trigger> transition2Triggers = vertices.get(1).getOutgoings().get(1).getTriggers();
        assertEquals(2, transition2Triggers.size());
        assertTrue(transition2Triggers.get(0).getEvent() instanceof CallEvent);
        assertSame(op1, ((CallEvent) transition2Triggers.get(0).getEvent()).getOperation());
        assertTrue(transition2Triggers.get(1).getEvent() instanceof CallEvent);
        assertSame(op2, ((CallEvent) transition2Triggers.get(1).getEvent()).getOperation());

        EList<Trigger> transition3Triggers = vertices.get(1).getOutgoings().get(2).getTriggers();
        assertTrue(transition3Triggers.get(0).getEvent() instanceof CallEvent);
        assertSame(op3, ((CallEvent) transition3Triggers.get(0).getEvent()).getOperation());

        assertEquals(1, vertices.get(2).getOutgoings().size());
        assertEquals(vertices.get(3), vertices.get(2).getOutgoings().get(0).getTarget());
        assertTrue(vertices.get(2) instanceof State);

        assertEquals(0, vertices.get(3).getOutgoings().size());
        assertTrue(vertices.get(3) instanceof Pseudostate);
        assertTrue(((Pseudostate) vertices.get(3)).getKind() == PseudostateKind.TERMINATE_LITERAL);
    }

    public void testStateMachineBehavior() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "import base;\n";
        source += "  class SimpleClass\n";
        source += "    statemachine Status\n";
        source += "      initial state\n";
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
        assertEquals(1, sm1.getRegions().size());
        List<Vertex> vertices = sm1.getRegions().get(0).getSubvertices();
        assertEquals(2, vertices.size());
        State s1 = (State) sm1.getRegions().get(0).getSubvertex("S1");
        assertNotNull(s1.getEntry());
        assertSame(c1, ActivityUtils.getContext(s1.getEntry()));
        assertNotNull(s1.getDoActivity());
        assertSame(c1, ActivityUtils.getContext(s1.getDoActivity()));
        assertNotNull(s1.getExit());
        assertSame(c1, ActivityUtils.getContext(s1.getExit()));
        assertEquals(1, s1.getOutgoings().size());
        Transition t = s1.getOutgoings().get(0);
        assertNotNull(t.getGuard());
        assertNotNull(t.getEffect());
        assertSame(c1, ActivityUtils.getContext(t.getEffect()));
    }

}
