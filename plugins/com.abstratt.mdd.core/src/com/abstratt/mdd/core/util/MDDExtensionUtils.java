package com.abstratt.mdd.core.util;

import static com.abstratt.mdd.core.util.ClassifierUtils.toEnumerationLiterals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Feature;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.LiteralNull;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.core.MDDCore;

public class MDDExtensionUtils {
	public static final String EXTENSIONS_PROFILE = "mdd_extensions";
	private static final String CONSTRAINT_BEHAVIOR_CONSTRAINT_PROPERTY = "constraint";
	public static final String SYSTEM_USER_CLASS = "mdd_types::SystemUser";
	private static final String CONTEXTUALIZED_CONSTRAINT_STEREOTYPE = EXTENSIONS_PROFILE + "::ContextualizedConstraint";
	private static final String CONTEXTUALIZED_CONSTRAINT_IS_STATIC_PROPERTY = "isStatic";
	private static final String DERIVATION_STEREOTYPE = EXTENSIONS_PROFILE + "::Derivation";
	private static final String DERIVATION_CONTEXT = "context";
	private static final String BASIC_VALUE_STEREOTYPE = EXTENSIONS_PROFILE + "::BasicValue";
	private static final String VERTEX_LITERAL_STEREOTYPE = EXTENSIONS_PROFILE + "::VertexLiteral";
	private static final String CLOSURE_STEREOTYPE = EXTENSIONS_PROFILE + "::Closure";
	private static final String APPLICATION_STEREOTYPE = EXTENSIONS_PROFILE + "::Application";
	private static final String LIBRARY_STEREOTYPE = EXTENSIONS_PROFILE + "::Library";
	private static final String CONSTRAINT_BEHAVIOR_STEREOTYPE = EXTENSIONS_PROFILE + "::ConstraintBehavior";
	private static final String WILDCARD_TYPE_STEREOTYPE = EXTENSIONS_PROFILE + "::WildcardType";
	private static final String WILDCARD_TYPE_CONTEXT = "context";
	public static final String WILDCARD_TYPE_CONTEXT_STEREOTYPE = EXTENSIONS_PROFILE + "::WildcardTypeContext";
	public static final String WILDCARD_TYPE_CONTEXT_TYPES = "wildcardTypes";
	public static final String DEBUGGABLE_STEREOTYPE = EXTENSIONS_PROFILE + "::Debuggable";
	private static final String ENTRY_POINT_STEREOTYPE = EXTENSIONS_PROFILE + "::EntryPoint";
	private static final String EXTERNAL_CLASS_STEREOTYPE = EXTENSIONS_PROFILE + "::External";
	public static final String ROLE_CLASS_STEREOTYPE = EXTENSIONS_PROFILE + "::Role";
	private static final String OBJECT_INITIALIZATION_STEREOTYPE = EXTENSIONS_PROFILE + "::ObjectInitialization";
	private static final String CAST_STEREOTYPE = EXTENSIONS_PROFILE + "::Cast";
	private static final String SIGNATURE_STEREOTYPE = EXTENSIONS_PROFILE + "::Signature";
	private static final String SIGNATURE_CONTEXT = "context";
	private static final String RULE_STEREOTYPE = EXTENSIONS_PROFILE + "::Rule";
	public static final String INVARIANT_STEREOTYPE = EXTENSIONS_PROFILE + "::Invariant";
	public static final String ACCESS_STEREOTYPE = EXTENSIONS_PROFILE + "::Access";
	private static final String ACCESS_CAPABILITY_ENUMERATION = EXTENSIONS_PROFILE + "::AccessCapability";
	public static final String ACCESS_ALLOWED = "allowed";
	public static final String ACCESS_DENIED = "denied";
	public static final String ACCESS_ROLES = "roles";
	private static final String META_REFERENCE_STEREOTYPE = EXTENSIONS_PROFILE + "::MetaReference";

	public static void addDebugInfo(Element toEnhance, String source, int lineNumber) {
		Stereotype debuggableStereotype = StereotypeUtils.findStereotype(DEBUGGABLE_STEREOTYPE);
		toEnhance.applyStereotype(debuggableStereotype);
		toEnhance.setValue(debuggableStereotype, "lineNumber", lineNumber);
		toEnhance.setValue(debuggableStereotype, "source", source);
	}

