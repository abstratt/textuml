/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Vladimir Sosnin - fix to bug 2796613
 *******************************************************************************/ 
package com.abstratt.mdd.internal.frontend.textuml.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.util.StructuralFeatureUtils;
import com.abstratt.mdd.frontend.core.NotAConcreteClassifier;
import com.abstratt.mdd.frontend.core.spi.DeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker.Step;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAnnotation;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAnnotationValueSpec;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PAnnotations;
import com.abstratt.mdd.frontend.textuml.grammar.node.TFalse;
import com.abstratt.mdd.frontend.textuml.grammar.node.TIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.TInteger;
import com.abstratt.mdd.frontend.textuml.grammar.node.TReal;
import com.abstratt.mdd.frontend.textuml.grammar.node.TString;
import com.abstratt.mdd.frontend.textuml.grammar.node.TTrue;

public class AnnotationProcessor implements NodeProcessor<PAnnotations> {

	private class Annotation {
		private Node node;
		private String stereotypeName;
		private Map<String, Object[]> values = new HashMap<String, Object[]>();

		Annotation(String stereotypeName, Node node) {
			this.stereotypeName = stereotypeName;
			this.node = node;
		}

		public Node getNode() {
			return node;
		}

		public Collection<String> getPropertyNames() {
			return values.keySet();
		}

		public Node getPropertyNode(String propertyName) {
			final Object value = values.get(propertyName);
			return (Node) (value == null ? null : ((Object[]) value)[1]);
		}

		public String getStereotypeName() {
			return stereotypeName;
		}

		public Object getValue(String propertyName) {
			final Object value = values.get(propertyName);
			return value == null ? null : ((Object[]) value)[0];
		}

		public boolean hasValue(String propertyName) {
			return values.containsKey(propertyName);
		}

		public void setValue(String propertyName, Object value, Node node) {
			values.put(propertyName, new Object[] { value, node });
		}
	}

	private class Visitor extends DepthFirstAdapter {
		@Override
		public final void caseAAnnotation(final AAnnotation node) {
			String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getQualifiedIdentifier());
			final Annotation annotation = new Annotation(qualifiedIdentifier, node.getQualifiedIdentifier());
			annotations.add(annotation);
			super.caseAAnnotation(node);
		}

		@Override
		public final void caseAAnnotationValueSpec(AAnnotationValueSpec node) {
			super.caseAAnnotationValueSpec(node);
			Annotation currentAnnotation = annotations.get(annotations.size() - 1);
			String propertyName = Util.stripEscaping(node.getIdentifier().getText());
			final Object[] value = { null };
			node.getAnnotationValue().apply(new DepthFirstAdapter() {
				@Override
				public void caseTTrue(TTrue node) {
					value[0] = Boolean.TRUE;
				}

				@Override
				public void caseTFalse(TFalse node) {
					value[0] = Boolean.FALSE;
				}

				@Override
				public void caseTInteger(TInteger node) {
					try {
						value[0] = Integer.parseInt(node.getText());
					} catch (NumberFormatException nfe) {
						AnnotationProcessor.this.problemBuilder.addError("Stereotype properties accept only booleans, integers, strings or null", node);
					}
				}

				@Override
				public void caseTString(TString node) {
					String text = node.getText().substring(1);
					text = text.substring(0, text.length() - 1);
					value[0] = text;
				}

				@Override
				public void caseTReal(TReal node) {
					AnnotationProcessor.this.problemBuilder.addError(
									"Stereotype properties accept only booleans, integers, strings, enumeration literals or null", node);
				}
				
				@Override
				public void caseTIdentifier(TIdentifier node) {
					value[0] = node.getText();
				}
			});
			if (value[0] != null)
				currentAnnotation.setValue(propertyName, value[0], node.getIdentifier());
		}

	}

	private List<Annotation> annotations = new ArrayList<Annotation>();

	private ProblemBuilder<Node> problemBuilder;

	private IReferenceTracker refTracker;

	AnnotationProcessor(IReferenceTracker refTracker, ProblemBuilder<Node> problemBuilder) {
		this.refTracker = refTracker;
		this.problemBuilder = problemBuilder;
	}

	/**
	 * Applies the current annotations to the given element.
	 * @param referenceNode
	 *            the node to get the text position from in case of error
	 */
	protected void applyAnnotations(final Element targetElement, final Node referenceNode) {
		Step linkStep = IReferenceTracker.Step.STEREOTYPE_APPLICATIONS;
		Package currentPackage = targetElement.getNearestPackage();
		for (Iterator<Annotation> i = annotations.iterator(); i.hasNext();) {
			final Annotation annotation = i.next();
			refTracker.add(new DeferredReference<Stereotype>(annotation.getStereotypeName(), IRepository.PACKAGE.getStereotype(),
							currentPackage) {
				protected void onBind(Stereotype stereotype) {
					if (stereotype == null) {
						problemBuilder.addError("Unknown stereotype: '" + getSymbolName() + "'", annotation
										.getNode());
						return;
					}
					// make sure stereotype is applicable to element
					if (stereotype.isAbstract()) {
						problemBuilder.addProblem(new NotAConcreteClassifier(stereotype.getQualifiedName()),
										referenceNode);
						return;
					} 
					if (!targetElement.isStereotypeApplicable(stereotype)) {
						problemBuilder.addError("Stereotype '" + stereotype.getQualifiedName()
										+ "' is not applicable",
										referenceNode);
						return;
					}
					if (targetElement.isStereotypeApplied(stereotype)) {
						problemBuilder.addError("Stereotype '" + stereotype.getQualifiedName()
										+ "' is already applied",
										referenceNode);
						return;
					}
					targetElement.applyStereotype(stereotype);
					// set values for each property referenced
					for (String propertyName : annotation.getPropertyNames()) {
						Property propertyFound = StructuralFeatureUtils.findAttribute(stereotype, propertyName, false, true);
						if (propertyFound == null) {
							problemBuilder.addError(
											"Property not defined for stereotype: '" + propertyName + "'", annotation
															.getPropertyNode(propertyName));
							return;
						}
						Object value = annotation.getValue(propertyName);
						if (propertyFound.getType() instanceof Enumeration)
							value = ((Enumeration) propertyFound.getType()).getOwnedLiteral((String) value);
						try {
							targetElement.setValue(stereotype, propertyName, annotation.getValue(propertyName));
						} catch (ClassCastException cce) {
							problemBuilder.addError(
									"Value does not match expected type for property '"+ propertyName + "'", annotation
													.getPropertyNode(propertyName));
							return;
						}
					}
				}
			}, linkStep);
		}
		discardAnnotations();
	}

	public void process(PAnnotations node) {
		Assert.isTrue(annotations.isEmpty());
		if (node == null)
			return;
		node.apply(new Visitor());
	}
	
	public void discardAnnotations() {
		annotations.clear();
	}

}
