package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.AddVariableValueAction;
import org.eclipse.uml2.uml.Clause;
import org.eclipse.uml2.uml.ConditionalNode;
import org.eclipse.uml2.uml.WriteStructuralFeatureAction;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.frontend.core.TypeMismatch;

public class ExpressionTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(ExpressionTests.class);
    }

    public ExpressionTests(String name) {
        super(name);
    }
    
    static String structure = "";

    static {
        structure += "model simple;\n";
        structure += "import base;\n";
        structure += "  class SimpleClass\n";
        structure += "    operation foo();\n";
        structure += "  end;\n";
        structure += "end.";
    }

    public void testIntegerBinaryOperands() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Integer, y : Integer;";
        source += "y := x + 1;";
        source += "y := x - 1;";
        source += "y := x * 2;";
        source += "y := x / 2;";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }
    
    public void testStringConcatenation() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : String;\n";
        source += "x := \"a\" + 1;\n";
        source += "x := (\"a\" + 1) + \"b\";\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }

    public void testNotNull() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var x : Boolean, y : String;\n";
        source += "x := ?y;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
    }
    
    public void testElvis() throws CoreException {
        String source;
        source = "model simple;\n";
        source += "operation SimpleClass.foo;\n";
        source += "begin\n";
        source += "var optional1 : SimpleClass[0,1], required1 : SimpleClass;";
        source += "required1 := optional1 ?: required1;";
        source += "end;\n";
        source += "end.";
        parseAndCheck(structure, source);
        Activity activity = getActivity("simple::SimpleClass::foo");
        List<Action> statements = ActivityUtils.findStatements(activity);
        assertEquals(1, statements.size());
        AddVariableValueAction writeRequired1 = (AddVariableValueAction) statements.get(0);
        ConditionalNode elvis = (ConditionalNode) ActivityUtils.getSourceAction(writeRequired1);
        EList<Clause> clauses = elvis.getClauses();
        assertEquals(2, clauses.size());
        assertEquals(1, elvis.getResults().size());
        assertSame(getClass("simple::SimpleClass"), elvis.getResults().get(0).getType());
        assertEquals(1, elvis.getResults().get(0).getLower());
    }
    
	
	public void testTernary_invalidConditionType() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "operation SimpleClass.foo;\n";
		source += "begin\n";
		source += "  var result : Integer;\n";
		source += "  result := (2+3) ? 1 : 0;\n";
		source += "end;\n";
		source += "end.";
		IProblem[] result = parse(structure, source);
		TypeMismatch error = assertExpectedProblem(TypeMismatch.class, result);
        assertEquals(Severity.ERROR, error.getSeverity());
        assertEquals(Integer.valueOf(5), result[0].getAttribute(IProblem.LINE_NUMBER));
	}
	
	public void testTernary() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "operation SimpleClass.foo;\n";
		source += "begin\n";
		source += "  var result : Integer;\n";
		source += "  result := true ? 1 : 0;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(structure, source);
	}
    
    protected Properties createDefaultSettings() {
        Properties creationSettings = super.createDefaultSettings();
        creationSettings.setProperty(IRepository.EXTEND_BASE_OBJECT, Boolean.TRUE.toString());
        return creationSettings;
    }
}
