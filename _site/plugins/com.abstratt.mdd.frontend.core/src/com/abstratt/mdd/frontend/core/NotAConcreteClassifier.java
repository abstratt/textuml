package com.abstratt.mdd.frontend.core;


public class NotAConcreteClassifier extends Problem {

	private String classifierName;

	public NotAConcreteClassifier(String classifierName) {
		super(Severity.ERROR);
		this.classifierName = classifierName;
	}

	public String getMessage() {
		return "Not a concrete classifier: '" + classifierName + "'";
	}

}
