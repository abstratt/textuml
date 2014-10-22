package com.abstratt.mdd.core;

public class ModelException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final IProblem problem;

    public ModelException(IProblem problem) {
        this.problem = problem;
    }
    
    public IProblem getProblem() {
        return problem;
    }
    
    @Override
    public String getMessage() {
        return problem.getMessage();
    }
}