	/**
	 * The meta reference facility is not currently in use.
	 */
	@Deprecated
	public static ValueSpecification buildMetaReference(Package parent, Element referred, Type type) {
		LiteralNull nullLiteral = MDDUtil.createLiteralNull(parent);
		Stereotype referenceStereotype = StereotypeUtils.findStereotype(META_REFERENCE_STEREOTYPE);
		nullLiteral.applyStereotype(referenceStereotype);
		nullLiteral.setValue(referenceStereotype, "target", referred);
		nullLiteral.setType(type);
		return nullLiteral;
	}

	@Deprecated
	public static boolean isMetaReference(ValueSpecification specification) {
		return specification instanceof LiteralNull
				&& StereotypeUtils.hasStereotype(specification, META_REFERENCE_STEREOTYPE);
	}

	@Deprecated
	public static Type resolveMetaReference(ValueSpecification value) {
		Assert.isLegal(isMetaReference(value));
		LiteralNull nullLiteral = (LiteralNull) value;
		Stereotype referenceStereotype = nullLiteral.getAppliedStereotype(META_REFERENCE_STEREOTYPE);
		return (Type) nullLiteral.getValue(referenceStereotype, "target");
	}

	public static ValueSpecification buildBasicValue(Package parent, Classifier type, String value) {
		LiteralString valueSpec = MDDUtil.createLiteralString(parent, value);
		Stereotype basicValueStereotype = StereotypeUtils.findStereotype(BASIC_VALUE_STEREOTYPE);
		valueSpec.applyStereotype(basicValueStereotype);
		valueSpec.setValue(value);
		valueSpec.setValue(basicValueStereotype, "basicType", type);
		valueSpec.setType(type);
		return valueSpec;
	}

	public static ValueSpecification buildVertexLiteral(Package parent, Vertex vertex) {
		LiteralNull valueSpec = MDDUtil.createLiteralNull(parent);
		Stereotype vertexLiteralStereotype = StereotypeUtils.findStereotype(VERTEX_LITERAL_STEREOTYPE);
		valueSpec.applyStereotype(vertexLiteralStereotype);
		valueSpec.setValue(vertexLiteralStereotype, "vertex", vertex);
		valueSpec.setType(vertex.containingStateMachine());
		return valueSpec;
	}

	public static Activity createClosure(BehavioredClassifier parent, StructuredActivityNode context) {
		final Activity newClosure = (Activity) parent.createOwnedBehavior(null, Literals.ACTIVITY);
		Stereotype closureStereotype = StereotypeUtils.findStereotype(CLOSURE_STEREOTYPE);
		newClosure.applyStereotype(closureStereotype);
		newClosure.setValue(closureStereotype, "context", context);
		newClosure.setIsReadOnly(ActivityUtils.getOwningActivity(context).isReadOnly());
		return newClosure;
	}

	public static Activity createConstraintBehavior(BehavioredClassifier parent, Constraint constraint) {
		final Activity newConstraintBehavior = (Activity) parent.createOwnedBehavior(null, Literals.ACTIVITY);
		newConstraintBehavior.setIsReadOnly(true);
		newConstraintBehavior.setName(constraint.getName());
		Stereotype constraintStereotype = StereotypeUtils.findStereotype(CONSTRAINT_BEHAVIOR_STEREOTYPE);
		newConstraintBehavior.applyStereotype(constraintStereotype);
		newConstraintBehavior.setValue(constraintStereotype, CONSTRAINT_BEHAVIOR_CONSTRAINT_PROPERTY, constraint);
		return newConstraintBehavior;
	}

	public static boolean isWildcardType(Type toCheck) {
		return StereotypeUtils.hasStereotype(toCheck, WILDCARD_TYPE_STEREOTYPE);
	}

	public static Namespace getWildcardTypeContext(Type wildcardType) {
		Stereotype wildcardTypeStereotype = wildcardType.getAppliedStereotype(WILDCARD_TYPE_STEREOTYPE);
		return (Namespace) wildcardType.getValue(wildcardTypeStereotype, WILDCARD_TYPE_CONTEXT);
	}

