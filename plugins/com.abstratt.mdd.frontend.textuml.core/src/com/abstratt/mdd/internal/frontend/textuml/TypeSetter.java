/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/ 
package com.abstratt.mdd.internal.frontend.textuml;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.util.DataTypeUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.internal.frontend.textuml.analysis.DepthFirstAdapter;
import com.abstratt.mdd.internal.frontend.textuml.node.AAnySingleTypeIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.AFunctionTypeIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.AMinimalTypeIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.AMultiplicityConstraints;
import com.abstratt.mdd.internal.frontend.textuml.node.ANonuniqueMultiplicityConstraint;
import com.abstratt.mdd.internal.frontend.textuml.node.AOptionalMultiplicity;
import com.abstratt.mdd.internal.frontend.textuml.node.AOrderedMultiplicityConstraint;
import com.abstratt.mdd.internal.frontend.textuml.node.AQualifiedSingleTypeIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.ATupleTypeSingleTypeIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.ATupleTypeSlot;
import com.abstratt.mdd.internal.frontend.textuml.node.ATypeIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.AUniqueMultiplicityConstraint;
import com.abstratt.mdd.internal.frontend.textuml.node.AUnorderedMultiplicityConstraint;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.POptionalMultiplicity;
import com.abstratt.mdd.internal.frontend.textuml.node.TInteger;
import com.abstratt.mdd.internal.frontend.textuml.node.TMult;

/**
 * A node processor  that finds and resolves a  type identifier for a target typed element.
 */
public class TypeSetter extends AbstractTypeResolver implements NodeProcessor<Node> {
	class Visitor extends DepthFirstAdapter {

		@Override
		public void caseAAnySingleTypeIdentifier(AAnySingleTypeIdentifier node) {
			final Type anyType =
				(Type) getContext().getRepository().findNamedElement(TypeUtils.ANY_TYPE,
								IRepository.PACKAGE.getType(), null);
			if (anyType == null) {
				problemBuilder.addProblem(new UnresolvedSymbol(TypeUtils.ANY_TYPE), node);
				throw new AbortedStatementCompilationException();
			}
			setType(anyType);
		}

		private void createDefaultMultiplicity() {
			if (!(target instanceof MultiplicityElement))
				return;
			// default multiplicities when not provided: [1,1]
			MultiplicityElement multiplicityTarget = (MultiplicityElement) target;
			multiplicityTarget.setLowerValue(MDDUtil.createLiteralUnlimitedNatural(getCurrentNamespace().getNearestPackage(), 1));
			multiplicityTarget.setUpperValue(MDDUtil.createLiteralUnlimitedNatural(getCurrentNamespace().getNearestPackage(), 1));
		}

		@Override
		public void caseAFunctionTypeIdentifier(AFunctionTypeIdentifier node) {
			processMultiplicity(node.getOptionalMultiplicity());
			final Type signature = MDDExtensionUtils.createSignature(getCurrentNamespace().getNearestPackage());
			node.getFunctionSignature().apply(new SignatureProcessor(getSourceContext(), getCurrentNamespace(), true, true) {
				@Override
				protected Parameter createParameter(String name) {
					return MDDExtensionUtils.createSignatureParameter(signature, name, null);
				}
			});
			setType(signature);
			signature.setName(MDDUtil.computeSignatureName(signature));
		}

		@Override
		public void caseATupleTypeSingleTypeIdentifier(ATupleTypeSingleTypeIdentifier node) {
			final List<String> slotNames = new ArrayList<String>();
			final List<Type> slotTypes = new ArrayList<Type>();
			node.getTupleType().apply(new DepthFirstAdapter() {
				@Override
				public void caseATupleTypeSlot(ATupleTypeSlot node) {
					String slotTypeIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getTypeIdentifier());
					String slotName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
					slotNames.add(slotName);
					Type slotType = resolveType(node.getTypeIdentifier(), slotTypeIdentifier);
					slotTypes.add(slotType);
				}			
   			});
			DataType found = DataTypeUtils.findOrCreateDataType(getCurrentNamespace().getNearestPackage(), slotNames, slotTypes);
			setType(found);
		}

