package com.abstratt.mdd.core.tests.frontend.builder;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ReadExtentAction;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.frontend.core.builder.ActivityBuilder;
import com.abstratt.mdd.frontend.core.builder.ClassifierBuilder;
import com.abstratt.mdd.frontend.core.builder.OperationBuilder;
import com.abstratt.mdd.frontend.core.builder.PackageBuilder;
import com.abstratt.mdd.frontend.core.builder.UML2BuilderFactory;
import com.abstratt.mdd.frontend.core.builder.UML2ModelBuildDriver;
import com.abstratt.mdd.frontend.core.builder.UML2ProductKind;
import com.abstratt.mdd.frontend.core.builder.actions.StructuredActivityNodeBuilder;

public class ActionBuilderTests extends AbstractElementBuilderTests {
	public ActionBuilderTests(String name) {
		super(name);
	}
	public void testBasicActivity() {
		PackageBuilder packageBuilder = new UML2BuilderFactory().newBuilder(UML2ProductKind.PACKAGE);
		packageBuilder.name("myPackage");
		ClassifierBuilder classBuilder = packageBuilder.newClassifier(UML2ProductKind.CLASS);
		classBuilder.name("myClass");
		OperationBuilder operationBuilder = classBuilder.newOperation(UML2ProductKind.OPERATION);
		operationBuilder.name("myOperation");
		ActivityBuilder newActivity = operationBuilder.newMethod();
		newActivity.name("myActivity");
		StructuredActivityNodeBuilder block = newActivity.newBlock();
		block.readExtent("myClass");
		new UML2ModelBuildDriver().build(buildContext, packageBuilder);
		Activity activity = getRepository().findNamedElement("myPackage::myClass::myActivity", Literals.ACTIVITY, null);
		assertNotNull(activity);
		assertEquals(1, activity.getNodes().size());
		assertTrue(activity.getNodes().get(0) instanceof StructuredActivityNode);
		StructuredActivityNode body = (StructuredActivityNode) activity.getNodes().get(0);
		assertEquals(1, body.getNodes().size());
		assertTrue(body.getNodes().get(0) instanceof StructuredActivityNode);
		StructuredActivityNode rootBlock = (StructuredActivityNode) body.getNodes().get(0);
		assertTrue(rootBlock.getNodes().get(0) instanceof ReadExtentAction);
		assertNotNull(classBuilder.getProduct() != null);
		assertSame(classBuilder.getProduct(), ((ReadExtentAction) rootBlock.getNodes().get(0)).getClassifier());
		assertNotNull(operationBuilder.getProduct() != null);
		assertSame(operationBuilder.getProduct(), activity.getSpecification());
	}

}