	public static boolean isWildcardTypeContext(NamedElement toCheck) {
		return StereotypeUtils.hasStereotype(toCheck, WILDCARD_TYPE_CONTEXT_STEREOTYPE);
	}

	public static List<Type> getWildcardTypes(Namespace context) {
		Stereotype contextStereotype = context.getAppliedStereotype(WILDCARD_TYPE_CONTEXT_STEREOTYPE);
		return (List<Type>) context.getValue(contextStereotype, WILDCARD_TYPE_CONTEXT_TYPES);
	}

	public static String computeWildcardTypeName(String simpleName, Namespace context) {
		return "__wildcard_" + context.getName() + simpleName;
	}

	public static Class createWildcardType(Namespace context, String name) {
		Class wildcardType = ClassifierUtils.createClassifier(context, null, Literals.CLASS);
		if (context.getNearestPackage() != context) {
			ElementImport elementImport = context.createElementImport(wildcardType);
			elementImport.setAlias(name);
		}
		Stereotype constraintStereotype = StereotypeUtils.findStereotype(WILDCARD_TYPE_STEREOTYPE);
		wildcardType.applyStereotype(constraintStereotype);
		wildcardType.setValue(constraintStereotype, WILDCARD_TYPE_CONTEXT, context);

		Stereotype contextStereotype = StereotypeUtils.findStereotype(WILDCARD_TYPE_CONTEXT_STEREOTYPE);
		List<Type> newTypes;
		if (!context.isStereotypeApplied(contextStereotype)) {
			context.applyStereotype(contextStereotype);
			newTypes = new ArrayList<Type>();
		} else
			newTypes = new ArrayList<Type>(
					(List<Type>) context.getValue(contextStereotype, WILDCARD_TYPE_CONTEXT_TYPES));
		newTypes.add(wildcardType);
		context.setValue(contextStereotype, WILDCARD_TYPE_CONTEXT_TYPES, newTypes);
		return wildcardType;
	}

	public static boolean isConstraintBehavior(Behavior toCheck) {
		return StereotypeUtils.hasStereotype(toCheck, CONSTRAINT_BEHAVIOR_STEREOTYPE);
	}
	
	public static Constraint getBehaviorConstraint(Behavior toCheck) {
		return (Constraint) StereotypeUtils.getValue(toCheck, CONSTRAINT_BEHAVIOR_STEREOTYPE, CONSTRAINT_BEHAVIOR_CONSTRAINT_PROPERTY);
	}

	public static Type createSignature(Namespace namespace) {
		namespace = NamedElementUtils.findNearestNamespace(namespace, UMLPackage.Literals.PACKAGE,
				UMLPackage.Literals.CLASS);
		Interface signature = ClassifierUtils.createClassifier(namespace, null, Literals.INTERFACE);
		signature.createOwnedOperation("signatureOperation", null, null);
		Stereotype signatureStereotype = StereotypeUtils.findStereotype(SIGNATURE_STEREOTYPE);
		signature.applyStereotype(signatureStereotype);
		signature.setValue(signatureStereotype, SIGNATURE_CONTEXT, namespace);
		return signature;
	}

	public static Parameter createSignatureParameter(Type signature, String name, Type type) {
		Assert.isLegal(isSignature(signature));
		Operation signatureOperation = getSignatureOperation((Interface) signature);
		Parameter created = signatureOperation.createOwnedParameter(name, type);
        return created;
	}

	/**
	 * Creates a rule. A rule is a constraint that has a corresponding violation
	 * class.
	 * 
	 * @param constraint
	 * @param violationClass
	 */
	public static void makeRule(Constraint constraint, Classifier violationClass) {
		Stereotype ruleStereotype = StereotypeUtils.findStereotype(RULE_STEREOTYPE);
		constraint.applyStereotype(ruleStereotype);
		constraint.setValue(ruleStereotype, "violation", violationClass);
	}

	public static Classifier getRuleViolationClass(Constraint violated) {
		Stereotype ruleStereotype = violated.getAppliedStereotype(RULE_STEREOTYPE);
		return (Classifier) (ruleStereotype == null ? null : violated.getValue(ruleStereotype, "violation"));
	}