		@Override
		public void caseAOptionalMultiplicity(AOptionalMultiplicity node) {
			multiplicities = new LinkedList<Integer>();
			super.caseAOptionalMultiplicity(node);
			if (!(multiplicities.isEmpty() || target instanceof MultiplicityElement)) {
				problemBuilder.addError("Multiplicity specification not allowed here", node.getMultiplicitySpec());
				return;
			}
			Integer lowerBound;
			Integer upperBound;
			if (multiplicities.size() == 1) {
				upperBound = multiplicities.get(0);
				// if upper is unlimited, lower is 0 - otherwise, they are equal 
				lowerBound = upperBound == null ? 0 : upperBound;
			} else {
				lowerBound = multiplicities.get(0);
				upperBound = multiplicities.get(1);
				// validate multiplicity
				if (compare(lowerBound, upperBound) > 0) {
					problemBuilder.addError("Upper bound must be greater or equals to lower bound", node.getMultiplicitySpec());
					return;
				}
			}
			// validate zero upper bound
			if (Integer.valueOf(0).equals(upperBound)) {
				problemBuilder.addError("Upper bound must be greater than zero", node.getMultiplicitySpec());
				return;
			}
			MultiplicityElement multiplicityTarget = (MultiplicityElement) target;
			multiplicityTarget.setLower(lowerBound);
			multiplicityTarget.setUpper(upperBound == null ? LiteralUnlimitedNatural.UNLIMITED : upperBound);
		}

		@Override
		public void caseAMultiplicityConstraints(AMultiplicityConstraints node) {
			final MultiplicityElement multiplicityTarget = (MultiplicityElement) target;
			node.apply(new DepthFirstAdapter() {
				@Override
				public void caseANonuniqueMultiplicityConstraint(ANonuniqueMultiplicityConstraint node) {
					multiplicityTarget.setIsUnique(false);
				}

				@Override
				public void caseAOrderedMultiplicityConstraint(AOrderedMultiplicityConstraint node) {
					multiplicityTarget.setIsOrdered(true);
				}

				@Override
				public void caseAUniqueMultiplicityConstraint(AUniqueMultiplicityConstraint node) {
					multiplicityTarget.setIsUnique(true);
				}

				@Override
				public void caseAUnorderedMultiplicityConstraint(AUnorderedMultiplicityConstraint node) {
					multiplicityTarget.setIsOrdered(false);
				}
			});
		}

		@Override
		public void caseAQualifiedSingleTypeIdentifier(AQualifiedSingleTypeIdentifier node) {
			//super.caseAQualifiedSingleTypeIdentifier(node);
			TemplateBindingProcessor<Classifier, Type> tbp = new TemplateBindingProcessor<Classifier, Type>();
			tbp.process(node);
			parameterIdentifiers = tbp.getParameterIdentifiers();
			node.getMinimalTypeIdentifier().apply(this);
		}
		
		@Override
		public void caseAMinimalTypeIdentifier(AMinimalTypeIdentifier node) {
			final String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node);
			Type type = resolveType(node, qualifiedIdentifier);
			if (type != null)
				setType(type);
		}
		
		@Override
		public void caseATypeIdentifier(ATypeIdentifier node) {
			processMultiplicity(node.getOptionalMultiplicity());
			node.getSingleTypeIdentifier().apply(this);
		}
		
		
		private void processMultiplicity(POptionalMultiplicity optionalMultiplicity) {
			// processes multiplicities and bindings
			if (optionalMultiplicity != null)
				optionalMultiplicity.apply(this);
			else
				  createDefaultMultiplicity();
		}

		public void caseTInteger(TInteger node) {
			// multiplicity: integer
			super.caseTInteger(node);
			multiplicities.add(Integer.parseInt(node.getText()));
		}

		public void caseTMult(TMult node) {
			// multiplicity: *
			super.caseTMult(node);
			// null == unlimited
			multiplicities.add(null);
		}
	}

	protected List<Integer> multiplicities;
	protected TypedElement target;
	public TypeSetter(SourceCompilationContext<Node> sourceContext, Namespace currentNamespace, TypedElement target) {
		super(sourceContext, currentNamespace);
		this.target = target;
	}

	private int compare(Integer value1, Integer value2) {
		if (value1 == null)
			return value2 == null ? 0 : 1;
		return value2 == null ? -1 : value1.intValue() - value2.intValue();
	}

	public void process(Node node) {
		node.apply(new Visitor());
	}

	void setType(Type type) {
	    this.target.setType(type);
	}
}