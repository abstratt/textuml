package com.abstratt.mdd.frontend.core;

import com.abstratt.mdd.core.Problem;

public class RequiredPortHasNoMatchingProviderPort extends Problem {

	private String portName;

	public RequiredPortHasNoMatchingProviderPort(String symbolName) {
		super(Severity.ERROR);
		this.portName = symbolName;
	}

	public String getMessage() {
		return "Could not find provider for port: '" + portName + "'";
	}

}
