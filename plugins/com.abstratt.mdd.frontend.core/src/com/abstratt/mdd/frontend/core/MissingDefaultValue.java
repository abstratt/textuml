package com.abstratt.mdd.frontend.core;

import com.abstratt.mdd.core.Problem;

public class MissingDefaultValue extends Problem {

	public MissingDefaultValue() {
		super(Severity.ERROR);
	}

	public String getMessage() {
		return "Derived attributes must have a default value";
	}

}
