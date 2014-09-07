package com.abstratt.mdd.frontend.core.builder;

import java.util.HashMap;
import java.util.Map;

public class NameReference {
	private Integer column;
	private UML2ProductKind elementType;
	private Integer line;
	private String location;
	private String name;
	private Map<String, Object> properties;

	public NameReference(String name, UML2ProductKind elementType) {
		this.name = name;
		this.elementType = elementType;
	}

	protected Integer getColumn() {
		return column;
	}

	public UML2ProductKind getElementType() {
		return elementType;
	}

	protected Integer getLine() {
		return line;
	}

	public String getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public void setColumn(Integer column) {
		this.column = column;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setProperty(String name, Object value) {
		if (properties == null)
			properties = new HashMap<String, Object>();
		properties.put(name, value);
	}
	
	public Object getProperty(String name) {
		return properties == null ? null : properties.get(name);
	}
}
