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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.ParameterableElement;
import org.eclipse.uml2.uml.TemplateBinding;
import org.eclipse.uml2.uml.TemplateParameterSubstitution;
import org.eclipse.uml2.uml.TemplateableElement;

public class ParameterSubstitutionMap {
//	private ParameterSubstitutionMap parent;
	private Map<ParameterableElement, ParameterableElement> substitutions =
					new HashMap<ParameterableElement, ParameterableElement>();

	public ParameterSubstitutionMap(TemplateBinding startingPoint) {
		this(startingPoint.getBoundElement());
	}
	
	public ParameterSubstitutionMap(TemplateableElement... startingPoints) {
	    for (TemplateableElement current : startingPoints)
	        addSubstitutions(current);
	}

	public void addSubstitution(TemplateParameterSubstitution substitution) {
		substitutions.put(substitution.getFormal().getParameteredElement(), UML2Compatibility.getActualParameter(substitution));
	}

	public void addSubstitutions(TemplateableElement bound) {
		EList<TemplateBinding> allBindings = bound.getTemplateBindings();
		for (TemplateBinding templateBinding : allBindings) {
			for (TemplateParameterSubstitution substitution : templateBinding.getParameterSubstitutions())
				addSubstitution(substitution);
			addSubstitutions(templateBinding.getSignature().getTemplate());
		}
		if (bound instanceof Classifier) {
			EList<Classifier> generals = ((Classifier) bound).getGenerals();
			for (Classifier classifier : generals)
				addSubstitutions(classifier);
		}
	}

	/**
	 * Returns the actual parameter corresponding to the given formal template
	 * parameter, or <code>null</code> if it is not a parameter belonging to
	 * this parameter substitution map.
	 * 
	 * @param parameterable
	 * @return the resolved parameter, or <code>null</code>
	 */
	public <PE extends ParameterableElement> PE resolveTemplateParameter(PE parameterable) {
		if (!parameterable.isTemplateParameter())
			return parameterable;
		PE substituted = (PE) substitutions.get(parameterable);
		if (substituted == null || !substituted.isTemplateParameter() || substituted == parameterable)
			return substituted;
		return resolveTemplateParameter(substituted);
	}
}