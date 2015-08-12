/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public abstract class Problem implements IProblem, Comparable<IProblem> {

	private Map<String, Object> attributes = Collections.emptyMap();
	private Severity severity;

	public Problem(Severity severity) {
		this.severity = severity;
	}

	public Problem() {
		this(Severity.ERROR);
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setAttribute(String key, Object value) {
		if (value == null) {
			attributes.remove(key);
			return;
		}
		if (attributes.isEmpty())
			attributes = new TreeMap<String, Object>();
		attributes.put(key, value);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(severity);
		buffer.append(": ");
		for (Iterator i = attributes.entrySet().iterator(); i.hasNext();) {
			Map.Entry current = (Map.Entry) i.next();
			buffer.append(current.getKey());
			buffer.append(": ");
			buffer.append(current.getValue());
			buffer.append(" - ");
		}
		buffer.append(getMessage());
		return buffer.toString();
	}

	@Override
	public int compareTo(IProblem o) {
		Integer thisLine = (Integer) getAttribute(LINE_NUMBER);
		Integer otherLine = (Integer) o.getAttribute(LINE_NUMBER);
		if (thisLine == otherLine)
			return 0;
		if (thisLine == null)
			return -1;
		if (otherLine == null)
			return 1;
		return thisLine - otherLine;
	}
}
