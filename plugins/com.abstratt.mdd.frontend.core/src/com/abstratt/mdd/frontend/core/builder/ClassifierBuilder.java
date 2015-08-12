package com.abstratt.mdd.frontend.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.frontend.core.NotAMetaclass;
import com.abstratt.mdd.frontend.core.WrongMetaclass;

public class ClassifierBuilder extends DefaultParentBuilder<Classifier> {
	private List<NameReference> generals = new ArrayList<NameReference>();
	private List<NameReference> contracts = new ArrayList<NameReference>();
	private List<NameReference> extensions = new ArrayList<NameReference>();
	private List<String> enumerationLiterals = new ArrayList<String>();

	public ClassifierBuilder(UML2ProductKind kind) {
		super(kind);
	}

	public ClassifierBuilder specialize(String... className) {
		for (String c : className)
			this.generals.add(reference(c, getKind()));
		return this;
	}

	public ClassifierBuilder implement(String... interfaceName) {
		if (!Literals.BEHAVIORED_CLASSIFIER.isSuperTypeOf(getEClass()))
			getContext().getProblemTracker().add(
			        new WrongMetaclass(Literals.INTERFACE_REALIZATION, Literals.BEHAVIORED_CLASSIFIER, getEClass()));
		else
			for (String i : interfaceName)
				this.contracts.add(reference(i, UML2ProductKind.INTERFACE));
		return this;
	}

	public ClassifierBuilder modify(String... modifier) {
		return this;
	}

	public PropertyBuilder newProperty(UML2ProductKind kind) {
		return (PropertyBuilder) newChildBuilder(kind);
	}

	public OperationBuilder newOperation(UML2ProductKind kind) {
		return (OperationBuilder) newChildBuilder(kind);
	}

	public ActivityBuilder newActivity() {
		if (getKind() != UML2ProductKind.CLASS)
			getContext().getProblemTracker().add(
			        new WrongMetaclass(Literals.CLASS__NESTED_CLASSIFIER, Literals.CLASS, getEClass()));
		return newChildBuilder(UML2ProductKind.ACTIVITY);
	}

	@Override
	protected Classifier createProduct() {
		if (getParentProduct() != null)
			return (Classifier) ((Package) getParentProduct()).createOwnedType(getName(), getEClass());
		return (Classifier) EcoreUtil.create(getEClass());
	}

	@Override
	protected void enhance() {
		super.enhance();
		specializeClasses();
		realizeInterfaces();
		extendMetaclasses();
		createEnumerationLiterals();
	}

	private void createEnumerationLiterals() {
		for (String literalName : enumerationLiterals)
			((Enumeration) getProduct()).createOwnedLiteral(literalName);
	}

	private void extendMetaclasses() {
		for (NameReference toResolve : extensions) {
			final Boolean required = (Boolean) toResolve.getProperty(Literals.EXTENSION__IS_REQUIRED.getName());
			new ReferenceSetter<Class>(toResolve, getParentProduct(), getContext()) {
				@Override
				protected void link(Class metaclass) {
					if (!metaclass.isMetaclass())
						getContext().getProblemTracker().add(new NotAMetaclass(metaclass.getQualifiedName()));
					else {
						Stereotype stereotype = (Stereotype) getProduct();
						if (!stereotype.getProfile().getReferencedMetamodels().contains(metaclass.getModel()))
							stereotype.getProfile().createMetamodelReference(metaclass.getNearestPackage());
						stereotype.createExtension(metaclass, required != null && required);
					}
				}
			};
		}
	}

	private void realizeInterfaces() {
		for (NameReference toResolve : contracts)
			new ReferenceSetter<Interface>(toResolve, getParentProduct(), getContext()) {
				@Override
				protected void link(Interface contract) {
					((BehavioredClassifier) getProduct()).createInterfaceRealization(null, contract);
				}
			};
	}

	private void specializeClasses() {
		for (NameReference toResolve : generals) {
			new ReferenceSetter<Classifier>(toResolve, getParentProduct(), getContext()) {
				@Override
				protected void link(Classifier general) {
					getProduct().createGeneralization(general);
				}
			};
		}
	}

	public ClassifierBuilder extend(String metaclass, boolean required) {
		if (getKind() != UML2ProductKind.STEREOTYPE)
			getContext().getProblemTracker().add(
			        new WrongMetaclass(Literals.EXTENSION, Literals.STEREOTYPE, getEClass()));
		else {
			NameReference reference = reference(metaclass, UML2ProductKind.CLASS);
			reference.setProperty(Literals.EXTENSION__IS_REQUIRED.getName(), required);
			extensions.add(reference);
		}
		return this;
	}

	public ClassifierBuilder enumerationLiteral(String enumLiteral) {
		if (getKind() != UML2ProductKind.ENUMERATION)
			getContext().getProblemTracker().add(
			        new WrongMetaclass(Literals.ENUMERATION_LITERAL, Literals.ENUMERATION, getEClass()));
		else {
			enumerationLiterals.add(enumLiteral);
		}
		return this;
	}
}
