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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.ObjectNode;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.TemplateBinding;
import org.eclipse.uml2.uml.TemplateableElement;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.ValueSpecification;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.IRepository;

/**
 * Several utility methods for TypedElements and Type subclasses.
 */
public class TypeUtils {

	public final static String ANY_TYPE = makeTypeName("AnyType");

	public final static String NULL_TYPE = makeTypeName("NullType");
	
	public static String makeTypeName(String simpleName) {
		if (simpleName.startsWith(IRepository.TYPES_NAMESPACE + NamedElement.SEPARATOR))
			return simpleName;
		return IRepository.TYPES_NAMESPACE + NamedElement.SEPARATOR + simpleName;
	}

	/**
	 * Checks the compatibility of the input pins in the given action with their
	 * corresponding incoming output pins. If a mismatch is found, the
	 * corresponding flow is returned.
	 * 
	 * @param repository
	 * @param action
	 *            the action to check
	 * @param substitutions
	 *            a list of template parameter substitutions, or
	 *            <code>null</code>
	 * @return the offending flow, or <code>null</code>
	 */
	public static ObjectFlow checkCompatibility(IBasicRepository repository, Action action, TemplateableElement bound) {
		for (InputPin current : ActivityUtils.getActionInputs(action)) {
			List<ActivityEdge> incomings = current.getIncomings();
			if (incomings.isEmpty())
				continue;
			flowLoop: for (ActivityEdge edge : incomings) {
				ObjectFlow flow = (ObjectFlow) edge;
				// we cannot assume source is a pin (it can be another type of
				// object node)
				final ObjectNode source = (ObjectNode) flow.getSource();
				final ObjectNode target = (ObjectNode) flow.getTarget();
				if (!isCompatible(repository, source, target, null)) {
					if (bound != null)
						for (TemplateBinding binding : bound.getTemplateBindings())
							if (isCompatible(repository, source, target, new ParameterSubstitutionMap(binding)))
								// found one substitution that works
								continue flowLoop;
					return flow;
				}
			}
		}
		// no problems found
		return null;
	}

	public static void copyType(TypedElement source, TypedElement target) {
		copyType(source, target, null);
	}

	/**
	 * Copies the type from the source element to the target element. It also
	 * does a few other things:
	 * <ol>
	 * <li>if the source type is a template formal parameter, replaces it with
	 * the actual parameter</li>
	 * <li>if the source element has multiplicity > 1, sets the target type to
	 * be the corresponding collection type</li>
	 * </ol>
	 * 
	 * @param source
	 * @param target
	 * @param boundElement
	 */
	public static void copyType(TypedElement source, TypedElement target, TemplateableElement boundElement) {
		Type sourceType = source.getType();
		if (sourceType == null) {
			target.setType(null);
			return;
		}
		// if source is a template parameter, resolve it if possible  
		Assert.isTrue(!sourceType.isTemplateParameter() || boundElement != null);
		Type resolvedType = TemplateUtils.resolveTemplateParameters(boundElement, (Classifier) sourceType);
		target.setType(resolvedType);
		Assert.isLegal(source instanceof MultiplicityElement == target instanceof MultiplicityElement);
		if (!(source instanceof MultiplicityElement))
			return;
		copyMultiplicity((MultiplicityElement)source, (MultiplicityElement)target);
	}
	
	public static void copyMultiplicity(MultiplicityElement multipleSource, MultiplicityElement multipleTarget) {
		multipleTarget.setIsOrdered(multipleSource.isOrdered());
		multipleTarget.setIsUnique(multipleSource.isUnique());
		// need to copy otherwise value specs are moved from source to target
		multipleTarget.setLowerValue((ValueSpecification) (multipleSource.getLowerValue() == null ? null : EcoreUtil
						.copy(multipleSource.getLowerValue())));
		multipleTarget.setUpperValue((ValueSpecification) (multipleSource.getUpperValue() == null ? null : EcoreUtil
						.copy(multipleSource.getUpperValue())));
	}

