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
package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.TemplateBinding;
import org.eclipse.uml2.uml.TemplateSignature;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.IRepository;

public class FeatureUtils {
	public static boolean isParametrizedConstraint(Constraint constraint) {
		Behavior toExecute = ActivityUtils.resolveBehaviorReference(constraint.getSpecification());
		return toExecute.getOwnedParameters().size() > 1;
	}
	
	public static List<Parameter> filterParameters(List<Parameter> original,
			ParameterDirectionKind... direction) {
		List<ParameterDirectionKind> directionList = Arrays.asList(direction);
		List<Parameter> filtered = new ArrayList<Parameter>(original.size());
		for (Parameter parameter : original)
			if (directionList.contains(parameter.getDirection()))
				filtered.add(parameter);
		return filtered;
	}
	
	public static Parameter findReturnParameter(List<Parameter> original) {
		List<Parameter> found = filterParameters(original, ParameterDirectionKind.RETURN_LITERAL);
		return found.isEmpty() ? null : found.get(0);
	}

	public static Operation findOperation(IRepository repository,
			Classifier classifier, String operationName,
			List<TypedElement> arguments) {
		return findOperation(repository, classifier, operationName, arguments,
				null, false, true);
	}

	public static Operation findOperation(IRepository repository,
			Classifier classifier, String operationName,
			List<TypedElement> arguments, boolean ignoreCase, boolean recurse) {
		return findOperation(repository, classifier, operationName, arguments,
				null, ignoreCase, recurse);
	}
	
	public static BehavioralFeature findCompatibleOperation(
			IRepository repository, Classifier classifier,
			Operation operation) {
		List<TypedElement> parameters = new ArrayList<TypedElement>();
		for (Parameter parameter : FeatureUtils.filterParameters(operation.getOwnedParameters(), ParameterDirectionKind.IN_LITERAL))
			parameters.add(parameter);
		return findOperation(repository, classifier, operation.getName(), parameters, null, false, true);
	}


	/**
	 * Finds an operation in the given classifier.
	 * 
	 * @param arguments the arguments to match, or null for name-based matching only
	 */
	public static Operation findOperation(IRepository repository,
			Classifier classifier, String operationName,
			List<TypedElement> arguments,
			ParameterSubstitutionMap substitutions, boolean ignoreCase,
			boolean recurse) {
		for (Operation operation : classifier.getOperations()) {
			if (isNameMatch(operation, operationName, ignoreCase)
					&& (arguments == null || isMatch(repository, operation, arguments, substitutions)))
				return operation;
		}
		if (!recurse)
			return null;
		Operation found;
		final EList<TemplateBinding> templateBindings = classifier
				.getTemplateBindings();
		if (!templateBindings.isEmpty())
			for (TemplateBinding templateBinding : templateBindings) {
				TemplateSignature signature = templateBinding.getSignature();
				ParameterSubstitutionMap newSubstitutions = new ParameterSubstitutionMap(
						templateBinding);
				found = findOperation(repository,
						(Classifier) signature.getTemplate(), operationName,
						arguments, newSubstitutions, ignoreCase, true);
				if (found != null)
					return found;
			}
		for (Generalization generalization : classifier.getGeneralizations())
			if ((found = findOperation(repository, generalization.getGeneral(),
					operationName, arguments, substitutions, ignoreCase, true)) != null)
				return found;
		// recurse to owning classifier
		for (Namespace owner = (Namespace) classifier.getOwner();owner instanceof Classifier; owner = (Namespace) owner.getOwner())
			if ((found = findOperation(repository, (Classifier) owner,
					operationName, arguments, substitutions, ignoreCase, true)) != null)
				return found;
		// fallback to interfaces realized
		if (classifier instanceof BehavioredClassifier) {
			BehavioredClassifier asBehaviored = (BehavioredClassifier) classifier;
			for (Interface implemented : asBehaviored.getImplementedInterfaces())
				if ((found = findOperation(repository, implemented,
						operationName, arguments, substitutions, ignoreCase, true)) != null)
					return found;
		}

		return null;
	}

	public static boolean isMatch(IRepository repository, Operation operation,
			List<TypedElement> arguments, ParameterSubstitutionMap substitutions) {
		if (arguments == null)
			return true;
		List<Parameter> operationParameters = filterParameters(operation
				.getOwnedParameters(), ParameterDirectionKind.IN_LITERAL);
		if (arguments.size() != operationParameters.size())
			return false;
		for (Iterator<?> argIter = arguments.iterator(), parIter = operationParameters
				.iterator(); argIter.hasNext();) {
			Parameter parameter = (Parameter) parIter.next();
			TypedElement argument = (TypedElement) argIter.next();
			if (!TypeUtils.isCompatible(repository, argument, parameter,
					substitutions))
				return false;
		}
		return true;
	}

	public static boolean isNameMatch(NamedElement element, String elementName,
			boolean ignoreCase) {
		return element.getName().equals(elementName)
				|| (ignoreCase && element.getName().equalsIgnoreCase(
						elementName));

	}

	public static Property findAttribute(
			Classifier classifier, String attributeIdentifier,
			boolean ignoreCase, boolean recurse) {
		return findProperty(classifier, attributeIdentifier, ignoreCase, recurse, null);
	}
	
	public static Port findPort(
			Classifier classifier, String portIdentifier,
			boolean ignoreCase, boolean recurse) {
		return (Port) findProperty(classifier, portIdentifier, ignoreCase, recurse, UMLPackage.Literals.PORT);
	}
	
	public static Property findProperty(
			Classifier classifier, String attributeIdentifier,
			boolean ignoreCase, boolean recurse, EClass propertyClass) {
		Property found = classifier.getAttribute(attributeIdentifier, null,
				ignoreCase, propertyClass);
		if (found != null || !recurse)
			return found;
		final EList<TemplateBinding> templateBindings = classifier
				.getTemplateBindings();
		if (!templateBindings.isEmpty()) {
			for (TemplateBinding templateBinding : templateBindings) {
				TemplateSignature signature = templateBinding.getSignature();
				found = findProperty((Classifier) signature
						.getTemplate(), attributeIdentifier, ignoreCase, true, propertyClass);
				if (found != null)
					return found;
			}
		}
		for (Generalization generalization : classifier.getGeneralizations())
			if ((found = findProperty(generalization.getGeneral(),
					attributeIdentifier, ignoreCase, true, propertyClass)) != null)
				return found;
		return null;
	}
	
	public static List<Property> getSimpleAttributes(Classifier classifier) {
		List<Property> simpleAttributes = new ArrayList<Property>();
		for (Property current : classifier.getAllAttributes())
			if (current.getAssociation() == null)
				simpleAttributes.add(current);
		return simpleAttributes;
	}

	public static boolean isChild(Property property) {
        return property.getAggregation() != AggregationKind.NONE_LITERAL;
	}

	public static List<Parameter> getInputParameters(List<Parameter> ownedParameters) {
		return filterParameters(ownedParameters, ParameterDirectionKind.IN_LITERAL, ParameterDirectionKind.INOUT_LITERAL);
	}

	public static Parameter getReturnParameter(EList<Parameter> ownedParameters) {
		List<Parameter> found = filterParameters(ownedParameters, ParameterDirectionKind.RETURN_LITERAL);
		return found.isEmpty() ? null : found.get(0);
	}
}
