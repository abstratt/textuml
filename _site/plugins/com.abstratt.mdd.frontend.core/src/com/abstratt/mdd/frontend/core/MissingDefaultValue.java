package com.abstratt.mdd.frontend.core;

public class MissingDefaultValue extends Problem {

	public MissingDefaultValue() {
		super(Severity.ERROR);
	}

	public String getMessage() {
		return "Derived attributes must have a default value";
	}

}
