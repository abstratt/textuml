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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.ParameterableElement;
import org.eclipse.uml2.uml.TemplateBinding;
import org.eclipse.uml2.uml.TemplateParameter;
import org.eclipse.uml2.uml.TemplateParameterSubstitution;
import org.eclipse.uml2.uml.TemplateSignature;
import org.eclipse.uml2.uml.TemplateableElement;
import org.eclipse.uml2.uml.Type;

public class TemplateUtils {

    /**
     * Applies substitutions to a parameterable element in the context of a
     * bound element.
     * 
     * @param <PE>
     * @param <TE>
     * @param templateable
     * @param parameterable
     * @return
     */
    public static <PE extends ParameterableElement, TE extends TemplateableElement> PE applySubstitution(TE templateable, PE parameterable) {
        if (!parameterable.isTemplateParameter())
            return parameterable;
        ParameterSubstitutionMap substitutions = new ParameterSubstitutionMap(templateable);
        return applySubstitution(parameterable, substitutions);
    }

    public static <PE extends ParameterableElement> PE applySubstitution(PE parameterable, ParameterSubstitutionMap substitutions) {
        return (PE) substitutions.resolveTemplateParameter(parameterable);
    }

    /**
     * Creates a template binding for the given template and parameters.
     * 
     * @param template
     * @param actualParam
     * @return the created bound element
     */
    @SuppressWarnings("unchecked")
    public static <TE extends TemplateableElement & NamedElement, PE extends ParameterableElement> TE createBinding(Namespace namespace,
            TE template, List<PE> actualParams) {
        TemplateSignature signature = template.getOwnedTemplateSignature();
        List<TemplateParameter> formalParameters = signature.getParameters();
        Assert.isTrue(formalParameters.size() == actualParams.size());
        TE result;
        if (template instanceof Classifier) {
            result = (TE) namespace.getNearestPackage().createOwnedType(null, template.eClass());
            result.setName(generateTemplateInstanceName(template, actualParams));
        } else if (template instanceof Operation)
            result = (TE) FeatureUtils.createOperation((Classifier) namespace, template.getName());
        else
            throw new IllegalArgumentException(template.eClass().getName());
        TemplateBinding binding = result.createTemplateBinding(signature);
        for (int i = 0; i < formalParameters.size(); i++) {
            TemplateParameterSubstitution substitution = binding.createParameterSubstitution();
            substitution.setFormal(signature.getOwnedParameters().get(i));
            UML2Compatibility.setActualParameter(substitution, actualParams.get(i));
        }
        return result;
    }

    private static <TE, PE> String generateTemplateInstanceName(TE template, List<PE> actualParams) {
        NamedElement namedTemplate = (NamedElement) template;
        StringBuilder resultName = new StringBuilder(namedTemplate.getName());
        resultName.append("<");
        List<String> paramNames = new ArrayList<String>();
        for (PE actualParam : actualParams)
            paramNames.add(((NamedElement) actualParam).getName());
        resultName.append(StringUtils.join(paramNames, ','));
        resultName.append(">");
        String finalName = resultName.toString();
        return finalName;
    }

    public static void createSubclassTemplateBinding(Classifier base, Classifier sub, List<Type> subParams) {
        TemplateSignature signature = base.getOwnedTemplateSignature();
        List<TemplateParameter> formalParameters = signature.getParameters();
        Assert.isTrue(formalParameters.size() == subParams.size());
        TemplateBinding binding = sub.createTemplateBinding(signature);
        for (int i = 0; i < formalParameters.size(); i++) {
            TemplateParameterSubstitution substitution = binding.createParameterSubstitution();
            substitution.setFormal(signature.getOwnedParameters().get(i));
            UML2Compatibility.setActualParameter(substitution, subParams.get(i));
        }
    }

    /**
     * Returns the template binding in the given bound element that is related
     * to the selected signature.
     * 
     * @param bound
     * @param signature
     * @return
     */
    public static TemplateBinding findTemplateBinding(Element bound, TemplateSignature signature) {
        if (bound instanceof TemplateableElement) {
            TemplateableElement templateable = (TemplateableElement) bound;
            TemplateBinding binding = templateable.getTemplateBinding(signature);
            if (binding != null)
                return binding;
            if (bound instanceof Classifier) {
                // try the super classes, they might own the template binding
                List<Classifier> generals = ((Classifier) bound).getGenerals();
                for (Classifier general : generals) {
                    TemplateBinding found = findTemplateBinding(general, signature);
                    if (found != null)
                        return found;
                }
            }
            // try the templates themselves, they might have the bindings
            List<TemplateBinding> bindings = templateable.getTemplateBindings();
            for (TemplateBinding templateBinding : bindings) {
                TemplateBinding found = findTemplateBinding(templateBinding.getSignature().getTemplate(), signature);
                if (found != null)
                    return found;
            }
        }
        return bound.getOwner() == null ? null : findTemplateBinding(bound.getOwner(), signature);
    }