	/**
	 * For a typed element that has a classifier as its type, determines the
	 * actual target classifier.
	 * 
	 * The actual target classifier will be a collection type if the given typed
	 * element is multivalued, otherwise it will be the original type itself.
	 */
	public static Type getTargetType(IBasicRepository repository, TypedElement typed, boolean resolveCollectionTypes) {
		Type type = typed.getType();
        if (!resolveCollectionTypes)
			return type;
		if (!(typed instanceof MultiplicityElement))
			return type;
		MultiplicityElement multiple = (MultiplicityElement) typed;
		if (!multiple.isMultivalued())
			return type;
		final boolean ordered = multiple.isOrdered();
		final boolean unique = multiple.isUnique();
		String collectionTypeName =
						"mdd_collections::" + (ordered ? (unique ? "OrderedSet" : "Sequence") : (unique ? "Set" : "Bag"));
		Classifier collectionType =
						(Classifier) repository.findNamedElement(collectionTypeName, IRepository.PACKAGE.getClass_(),
										null);
		Assert.isNotNull(collectionType, "Could not find collection type: " + collectionTypeName);
		return TemplateUtils.createBinding(typed.getNearestPackage(), collectionType, Collections.singletonList(type));
	}

	public static boolean isCompatible(IBasicRepository repository, List<? extends TypedElement> source,
					List<? extends TypedElement> destination, ParameterSubstitutionMap substitutions) {
		if (destination.size() != source.size())
			return false;
		final int elementCount = destination.size();
		for (int i = 0; i < elementCount; i++) {
			if (!isCompatible(repository, source.get(i), destination.get(i), substitutions))
				return false;
		}
		return true;
	}

	/**
	 * Returns whether a destination behavior is compatible with a source
	 * behavior. Comparison takes parameters into account (type and direction,
	 * butnot names).
	 * </p>
	 * 
	 * @param repository
	 * @param sourceBehavior
	 * @param destinationBehavior
	 * @return
	 */
	public static boolean isCompatible(IBasicRepository repository, Parameter[] sourceParameters,
					Parameter[] destinationParameters, ParameterSubstitutionMap substitutions) {
		if (destinationParameters.length != sourceParameters.length)
			return false;
		for (int i = 0; i < sourceParameters.length; i++) {
			if (destinationParameters[i].getDirection() != sourceParameters[i].getDirection()
							|| !isCompatible(repository, sourceParameters[i], destinationParameters[i], substitutions))
				return false;
		}
		return true;
	}

	/**
	 * Returns whether the source and destination have compatible types.
	 * 
	 * @param source
	 * @param destination
	 * @param substitutions
	 *            a list of template parameter substitutions
	 * @return a boolean indicating whether the source and destination types are
	 *         compatible
	 */
	public static <T extends TypedElement> boolean isCompatible(IBasicRepository repository, T source, T destination,
					ParameterSubstitutionMap substitutions) {
		if ((source instanceof MultiplicityElement) && !(destination instanceof MultiplicityElement))
			return false;
		if (source instanceof MultiplicityElement) {
			MultiplicityElement sourceAsMultiple = (MultiplicityElement) source;
			MultiplicityElement destinationAsMultiple = (MultiplicityElement) destination;
			if (!destinationAsMultiple.isMultivalued() && sourceAsMultiple.isMultivalued())
				// multi -> single is bad, single -> multi is ok
				return false;
		}
		return isCompatible(repository, source.getType(), destination.getType(), substitutions);
	}

