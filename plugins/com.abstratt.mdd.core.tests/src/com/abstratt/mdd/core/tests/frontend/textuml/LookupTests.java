package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.NamedElement;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;

public class LookupTests extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(LookupTests.class);
    }

    public LookupTests(String name) {
        super(name);
    }

    public void testEscaping() throws CoreException {
        String source = "";
        source += "model my\\-model;\n";
        source += "import base;\n";
        source += "class My\\-Class\n";
        source += "attribute my\\-attribute : Integer;\n";
        source += "operation my\\-Operation();\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(source);
        NamedElement found;
        found = getRepository().findNamedElement("my-model", IRepository.PACKAGE.getModel(), null);
        assertNotNull(found);
        assertEquals("my-model", found.getQualifiedName());
        found = getRepository().findNamedElement("my-model::My-Class", IRepository.PACKAGE.getClass_(), null);
        assertNotNull(found);
        assertEquals("my-model::My-Class", found.getQualifiedName());
        found = getRepository().findNamedElement("my-model::My-Class::my-attribute", IRepository.PACKAGE.getProperty(),
                null);
        assertNotNull(found);
        assertEquals("my-model::My-Class::my-attribute", found.getQualifiedName());
        found = getRepository().findNamedElement("my-model::My-Class::my-Operation",
                IRepository.PACKAGE.getOperation(), null);
        assertNotNull(found);
        assertEquals("my-model::My-Class::my-Operation", found.getQualifiedName());
    }

    public void testFindElementInNonExistingPackage() {
        assertNull(getRepository().findNamedElement("foo::Bar", IRepository.PACKAGE.getModel(), null));
    }

}