    public static ParameterSubstitutionMap findTemplateParameterSubstitutions(Element source, TemplateSignature targetSignature) {
        TemplateBinding binding = findTemplateBinding(source, targetSignature);
        return binding == null ? null : new ParameterSubstitutionMap(binding.getBoundElement());
    }

    /**
     * Generates an element name for the given template and parameter nodes.
     * Fails if any of the given objects are not named elements.
     */
    public static <PE extends ParameterableElement> String generateBoundElementName(TemplateableElement template,
            List<PE> templateParameters) {
        StringBuffer name = new StringBuffer(((NamedElement) template).getName());
        name.append("_of");
        for (ParameterableElement parameter : templateParameters) {
            name.append('_');
            name.append(((NamedElement) parameter).getName());
        }
        return name.toString();
    }

    /**
     * Returns whether the templateable destination is assignment-compatible
     * with the templateable source. At least one of the elements must not be a
     * template.
     * 
     * @param sourceBindings
     * @param destinationBindings
     * @return <code>true</code> if the bindings are assignment-compatible,
     *         <code>false</code> otherwise
     * @throws IllegalArgumentException
     *             if both templateable elements are templates
     */
    static boolean isCompatible(TemplateableElement source, TemplateableElement destination) {
        Assert.isLegal(!source.isTemplate() && !destination.isTemplate());
        // general case: both are bound elements
        EList<TemplateBinding> sourceBindings = source.getTemplateBindings();
        EList<TemplateBinding> destinationBindings = destination.getTemplateBindings();
        for (TemplateBinding sourceBinding : sourceBindings) {
            TemplateBinding destinationBinding = destination.getTemplateBinding(sourceBinding.getSignature(), true);
            if (destinationBinding == null)
                return false;
            // same signature implies same template... now check the actual
            // parameters for each substitution
            final EList<TemplateParameterSubstitution> sourceParameterSubstitutions = sourceBinding.getParameterSubstitutions();
            final EList<TemplateParameterSubstitution> destinationParameterSubstitutions = destinationBinding.getParameterSubstitutions();
            if (sourceParameterSubstitutions.size() != destinationParameterSubstitutions.size())
                return false;
            for (int i = 0; i < sourceParameterSubstitutions.size(); i++) {
                TemplateParameterSubstitution sourceSubstitution = sourceParameterSubstitutions.get(i);
                TemplateParameterSubstitution destinationSubstitution = destinationParameterSubstitutions.get(i);
                if (!UML2Compatibility.getActualParameter(sourceSubstitution).equals(
                        UML2Compatibility.getActualParameter(destinationSubstitution)))
                    return false;
            }
        }
        return sourceBindings.size() == destinationBindings.size();
    }

    public static boolean isTemplateInstance(TemplateableElement t) {
        return !t.getTemplateBindings().isEmpty();
    }
    
    public static boolean isFullyResolvedTemplateInstance(Classifier t) {
        if (t.isTemplate())
            return false;
        if (t.isTemplateParameter())
            return false;
        for (TemplateBinding templateBinding : t.getTemplateBindings())
            for (TemplateParameterSubstitution subs : templateBinding.getParameterSubstitutions())
                if (subs.getActual() instanceof TemplateableElement && !isFullyResolvedTemplateInstance((Classifier) subs.getActual()))
                    return false;
        return true;
                    
    }

    /**
     * Resolves all template parameters.
     * 
     * @param boundElement
     * @param toResolve
     * @return a fully resolved type
     */
    public static Type resolveTemplateParameters(TemplateableElement boundElement, Classifier toResolve) {
        if (isFullyResolvedTemplateInstance(toResolve))
            return toResolve;
        ParameterSubstitutionMap substitutionMap = new ParameterSubstitutionMap(boundElement, toResolve);
        // first, trivial resolution (formal -> actual parameters):
        Classifier resolved = substitutionMap.resolveTemplateParameter(toResolve);
        if (!isFullyResolvedTemplateInstance(resolved)) {
            // still things to resolve - gotta need to instantiate the template
            TemplateSignature signature = toResolve.getOwnedTemplateSignature();
            if (signature == null) {
                EList<TemplateBinding> additionalBindings = toResolve.getTemplateBindings();
                TemplateBinding additionalBinding = additionalBindings.get(0);
                signature = additionalBinding.getSignature();
                toResolve = (Classifier) signature.getTemplate();
            }
            List<ParameterableElement> actuals = new ArrayList<ParameterableElement>();
            for (TemplateParameter templateParameter : signature.getParameters()) {
                ParameterableElement formal = templateParameter.getParameteredElement();
                ParameterableElement actual = substitutionMap.resolveTemplateParameter(formal);
                actuals.add(actual);
            }
            resolved = createBinding(boundElement.getNearestPackage(), toResolve, actuals);
        }
        // try to resolve again in case there are new parameters to resolve
        return resolved == toResolve ? toResolve : resolveTemplateParameters(boundElement, resolved);
    }
}