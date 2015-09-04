package com.abstratt.mdd.modelrenderer.uml2dot;

/**
 * Defines preference keys and corresponding values.
 */
public class UML2DOTPreferences {

    public enum ShowClassifierCompartmentForPackageOptions {
        Current, Immediate, Local, Any;
        public static String KEY = SHOW_CLASSIFIER_COMPARTMENT_FOR_PACKAGE;
    }

    public enum ShowClassifierCompartmentOptions {
        NotEmpty, Never, Always;
        public static String KEY = SHOW_CLASSIFIER_COMPARTMENT;
    }

    public enum ShowCrossPackageElementOptions {
        Never, Immediate, Local, Always;
        public static String KEY = SHOW_ELEMENTS_IN_OTHER_PACKAGES;
    }

    public static final String SHOW_ASSOCIATION_END_OWNERSHIP = "showAssociationEndOwnership";
    public static final String SHOW_STRUCTURAL_FEATURE_VISIBILITY = "showStructuralFeatureVisibility";
    public static final String SHOW_ASSOCIATION_END_MULTIPLICITY = "showAssociationEndMultiplicity";
    public static final String SHOW_ASSOCIATION_NAME = "showAssociationName";
    public static final String SHOW_ASSOCIATION_END_NAME = "showAssociationEndName";
    /**
     * Lack of constraints may crash Graphviz or prevent associations from
     * showing.
     */
    public static final String OMIT_CONSTRAINTS_FOR_NAVIGABILITY = "omitConstraintsForNavigability";
    public static final String SHOW_PARAMETER_DIRECTION = "showParameterDirection";
    public static final String SHOW_PARAMETER_NAMES = "showParameterNames";
    public static final String SHOW_COMMENTS = "showComments";
    public static final String SHOW_PARAMETERS = "showParameters";
    public static final String SHOW_RETURN_PARAMETER = "showReturnParameter";
    public static final String SHOW_ELEMENTS_IN_OTHER_PACKAGES = "showElementsInOtherPackage";
    public static final String SHOW_CLASSIFIER_COMPARTMENT = "showClassifierCompartments";
    public static final String SHOW_CLASSIFIER_COMPARTMENT_FOR_PACKAGE = "showClassifierCompartmentForPackage";
    public static final String SHOW_ELEMENTS_IN_LIBRARIES = "showLibraries";
    public static final String SHOW_PRIMITIVES = "showPrimitives";
    public static final String SHOW_CLASSES = "showClasses";
    public static final String SHOW_INTERFACES = "showInterfaces";
    public static final String SHOW_STATEMACHINES = "showStateMachines";
    public static final String SHOW_ATTRIBUTES = "showAttributes";
    public static final String SHOW_OPERATIONS = "showOperations";
    public static final String SHOW_DATATYPES = "showDataTypes";
    public static final String SHOW_ENUMERATIONS = "showEnumerations";
    public static final String SHOW_SIGNALS = "showSignals";
    public static final String SHOW_CLASSIFIER_STEREOTYPES = "showClassifierStereotypes";
    public static final String SHOW_FEATURE_STEREOTYPES = "showFeatureStereotypes";
    public static final String SHOW_RELATIONSHIP_STEREOTYPES = "showRelationshipStereotypes";
}
