package com.abstratt.mdd.core;
public enum Step {
    /** Package structuring */
    PACKAGE_STRUCTURE(true),
    AFTER_PACKAGE_STRUCTURE(false),
    /** Import packages, profile definitions. */
    PACKAGE_IMPORTS(false),
    /** type references, default resolution step */
    GENERAL_RESOLUTION(false),
    AFTER_GENERAL_RESOLUTION(false),
    /** Use sparingly, for read-only validation of the structural model. */
    STRUCTURE_VALIDATION(false),
    /** Profile definitions */
    DEFINE_PROFILES(false),
    /** profile applications */
    PROFILE_APPLICATIONS(false),
    /** stereotype applications */
    STEREOTYPE_APPLICATIONS(false),
    AFTER_STRUCTURE(false),
    /** Meant for activity compilation. */
    BEHAVIOR(false),
    /** Validations that do not prevent a model from being built. */
    WARNINGS(false);
    private boolean ordered;

    /**
     * Whether references in this stage are supposed to be comparable to
     * each other.
     */
    public boolean isOrdered() {
        return ordered;
    }

    private Step(boolean ordered) {
        this.ordered = ordered;
    }
}
