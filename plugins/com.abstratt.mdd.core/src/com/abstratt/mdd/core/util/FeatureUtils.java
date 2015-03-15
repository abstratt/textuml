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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Feature;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.TemplateBinding;
import org.eclipse.uml2.uml.TemplateSignature;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecificationAction;

import com.abstratt.mdd.core.IRepository;

public class FeatureUtils {
	public static boolean isParametrizedConstraint(Constraint constraint) {
		Behavior toExecute = ActivityUtils.resolveBehaviorReference(constraint.getSpecification());
		return toExecute.getOwnedParameters().size() > 1;
	}
	
	public static Classifier getOwningClassifier(Feature operation) {
	    return (Classifier) operation.eContainer(); 
	}
	
	public static Operation createOperation(Classifier parent, String operationName) {
        Operation operation;
        if (parent instanceof Class)
            operation = ((Class) parent).createOwnedOperation(operationName, null, null, null);
        else if (parent instanceof Interface)
            operation = ((Interface) parent).createOwnedOperation(operationName, null, null, null);
        else if (parent instanceof DataType)
            operation = ((DataType) parent).createOwnedOperation(operationName, null, null, null);
        else
            throw new IllegalArgumentException("Cannot create operation for "+ parent.eClass().getName());
        return operation;
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
					&& (isMatch(repository, operation, arguments, substitutions)))
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
	
	public static Map<Type, Type> buildWildcardSubstitutions(Map<Type, Type> wildcardSubstitutions, List<? extends TypedElement> parameters, List<? extends TypedElement> arguments) {
        for (Iterator<?> argIter = arguments.iterator(), parIter = parameters
                .iterator(); argIter.hasNext();) {
            TypedElement parameter = (Parameter) parIter.next();
            TypedElement argument = (TypedElement) argIter.next();
            if (MDDExtensionUtils.isWildcardType(parameter.getType())) {
                Type existingWildcardSub = wildcardSubstitutions.put(parameter.getType(), argument.getType());
                if (existingWildcardSub != null && existingWildcardSub != argument.getType())
                    return Collections.emptyMap();
            } else if (MDDExtensionUtils.isSignature(parameter.getType())) {
                Type signature = parameter.getType();
                // in the case of a signature-typed parameter, we need to figure out the signature of the closure
                // and perform substitutions based on that
                List<Parameter> rawActualParameters = new ArrayList<Parameter>();
                Action action = ActivityUtils.getOwningAction((OutputPin) argument);
                if (action instanceof ValueSpecificationAction && ActivityUtils.isBehaviorReference(((ValueSpecificationAction) action).getValue())) {
                    Activity closure = (Activity) ActivityUtils.resolveBehaviorReference(action);
                    rawActualParameters = closure.getOwnedParameters();
                } else if (MDDExtensionUtils.isSignature(argument.getType()))
                    rawActualParameters = MDDExtensionUtils.getSignatureParameters(argument.getType());
                // closure (or signature) actual parameters
                List<Parameter> actualParameters = new ArrayList<Parameter>(FeatureUtils.getInputParameters(rawActualParameters));
                Parameter actualReturnParameter = FeatureUtils.getReturnParameter(rawActualParameters);
                if (actualReturnParameter != null)
                    actualParameters.add(actualReturnParameter);
                
                // signature canonical parameters
                List<Parameter> allExpectedParameters = MDDExtensionUtils.getSignatureParameters(signature);
                List<Parameter> expectedParameters = new ArrayList<Parameter>(FeatureUtils.getInputParameters(allExpectedParameters));
                Parameter expectedReturnParameter = FeatureUtils.getReturnParameter(allExpectedParameters);
                if (expectedReturnParameter != null)
                    expectedParameters.add(expectedReturnParameter);
                
                buildWildcardSubstitutions(wildcardSubstitutions, expectedParameters, actualParameters);
            }
        }
        return wildcardSubstitutions;
	}

	public static boolean isMatch(IRepository repository, Operation operation,
			List<TypedElement> arguments, ParameterSubstitutionMap substitutions) {
		if (arguments == null)
			return true;
		List<Parameter> operationParameters = filterParameters(operation
				.getOwnedParameters(), ParameterDirectionKind.IN_LITERAL);
		if (arguments.size() != operationParameters.size())
			return false;
		Map<Type, Type> wildcardSubstitutions = new HashMap<Type, Type>();
		for (Iterator<?> argIter = arguments.iterator(), parIter = operationParameters
				.iterator(); argIter.hasNext();) {
			Parameter parameter = (Parameter) parIter.next();
			TypedElement argument = (TypedElement) argIter.next();
            if (MDDExtensionUtils.isWildcardType(parameter.getType())) {
                Type existingWildcardSub = wildcardSubstitutions.put(parameter.getType(), argument.getType());
                if (existingWildcardSub != null && existingWildcardSub != argument.getType())
                    return false;
            } else
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

	public static Parameter getReturnParameter(List<Parameter> ownedParameters) {
		List<Parameter> found = filterParameters(ownedParameters, ParameterDirectionKind.RETURN_LITERAL);
		return found.isEmpty() ? null : found.get(0);
	}
}