	/**
	 * Returns whether two types are compatible. Optionally, takes template
	 * parameter substitutions into account.
	 * 
	 * @param repository
	 * @param source
	 * @param destination
	 * @param substitutions
	 *            a list of template parameter substitutions, or
	 *            <code>null</code>
	 * @return
	 */
	public static boolean isCompatible(IBasicRepository repository, Type source, Type destination,
					ParameterSubstitutionMap substitutions) {
		if (destination == null
						|| destination == repository.findNamedElement(ANY_TYPE, IRepository.PACKAGE.getClass_(), null) || source == repository.findNamedElement(NULL_TYPE, IRepository.PACKAGE.getClass_(), null))
			return true;
		if (source == null)
			return false;
		if (source == destination)
			return true;
		// do not check if wildcard
		if (MDDExtensionUtils.isWildcardType(destination))
		    return true;
		Boolean templateCompatible = null;
		// if destination is actually a template parameter, test compatibility with the resolved parameter
		if (substitutions != null && destination.isTemplateParameter()) {
			Type actualDestination = (Type) substitutions.resolveTemplateParameter(destination);
			return actualDestination == null ? false : isCompatible(repository, source, actualDestination,
							substitutions);
		}
		if ((source instanceof TemplateableElement) && !(destination instanceof TemplateableElement)) {
			if (((TemplateableElement) source).isTemplate())
			    return false;
		} else if (!(source instanceof TemplateableElement) && (destination instanceof TemplateableElement)) {
			if (((TemplateableElement) destination).isTemplate())
			    return false;
		} else if (source instanceof TemplateableElement && destination instanceof TemplateableElement) {
			final TemplateableElement templateableSource = (TemplateableElement) source;
			final TemplateableElement templateableDestination = (TemplateableElement) destination;
			if (templateableSource.isTemplate() != templateableDestination.isTemplate())
				// one of them is a template, the other is not, cannot be compatible
				return false;
			if (templateableSource.isTemplate())
				// if both are templates, general conformance checking should be
				// enough
				return source.conformsTo(destination);
			// if both are bound elements, use template-aware conformance checking
			if (!templateableSource.getTemplateBindings().isEmpty() || !templateableDestination.getTemplateBindings().isEmpty())
			    templateCompatible = TemplateUtils.isCompatible(templateableSource, templateableDestination);
		}
		// behavior comparison takes parameters into account
		if (source instanceof Behavior || MDDExtensionUtils.isSignature(source)) {
		    if (!MDDExtensionUtils.isSignature(destination))
		        return false;
			final List<Parameter> destinationParams;
			final List<Parameter> sourceParams;
			if (source instanceof Behavior)
			    sourceParams = ((Behavior) source).getOwnedParameters();
			else
			    // source is not an inlined closure
			    // (note this is currently not supported, see issue #50)
			    sourceParams = MDDExtensionUtils.getSignatureParameters(source);
			destinationParams = MDDExtensionUtils.getSignatureParameters(destination);
			return isCompatible(repository, sourceParams.toArray(new Parameter[sourceParams.size()]), destinationParams
							.toArray(new Parameter[destinationParams.size()]), substitutions);
		}
		// for data types, we perform shape-based compatibility check
		if (destination instanceof DataType && source instanceof Classifier) {
			List<Property> destinationAttributes = ((Classifier) destination).getAllAttributes();
			List<Property> sourceAttributes = ((Classifier) source).getAllAttributes();
			if (destinationAttributes.size() != sourceAttributes.size())
				return false;
			for (int i = 0; i < sourceAttributes.size(); i++) {
				// if any defines a property name, names must match
				String destinationName = StringUtils.trimToNull(destinationAttributes.get(i).getName());
				String sourceName = StringUtils.trimToNull(sourceAttributes.get(i).getName());
				if (destinationName != null && sourceName != null && !destinationName.equals(sourceName))
					return false;
				if (!isCompatible(repository, sourceAttributes.get(i), destinationAttributes.get(i), substitutions))
					return false;
			}
			return true;
		}
		if (Boolean.TRUE.equals(templateCompatible))
			// if they are deemed template compatible, go with that - conformance doesn't understand templates
			return true;
		// general type conformance
		return source.conformsTo(destination) ;
	}
}