	public static Object getBasicValue(ValueSpecification specification) {
		Assert.isLegal(isBasicValue(specification));
		String stringValue = ((LiteralString) specification).getValue();
		Stereotype basicValueStereotype = specification.getAppliedStereotype(BASIC_VALUE_STEREOTYPE);
		Classifier basicType = (Classifier) specification.getValue(basicValueStereotype, "basicType");
		return BasicTypeUtils.buildBasicValue(basicType, stringValue);
	}

	public static Vertex resolveVertexLiteral(ValueSpecification specification) {
		Assert.isLegal(isVertexLiteral(specification));
		Stereotype vertexLiteralStereotype = specification.getAppliedStereotype(VERTEX_LITERAL_STEREOTYPE);
		return (Vertex) specification.getValue(vertexLiteralStereotype, "vertex");
	}

	public static StructuredActivityNode getClosureContext(Activity closure) {
		Assert.isTrue(isClosure(closure));
		Stereotype closureStereotype = closure.getAppliedStereotype(CLOSURE_STEREOTYPE);
		return (StructuredActivityNode) closure.getValue(closureStereotype, "context");
	}

	public static String getExternalClassName(Classifier classifier) {
		Assert.isLegal(isExternal(classifier));
		Stereotype externalStereotype = classifier.getAppliedStereotype(EXTERNAL_CLASS_STEREOTYPE);
		return (String) classifier.getValue(externalStereotype, "className");
	}

	public static Integer getLineNumber(Element element) {
		if (!hasDebugInfo(element))
			return null;
		Stereotype debuggableStereotype = element.getAppliedStereotype(DEBUGGABLE_STEREOTYPE);
		return (Integer) element.getValue(debuggableStereotype, "lineNumber");
	}

	private static Operation getSignatureOperation(Interface signature) {
		Assert.isLegal(isSignature(signature));
		return signature.getOperation("signatureOperation", null, null);
	}

	public static List<Parameter> getSignatureParameters(Type destination) {
		Assert.isTrue(isSignature(destination));
		return getSignatureOperation((Interface) destination).getOwnedParameters();
	}

	public static String getSource(Element element) {
		if (!hasDebugInfo(element))
			return null;
		Stereotype debuggableStereotype = element.getAppliedStereotype(DEBUGGABLE_STEREOTYPE);
		return (String) element.getValue(debuggableStereotype, "source");
	}

	public static boolean isBasicValue(ValueSpecification specification) {
		return specification instanceof LiteralString
				&& StereotypeUtils.hasStereotype(specification, BASIC_VALUE_STEREOTYPE);
	}

	public static boolean isVertexLiteral(ValueSpecification specification) {
		return specification instanceof LiteralNull
				&& StereotypeUtils.hasStereotype(specification, VERTEX_LITERAL_STEREOTYPE);
	}

	public static boolean isClosure(Element element) {
		return element instanceof Activity && StereotypeUtils.hasStereotype(element, CLOSURE_STEREOTYPE);
	}

	public static boolean isDebuggable(Element element) {
		return StereotypeUtils.isApplicable(element, DEBUGGABLE_STEREOTYPE);
	}

	public static boolean hasDebugInfo(Element element) {
		return StereotypeUtils.hasStereotype(element, DEBUGGABLE_STEREOTYPE);
	}

	public static boolean isEntryPoint(Operation operation) {
		return StereotypeUtils.hasStereotype(operation, ENTRY_POINT_STEREOTYPE);
	}

	public static boolean isExternal(Classifier classifier) {
		return StereotypeUtils.hasStereotype(classifier, EXTERNAL_CLASS_STEREOTYPE);
	}

	public static void makeExternal(Classifier currentClassifier) {
		Stereotype externalStereotype = StereotypeUtils.findStereotype(EXTERNAL_CLASS_STEREOTYPE);
		StereotypeUtils.safeApplyStereotype(currentClassifier, externalStereotype);
	}

	public static boolean isSignature(Element element) {
		return element instanceof Interface && StereotypeUtils.hasStereotype(element, SIGNATURE_STEREOTYPE);
	}
	
