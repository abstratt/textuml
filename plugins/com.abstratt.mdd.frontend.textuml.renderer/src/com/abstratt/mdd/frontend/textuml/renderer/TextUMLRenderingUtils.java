/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Vladimir Sosnin - #2798743
 *******************************************************************************/
package com.abstratt.mdd.frontend.textuml.renderer;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.abstratt.mdd.frontend.textuml.core.TextUMLConstants;

public class TextUMLRenderingUtils {
	public static String renderMultiplicity(MultiplicityElement multiple, boolean brackets) {
		if (!multiple.isMultivalued() && multiple.getLower() == 0)
			return "";
		String constraints = "";
		if (multiple.isMultivalued()) {
			if (multiple.isOrdered() && multiple.isUnique())
				constraints = "{ordered}";
			else if (multiple.isOrdered() && !multiple.isUnique())
				constraints = "{ordered, nonunique}";
			else if (!multiple.isOrdered() && !multiple.isUnique())
				constraints = "{nonunique}";
		}
		if (multiple.lowerBound() == multiple.upperBound()) {
			if (multiple.upperBound() == -1)
				return wrapInBrackets("*", brackets) + constraints;
			else if (multiple.upperBound() != 1) {
				return wrapInBrackets(Integer.toString(multiple.upperBound()), brackets) + constraints;
			}
			return "";
		}
		StringBuffer interval = new StringBuffer();
		interval.append(multiple.lowerBound());
		interval.append(", ");
		interval.append(multiple.upperBound() == -1 ? "*" : multiple.upperBound());
		return wrapInBrackets(interval.toString(), brackets) + constraints;
	}

	private static String wrapInBrackets(String original, boolean useBrackets) {
		return useBrackets ? "[" + original + "]" : " " + original;
	}

	public static String renderVisibility(VisibilityKind visibility) {
		if (visibility == null)
			return "";
		return visibility.getName() + " ";
	}

	public static void renderStereotypeApplications(PrintWriter writer, NamedElement element) {
		renderStereotypeApplications(writer, element, true);
	}

	public static void renderStereotypeApplications(PrintWriter writer, NamedElement element, boolean newLine) {
		List<EObject> applications = element.getStereotypeApplications();
		if (applications.isEmpty())
			return;
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (EObject application : applications) {
			renderStereotypeApplication(element, builder, application);
			builder.append(", ");
		}
		builder.delete(builder.length() - 2, builder.length());
		builder.append("]");
		writer.print(builder);
		if (newLine)
			writer.println();
		else
			writer.print(' ');
	}

	/**
	 * Produces the name of the type of the typed element including
	 * multiplicity.
	 */
	public static String getQualifiedNameIfNeeded(TypedElement typedElement) {
		StringBuilder builder = new StringBuilder();
		String typeReference;
		final Type type = typedElement.getType();
		final Namespace namespace = typedElement.getNamespace();
		typeReference = getQualifiedNameIfNeeded(type, namespace);
		builder.append(typeReference);
		if (typedElement instanceof MultiplicityElement)
			builder.append(TextUMLRenderingUtils.renderMultiplicity((MultiplicityElement) typedElement, true));
		return builder.toString();
	}

	public static String getQualifiedNameIfNeeded(final Type type, final Namespace namespace) {
		String typeReference;
		if (type != null && type.getName() != null) {
			typeReference = getQualifiedNameIfNeeded((NamedElement) type, namespace);
		} else
			typeReference = "any";
		return typeReference;
	}

	public static String getQualifiedNameIfNeeded(NamedElement element, Namespace currentNamespace) {
		if (element.getName() == null)
			return "unnamed";
		if (currentNamespace == null)
			return qualifiedName(element);
		if (element.getNamespace() == currentNamespace)
			return name(element);
		if (element.getNamespace() instanceof Package
		        && currentNamespace.getImportedPackages().contains(element.getNamespace()))
			return name(element);
		if (currentNamespace.getImportedElements().contains(element))
			return name(element);
		Namespace parentNamespace = currentNamespace.getNamespace();
		return parentNamespace == null ? qualifiedName(element) : getQualifiedNameIfNeeded(element, parentNamespace);
	}

	private static void renderStereotypeApplication(NamedElement element, StringBuilder builder, EObject application) {
		Stereotype stereotype = UMLUtil.getStereotype(application);
		builder.append(getQualifiedNameIfNeeded(stereotype, (Namespace) application.eContainer()));
		builder.append("(");
		int mark = builder.length();
		for (Property stereotypeProperty : stereotype.getAllAttributes()) {
			if (stereotypeProperty.getAssociation() instanceof Extension)
				continue;
			Object taggedValue = element.getValue(stereotype, stereotypeProperty.getName());
			if (taggedValue == null)
				continue;
			if (taggedValue instanceof Element)
				if (!(taggedValue instanceof NamedElement))
					continue;
			// default value for required property of type Integer is 0
			// so don't show it
			if (taggedValue instanceof Integer && stereotypeProperty.getLower() >= 1
			        && ((Integer) taggedValue).equals(0))
				continue;
			// don't show default values for stereotype properties
			String defaultValue = stereotypeProperty.getDefault();
			if (defaultValue != null && defaultValue.equals(taggedValue.toString())) {
				continue;
			}
			builder.append(name(stereotypeProperty));
			builder.append("=");
			boolean isString = taggedValue instanceof String;
			if (isString)
				builder.append('"');
			if (taggedValue instanceof NamedElement)
				taggedValue = ((NamedElement) taggedValue).getName();
			builder.append(taggedValue);
			if (isString)
				builder.append('"');
			builder.append(",");
		}
		if (builder.length() > mark) {
			builder.delete(builder.length() - 1, builder.length());
			builder.append(")");
		} else
			builder.delete(mark - 1, mark);
	}

	private static String escapeIdentifierIfNeeded(String identifier) {
		if (StringUtils.isBlank(identifier))
			return "";
		if (!StringUtils.isAlpha(identifier)) {
			StringBuffer result = new StringBuffer(identifier.length());
			char[] chars = identifier.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] != '_'
				        && ((Character.isDigit(chars[i]) && i == 0) || !Character.isLetterOrDigit(chars[i])))
					result.append('\\');
				result.append(chars[i]);
			}
			return result.toString();
		}
		if (Arrays.binarySearch(TextUMLConstants.KEYWORDS, identifier) >= 0)
			return "\\" + identifier;
		return identifier;
	}

	public static String name(NamedElement named) {
		return escapeIdentifierIfNeeded(named.getName());
	}

	public static String qualifiedName(NamedElement named) {
		StringBuffer qualifiedName = new StringBuffer(name(named));
		for (Namespace namespace : named.allNamespaces()) {
			String namespaceName = name(namespace);
			if (namespaceName == null || namespaceName.isEmpty())
				return null;
			qualifiedName.insert(0, NamedElement.SEPARATOR);
			qualifiedName.insert(0, namespaceName);
		}
		return qualifiedName.toString();
	}
}
