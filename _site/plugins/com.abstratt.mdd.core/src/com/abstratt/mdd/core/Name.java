/*******************************************************************************
 * Copyright (c) 2008, 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.core;

import org.eclipse.uml2.uml.NamedElement;

/**
 * A name of a UML named element.
 */
public final class Name {

	private static final String SEPARATOR = NamedElement.SEPARATOR;
	private final static String ESCAPED_SEPARATOR = "\\" + SEPARATOR;
	private String[] segments;
	private int start;
	private int end;

	/**
	 * Creates a name from the given unparsed string.
	 * 
	 * @param toParse
	 *            a non-empty (after trimmed) string
	 */
	public Name(String toParse) {
		this.segments = parseSegments(toParse);
		this.start = 0;
		this.end = segments.length;
	}

	public Name(String[] source) {
		if (source.length == 0)
			throw new IllegalArgumentException("name is empty");
		this.segments = new String[source.length];
		System.arraycopy(source, 0, this.segments, 0, source.length);
		this.start = 0;
		this.end = this.segments.length;
	}

	private Name(String[] source, int start, int end) {
		if (start < 0 || end - start <= 0 || end > source.length)
			throw new IllegalArgumentException("invalid range: start: " + start + ", end: " + end);
		this.segments = source;
		this.start = start;
		this.end = end;
	}

	private static String[] parseSegments(String fullName) {
		if (fullName.trim().length() == 0)
			throw new IllegalArgumentException("name is empty");
		fullName = fullName.replace(ESCAPED_SEPARATOR, "\u0000");
		String[] segments = fullName.split(SEPARATOR);
		for (int i = 0; i < segments.length; i++)
			segments[i] = segments[i].replace("\u0000", SEPARATOR);
		return segments;
	}

	/**
	 * Returns the number of segments in this name.
	 * 
	 * @return the number of segments, a positive number
	 */
	public int segmentCount() {
		return end - start;
	}

	/**
	 * Returns the string segment with the given index.
	 * 
	 * @param index
	 *            the index of the segment to retrieve
	 * @return the chosen string segment
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the index is not valid
	 */
	public String segment(int index) {
		return segments[start + index];
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(segments[start]);
		for (int i = start + 1; i < end; i++) {
			result.append(SEPARATOR);
			result.append(segments[i]);
		}
		return result.toString();
	}

	public Name removeLastSegment() {
		return new Name(segments, start, end - 1);
	}

	public String lastSegment() {
		return segments[end - 1];
	}

	public String firstSegment() {
		return segments[start];
	}

	public boolean isQualified() {
		return segmentCount() > 1;
	}
}