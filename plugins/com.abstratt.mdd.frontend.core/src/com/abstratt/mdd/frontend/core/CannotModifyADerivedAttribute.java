package com.abstratt.mdd.frontend.core;

public class CannotModifyADerivedAttribute extends Problem {

	public CannotModifyADerivedAttribute() {
		super(Severity.ERROR);
	}

	public String getMessage() {
		return "Cannot modify a derived attribute";
	}

}
