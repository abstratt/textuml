package com.abstratt.mdd.core.tests.frontend.builder;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.frontend.core.BasicProblemTracker;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.builder.ClassifierBuilder;
import com.abstratt.mdd.frontend.core.builder.ConditionalBuilderSet;
import com.abstratt.mdd.frontend.core.builder.NameReference;
import com.abstratt.mdd.frontend.core.builder.NamedElementBuilder;
import com.abstratt.mdd.frontend.core.builder.PackageBuilder;
import com.abstratt.mdd.frontend.core.builder.PropertyBuilder;
import com.abstratt.mdd.frontend.core.builder.UML2BuilderFactory;
import com.abstratt.mdd.frontend.core.builder.UML2ModelBuildDriver;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;

public class ConditionalBuilderSetTests extends AbstractElementBuilderTests {

    class ResilientClassifierBuilder extends ClassifierBuilder {

        public ResilientClassifierBuilder() {
            super(UML2ProductKind.CLASS);
        }

        @Override
        protected void buildChildren() {
            try {
                super.buildChildren();
            } catch (AbortedStatementCompilationException e) {
                // already caught
            }
        }
    }

    public ConditionalBuilderSetTests(String name) {
        super(name);
    }

    /**
     * 1 - both conditions valid, first one should be built 2 - the second
     * condition only is valid, second one should be built 3 - no condition is
     * valid, should result in a build error
     */
    public void testConditionalBuilder() {
        // first only true
        testConditionalBuilder("myClass1", "myClass2", "myClass1", "myClass2a", 1);
        // both true
        testConditionalBuilder("myClass1", "myClass2", "myClass1", "myClass2", 1);
        // both true, referring to same object
        testConditionalBuilder("myClass1", "myClass2", "myClass1", "myClass1", 1);
        // second true
        testConditionalBuilder("myClass1", "myClass2", "myClass1a", "myClass2", 2);
        // none true
        testConditionalBuilder("myClass1", "myClass2", "myClass1a", "myClass2a", 0);
    }

    /**
     * Exercises a conditional builder scenario.
     * 
     * @param actualTargetName1
     * @param actualTargetName2
     * @param nameReference1
     * @param nameReference2
     * @param expectedOutcome
     *            1 for the first builder to be chosen, 2 for the second
     *            builder, 0 for none
     */
    public void testConditionalBuilder(String actualTargetName1, String actualTargetName2, String nameReference1,
            String nameReference2, int expectedOutcome) {
        PackageBuilder packageBuilder = new UML2BuilderFactory().newBuilder(UML2ProductKind.PACKAGE);
        packageBuilder.name("myPackage");

        // one reference target
        ClassifierBuilder class1Builder = packageBuilder.newClassifier(UML2ProductKind.CLASS);
        class1Builder.name(actualTargetName1);

        // another reference target
        ClassifierBuilder class2Builder = packageBuilder.newClassifier(UML2ProductKind.CLASS);
        class2Builder.name(actualTargetName2);

        // the referrer actually using the conditional builder
        ClassifierBuilder referrerClassBuilder = packageBuilder.addChildBuilder(new ResilientClassifierBuilder());
        referrerClassBuilder.name("ReferrerClass");

        ConditionalBuilderSet conditional = new ConditionalBuilderSet();
        PropertyBuilder property1 = referrerClassBuilder.newChildBuilder(UML2ProductKind.PROPERTY);
        property1.type(nameReference1);
        conditional.addOption(new NameReference(nameReference1, UML2ProductKind.CLASS), property1);
        PropertyBuilder property2 = referrerClassBuilder.newChildBuilder(UML2ProductKind.PROPERTY);
        property2.type(nameReference2);
        conditional.addOption(new NameReference(nameReference2, UML2ProductKind.CLASS), property2);

        new UML2ModelBuildDriver().build(buildContext, packageBuilder);

        assertNotNull(class1Builder.getProduct());
        assertNotNull(class2Builder.getProduct());
        assertNotNull(referrerClassBuilder.getProduct());

        IProblem[] allProblems = buildContext.getProblemTracker().getAllProblems();
        if (expectedOutcome == 0) {
            // first builder picked by default
            assertSame(property1, conditional.getChosenBuilder());
            assertNull(property1.getProduct().getType());
            assertEquals(1, allProblems.length);
            assertTrue(allProblems[0].toString(), allProblems[0] instanceof UnresolvedSymbol);
            // when both fail to resolve, we should report error on the attempt
            // to resolve the first symbol
            assertEquals(nameReference1, ((UnresolvedSymbol) allProblems[0]).getSymbol());
            return;
        }
        NamedElementBuilder<?> expectedChoice = expectedOutcome == 1 ? property1 : property2;
        NamedElementBuilder<?> unexpectedChoice = expectedOutcome == 1 ? property2 : property1;

        FixtureHelper.assertCompilationSuccessful(allProblems);
        assertNotNull(conditional.getChosenBuilder());
        assertNotNull(conditional.getChosenBuilder().getProduct());
        assertSame(expectedChoice, conditional.getChosenBuilder());
        assertNull(unexpectedChoice.getProduct());
        assertSame(expectedChoice.getProduct().getOwner(), referrerClassBuilder.getProduct());
    }

    @Override
    protected IProblemTracker createProblemTracker() {
        return new BasicProblemTracker();
    }

}
