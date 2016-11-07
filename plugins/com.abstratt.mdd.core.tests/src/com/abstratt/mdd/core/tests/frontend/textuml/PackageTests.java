package com.abstratt.mdd.core.tests.frontend.textuml;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.frontend.core.CannotLoadFromLocation;
import com.abstratt.mdd.frontend.core.InvalidPackageNesting;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;

public class PackageTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(PackageTests.class);
    }

    public PackageTests(String name) {
        super(name);
    }

    public void testApplyProfile() throws CoreException {
        String sourceProfile = "";
        sourceProfile += "profile someProfile;\n";
        sourceProfile += "end.";
        String sourceModel = "";
        sourceModel += "model someModel;\n";
        sourceModel += "apply someProfile;\n";
        sourceModel += "end.";
        parseAndCheck(sourceProfile, sourceModel);
        Model thisModel = (Model) getRepository().findPackage("someModel", IRepository.PACKAGE.getModel());
        Profile someProfile = (Profile) getRepository().findPackage("someProfile", IRepository.PACKAGE.getProfile());
        assertTrue(thisModel.isProfileApplied(someProfile));
    }

    public void testSystemPackageReferences() throws CoreException {
        String sourcePackage = "";
        sourcePackage += "package somePackage;\n";
        sourcePackage += "class Foo\n";
        sourcePackage += "attribute s : PrimitiveTypes::String;\n";
        sourcePackage += "end;\n";
        sourcePackage += "end.";
        parseAndCheck(sourcePackage);
    }

    public void testImportBase() throws CoreException {
        String sourcePackage = "";
        sourcePackage += "package somePackage;\n";
        sourcePackage += "import base;\n";
        sourcePackage += "end.";
        parseAndCheck(sourcePackage);
    }

    public void testClashBetweenImportedAndLocal() throws CoreException {
        String sourcePackage = "";
        sourcePackage += "package somePackage;\n";
        sourcePackage += "class Expression\n";
        sourcePackage += "end;\n";
        sourcePackage += "class Foo\n";
        sourcePackage += "attribute attr1 : Expression;\n";
        sourcePackage += "end;\n";
        sourcePackage += "end.";
        parseAndCheck(sourcePackage);
        Class localExpressionClass = getRepository().findNamedElement("somePackage::Expression",
                UMLPackage.Literals.CLASS, null);
        assertEquals("somePackage::Expression", localExpressionClass.getQualifiedName());
        Class fooClass = getRepository().findNamedElement("somePackage::Foo", UMLPackage.Literals.CLASS, null);
        Class umlExpressionClass = getRepository().findNamedElement("UML::Expression", UMLPackage.Literals.CLASS, null);
        assertNotNull(localExpressionClass);
        assertNotNull(fooClass);
        assertNotNull(umlExpressionClass);
        Property attribute = fooClass.getAttribute("attr1", null);
        assertEquals(localExpressionClass.getQualifiedName(), attribute.getType().getQualifiedName());
    }

    public void testImportModel() throws CoreException {
        String sourceOtherModel = "";
        sourceOtherModel += "model otherModel;\n";
        sourceOtherModel += "end.";
        String sourceSomeModel = "";
        sourceSomeModel += "model someModel;\n";
        sourceSomeModel += "import otherModel;\n";
        sourceSomeModel += "end.";
        parseAndCheck(sourceOtherModel, sourceSomeModel);
        Model thisModel = (Model) getRepository().findPackage("someModel", IRepository.PACKAGE.getModel());
        assertNotNull(thisModel);
        Model otherModel = (Model) getRepository().findPackage("otherModel", IRepository.PACKAGE.getModel());
        assertNotNull(otherModel);
        assertTrue(thisModel.getImportedPackages().contains(otherModel));
    }

    public void testImportPackage() throws CoreException {
        String sourceOtherPackage = "";
        sourceOtherPackage += "package otherModel::otherPackage;\n";
        sourceOtherPackage += "end.\n";
        String sourceModel = "";
        sourceModel += "model someModel;\n";
        sourceModel += "import otherModel::otherPackage;\n";
        sourceModel += "end.";
        String otherModel = "";
        otherModel += "model otherModel;\n";
        otherModel += "end.";
        parseAndCheck(sourceOtherPackage, sourceModel, otherModel);
        Model thisModel = (Model) getRepository().findPackage("someModel", IRepository.PACKAGE.getModel());
        assertNotNull(thisModel);
        Package otherPackage = getRepository()
                .findPackage("otherModel::otherPackage", IRepository.PACKAGE.getPackage());
        assertNotNull(otherPackage);
        assertTrue(thisModel.getImportedPackages().contains(otherPackage));
    }

    public void testImportPackageCommonParent() throws CoreException {
        String sourceOtherPackage = "";
        sourceOtherPackage += "package otherModel::otherPackage;\n";
        sourceOtherPackage += "end.\n";
        String sourceModel = "";
        sourceModel += "model someModel;\n";
        sourceModel += "import otherModel::otherPackage;\n";
        sourceModel += "end.";
        String otherModel = "";
        otherModel += "model otherModel;\n";
        otherModel += "end.";
        parseAndCheck(sourceOtherPackage, sourceModel, otherModel);
        Model thisModel = (Model) getRepository().findPackage("someModel", IRepository.PACKAGE.getModel());
        assertNotNull(thisModel);
        Package otherPackage = getRepository()
                .findPackage("otherModel::otherPackage", IRepository.PACKAGE.getPackage());
        assertNotNull(otherPackage);
        assertTrue(thisModel.getImportedPackages().contains(otherPackage));
    }

    /**
     * Issue 2890642.
     */
    public void testCycleInLookup() throws CoreException {
        String parentPackageSrc = "";
        parentPackageSrc += "package parent;\n";
        parentPackageSrc += "import parent::child;\n";
        parentPackageSrc += "end.\n";
        String childPackageSrc = "";
        childPackageSrc += "package parent::child;\n";
        childPackageSrc += "end.";
        parseAndCheck(parentPackageSrc, childPackageSrc);
        Package parentPackage = (Package) getRepository().findPackage("parent", IRepository.PACKAGE.getPackage());
        assertNotNull(parentPackage);
        Package childPackage = (Package) getRepository().findPackage("parent::child", IRepository.PACKAGE.getPackage());
        assertNotNull(childPackage);
        assertSame(childPackage,
                getRepository().findNamedElement("child", IRepository.PACKAGE.getPackage(), parentPackage));
        assertSame(parentPackage,
                getRepository().findNamedElement("parent", IRepository.PACKAGE.getPackage(), childPackage));
        assertNull(getRepository().findNamedElement("NonExisting", IRepository.PACKAGE.getClass_(), parentPackage));
        assertNull(getRepository().findNamedElement("NonExisting", IRepository.PACKAGE.getClass_(), childPackage));
    }

    public void testImportPrivatePackage() throws CoreException {
        String sourceA = "";
        sourceA += "model modelA;\n";
        sourceA += "class Class1 end;\n";
        sourceA += "end.";

        String sourceB = "";
        sourceB += "model modelB;\n";
        sourceB += "private import modelA;\n";
        sourceB += "end.";

        String sourceC = "";
        sourceC += "model modelC;\n";
        sourceC += "import modelB;\n";
        sourceC += "class Class2\n";
        sourceC += " attribute class1Ref : Class1;\n";
        sourceC += "end;\n";
        sourceC += "end.";
        IProblem[] problems = compile(sourceA, sourceB, sourceC);
        FixtureHelper.assertTrue(problems, problems.length == 1);
        assertTrue(problems[0].toString(), problems[0] instanceof UnresolvedSymbol);
        assertEquals("Class1", ((UnresolvedSymbol) problems[0]).getSymbol());
    }

    public void testImportPublicPackage() throws CoreException {
        String sourceA = "";
        sourceA += "model modelA;\n";
        sourceA += "class Class1 end;\n";
        sourceA += "end.";

        String sourceB = "";
        sourceB += "model modelB;\n";
        sourceB += "import modelA;\n";
        sourceB += "end.";

        String sourceC = "";
        sourceC += "model modelC;\n";
        sourceC += "import modelB;\n";
        sourceC += "class Class2 specializes Class1 end;\n";
        sourceC += "end.";

        parseAndCheck(sourceA, sourceB, sourceC);

        Class class1 = (Class) getRepository()
                .findNamedElement("modelA::Class1", IRepository.PACKAGE.getClass_(), null);
        Class class2 = (Class) getRepository()
                .findNamedElement("modelC::Class2", IRepository.PACKAGE.getClass_(), null);
        assertNotNull(class1);
        assertNotNull(class2);
        assertTrue(class2.getSuperClasses().contains(class1));
    }

    public void testLoadSystemPackage() throws CoreException {
        String source = "";
        source += "model someModel;\n";
        source += "load <<" + UMLResource.UML_METAMODEL_URI + ">>;";
        source += "end.";
        parseAndCheck(source);
        assertNotNull(getRepository().findPackage("someModel", IRepository.PACKAGE.getModel()));
        assertNotNull(getRepository().findNamedElement("UML", IRepository.PACKAGE.getPackage(), null));
    }

    public void testModelPath() throws CoreException, IOException {

        String librarySource = "";
        librarySource += "model library;\n";
        librarySource += "class BaseClass end;\n";
        librarySource += "end.";
        parseAndCheck(librarySource);

        IFileStore originalLocation = getRepositoryDir();
        IFileStore destination = originalLocation.getParent().getChild(getName() + "_library");
        originalLocation.move(destination, EFS.NONE, null);
        
        IFileStore loadedModelLocation = destination.getChild("library.uml");
		assertTrue(loadedModelLocation.fetchInfo().exists());
        
        IFileStore projectRoot = originalLocation;

        Properties settings = createDefaultSettings();
        settings.put(IRepository.LOADED_PACKAGES, loadedModelLocation.toURI().toString());
		saveSettings(projectRoot, settings);
        String source = "";
        source += "model someModel;\n";
        source += "import  library;\n";
        source += "class MyClass specializes library::BaseClass end;\n";
        source += "end.";
        parseAndCheck(source);

        assertTrue(originalLocation.getChild("someModel.uml").fetchInfo().exists());
        assertFalse(originalLocation.getChild("library.uml").fetchInfo().exists());

        assertNotNull(getRepository().findPackage("someModel", IRepository.PACKAGE.getModel()));
        assertNotNull(getRepository().findNamedElement("library::BaseClass", IRepository.PACKAGE.getClass_(), null));

        Package someModel = getRepository().findPackage("someModel", null);
        Package library = getRepository().findPackage("library", null);

        assertTrue(someModel.getImportedPackages().contains(library));

    }

    public void testLoadAnotherPackage() throws CoreException {

        String librarySource = "";
        librarySource += "model library;\n";
        librarySource += "class BaseClass end;\n";
        librarySource += "end.";
        parseAndCheck(librarySource);

        IFileStore originalLocation = getRepositoryDir();
        IFileStore destination = originalLocation.getParent().getChild(getName() + "_library");
        originalLocation.move(destination, EFS.NONE, null);

        assertTrue(destination.getChild("library.uml").fetchInfo().exists());

        String source = "";
        source += "model someModel;\n";
        source += "load <<" + destination.getChild("library.uml").toURI() + ">>;";
        source += "class MyClass specializes library::BaseClass end;\n";
        source += "end.";
        parseAndCheck(source);

        assertTrue(originalLocation.getChild("someModel.uml").fetchInfo().exists());
        assertFalse(originalLocation.getChild("library.uml").fetchInfo().exists());

        assertNotNull(getRepository().findPackage("someModel", IRepository.PACKAGE.getModel()));
        assertNotNull(getRepository().findNamedElement("library::BaseClass", IRepository.PACKAGE.getClass_(), null));

        Package someModel = getRepository().findPackage("someModel", null);
        Package library = getRepository().findPackage("library", null);

        assertTrue(someModel.getImportedPackages().contains(library));

    }

    
    public void testLoadPackageNonExistingLocation() throws CoreException {
        String source = "";
        source += "model someModel;\n";
        source += "load <<file://made/up/location>>;";
        source += "end.";
        IProblem[] problems = compile(source);
        // expect an error: location loaded does not exist
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
        assertEquals(problems[0].toString(), IProblem.Severity.ERROR, problems[0].getSeverity());
        assertEquals(CannotLoadFromLocation.class, problems[0].getClass());
    }

    public void testLoadPackageFromInvalidURI() throws CoreException {
        String source = "";
        source += "model someModel;\n";
        source += "load <<a : b : c:>>;";
        source += "end.";
        IProblem[] problems = compile(source);
        // expect an error: location loaded does not exist
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
        assertEquals(problems[0].toString(), IProblem.Severity.ERROR, problems[0].getSeverity());
        assertEquals(CannotLoadFromLocation.class, problems[0].getClass());
    }

    public void testModelAndPackageCreation() throws CoreException {
        String sourcePackage = "";
        sourcePackage += "package someModel::somePackage;\n";
        sourcePackage += "end.";
        String sourceModel = "";
        sourceModel += "model someModel;\n";
        sourceModel += "end.";
        parseAndCheck(sourceModel, sourcePackage);
        final Model someModel = (Model) getRepository().findPackage("someModel", IRepository.PACKAGE.getPackage());
        assertNotNull(someModel);
        final Package somePackage = getRepository().findPackage("someModel::somePackage",
                IRepository.PACKAGE.getPackage());
        assertNotNull(somePackage);
        assertSame(someModel, somePackage.getOwner());
    }

    public void testPackageAndModelCreation() throws CoreException {
        String sourcePackage = "";
        sourcePackage += "package someModel::somePackage;\n";
        sourcePackage += "end.";
        String sourceModel = "";
        sourceModel += "model someModel;\n";
        sourceModel += "end.";
        // inverted order
        parseAndCheck(sourcePackage, sourceModel);
        final Model someModel = (Model) getRepository().findPackage("someModel", IRepository.PACKAGE.getPackage());
        assertNotNull(someModel);
        final Package somePackage = getRepository().findPackage("someModel::somePackage",
                IRepository.PACKAGE.getPackage());
        assertNotNull(somePackage);
        assertSame(someModel, somePackage.getOwner());
    }

    public void testModelCreation() throws CoreException {
        String source = "";
        source += "model someModel;\n";
        source += "end.";
        parseAndCheck(source);
        assertNotNull(getRepository().findPackage("someModel", IRepository.PACKAGE.getModel()));
    }

    public void testNestedPackage() throws CoreException {
        String firstPackage = "package parent;\nend.";
        String secondPackage = "package parent::child1;\n class Class1 end;\n end.";
        // normal order
        parseAndCheck(secondPackage, firstPackage);
        assertNotNull(getRepository().findPackage("parent", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("parent::child1", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findNamedElement("parent::child1::Class1", IRepository.PACKAGE.getClass_(), null));
    }

    public void testNestedPackageTwoSeparateCU() throws CoreException {
        String firstPackage = "package parent;\nend.";
        String secondPackage = "package parent::child1;\n class Class1 end;\n end.";
        String thirdPackage = "package parent::child2;\n class Class2 end;\n end.";
        // normal order
        parseAndCheck(firstPackage, secondPackage, thirdPackage);
        assertNotNull(getRepository().findPackage("parent", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("parent::child1", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("parent::child2", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findNamedElement("parent::child1::Class1", IRepository.PACKAGE.getClass_(), null));
        assertNotNull(getRepository().findNamedElement("parent::child2::Class2", IRepository.PACKAGE.getClass_(), null));
    }

    /**
     * Test for issue #2890642.
     */
    public void test3LevelNestedPackage() throws CoreException {
        String firstPackage = "package first;\nend.";
        String secondPackage = "package first::second;\nend.";
        String thirdPackage = "package first::second::third;\nend.";
        // normal order
        parseAndCheck(firstPackage, secondPackage, thirdPackage);
        assertNotNull(getRepository().findPackage("first", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("first::second", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("first::second::third", IRepository.PACKAGE.getPackage()));
    }

    /**
     * Test for issue #2890642.
     */
    public void test3LevelNestedPackageInverseCompilationOrder() throws CoreException {
        String firstPackage = "package first;\nend.";
        String secondPackage = "package first::second;\nend.";
        String thirdPackage = "package first::second::third;\nend.";
        // inverted order
        parseAndCheck(thirdPackage, secondPackage, firstPackage);
        assertNotNull(getRepository().findPackage("first", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("first::second", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("first::second::third", IRepository.PACKAGE.getPackage()));
    }

    /**
     * Test for issue #2890642.
     */
    public void test3LevelNestedPackageXRef() throws CoreException {
        String firstPackage = "package first;\n class First end;\n end.";
        String secondPackage = "package first::second;\n class Second specializes First end;\n end.";
        String thirdPackage = "package first::second::third;\n class Third specializes Second end;\n end.";
        // normal order
        parseAndCheck(firstPackage, secondPackage, thirdPackage);
        assertNotNull(getRepository().findPackage("first", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("first::second", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("first::second::third", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findNamedElement("first::First", IRepository.PACKAGE.getClass_(), null));
        assertNotNull(getRepository().findNamedElement("first::second::Second", IRepository.PACKAGE.getClass_(), null));
        assertNotNull(getRepository().findNamedElement("first::second::third::Third", IRepository.PACKAGE.getClass_(),
                null));
    }

    /**
     * Test for issue #2890642.
     */
    public void test3LevelNestedPackageXRefInverseCompilationOrder() throws CoreException {
        String firstPackage = "package first;\n class First end;\n end.";
        String secondPackage = "package first::second;\n class Second specializes First end;\n end.";
        String thirdPackage = "package first::second::third;\n class Third specializes Second end;\n end.";
        // inverted order
        parseAndCheck(thirdPackage, secondPackage, firstPackage);
        assertNotNull(getRepository().findPackage("first", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("first::second", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findPackage("first::second::third", IRepository.PACKAGE.getPackage()));
        assertNotNull(getRepository().findNamedElement("first::First", IRepository.PACKAGE.getClass_(), null));
        assertNotNull(getRepository().findNamedElement("first::second::Second", IRepository.PACKAGE.getClass_(), null));
        assertNotNull(getRepository().findNamedElement("first::second::third::Third", IRepository.PACKAGE.getClass_(),
                null));
    }

    public void testPackageCreation() throws CoreException {
        String source = "";
        source += "package somePackage;\n";
        source += "end.";
        parseAndCheck(source);
        final Package somePackage = getRepository().findPackage("somePackage", IRepository.PACKAGE.getPackage());
        assertNotNull(somePackage);
    }

    public void testNestedPackageSingleCU() throws CoreException {
        String source = "";
        source += "model someModel;\n";
        source += "class Foo end;\n";
        source += "package somePackage;\n";
        source += "class Bar end;\n";
        source += "end;";
        source += "class Fred end;\n";
        source += "end.";
        parseAndCheck(source);
        final Model someModel = (Model) getRepository().findPackage("someModel", IRepository.PACKAGE.getModel());
        assertNotNull(someModel);
        final Package somePackage = getRepository().findPackage("someModel::somePackage",
                IRepository.PACKAGE.getPackage());
        assertNotNull(somePackage);
        assertSame(someModel, somePackage.getOwner());

        final Class fooClass = getRepository()
                .findNamedElement("someModel::Foo", IRepository.PACKAGE.getClass_(), null);
        assertNotNull(fooClass);
        assertSame(someModel, fooClass.getOwner());
        final Class barClass = getRepository().findNamedElement("someModel::somePackage::Bar",
                IRepository.PACKAGE.getClass_(), null);
        assertNotNull(barClass);
        assertSame(somePackage, barClass.getOwner());
        final Class fredClass = getRepository().findNamedElement("someModel::Fred", IRepository.PACKAGE.getClass_(),
                null);
        assertNotNull(fredClass);
        assertSame(someModel, fredClass.getOwner());
    }

    public void testProfileCreation() throws CoreException {
        String source = "";
        source += "profile someProfile;\n";
        source += "end.";
        parseAndCheck(source);
        final Profile profile = (Profile) getRepository().findPackage("someProfile", IRepository.PACKAGE.getProfile());
        assertNotNull(profile);
        assertTrue(profile.isDefined());
    }

    public void testInvalidProfileCreation() throws CoreException {
        String sourceProfile = "";
        sourceProfile += "profile somePackage::someProfile;\n";
        sourceProfile += "end.";
        IProblem[] problems = compile(sourceProfile);
        // expect an error: profiles must be top-level packages
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
        assertEquals(problems[0].toString(), IProblem.Severity.ERROR, problems[0].getSeverity());
        assertEquals(InvalidPackageNesting.class.getName(), problems[0].getClass().getName());
    }

    public void testInvalidModelCreation() throws CoreException {
        String source = "";
        source += "model somePackage::someModel;\n";
        source += "end.";
        IProblem[] problems = compile(source);
        // expect an error: models must be top-level packages
        assertEquals(Arrays.asList(problems).toString(), 1, problems.length);
        assertEquals(problems[0].toString(), IProblem.Severity.ERROR, problems[0].getSeverity());
        assertEquals(InvalidPackageNesting.class, problems[0].getClass());
    }
}
