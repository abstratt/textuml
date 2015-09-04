package com.abstratt.mdd.core.tests.frontend.textuml;

import java.util.Collections;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.tests.harness.FixtureHelper;
import com.abstratt.mdd.core.util.ConnectorUtils;
import com.abstratt.mdd.frontend.core.AnonymousDisconnectedPort;
import com.abstratt.mdd.frontend.core.InvalidConnector;

public class ComponentTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(ComponentTests.class);
    }

    public ComponentTests(String name) {
        super(name);
    }

    @Override
    protected Properties createDefaultSettings() {
        Properties basicSettings = super.createDefaultSettings();
        basicSettings.setProperty(IRepository.LIBRARY_PROJECT, Boolean.TRUE.toString());
        return basicSettings;
    }

    public void testPorts() throws CoreException {
        String source = "";
        source += "model someModel;\n";
        source += "interface Interface1\n";
        source += "end;\n";
        source += "interface Interface2\n";
        source += "end;\n";
        source += "class SomeClass\n";
        source += "    required port p1 : Interface1;\n";
        source += "    provided port p2 : Interface2;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        Class someClass = getClass("someModel::SomeClass");
        Interface someInterface1 = get("someModel::Interface1", IRepository.PACKAGE.getInterface());
        Interface someInterface2 = get("someModel::Interface2", IRepository.PACKAGE.getInterface());

        Port p1 = someClass.getOwnedPort("p1", null);
        assertNotNull(p1);
        assertNotNull(p1.getType());
        assertTrue(p1.getType() instanceof BehavioredClassifier);
        BehavioredClassifier p1Type = (BehavioredClassifier) p1.getType();
        assertEquals(Collections.singletonList(someInterface1), p1Type.getImplementedInterfaces());
        assertEquals(0, p1Type.getUsedInterfaces().size());

        Port p2 = someClass.getOwnedPort("p2", null);
        assertNotNull(p2.getType());
        assertNotNull(p2);
        assertTrue(p2.getType() instanceof BehavioredClassifier);
        BehavioredClassifier p2Type = (BehavioredClassifier) p2.getType();
        assertEquals(Collections.singletonList(someInterface2), p2Type.getImplementedInterfaces());
        assertEquals(0, p2Type.getUsedInterfaces().size());

        assertEquals(0, p1.getProvideds().size());
        assertEquals(Collections.singletonList(someInterface1), p1.getRequireds());

        assertEquals(0, p2.getRequireds().size());
        assertEquals(Collections.singletonList(someInterface2), p2.getProvideds());
    }

    public void testUnnamedDisconnectedPort() throws CoreException {
        String source = "";
        source += "model someModel;\n";
        source += "interface Interface1\n";
        source += "end;\n";
        source += "class SomeClass\n";
        source += "    required port : Interface1;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] errors = parse(source);
        FixtureHelper.assertTrue(errors, 1 == errors.length);
        FixtureHelper.assertTrue(errors, errors[0] instanceof AnonymousDisconnectedPort);
    }

    public void testComponent() throws CoreException {
        String userNotificationSrc = "";
        userNotificationSrc += "model userNotification;\n";
        userNotificationSrc += "interface UserNotification\n";
        userNotificationSrc += "end;\n";
        userNotificationSrc += "interface Commenting\n";
        userNotificationSrc += "end;\n";
        userNotificationSrc += "end.";

        String issueSrc = "";
        issueSrc += "model issues;\n";
        issueSrc += "import userNotification;\n";
        issueSrc += "class Issue\n";
        issueSrc += "    required port userNotification : UserNotification;\n";
        issueSrc += "    required port commenting : Commenting;\n";
        issueSrc += "end;\n";
        issueSrc += "component IssueCore\n";
        issueSrc += "    composition issues : Issue[*];\n";
        issueSrc += "    required port userNotification : UserNotification connector issues.userNotification;\n";
        issueSrc += "    required port commenting : Commenting connector issues.commenting;\n";
        issueSrc += "end;\n";
        issueSrc += "end.";

        String emailSrc = "";
        emailSrc += "model email;\n";
        emailSrc += "import userNotification::UserNotification;\n";
        emailSrc += "class EmailNotification implements UserNotification\n";
        emailSrc += "end;\n";
        emailSrc += "component EmailService\n";
        emailSrc += "    composition emailNotification : EmailNotification;\n";
        emailSrc += "    provided port userNotification : UserNotification connector emailNotification;\n";
        emailSrc += "end;\n";
        emailSrc += "end.";

        String commentingSrc = "";
        commentingSrc += "model commenting;\n";
        commentingSrc += "import userNotification;\n";
        commentingSrc += "class DisqusCommenting implements Commenting\n";
        commentingSrc += "    required port userNotification : UserNotification;\n";
        commentingSrc += "end;\n";
        commentingSrc += "component DisqusCommentingService\n";
        commentingSrc += "    composition disqusCommenting : DisqusCommenting;\n";
        commentingSrc += "    provided port commenting : Commenting connector disqusCommenting;\n";
        commentingSrc += "    required port userNotification : UserNotification connector disqusCommenting.userNotification;\n";
        commentingSrc += "end;\n";
        commentingSrc += "end.";

        String issueAppSrc = "";
        issueAppSrc += "model issue_tracking_app;\n";
        issueAppSrc += "import issues;\n";
        issueAppSrc += "import email;\n";
        issueAppSrc += "import commenting;\n";
        issueAppSrc += "component IssueTrackingApp\n";
        issueAppSrc += "    composition emailService : EmailService;\n";
        issueAppSrc += "    composition issueCore : IssueCore;\n";
        issueAppSrc += "    composition disqusCommenting : DisqusCommentingService;\n";
        issueAppSrc += "    connector issueCore.userNotification, emailService.userNotification, disqusCommenting.userNotification;\n";
        issueAppSrc += "    connector issueCore.commenting, disqusCommenting.commenting;\n";
        issueAppSrc += "end;\n";
        issueAppSrc += "end.";

        parseAndCheck(userNotificationSrc, commentingSrc, issueSrc, emailSrc, issueAppSrc);

        Component issueTrackingApp = get("issue_tracking_app::IssueTrackingApp", UMLPackage.Literals.COMPONENT);
        assertTrue(issueTrackingApp.isIndirectlyInstantiated());

        Connector connector = issueTrackingApp.getOwnedConnectors().get(0);
        assertEquals(3, connector.getEnds().size());

        Port issueUserNotification = get("issues::Issue::userNotification", UMLPackage.Literals.PORT);
        Port issueCommenting = get("issues::Issue::commenting", UMLPackage.Literals.PORT);
        Port commentingUserNotification = get("commenting::DisqusCommenting::userNotification",
                UMLPackage.Literals.PORT);
        assertSame(get("email::EmailService::emailNotification", UMLPackage.Literals.PROPERTY),
                ConnectorUtils.findProvidingPart(issueUserNotification));
        assertSame(get("commenting::DisqusCommentingService::disqusCommenting", UMLPackage.Literals.PROPERTY),
                ConnectorUtils.findProvidingPart(issueCommenting));
        assertSame(get("email::EmailService::emailNotification", UMLPackage.Literals.PROPERTY),
                ConnectorUtils.findProvidingPart(commentingUserNotification));
    }

    public void testConnector() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "interface SimpleInterface\n";
        source += "end;\n";
        source += "class SimpleClass implements SimpleInterface\n";
        source += "end;\n";
        source += "class SimpleClass2\n";
        source += "    provided port c : SimpleInterface;\n";
        source += "end;\n";
        source += "component SimpleComponent\n";
        source += "    composition a : SimpleClass;\n";
        source += "    composition b : SimpleClass2[*];\n";
        source += "    provided port p : SimpleInterface;\n";
        source += "    connector p, a, b.c;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);

        Port port = get("simple::SimpleComponent::p", UMLPackage.Literals.PORT);
        assertEquals("p", port.getName());
        Property attributeA = get("simple::SimpleComponent::a", UMLPackage.Literals.PROPERTY);
        Property attributeC = get("simple::SimpleClass2::c", UMLPackage.Literals.PROPERTY);
        validatePort(port, attributeA, attributeC);
    }

    // removed in UML2 5.0 -
    // https://wiki.eclipse.org/MDT/UML2/UML2_5.0_Migration_Guide#Constraints
    // public void testConnector_PortCompatibility() throws CoreException {
    // String source = "";
    // source += "model simple;\n";
    // source += "interface SimpleInterface1\n";
    // source += "end;\n";
    // source += "interface SimpleInterface2\n";
    // source += "end;\n";
    // source += "component SimpleComponent\n";
    // source += "    required port a : SimpleInterface1;\n";
    // source += "    provided port b : SimpleInterface2;\n";
    // source += "    connector a,b;\n";
    // source += "end;\n";
    // source += "end.";
    // IProblem[] errors = parse(source);
    // FixtureHelper.assertTrue(errors, 1 == errors.length);
    // FixtureHelper.assertTrue(errors, errors[0] instanceof InvalidConnector);
    // assertEquals(InvalidConnector.Reason.BetweenInterfacesPorts,
    // ((InvalidConnector) errors[0]).getReason());
    // }

    public void testConnector_ConnectorMustOwnPortOrPartWithPort() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "interface SimpleInterface\n";
        source += "end;\n";
        source += "class SimpleClass implements SimpleInterface\n";
        source += "    attribute p1 : SimpleInterface;\n";
        source += "end;\n";
        source += "component SimpleComponent\n";
        source += "    composition a : SimpleClass;\n";
        source += "    provided port p2 : SimpleInterface connector a.p1;\n";
        source += "end;\n";
        source += "end.";
        IProblem[] errors = parse(source);
        FixtureHelper.assertTrue(errors, 1 == errors.length);
        FixtureHelper.assertTrue(errors, errors[0] instanceof InvalidConnector);
        assertEquals(errors[0].getMessage(), InvalidConnector.Reason.Roles.getCode(), ((InvalidConnector) errors[0])
                .getReason().getCode());
        assertEquals(errors[0].getMessage(), InvalidConnector.Reason.Roles, ((InvalidConnector) errors[0]).getReason());
    }

    public void testConnector_UnnamedPort() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "interface SimpleInterface\n";
        source += "end;\n";
        source += "class SimpleClass implements SimpleInterface\n";
        source += "end;\n";
        source += "class SimpleClass2\n";
        source += "    required port c : SimpleInterface;\n";
        source += "end;\n";
        source += "component SimpleComponent\n";
        source += "    composition a : SimpleClass;\n";
        source += "    composition b : SimpleClass2;\n";
        source += "    provided port  : SimpleInterface connector a, b.c;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);

        Component component = get("simple::SimpleComponent", UMLPackage.Literals.COMPONENT);
        assertEquals(1, component.getOwnedPorts().size());
        Port port = component.getOwnedPorts().get(0);
        assertNull(port.getName());
        Property attributeA = get("simple::SimpleComponent::a", UMLPackage.Literals.PROPERTY);
        Property attributeC = get("simple::SimpleClass2::c", UMLPackage.Literals.PROPERTY);
        validatePort(port, attributeA, attributeC);
    }

    public void validatePort(Port port, Property attributeA, Property attributeC) {
        assertEquals(1, port.getEnds().size());
        Connector connector = (Connector) port.getEnds().get(0).getOwner();
        assertEquals(3, connector.getEnds().size());
        assertSame(port.getEnds().get(0), connector.getEnds().get(0));
        assertSame(attributeA.getEnds().get(0), connector.getEnds().get(1));
        assertSame(attributeC.getEnds().get(0), connector.getEnds().get(2));
    }
}