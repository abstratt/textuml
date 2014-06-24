package com.abstratt.mdd.frontend.core;

public class CannotSpecializeClassifier extends Problem {

	private String general;
	private String specific;

	public CannotSpecializeClassifier(String general, String specific) {
		super(Severity.ERROR);
		this.general = general;
		this.specific = specific;
	}

	public String getMessage() {
		return specific + " cannot specialize " + general;
	}

}
