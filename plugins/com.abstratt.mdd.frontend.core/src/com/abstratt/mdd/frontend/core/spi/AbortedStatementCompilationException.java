package com.abstratt.mdd.frontend.core.spi;

/**
 * This runtime exception is used internally by compilers to abort compilation
 * of a statement when an unrecoverable parsing error is found.
 */
public class AbortedStatementCompilationException extends AbortedScopeCompilationException {
    private static final long serialVersionUID = 1L;
}