	public static boolean isStaticConstraint(Constraint constraint) {
		return Boolean.TRUE.equals(StereotypeUtils.getValue(constraint, CONTEXTUALIZED_CONSTRAINT_STEREOTYPE, CONTEXTUALIZED_CONSTRAINT_IS_STATIC_PROPERTY));
	}
	
	public static Constraint createConstraint(NamedElement constrainedElement, String constraintName, String stereotype) {
		boolean isStatic = !(constrainedElement instanceof Feature) || ((Feature) constrainedElement).isStatic();
		return createConstraint(constrainedElement, constraintName, StereotypeUtils.findStereotype(stereotype), isStatic);
	}

	public static Constraint createConstraint(NamedElement element, String constraintName, String stereotype, boolean isStatic) {
		return createConstraint(element, constraintName, StereotypeUtils.findStereotype(stereotype), isStatic);
	}

	public static Constraint createConstraint(NamedElement element, String constraintName,
			Stereotype invariantStereotype, boolean isStatic) {
		Namespace namespace = element instanceof Namespace ? ((Namespace) element) : element.getNamespace();
		Constraint invariant = namespace.createOwnedRule(constraintName);
		invariant.applyStereotype(invariantStereotype);
		setConstraintAsStatic(invariant, isStatic);
		invariant.getConstrainedElements().add(element);
		return invariant;
	}

	public static void setConstraintAsStatic(Constraint constraint, boolean isStatic) {
		EList<Stereotype> appliedStereotypes = constraint.getAppliedStereotypes();
		Stereotype stereotype = appliedStereotypes.stream().filter(it -> { 
			EList<Class> allExtendedMetaclasses = it.getAllExtendedMetaclasses();
			return allExtendedMetaclasses.stream().anyMatch(mc -> {
				boolean isConstraint = UMLPackage.Literals.CONSTRAINT.getName().equals(mc.getName());
				return isConstraint;
			});
		}).findAny().orElseThrow(() -> new IllegalArgumentException());
		constraint.setValue(stereotype, CONTEXTUALIZED_CONSTRAINT_IS_STATIC_PROPERTY, isStatic);
	}

	public static boolean isInvariant(Constraint constraint) {
		return constraint.getAppliedStereotype(INVARIANT_STEREOTYPE) != null;
	}

	public static boolean isAccess(Constraint constraint) {
		return constraint.getAppliedStereotype(ACCESS_STEREOTYPE) != null;
	}

	public static Constraint createAccessConstraint(NamedElement constrainedElement, String name,
			Collection<Class> roles, Collection<AccessCapability> allowed) {
		Stereotype accessStereotype = StereotypeUtils.findStereotype(ACCESS_STEREOTYPE);
		Enumeration accessCapabilityEnum = MDDCore.getInProgressRepository()
				.findNamedElement(ACCESS_CAPABILITY_ENUMERATION, Literals.ENUMERATION, null);
		boolean isStatic = allowed.stream().filter(it -> !it.isInstance()).findAny().isPresent();
		Constraint constraint = createConstraint(constrainedElement, name, accessStereotype, isStatic);
		constraint.setValue(accessStereotype, ACCESS_ALLOWED, toEnumerationLiterals(accessCapabilityEnum, allowed));
		constraint.setValue(accessStereotype, ACCESS_ROLES, new LinkedList<>(roles));
		return constraint;
	}

	public static List<Classifier> getAccessRoles(Constraint accessConstraint) {
		Object value = StereotypeUtils.getValue(accessConstraint, ACCESS_STEREOTYPE, ACCESS_ROLES);
		return value == null ? Collections.emptyList() : (List<Classifier>) value;
	}

	public static List<AccessCapability> getAllowedCapabilities(Constraint accessConstraint) {
		List<EEnumLiteral> value = (List<EEnumLiteral>) StereotypeUtils.getValue(accessConstraint, ACCESS_STEREOTYPE,
				ACCESS_ALLOWED);
		if (value.isEmpty())
		    // there is a constraint, but no capabilities were granted (as opposed to no constraints)
		    return Collections.singletonList(AccessCapability.None);
		return ClassifierUtils.fromEnumerationLiterals(AccessCapability.class, value);
	}

	public static List<Constraint> findAccessConstraints(NamedElement element) {
		return ConstraintUtils.findConstraints(element, ACCESS_STEREOTYPE);
	}

