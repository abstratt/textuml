package com.abstratt.mdd.frontend.core.builder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.frontend.core.builder.actions.ActionBuilder;

/**
 * Enumeration of element types. Maps UML metamodel elements to builder impleementations.
 */
public enum UML2ProductKind {
	ASSOCIATION,
	CLASS(Literals.CLASS, ClassifierBuilder.class),
	DATA_TYPE(Literals.DATA_TYPE, ClassifierBuilder.class),
	INTERFACE(Literals.INTERFACE, ClassifierBuilder.class),
	PACKAGE(Literals.PACKAGE, PackageBuilder.class),
	PROFILE(Literals.PROFILE, PackageBuilder.class),
	MODEL(Literals.MODEL, PackageBuilder.class),
	ENUMERATION(Literals.ENUMERATION, ClassifierBuilder.class),
	STEREOTYPE(Literals.STEREOTYPE, ClassifierBuilder.class),
	CLASSIFIER(Literals.CLASSIFIER, null),
	PROPERTY,
	OPERATION,
	PARAMETER,  
	ADD_VARIABLE_VALUE_ACTION,
	CALL_OPERATION_ACTION,
	READ_EXTENT_ACTION,
	READ_SELF_ACTION,
	READ_VARIABLE_ACTION,
	READ_STRUCTURAL_FEATURE_ACTION,
	STRUCTURED_ACTIVITY_NODE,
	ACTIVITY,
	ACTION(Literals.ACTION, null), 
	VARIABLE, 
	ADD_STRUCTURAL_FEATURE_VALUE_ACTION, 
	VALUE_SPECIFICATION_ACTION, 
	LITERAL_BOOLEAN,
	LITERAL_INTEGER,
	LITERAL_STRING,
	LITERAL_UNLIMITED_NATURAL,
	LITERAL_NULL,
	BASIC_TYPE_LITERAL(Literals.LITERAL_STRING, BasicTypeValueSpecificationBuilder.class);
	private static Map<EClass, UML2ProductKind> metaClassToKind;
	private EClass metaClass;
	private Class<? extends ElementBuilder<?>> builderClass;
	private void registerMetaClass() {
		if (metaClassToKind == null)
			metaClassToKind = new HashMap<EClass, UML2ProductKind>();
		if (metaClass != null)
			metaClassToKind.put(this.metaClass, this);
	}
	UML2ProductKind(EClass metaClass, Class<? extends ElementBuilder<?>> builderClass) {
		this.metaClass = metaClass;
		this.builderClass = builderClass;
		registerMetaClass();
	}
	UML2ProductKind(Class<? extends ElementBuilder<?>> builderClass) {
		this(null, builderClass);
	}
	UML2ProductKind() {
		this.metaClass = findMetaClass();
		this.builderClass = findBuilderClass();
		registerMetaClass();
	}
	private Class<? extends ElementBuilder<?>> findBuilderClass() {
		if (metaClass == null)
			throw new IllegalStateException();
		if (Action.class.isAssignableFrom(metaClass.getInstanceClass()))
			return findBuilderClass(ActionBuilder.class.getPackage().getName());
		return findBuilderClass(ElementBuilder.class.getPackage().getName());
	}
	private Class<? extends ElementBuilder<?>> findBuilderClass(
			String packageName) {
		String metaClassSimpleName = metaClass.getInstanceClass().getSimpleName();
		try {
			return (Class<? extends ElementBuilder<?>>) Class.forName(packageName + '.' + metaClassSimpleName + "Builder");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	private EClass findMetaClass() {
		try {
			Field field = Literals.class.getField(name());
			return (EClass) field.get(null);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("No UML metaclass can be found for " + name());
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("No UML metaclass can be found for " + name());
		}
	}
	Class<? extends ElementBuilder<?>> getBuilderClass() {
		return builderClass;
	}
	public EClass getMetaClass() {
		return metaClass;
	}
	public static UML2ProductKind forClass(EClass actionClass) {
		UML2ProductKind uml2ProductKind = metaClassToKind.get(actionClass);
		if (uml2ProductKind == null)
			throw new IllegalArgumentException("No kind for " + actionClass);
		return uml2ProductKind;
	}
}
