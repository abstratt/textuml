package com.abstratt.mdd.frontend.core.spi;

/**
 * This runtime exception is used internally by compilers to abort compilation
 * of an activity when an unrecoverable parsing error is found.
 */
public class AbortedScopeCompilationException extends AbortedCompilationException {
    private static final long serialVersionUID = 1L;
}
