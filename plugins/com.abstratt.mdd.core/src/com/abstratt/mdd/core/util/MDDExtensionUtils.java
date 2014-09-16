package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.LiteralNull;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.Vertex;

public class MDDExtensionUtils {
	private static final String BASIC_VALUE_STEREOTYPE = "mdd_extensions::BasicValue";
	private static final String VERTEX_LITERAL_STEREOTYPE = "mdd_extensions::VertexLiteral";
	private static final String CLOSURE_STEREOTYPE = "mdd_extensions::Closure";
	private static final String CONSTRAINT_BEHAVIOR_STEREOTYPE = "mdd_extensions::ConstraintBehavior";
	private static final String DEBUGGABLE_STEREOTYPE = "mdd_extensions::Debuggable";
	private static final String ENTRY_POINT_STEREOTYPE = "mdd_extensions::EntryPoint";
	private static final String EXTERNAL_CLASS_STEREOTYPE = "mdd_extensions::External";
	private static final String OBJECT_INITIALIZATION_STEREOTYPE = "mdd_extensions::ObjectInitialization";
	private static final String ATTRIBUTE_INITIALIZATION_STEREOTYPE = "mdd_extensions::AttributeInitialization";
	private static final String PERSISTENT_STEREOTYPE = "mdd_extensions::Persistent";
	private static final String SIGNATURE_STEREOTYPE = "mdd_extensions::Signature";
	private static final String RULE_STEREOTYPE = "mdd_extensions::Rule";
	public static final String INVARIANT_STEREOTYPE = "mdd_extensions::Invariant";
	public static final String ACCESS_STEREOTYPE = "mdd_extensions::Access";
	private static final String META_REFERENCE_STEREOTYPE = "mdd_extensions::MetaReference";

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
        return specification instanceof LiteralNull && StereotypeUtils.hasStereotype(specification, META_REFERENCE_STEREOTYPE);	 
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
		final Activity newClosure =
			(Activity) parent.createOwnedBehavior(null,
							Literals.ACTIVITY);
		Stereotype closureStereotype = StereotypeUtils.findStereotype(CLOSURE_STEREOTYPE);
		newClosure.applyStereotype(closureStereotype);
		newClosure.setValue(closureStereotype, "context", context);
		newClosure.setIsReadOnly(ActivityUtils.getOwningActivity(context).isReadOnly());
		return newClosure;
	}
	
   public static Activity createConstraintBehavior(BehavioredClassifier parent, Constraint constraint) {
        final Activity newConstraintBehavior =
            (Activity) parent.createOwnedBehavior(null,
                            Literals.ACTIVITY);
        newConstraintBehavior.setIsReadOnly(true);
        newConstraintBehavior.setName(constraint.getName());
        Stereotype constraintStereotype = StereotypeUtils.findStereotype(CONSTRAINT_BEHAVIOR_STEREOTYPE);
        newConstraintBehavior.applyStereotype(constraintStereotype);
        newConstraintBehavior.setValue(constraintStereotype, "constraint", constraint);
        return newConstraintBehavior;
    }
   
	public static Type createSignature(Package nearestPackage) {
		Interface signature = (Interface) nearestPackage.createOwnedType(null, Literals.INTERFACE);
		signature.createOwnedOperation("signatureOperation", null, null);
		Stereotype signatureStereotype = StereotypeUtils.findStereotype(SIGNATURE_STEREOTYPE);
		signature.applyStereotype(signatureStereotype);
		return signature;
	}
	
	public static Parameter createSignatureParameter(Type signature,
			String name, Type type) {
		Assert.isLegal(isSignature(signature));
		Operation signatureOperation = getSignatureOperation((Interface) signature);
		return signatureOperation.createOwnedParameter(name, type);
	}
	
	/**
	 * Creates a rule. A rule is a constraint that has a corresponding violation class.
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
	
	public static Vertex getVertexLiteral(ValueSpecification specification) {
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
		if (!isDebuggable(element))
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
		if (!isDebuggable(element))
			return null;
		Stereotype debuggableStereotype = element.getAppliedStereotype(DEBUGGABLE_STEREOTYPE);
		return (String) element.getValue(debuggableStereotype, "source");
	}

	public static boolean isBasicValue(ValueSpecification specification) {
		return specification instanceof LiteralString && StereotypeUtils.hasStereotype(specification, BASIC_VALUE_STEREOTYPE);
	}
	
	public static boolean isVertexLiteral(ValueSpecification specification) {
		return specification instanceof LiteralNull && StereotypeUtils.hasStereotype(specification, VERTEX_LITERAL_STEREOTYPE);
	}

	public static boolean isClosure(Element owner) {
		return owner instanceof Activity && StereotypeUtils.hasStereotype(owner, CLOSURE_STEREOTYPE);
	}

	public static boolean isDebuggable(Element element) {
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
	
	public static boolean isPersistent(Classifier classifier) {
		return StereotypeUtils.hasStereotype(classifier, PERSISTENT_STEREOTYPE);
	}
	
	public static boolean isSignature(Element element) {
		return element instanceof Interface && StereotypeUtils.hasStereotype(element, SIGNATURE_STEREOTYPE);
	}

	public static Constraint createConstraint(NamedElement element, String constraintName, String stereotype) {
		Namespace namespace = element instanceof Namespace ? ((Namespace) element) : element.getNamespace();
		Constraint invariant = namespace.createOwnedRule(constraintName);
		Stereotype invariantStereotype = StereotypeUtils.findStereotype(stereotype);
		invariant.applyStereotype(invariantStereotype);
		invariant.getConstrainedElements().add(element);
		return invariant;
	}
	
	protected static boolean isInvariant(Constraint constraint) {
		return constraint.getAppliedStereotype(INVARIANT_STEREOTYPE) != null;
	}
	
	protected static boolean isAccess(Constraint constraint) {
		return constraint.getAppliedStereotype(ACCESS_STEREOTYPE) != null;
	}
	
	public static List<Constraint> findConstraints(NamedElement element, String stereotype) {
		List<Constraint> result = new ArrayList<Constraint>();
		Namespace namespace = element instanceof Namespace ? (Namespace) element : element.getNamespace();
		for (Constraint invariant : namespace.getOwnedRules()) {
	        Stereotype invariantStereotype = invariant.getAppliedStereotype(stereotype);
	        if (invariantStereotype != null && invariant.getConstrainedElements().contains(element))
	        	result.add(invariant);
		}
		return result;
	}

	public static List<Constraint> findAccessConstraints(NamedElement element) {
		return findConstraints(element, ACCESS_STEREOTYPE);
	}
	
	public static List<Constraint> findInvariantConstraints(NamedElement element) {
		return findConstraints(element, INVARIANT_STEREOTYPE);
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

	public static NamedElement getInvariantScope(Constraint violated) {
		if (violated.getAppliedStereotype(INVARIANT_STEREOTYPE) == null)
			return null;
		if (violated.getConstrainedElements().size() == 1 && violated.getConstrainedElements().get(0) instanceof NamedElement)
			return (NamedElement) violated.getConstrainedElements().get(0);
		return null;
	}
}