	public static List<Constraint> findInvariantConstraints(NamedElement element) {
		return ConstraintUtils.findConstraints(element, INVARIANT_STEREOTYPE);
	}

	public static boolean isRule(Constraint constraint) {
		return StereotypeUtils.hasStereotype(constraint, RULE_STEREOTYPE);
	}

	public static List<Constraint> findOwnedInvariantConstraints(Namespace modelClassifier) {
		List<Constraint> result = new ArrayList<Constraint>();
		for (Constraint constraint : modelClassifier.getOwnedRules())
			if (isInvariant(constraint))
				result.add(constraint);
		return result;
	}

	public static void makeObjectInitialization(StructuredActivityNode action) {
		Stereotype objectInitStereotype = StereotypeUtils.findStereotype(OBJECT_INITIALIZATION_STEREOTYPE);
		StereotypeUtils.safeApplyStereotype(action, objectInitStereotype);
	}

	public static void makeCast(StructuredActivityNode action) {
		Stereotype castStereotype = StereotypeUtils.findStereotype(CAST_STEREOTYPE);
		StereotypeUtils.safeApplyStereotype(action, castStereotype);
	}

	public static void makeDerivation(TypedElement context, Activity derivation) {
		Stereotype castStereotype = StereotypeUtils.findStereotype(DERIVATION_STEREOTYPE);
		StereotypeUtils.safeApplyStereotype(derivation, castStereotype);
		derivation.setValue(castStereotype, DERIVATION_CONTEXT, context);
	}

	public static boolean isDerivation(Activity toCheck) {
		return StereotypeUtils.hasStereotype(toCheck, DERIVATION_STEREOTYPE);
	}
	

	public static TypedElement getDerivationContext(Activity toCheck) {
		return (TypedElement) StereotypeUtils.getValue(toCheck, DERIVATION_STEREOTYPE, DERIVATION_CONTEXT);
	}

	public static boolean isCast(Action toCheck) {
		boolean isCast = StereotypeUtils.hasStereotype(toCheck, CAST_STEREOTYPE);
		return isCast;
	}

	public static boolean isObjectInitialization(Action toCheck) {
		return StereotypeUtils.hasStereotype(toCheck, OBJECT_INITIALIZATION_STEREOTYPE);
	}

	public static boolean isTestClass(Type toCheck) {
		return StereotypeUtils.hasStereotype(toCheck, "Test");
	}
	
    public static boolean isTestCase(Operation op) {
    	if (op.getVisibility() != VisibilityKind.PUBLIC_LITERAL)
    		return false;
    	if (!op.getOwnedParameters().isEmpty())
    		return false;
    	if (op.isStatic())
    		return false;
        return op.getClass_() != null && isTestClass(op.getClass_());
    }
    public static boolean isLibrary(Package toCheck) {
    	return StereotypeUtils.hasStereotype(toCheck, LIBRARY_STEREOTYPE) || toCheck.isModelLibrary();
    }

    public static boolean isApplication(Package toCheck) {
    	return StereotypeUtils.hasStereotype(toCheck, APPLICATION_STEREOTYPE) && !(toCheck instanceof Profile);
    }

	public static void makeApplication(Package package_) {
		Stereotype applicationStereotype = StereotypeUtils.findStereotype(APPLICATION_STEREOTYPE); 
		StereotypeUtils.safeApplyStereotype(package_, applicationStereotype);
	}
    
	public static void makeRole(Class class_) {
		Stereotype applicationStereotype = StereotypeUtils.findStereotype(ROLE_CLASS_STEREOTYPE);
		StereotypeUtils.safeApplyStereotype(class_, applicationStereotype);
	}

	public static boolean isRoleClass(Type toCheck) {
		return StereotypeUtils.hasStereotype(toCheck, ROLE_CLASS_STEREOTYPE);
	}

	public static boolean isSystemUserClass(Type toCheck) {
		return ClassifierUtils.isKindOf((Classifier) toCheck, SYSTEM_USER_CLASS);
	}

	public static boolean hasExtensionsApplied(Package toCheck) {
		return StereotypeUtils.hasProfile(toCheck, EXTENSIONS_PROFILE);
	}
}
