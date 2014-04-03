package com.abstratt.mdd.frontend.core;

public class CannotLoadFromLocation extends Problem {

	private String uri;
	
	public CannotLoadFromLocation(String uri) {
		super(Severity.ERROR);
		this.uri = uri;
	}

	@Override
	public String getMessage() {
		return "Could not load URI: '" + uri + "'";
	}

}
