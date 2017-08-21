package com.abstratt.mdd.core.tests.frontend.textuml;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;
import com.abstratt.mdd.core.util.MDDUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

public class UnicodeTests  extends AbstractRepositoryBuildingTests {

    public static Test suite() {
        return new TestSuite(UnicodeTests.class);
    }

    public UnicodeTests(String name) {
        super(name);
    }
    
    static String model = "";

    static {
        model += "(* Package with Latin characters in its name: packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ *)\n";
        model += "package packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ;\n";
        model += "    (* Class with Latin characters in its name: ClasseComAcentuação *)\n";
        model += "    class ClasseComAcentuação\n";
        model += "    end;\n";
        model += "    (* Class with Japanese characters in its name: これは有効なクラス名です *)\n";
        model += "    class これは有効なクラス名です\n";
        model += "    end;\n";
        model += "end.\n";
    }
    
    public void testIdentifiers() throws CoreException {
        parseAndCheck(model);
        assertNotNull(getClass("packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ::ClasseComAcentuação"));
        assertNotNull(get("packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ", UMLPackage.Literals.PACKAGE));
        assertNotNull(getClass("packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ::これは有効なクラス名です"));
    }
    
    public void testDocumentation() throws CoreException {
        parseAndCheck(model);
        assertEquals("Class with Latin characters in its name: ClasseComAcentuação", MDDUtil.getDescription(getClass("packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ::ClasseComAcentuação")));
        assertEquals("Package with Latin characters in its name: packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ", MDDUtil.getDescription(get("packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ", UMLPackage.Literals.PACKAGE)));
        assertEquals("Class with Japanese characters in its name: これは有効なクラス名です", MDDUtil.getDescription(getClass("packageWithUnicodeChars_áéíóúçÁÉÍÓÚÇ::これは有効なクラス名です")));
    }
}
