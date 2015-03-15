/*******************************************************************************
  * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Vladimir Sosnin - #2798455, #2797252
 *******************************************************************************/ 
package com.abstratt.mdd.internal.frontend.textuml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Feature;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.Slot;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.core.util.BasicTypeUtils;
import com.abstratt.mdd.core.util.ConnectorUtils;
import com.abstratt.mdd.core.util.FeatureUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.ProjectPropertyHelper;
import com.abstratt.mdd.core.util.ReceptionUtils;
import com.abstratt.mdd.core.util.TemplateUtils;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.AnonymousDisconnectedPort;
import com.abstratt.mdd.frontend.core.CannotLoadFromLocation;
import com.abstratt.mdd.frontend.core.CannotSpecializeClassifier;
import com.abstratt.mdd.frontend.core.DuplicateSymbol;
import com.abstratt.mdd.frontend.core.IdsShouldBeRequiredSingle;
import com.abstratt.mdd.frontend.core.InvalidPackageNesting;
import com.abstratt.mdd.frontend.core.MissingDefaultValue;
import com.abstratt.mdd.frontend.core.NonQualifiedIdentifierExpected;
import com.abstratt.mdd.frontend.core.NotAMetaclass;
import com.abstratt.mdd.frontend.core.RequiredPortHasNoMatchingProviderPort;
import com.abstratt.mdd.frontend.core.TypeMismatch;
import com.abstratt.mdd.frontend.core.UnknownAttribute;
import com.abstratt.mdd.frontend.core.UnknownParentPackage;
import com.abstratt.mdd.frontend.core.UnknownType;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.WrongNumberOfRoles;
import com.abstratt.mdd.frontend.core.spi.AbortedCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.core.spi.DeferredReference;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker.Step;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.internal.frontend.textuml.analysis.DepthFirstAdapter;
import com.abstratt.mdd.internal.frontend.textuml.node.AActorClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.AAggregationAssociationKind;
import com.abstratt.mdd.internal.frontend.textuml.node.AAggregationReferenceType;
import com.abstratt.mdd.internal.frontend.textuml.node.AApplyProfileDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationHeader;
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationMemberEnd;
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationOwnedEnd;
import com.abstratt.mdd.internal.frontend.textuml.node.AAssociationRoleDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AAttributeDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassHeader;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassImplementsItem;
import com.abstratt.mdd.internal.frontend.textuml.node.AClassSpecializesItem;
import com.abstratt.mdd.internal.frontend.textuml.node.AComplexInitializationExpression;
import com.abstratt.mdd.internal.frontend.textuml.node.AComponentClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.ACompositionAssociationKind;
import com.abstratt.mdd.internal.frontend.textuml.node.ACompositionReferenceType;
import com.abstratt.mdd.internal.frontend.textuml.node.AConnectorDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AConnectorEndList;
import com.abstratt.mdd.internal.frontend.textuml.node.ADatatypeClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.ADependencyDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AEmptyClassSpecializesSection;
import com.abstratt.mdd.internal.frontend.textuml.node.AEnumerationClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.AEnumerationLiteralDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AFeatureDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AFunctionDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AImportDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AInterfaceClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.AInvariantDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.ALoadDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AModifiers;
import com.abstratt.mdd.internal.frontend.textuml.node.ANamedSimpleValue;
import com.abstratt.mdd.internal.frontend.textuml.node.AOperationDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AOperationHeader;
import com.abstratt.mdd.internal.frontend.textuml.node.AOptionalAlias;
import com.abstratt.mdd.internal.frontend.textuml.node.AOptionalReturnType;
import com.abstratt.mdd.internal.frontend.textuml.node.AOptionalSubsetting;
import com.abstratt.mdd.internal.frontend.textuml.node.APackageHeading;
import com.abstratt.mdd.internal.frontend.textuml.node.APortConnector;
import com.abstratt.mdd.internal.frontend.textuml.node.APortDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.APrimitiveDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AProvidedPortModifier;
import com.abstratt.mdd.internal.frontend.textuml.node.AQueryOperationKeyword;
import com.abstratt.mdd.internal.frontend.textuml.node.AReceptionDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AReferenceDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.ASignalClassType;
import com.abstratt.mdd.internal.frontend.textuml.node.ASignature;
import com.abstratt.mdd.internal.frontend.textuml.node.ASimpleBlock;
import com.abstratt.mdd.internal.frontend.textuml.node.ASimpleInitialization;
import com.abstratt.mdd.internal.frontend.textuml.node.ASimpleInitializationExpression;
import com.abstratt.mdd.internal.frontend.textuml.node.ASimpleSignature;
import com.abstratt.mdd.internal.frontend.textuml.node.AStart;
import com.abstratt.mdd.internal.frontend.textuml.node.AStateMachineDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AStereotypeDef;
import com.abstratt.mdd.internal.frontend.textuml.node.AStereotypeDefHeader;
import com.abstratt.mdd.internal.frontend.textuml.node.AStereotypeExtension;
import com.abstratt.mdd.internal.frontend.textuml.node.AStereotypePropertyDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.ASubNamespace;
import com.abstratt.mdd.internal.frontend.textuml.node.AWildcardType;
import com.abstratt.mdd.internal.frontend.textuml.node.AWordyBlock;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.PAnnotations;
import com.abstratt.mdd.internal.frontend.textuml.node.PInitializationExpression;
import com.abstratt.mdd.internal.frontend.textuml.node.POperationHeader;
import com.abstratt.mdd.internal.frontend.textuml.node.PQualifiedIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.TAbstract;
import com.abstratt.mdd.internal.frontend.textuml.node.TExternal;
import com.abstratt.mdd.internal.frontend.textuml.node.TModelComment;
import com.abstratt.mdd.internal.frontend.textuml.node.TNavigable;
import com.abstratt.mdd.internal.frontend.textuml.node.TUri;

/**
 * This tree visitor will generate the structural model for a given input.
 */
public class StructureGenerator extends AbstractGenerator {

	/**
	 * Global annotation processor - prefer using a local one,
	 * if possible (application and processing invoked from the same 
	 * method), as that avoids conflicts between different usages of annotations.
	 * 
	 * @see #processAnnotations(PAnnotations, Element) 
	 */
	private AnnotationProcessor annotationProcessor;

	private String currentComment;

	private ModifierProcessor modifierProcessor;

	public StructureGenerator(CompilationContext context) {
		super(context);
		annotationProcessor = new AnnotationProcessor(getRefTracker(), problemBuilder);
		modifierProcessor = new ModifierProcessor(sourceMiner);
	}

	private void applyCurrentComment(Element element) {
		if (currentComment != null) {
			Comment newComment = element.createOwnedComment();
			newComment.setBody(currentComment);
			newComment.getAnnotatedElements().add(element);
		}
		currentComment = null;
	}
	
	/**
	 * Applies the current modifiers to the given element.
	 * 
	 * @param defaultVisibility
	 * @param node 
	 */
	private void applyModifiers(Feature feature, VisibilityKind defaultVisibility, final Node node) {
		feature.unsetVisibility();
		for (Modifier current : modifierProcessor.getModifiers(true)) {
			switch (current) {
			case STATIC:
				feature.setIsStatic(true);
				break;
			case ABSTRACT:
				if (feature instanceof BehavioralFeature)
					((BehavioralFeature) feature).setIsAbstract(true);
				else
					problemBuilder.addError("Invalid modifier: '" + current + "'", node);
				break;
			case PACKAGE:
			case PRIVATE:
			case PUBLIC:
			case PROTECTED:
				if (feature.isSetVisibility())
					problemBuilder.addError("Only one visibility modifier should be specified", node);
				else
					feature.setVisibility(getVisibility(current, null));
				break;
			case DERIVED:
				if (feature instanceof Property) {
					final Property property = ((Property) feature);
					property.setIsDerived(true);
				}
				else
					problemBuilder.addError("Invalid modifier: '" + current + "'", node);
				break;
			case ID:
				if (feature instanceof Property) {
					final Property property = ((Property) feature);
					property.setIsID(true);
					getRefTracker().add(new IDeferredReference() {
						@Override
						public void resolve(IBasicRepository repository) {
							if (property.getLower() != 1 || property.getUpper() != 1)
								problemBuilder.addProblem(new IdsShouldBeRequiredSingle(), node);
						}
					}, Step.WARNINGS);
				} else
					problemBuilder.addError("Invalid modifier: '" + current + "'", node);
				break;
			case READONLY:
				if (feature instanceof Property)
					((Property) feature).setIsReadOnly(true);
				else
					problemBuilder.addError("Invalid modifier: '" + current + "'", node);
				break;
			default:
				problemBuilder.addError("Modifier not expected in this context: '" + current + "'", node);
			}
		}
		if (!feature.isSetVisibility())
			feature.setVisibility(defaultVisibility);
	}

	@Override
	public final void caseAApplyProfileDecl(final AApplyProfileDecl node) {
		final Package currentPackageSnapshot = namespaceTracker.currentPackage();
		String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getQualifiedIdentifier());
		getRefTracker().add(
						new DeferredReference<Profile>(qualifiedIdentifier, IRepository.PACKAGE.getProfile(),
										currentPackageSnapshot) {
							@Override
							protected void onBind(Profile appliedProfile) {
								if (appliedProfile == null) {
									problemBuilder.addError("Unknown namespace: '" + getSymbolName() + "'", node
													.getQualifiedIdentifier());
									return;
								}
								// a profile can only be applied after it has
								// been defined
								if (!appliedProfile.isDefined()) {
									problemBuilder.addError("Profile has not been defined: '"
													+ appliedProfile.getQualifiedName() + "'", node
													.getQualifiedIdentifier());
									return;
								}
								if (!currentPackageSnapshot.getAppliedProfiles().contains(appliedProfile))
									currentPackageSnapshot.applyProfile(appliedProfile);
								// import the profile as well (note: this is a
								// convenience only,
								// not suggested or mandated by the UML spec)
								if (!currentPackageSnapshot.getImportedPackages().contains(appliedProfile))
									currentPackageSnapshot.createPackageImport(appliedProfile,
													VisibilityKind.PRIVATE_LITERAL);
							}
						}, IReferenceTracker.Step.PROFILE_APPLICATIONS);
	}

	private boolean ensureNameAvailable(String name, Node node, EClass... eClasses) {
		Namespace namespace = namespaceTracker.currentNamespace(null);
		if (name == null || namespace == null)
			return true;
		for (EClass expected : eClasses) {
			final NamedElement found = namespace.getMember(name, false, expected);
			if (found != null && EcoreUtil.equals(found.getNamespace(), namespace)) {
				problemBuilder.addProblem( new DuplicateSymbol(name, found.eClass()), node);
				return false;
			}
		}
		return true;
	}

	@Override
	public void caseAAssociationDef(final AAssociationDef node) {
		String associationName = TextUMLCore.getSourceMiner().getIdentifier(node.getAssociationHeader());
		if (!ensureNameAvailable(associationName, node.getAssociationHeader(), Literals.TYPE))
			return;
		Package parent = namespaceTracker.currentPackage();
		final Association association =
						(Association) parent.createPackagedElement(associationName, IRepository.PACKAGE
										.getAssociation());
		applyCurrentComment(association);
		processAnnotations(node.getAnnotations(), association);
		namespaceTracker.enterNamespace(association);
		try {
			node.getAssociationRoleDeclList().apply(this);
			// can only check number of ends in step two since non-owned ends
			// are added in step one
			getRefTracker().add(new IDeferredReference() {
				public void resolve(IBasicRepository repository) {
					// XXX implementation limitation - only binary associations
					// are supported
					if (association.getMemberEnds().size() != 2)
						problemBuilder.addProblem( new WrongNumberOfRoles(2, association.getMemberEnds().size()), node
										.getAssociationHeader());
					else if (!association.getMemberEnds().get(0).isNavigable() && !association.getMemberEnds().get(1).isNavigable())
						problemBuilder.addProblem( new UnclassifiedProblem("At least one of the ends must be navigable"), node
										.getAssociationHeader());
				}
			}, IReferenceTracker.Step.GENERAL_RESOLUTION);
			node.getAssociationHeader().apply(this);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}

	@Override
	public void caseAAssociationHeader(final AAssociationHeader node) {
		final Association association = ((Association) namespaceTracker.currentNamespace(null));
		// sets the aggregation kind - in step 2 as non-owned ends are added in
		// step 1
		getRefTracker().add(new IDeferredReference() {
			public void resolve(IBasicRepository repository) {
				if (association.getMemberEnds().size() != 2)
					return;
				node.getAssociationKind().apply(new DepthFirstAdapter() {
					@Override
					public void caseAAggregationAssociationKind(AAggregationAssociationKind node) {
						// the first end declared is the parent by convention
						Property memberEnd = association.getMemberEnds().get(0);
						memberEnd.setAggregation(AggregationKind.SHARED_LITERAL);
					}

					@Override
					public void caseACompositionAssociationKind(ACompositionAssociationKind node) {
						// the first end declared is the parent by convention
						Property memberEnd = association.getMemberEnds().get(0);
						memberEnd.setAggregation(AggregationKind.COMPOSITE_LITERAL);
					}
				});
			}
		}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}

	/**
	 * A reference is a short-hand for an anonymous association with one
	 * member-owned navigable end.
	 * 
	 * For instance, this:
	 * 
	 * <pre>
	 * class Foo
	 * 		reference bar : Bar;
	 * end;
	 * </pre>
	 * 
	 * is a shorthand for:
	 * 
	 * <pre>
	 * class Foo
	 * 		property bar : Bar;
	 * end;
	 * 
	 * association
	 * 		navigable role Foo.bar;
	 *      role &lt;null&gt; : Foo;  
	 * end;
	 * </pre>
	 * 
	 */
	@Override
	public void caseAReferenceDecl(final AReferenceDecl node) {
		final String propertyName = Util.stripEscaping(node.getIdentifier().getText());
		if (!ensureNameAvailable(propertyName, node, Literals.PROPERTY))
			return;
		final Property[] referrent = { null };
		final Classifier referringClassifier = (Classifier) namespaceTracker.currentNamespace(null);
		
		final Association newAssociation =
						(Association) referringClassifier.getNearestPackage().createPackagedElement(null,
										UMLPackage.Literals.ASSOCIATION);
		if (referringClassifier instanceof Class)
			referrent[0] = ((Class) referringClassifier).createOwnedAttribute(propertyName, null);
		else if (referringClassifier instanceof Interface)
			referrent[0] = ((Interface) referringClassifier).createOwnedAttribute(propertyName, null);
		else if (referringClassifier instanceof DataType)
			referrent[0] = ((DataType) referringClassifier).createOwnedAttribute(propertyName, null);
		else if (referringClassifier instanceof Signal)
			referrent[0] = ((Signal) referringClassifier).createOwnedAttribute(propertyName, null);
		else {
			problemBuilder.addError("Unexpected context: '"
					+ referringClassifier.getQualifiedName() + "'", node
					.getIdentifier());
		}
		if (node.getReferenceType() instanceof AAggregationReferenceType)
			referrent[0].setAggregation(AggregationKind.SHARED_LITERAL);
		else if (node.getReferenceType() instanceof ACompositionReferenceType)
			referrent[0].setAggregation(AggregationKind.COMPOSITE_LITERAL);
		applyCurrentComment(referrent[0]);
		annotationProcessor.applyAnnotations(referrent[0], node.getIdentifier());
		applyOptionalSubsetting(referrent[0], node);
		newAssociation.getMemberEnds().add(referrent[0]);
		// anonymous end
		newAssociation.createOwnedEnd(null, referringClassifier);
		applyModifiers(referrent[0], VisibilityKind.PUBLIC_LITERAL, node);
		new DeferredTypeSetter(sourceContext, referringClassifier, referrent[0]).process(node.getTypeIdentifier());
	}

	@Override
	public void caseADependencyDecl(ADependencyDecl node) {
		final Classifier referringClassifier = (Classifier) namespaceTracker.currentNamespace(null);
		final Dependency newDependency = (Dependency) referringClassifier
				.getNearestPackage().createPackagedElement(null,
						UMLPackage.Literals.DEPENDENCY);
		newDependency.getClients().add(referringClassifier);
		annotationProcessor.applyAnnotations(newDependency, node.getDependency());
		new DeferredCollectionFiller(sourceContext, referringClassifier, newDependency.getSuppliers()).process(node.getTypeIdentifier());
	}

	@Override
	public void caseAAssociationRoleDecl(final AAssociationRoleDecl node) {
		super.caseAAssociationRoleDecl(node);
		final boolean[] navigable = { false };
		node.getAssociationModifiers().apply(new DepthFirstAdapter() {
			@Override
			public void caseTNavigable(TNavigable node) {
				navigable[0] = true;
			}
		});
		final Association association = (Association) namespaceTracker.currentNamespace(null);
		node.getAssociationEnd().apply(new DepthFirstAdapter() {
			@Override
			public void caseAAssociationMemberEnd(final AAssociationMemberEnd memberNode) {
				final String classifierName = TextUMLCore.getSourceMiner().getQualifiedIdentifier(
						memberNode.getClassifier());
				final String propertyName = Util.stripEscaping(memberNode.getProperty().getText());
				if (node.getAnnotations() != null)
					problemBuilder.addWarning("Ignored annotations on member association end: " + propertyName,
							memberNode);
				getRefTracker().add(
						new DeferredReference<Classifier>(classifierName, IRepository.PACKAGE.getClassifier(), namespaceTracker
								.currentPackage()) {
							@Override
							protected void onBind(Classifier associationParticipant) {
								if (associationParticipant == null) {
									problemBuilder.addProblem(new UnknownType(getSymbolName()), memberNode
											.getClassifier());
									return;
								}
								Property property = FeatureUtils.findAttribute(associationParticipant, propertyName, false, true);
								if (property == null) {
									problemBuilder.addError("Could not find property: '" + propertyName
											+ "' in classifier '" + classifierName + "'", memberNode.getClassifier());
									return;
								}
								property.setAssociation(association);
							}
						}, IReferenceTracker.Step.GENERAL_RESOLUTION);
			}

			@Override
			public void caseAAssociationOwnedEnd(final AAssociationOwnedEnd ownedEndNode) {
				final String endIdentifier = TextUMLCore.getSourceMiner().getIdentifier(ownedEndNode.getIdentifier());
				// postpone creation as that is required for the member end case and we don't want to invert order (bug 3002575)
				getRefTracker().add(new IDeferredReference() {
					@Override
					public void resolve(IBasicRepository repository) {
						if (!ensureNameAvailable(endIdentifier, ownedEndNode.getIdentifier(), Literals.PROPERTY))
							return;
						Property ownedEnd;
						if (navigable[0])
							ownedEnd = association.createNavigableOwnedEnd(endIdentifier, null);
						else
							ownedEnd = association.createOwnedEnd(endIdentifier, null);
						new TypeSetter(sourceContext, association, ownedEnd).process(ownedEndNode.getTypeIdentifier());
						AAssociationRoleDecl roleDeclaration = sourceMiner.findParent(ownedEndNode, AAssociationRoleDecl.class);
						processAnnotations(roleDeclaration.getAnnotations(), ownedEnd);
						CommentUtils.applyComment(roleDeclaration.getModelComment(), ownedEnd);
					}
				}, IReferenceTracker.Step.GENERAL_RESOLUTION);
			}
		});
	}
	
	@Override
	public void caseAPortDecl(final APortDecl node) {
		if (node.getIdentifier() == null && node.getPortConnector() == null) {
			problemBuilder.addProblem(new AnonymousDisconnectedPort(), node.getPort());
			return;
		}
		final String portIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
		if (!ensureNameAvailable(portIdentifier, node, Literals.PROPERTY))
			return;
		final Class owningClass = (Class) namespaceTracker.currentNamespace(UMLPackage.Literals.CLASS);
		final Port newPort = owningClass.createOwnedPort(portIdentifier, null);
		newPort.setIsService(false);
		fillDebugInfo(newPort, node);
		applyCurrentComment(newPort);
		String interfaceName = sourceMiner.getIdentifier(node.getMinimalTypeIdentifier());
		getRefTracker().add(new DeferredReference<Interface>(interfaceName, UMLPackage.Literals.INTERFACE, owningClass) {
			@Override
			protected void onBind(Interface target) {
				if (target == null) {
					problemBuilder.addError("Could not find interface: '" + getSymbolName() + "'", node.getMinimalTypeIdentifier());
					return;
				}
				Class portType = (Class) owningClass.createNestedClassifier("", UMLPackage.Literals.CLASS);
				boolean provided = node.getPortModifier() instanceof AProvidedPortModifier;
				if (provided) {
					portType.createInterfaceRealization(null, target);
				} else {
					portType.createInterfaceRealization(null, target);
					newPort.setIsConjugated(true);
				}
				newPort.setType(portType);
				if (node.getPortConnector() != null) {
					ConnectorProcessor connectorProcessor = new ConnectorProcessor(sourceContext, owningClass);
					connectorProcessor.addEnd(newPort);
					connectorProcessor.process((AConnectorEndList) ((APortConnector) node.getPortConnector()).getConnectorEndList());
				}
				if (!provided && !ProjectPropertyHelper.isLibrary(context.getRepositoryProperties()))
					getRefTracker().add(new IDeferredReference() {
						
						@Override
						public void resolve(IBasicRepository repository) {
							Property provider = ConnectorUtils.findProvidingPart(newPort);
							if (provider == null)
								problemBuilder.addProblem(new RequiredPortHasNoMatchingProviderPort(getSymbolName()), node.getMinimalTypeIdentifier());
						}
					},Step.STRUCTURE_VALIDATION);
			}
		}, IReferenceTracker.Step.GENERAL_RESOLUTION);
		applyModifiers(newPort, VisibilityKind.PUBLIC_LITERAL, node);
		annotationProcessor.applyAnnotations(newPort, node.getIdentifier());
	}
	
	@Override
	public void caseAConnectorDecl(AConnectorDecl node) {
		Namespace currentNamespace = namespaceTracker.currentNamespace(null);
		if (!(currentNamespace instanceof Component)) {
			problemBuilder.addProblem(new UnclassifiedProblem("Connectors can only be defined by components"), node);
			return;
		}
		Component asComponent = (Component) currentNamespace;
		ConnectorProcessor connectorProcessor = new ConnectorProcessor(sourceContext, asComponent);
		connectorProcessor.process((AConnectorEndList) node.getConnectorEndList());
	}

	@Override
	public final void caseAAttributeDecl(AAttributeDecl node) {
		final String attributeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
		if (!ensureNameAvailable(attributeIdentifier, node, Literals.STRUCTURAL_FEATURE))
			return;
		// do not call super as we deal with everything here
		final Property newProperty;
		final Namespace currentNamespace = namespaceTracker.currentNamespace(null);
		if (currentNamespace instanceof Class)
			newProperty = ((Class) currentNamespace).createOwnedAttribute(attributeIdentifier, null);
		else if (currentNamespace instanceof Interface)
			newProperty = ((Interface) currentNamespace).createOwnedAttribute(attributeIdentifier, null);
		else if (currentNamespace instanceof DataType)
			newProperty = ((DataType) currentNamespace).createOwnedAttribute(attributeIdentifier, null);
		else if (currentNamespace instanceof Signal)
			newProperty = ((Signal) currentNamespace).createOwnedAttribute(attributeIdentifier, null);
		else {
			problemBuilder.addProblem( new UnclassifiedProblem("Cannot declare attribute under namespace: " + currentNamespace.getName()), node);
			return;
		}
		fillDebugInfo(newProperty, node);
		applyCurrentComment(newProperty);
		new DeferredTypeSetter(sourceContext, namespaceTracker.currentNamespace(null), newProperty) {
			public void doProcess(Node node) {
				super.doProcess(node); 
				if ((currentNamespace instanceof DataType || currentNamespace instanceof Signal || currentNamespace instanceof StateMachine) && newProperty.getType() != null) {
					Type propertyType = newProperty.getType();
					if (!(propertyType instanceof DataType || propertyType instanceof Signal || propertyType instanceof StateMachine || propertyType instanceof Enumeration) && !BasicTypeUtils.isBasicType(newProperty.getType())) 
					    problemBuilder.addProblem(new UnclassifiedProblem("Attribute must be primitive, datatype, state machine or enumeration: " + newProperty.getName()), node);
				}
			}
		}.process(node.getTypeIdentifier());
		
		applyModifiers(newProperty, VisibilityKind.PUBLIC_LITERAL, node);
		final PInitializationExpression initializationExpression = node.getInitializationExpression();
		if (newProperty.isDerived()) {
			if (!newProperty.isID() && initializationExpression == null) {
				problemBuilder.addProblem(new MissingDefaultValue(), node);
				return;
			}
		}
		// for non-constant attributes, initialization expression is optional 
		if (initializationExpression instanceof ASimpleInitializationExpression) {
			SimpleInitializationExpressionProcessor expressionProcessor = new SimpleInitializationExpressionProcessor(sourceContext, currentNamespace);
			ASimpleInitialization simpleInitialization = (ASimpleInitialization) ((ASimpleInitializationExpression) initializationExpression).getSimpleInitialization();
			expressionProcessor.process(node.getTypeIdentifier(), newProperty, simpleInitialization.getLiteralOrIdentifier());
		} else if (initializationExpression instanceof AComplexInitializationExpression) {
		    getRefTracker().add(new IDeferredReference() {
		        @Override
		        public void resolve(IBasicRepository repository) {
                    // required for resolving behavior
                    Class nearestClass = (Class) MDDUtil.getNearest(newProperty, UMLPackage.Literals.CLASS);
		            ComplexInitializationExpressionProcessor expressionProcessor = new ComplexInitializationExpressionProcessor(sourceContext, nearestClass);
		            expressionProcessor.process(newProperty, (AComplexInitializationExpression) initializationExpression);
		        }
		    }, IReferenceTracker.Step.LAST);
        } 
		annotationProcessor.applyAnnotations(newProperty, node.getIdentifier());
		applyOptionalSubsetting(newProperty, node);
	}

	private void applyOptionalSubsetting(final Property newProperty, Node node) {
		node.apply(new DepthFirstAdapter() {
			@Override
			public void caseAOptionalSubsetting(final AOptionalSubsetting node) {
				final String subsettedName = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getQualifiedIdentifier());
				getRefTracker().add(new IDeferredReference() {
					public void resolve(IBasicRepository repository) {
						Property subsettedProperty =
										(Property) getRepository().findNamedElement(subsettedName,
														IRepository.PACKAGE.getProperty(),
														newProperty.getNearestPackage());
						if (subsettedProperty == null) {
							problemBuilder.addProblem( new UnresolvedSymbol(subsettedName), node
											.getQualifiedIdentifier());
							return;
						}
						if (subsettedProperty.getType() == null || newProperty.getType() == null)
							// no type found, ignore subsetting
							return;
						if (!TypeUtils.isCompatible(repository, newProperty.getType(), subsettedProperty.getType(),
										null)) {
							problemBuilder.addProblem( new TypeMismatch(MDDUtil.getDisplayName(subsettedProperty),
											MDDUtil.getDisplayName(newProperty)), node);
							return;
						}
						if (!TypeUtils.isCompatible(repository, newProperty.getClass_(), subsettedProperty.getClass_(),
										null)) {
							problemBuilder.addProblem( new UnclassifiedProblem("'" + newProperty.getClass_().getQualifiedName() + "' cannot subset property from unrelated type '" + subsettedProperty.getClass_().getQualifiedName() + "'"), node);
							return;
						}
						newProperty.getSubsettedProperties().add(subsettedProperty);
					}

				}, IReferenceTracker.Step.GENERAL_RESOLUTION);
			}
		});
	}

	@Override
	public final void caseAClassDef(AClassDef node) {
		final String classifierIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getClassHeader());
		if (!ensureNameAvailable(classifierIdentifier, node.getClassHeader(), Literals.TYPE))
			return;
		try {
			annotationProcessor.process(node.getAnnotations());
			super.caseAClassDef(node);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}
	
	@Override
	public void caseAStateMachineDecl(AStateMachineDecl node) {
		new StateMachineProcessor(sourceContext, (BehavioredClassifier) namespaceTracker.currentNamespace(UMLPackage.Literals.BEHAVIORED_CLASSIFIER)).process(node);
	}

	@Override
	public final void caseAClassHeader(AClassHeader node) {
		final String classifierIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
		// creates classifier and enters namespace
		node.getClassType().apply(this);
		final Classifier currentClassifier = (Classifier) namespaceTracker.currentNamespace(null);
		applyCurrentComment(currentClassifier);
		fillDebugInfo(currentClassifier, node);
		currentClassifier.setName(classifierIdentifier);
		node.getClassModifiers().apply(new DepthFirstAdapter() {
			@Override
			public void caseTAbstract(TAbstract node) {
				currentClassifier.setIsAbstract(true);
			}
			@Override
			public void caseTExternal(TExternal node) {
				MDDExtensionUtils.makeExternal(currentClassifier);
			}
		});
		annotationProcessor.applyAnnotations(currentClassifier, node.getIdentifier());
		final TemplateProcessor templateProcessor =
						new TemplateProcessor(currentClassifier, IRepository.PACKAGE.getRedefinableTemplateSignature(),
										IRepository.PACKAGE.getClassifierTemplateParameter(), IRepository.PACKAGE
														.getClass_());
		templateProcessor.process(node.getOptionalFormalTemplateParameters());
		node.getClassSpecializesSection().apply(this);
		node.getClassImplementsSection().apply(this);
		if (context.getSourcePath() != null)
			currentClassifier.createEAnnotation(MDDUtil.UNIT).getDetails().put("name", context.getSourcePath());
	}

	@Override
	public final void caseAClassImplementsItem(final AClassImplementsItem node) {
		if (!(namespaceTracker.currentNamespace(null) instanceof BehavioredClassifier)) {
			problemBuilder
					.addError(
							"'"
									+ namespaceTracker.currentNamespace(null).getName()
									+ "' is not a behaviored classifier. Only behaviored classifiers (i.e. classes, stereotypes) can realize interfaces'",
							node.getMinimalTypeIdentifier());
			throw new AbortedCompilationException();
		}
		final BehavioredClassifier implementingClassifier = (BehavioredClassifier) namespaceTracker.currentNamespace(null);
		String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getMinimalTypeIdentifier());
		getRefTracker()
						.add(
										new DeferredReference<Interface>(qualifiedIdentifier, IRepository.PACKAGE.getInterface(),
														namespaceTracker.currentPackage()) {
											@Override
											protected void onBind(Interface interface_) {
												if (interface_ == null) {
													problemBuilder.addError("Could not find interface: '"
																	+ getSymbolName() + "'", node
																	.getMinimalTypeIdentifier());
													return;
												}
												InterfaceRealization realization = implementingClassifier.createInterfaceRealization(null, interface_);
												processAnnotations(node.getAnnotations(), realization);
											}
										}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}

	@Override
	public final void caseAClassSpecializesItem(final AClassSpecializesItem node) {
		final Classifier currentClassSnapshot = (Classifier) namespaceTracker.currentNamespace(null);
		String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getSingleTypeIdentifier());
		TemplateBindingProcessor<Classifier, Type> tbp = new TemplateBindingProcessor<Classifier, Type>();
		tbp.process(node.getSingleTypeIdentifier());
		final List<PQualifiedIdentifier> parameterIdentifiers = tbp.getParameterIdentifiers();
		getRefTracker().add(
						new DeferredReference<Classifier>(qualifiedIdentifier, IRepository.PACKAGE.getClassifier(),
										currentClassSnapshot) {
							@Override
							protected void onBind(Classifier superClass) {
								if (superClass == null) {
									problemBuilder.addProblem( new UnresolvedSymbol(getSymbolName()), node
													.getSingleTypeIdentifier());
									return;
								}
								if (!currentClassSnapshot.maySpecializeType(superClass)) {
									problemBuilder.addProblem( new CannotSpecializeClassifier(superClass.getQualifiedName(), currentClassSnapshot.getQualifiedName()), node
													.getSingleTypeIdentifier());
									return;
								}
								if (parameterIdentifiers != null) {
									// the super classifier is a template
									List<Type> parameters = new ArrayList<Type>(parameterIdentifiers.size());
									boolean allTemplateParameters = false;
									for (PQualifiedIdentifier parameterIdNode : parameterIdentifiers) {
										String parameterId = TextUMLCore.getSourceMiner().getQualifiedIdentifier(parameterIdNode);
										Classifier parameter =
														(Classifier) getRepository().findNamedElement(parameterId,
																		superClass.eClass(), currentClassSnapshot);
										if (parameter == null) {
											problemBuilder.addProblem( new UnresolvedSymbol(parameterId),
															parameterIdNode);
											return;
										}
										if (parameters.isEmpty())
											// the first parameters sets the
											// tone
											allTemplateParameters = parameter.isTemplateParameter();
										else if (allTemplateParameters ^ parameter.isTemplateParameter()) {
											problemBuilder
															.addProblem(new UnclassifiedProblem(
																			"Cannot mix formal and actual parameters"),
																			parameterIdNode);
											return;
										}
										parameters.add(parameter);
									}
									if (allTemplateParameters) {
										TemplateUtils.createSubclassTemplateBinding(superClass, currentClassSnapshot,
														parameters);
									} else {
										// replace a reference to it with one
										// one to a newly
										// created bound element
										Classifier bound = TemplateUtils.createBinding(currentClassSnapshot.getNearestPackage(), superClass, parameters);
										bound.setName(TemplateUtils.generateBoundElementName(superClass, parameters));
										superClass = bound;
									}
								}
								Generalization generalization = currentClassSnapshot.createGeneralization(superClass);
								processAnnotations(node.getAnnotations(), generalization);
							}
						}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}

	@Override
	public void caseAEmptyClassSpecializesSection(final AEmptyClassSpecializesSection node) {
		Classifier currentClassSnapshot = (Classifier) namespaceTracker.currentNamespace(null);
		if (IRepository.PACKAGE.getClass_() != currentClassSnapshot.eClass())
			// not a class
			return;
		createGeneralization(TypeUtils.makeTypeName("Object"), currentClassSnapshot, Literals.CLASS, node.parent());
	}
	
	@Override
	public void caseAPrimitiveDef(APrimitiveDef node) {
		final String primitiveName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
		PrimitiveType newPrimitiveType =
			(PrimitiveType) namespaceTracker.currentPackage().createPackagedElement(primitiveName, IRepository.PACKAGE
							.getPrimitiveType());
		newPrimitiveType.setName(primitiveName);
		annotationProcessor.process(node.getAnnotations());
		annotationProcessor.applyAnnotations(newPrimitiveType, node);
		applyCurrentComment(newPrimitiveType);
	}
	
	/**
	 * Helper method that creates generalizations.
	 */
	private void createGeneralization(String baseTypeName, final Classifier classifier, EClass superMetaClass, final Node contextNode) {
		boolean extendBaseObject = Boolean.TRUE.toString().equals(context.getRepositoryProperties().get(IRepository.EXTEND_BASE_OBJECT));
		if (!extendBaseObject)
			return;
		if (superMetaClass == null)
			superMetaClass = classifier.eClass();
		getRefTracker().add(new DeferredReference<Classifier>(baseTypeName, superMetaClass, namespaceTracker.currentPackage()) {
			@Override
			protected void onBind(Classifier valueClass) {
				if (valueClass == classifier)
					// avoid creating a specialization on itself
					return;
				if (valueClass == null)
					problemBuilder.addProblem( new UnresolvedSymbol(getSymbolName()), contextNode);
				else
					classifier.createGeneralization(valueClass);
			}
		}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}
	
	@Override
	public void caseAInvariantDecl(AInvariantDecl node) {
	    // ignore - handled by behavior generator
		// but discard annotations so they don't leak to another element	
		annotationProcessor.discardAnnotations();	
	}

	@Override
	public void caseAEnumerationLiteralDecl(final AEnumerationLiteralDecl node) {
		String literalName = TextUMLCore.getSourceMiner().getIdentifier(node);
		if (!ensureNameAvailable(literalName, node.getIdentifier(), Literals.ENUMERATION_LITERAL))
			return;
		final Enumeration enumeration = (Enumeration) namespaceTracker.currentNamespace(null);
        final EnumerationLiteral literal = enumeration.createOwnedLiteral(literalName);
		applyCurrentComment(literal);
		if (node.getEnumerationLiteralSlotValues() != null) {
		    getRefTracker().add(new IDeferredReference() {
                @Override
                public void resolve(IBasicRepository repository) {
                    node.getEnumerationLiteralSlotValues().apply(new DepthFirstAdapter() {
                        @Override
                        public void caseANamedSimpleValue(ANamedSimpleValue node) {
                            String attributeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
                            Property attribute = FeatureUtils.findAttribute(enumeration, attributeIdentifier, false, true);
                            if (attribute == null) {
                                problemBuilder.addProblem(new UnknownAttribute(enumeration.getName(), attributeIdentifier, false),
                                        node.getIdentifier());
                                return;
                            }
                            for (Slot slot : literal.getSlots())
                                if (slot.getDefiningFeature() == attribute) {
                                    problemBuilder.addProblem(new UnclassifiedProblem("A value has already been assigned for property '"
                                            + attribute.getName() + "'"), node.getIdentifier());
                                    return;
                                }
                            ValueSpecification value = LiteralValueParser.parseLiteralValue(node.getLiteralOrIdentifier(),
                                    enumeration.getNearestPackage(), problemBuilder);
                            if (attribute.getType() != value.getType())
                                problemBuilder.addProblem(new TypeMismatch(attribute.getType().getQualifiedName(), value.getType()
                                        .getQualifiedName()), node);

                            Slot slot = literal.createSlot();
                            slot.getValues().add(value);
                            slot.setDefiningFeature(attribute);
                        }
                    });
                }
            }, Step.GENERAL_RESOLUTION);
		}
	}

	@Override
	public void caseAFeatureDecl(AFeatureDecl node) {
		currentComment = null;
		annotationProcessor.process(node.getAnnotations());
		super.caseAFeatureDecl(node);
	}

	@Override
	public void caseAFunctionDecl(AFunctionDecl node) {
		final String identifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
		Package parent = (Package) this.namespaceTracker.currentNamespace(null);
		FunctionBehavior function =
						(FunctionBehavior) parent.createPackagedElement(identifier, IRepository.PACKAGE
										.getFunctionBehavior());
		namespaceTracker.enterNamespace(function);
		try {
			super.caseAFunctionDecl(node);
			annotationProcessor.applyAnnotations(function, node.getIdentifier());
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}

	@Override
	public void caseAImportDecl(final AImportDecl node) {
		final Package currentPackageSnapshot = namespaceTracker.currentPackage();
		modifierProcessor.collectModifierToken(node.getOptionalImportModifier());
		final Set<Modifier> tmpModifiers = modifierProcessor.getModifiers(true);
		final VisibilityKind importVisibility =
						tmpModifiers.isEmpty() ? VisibilityKind.PUBLIC_LITERAL : getVisibility(tmpModifiers.iterator().next(),
										VisibilityKind.PUBLIC_LITERAL);
		String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getQualifiedIdentifier());
		final String alias;
		if (node.getOptionalAlias() instanceof AOptionalAlias) {
			AOptionalAlias optionalAlias = (AOptionalAlias) node.getOptionalAlias();
			alias = Util.stripEscaping(optionalAlias.getIdentifier().getText());
		} else
			alias = null;
		getRefTracker().add(
						new DeferredReference<NamedElement>(qualifiedIdentifier, IRepository.PACKAGE.getNamedElement(),
										namespaceTracker.currentPackage()) {
							@Override
							protected void onBind(NamedElement element) {
								if (element == null) {
									problemBuilder.addProblem( new UnresolvedSymbol(getSymbolName()), node
													.getQualifiedIdentifier());
									return;
								}
								if (element instanceof Package) {
									if (alias != null) {
										problemBuilder.addError("Imported packages cannot be aliased", node
														.getQualifiedIdentifier());
										return;
									}

									Package importedPackage = (Package) element;
									if (currentPackageSnapshot.getImportedPackages().contains(importedPackage))
										// just ignore 
										return;
									PackageImport created = currentPackageSnapshot.createPackageImport(importedPackage);
									created.setVisibility(importVisibility);
								} else {
									PackageableElement packageableElement = (PackageableElement) element;
									ElementImport elementImport =
													currentPackageSnapshot.createElementImport(packageableElement);
									elementImport.setVisibility(importVisibility);
									if (alias != null)
										elementImport.setAlias(alias);
								}
							}
						}, IReferenceTracker.Step.PACKAGE_IMPORTS);
	}
	
	private <T extends Classifier> T createClassifier(EClass typeClass) {
		Package parent = (Package) namespaceTracker.currentNamespace(null);
		T newType = (T) parent.createOwnedType(null, typeClass);
		namespaceTracker.enterNamespace(newType);
		return newType;
	}
	
	@Override
	public final void caseAInterfaceClassType(AInterfaceClassType node) {
		super.caseAInterfaceClassType(node);
		createClassifier(UMLPackage.Literals.INTERFACE);
	}
	
	@Override
	public final void caseADatatypeClassType(ADatatypeClassType node) {
		super.caseADatatypeClassType(node);
		DataType newDataType = createClassifier(UMLPackage.Literals.DATA_TYPE);
		createGeneralization(TypeUtils.makeTypeName("Value"), newDataType, Literals.DATA_TYPE, node);
	}
	
	@Override
	public final void caseAClassClassType(AClassClassType node) {
		super.caseAClassClassType(node);
		createClassifier(UMLPackage.Literals.CLASS);
	}
	
	public void caseASignalClassType(ASignalClassType node) {
		super.caseASignalClassType(node);
		createClassifier(UMLPackage.Literals.SIGNAL);
	}

	@Override
	public void caseAComponentClassType(AComponentClassType node) {
		super.caseAComponentClassType(node);
		createClassifier(UMLPackage.Literals.COMPONENT);
	}

	@Override
	public void caseAEnumerationClassType(AEnumerationClassType node) {
	    super.caseAEnumerationClassType(node);
	    Classifier newEnumeration = createClassifier(UMLPackage.Literals.ENUMERATION);
        createGeneralization(TypeUtils.makeTypeName("Value"), newEnumeration, Literals.DATA_TYPE, node);
	}
	 
	@Override
	public final void caseAActorClassType(AActorClassType node) {
		super.caseAActorClassType(node);
		createClassifier(UMLPackage.Literals.ACTOR);
	}

	@Override
	public final void caseALoadDecl(ALoadDecl node) {
		super.caseALoadDecl(node);
		final TUri uriNode = node.getUri();
		String uriText = uriNode.getText();
		// remove brackets
		final String uri = uriText.substring(2).substring(0, uriText.length() - 4);
		// defer package resolution
		getRefTracker().add(new IDeferredReference() {
			public void resolve(IBasicRepository repository) {
				// TODO maybe allow package loading IBasicRepository to avoid casting 
				Package loaded = ((IRepository) repository).loadPackage(URI.createURI(uri));
				if (loaded != null)
					namespaceTracker.currentPackage().createPackageImport(loaded, VisibilityKind.PRIVATE_LITERAL);
				else
				    problemBuilder.addProblem(new CannotLoadFromLocation(uri), uriNode);
			}
		}, IReferenceTracker.Step.PACKAGE_IMPORTS);
	}

	@Override
	public void caseAModifiers(AModifiers node) {
		modifierProcessor.process(node);
	}
	
	@Override
	public void caseAReceptionDecl(final AReceptionDecl node) {
		final String receptionName = TextUMLCore.getSourceMiner().getIdentifier(node.getReceptionName());
		final Classifier parent = (Classifier) this.namespaceTracker.currentNamespace(null);
		final Reception reception = ReceptionUtils.createReception(parent, receptionName);
		if (reception == null) {
			problemBuilder.addError("Unexpected context for a reception", node.getReception());
			throw new AbortedCompilationException();
		}
		fillDebugInfo(reception, node);
		applyCurrentComment(reception);
		namespaceTracker.enterNamespace(reception);
		try {
			getRefTracker().add(new IDeferredReference() {
				public void resolve(IBasicRepository repository) {
					node.apply(new BehavioralFeatureSignatureProcessor(sourceContext, reception, false, true));
					Type signal = reception.getOwnedParameters().get(0).getType();
					if (!(signal instanceof Signal)) {
						problemBuilder.addError("Reception parameter must be a signal", node.getReception());
						throw new AbortedCompilationException();
					}
					if (ReceptionUtils.findBySignal(parent, (Signal) signal) != null) {
						problemBuilder.addError("Another reception for this signal already defined", node.getReception());
						throw new AbortedCompilationException();
					}
					reception.setSignal((Signal) signal);
				}
			}, IReferenceTracker.Step.GENERAL_RESOLUTION);

		} finally {
			namespaceTracker.leaveNamespace();
		}

	}

	@Override
	public final void caseAOperationDecl(AOperationDecl node) {
		AOperationHeader operationHeader = (AOperationHeader) node.getOperationHeader();
        final String operationName = TextUMLCore.getSourceMiner().getIdentifier(operationHeader.getIdentifier());
		boolean isQuery = operationHeader.getOperationKeyword() instanceof AQueryOperationKeyword;
		boolean hasReturn = sourceMiner.findChild(operationHeader, AOptionalReturnType.class, true) != null;
	    if (isQuery && !hasReturn) {
			problemBuilder.addError("A query operation must have a return type", operationHeader);
			throw new AbortedScopeCompilationException();
	    }
	    
		Classifier parent = (Classifier) this.namespaceTracker.currentNamespace(null);
		Operation operation = FeatureUtils.createOperation(parent, operationName);
		fillDebugInfo(operation, operationHeader);
		applyCurrentComment(operation);
		operation.setIsQuery(isQuery);
		namespaceTracker.enterNamespace(operation);
		try {
			super.caseAOperationDecl(node);
			applyModifiers(operation, VisibilityKind.PUBLIC_LITERAL, node);
			annotationProcessor.applyAnnotations(operation, operationHeader);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}
	
	@Override
	public void caseAWildcardType(AWildcardType node) {
	    super.caseAWildcardType(node);
	    String typeName = TextUMLCore.getSourceMiner().getIdentifier(node);
	    Operation currentOperation = (Operation) namespaceTracker.currentNamespace(UMLPackage.Literals.OPERATION);
	    MDDExtensionUtils.createWildcardType(currentOperation, typeName);
	}

	@Override
	public final void caseAPackageHeading(final APackageHeading node) {
		final String packageName = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getQualifiedIdentifier());
		// duplicate names for packages is fine
		final String parentName = MDDUtil.removeLastSegment(packageName);
		final String simpleName = MDDUtil.getLastSegment(packageName);
		EClass packageType = determinePackageType(node.getPackageType());
		Assert.isNotNull(packageType);
		if (namespaceTracker.currentPackage() != null && parentName != null) {
			// you can nest by physically embedding or reference, but not both
			problemBuilder.addProblem( new NonQualifiedIdentifierExpected(), node.getPackageType());
			throw new AbortedCompilationException();
		}
		boolean topLevel = namespaceTracker.currentPackage() == null && parentName == null;
		Package newPackage;
		if (topLevel) {
			newPackage = getRepository().createTopLevelPackage(packageName, packageType, null);
			if (!MDDUtil.isGenerated(newPackage)) {
				problemBuilder.addProblem( new UnclassifiedProblem("Cannot modify external packages: "
								+ packageName), node.getSemicolon());
				throw new AbortedCompilationException();
			}
		} else {
			if (packageType != Literals.PACKAGE) {
				// models or profiles cannot be nested into parent packages
				problemBuilder.addProblem( new InvalidPackageNesting(), node.getPackageType());
				throw new AbortedCompilationException();
			}
			if (namespaceTracker.currentPackage() != null)
				newPackage = namespaceTracker.currentPackage().createNestedPackage(simpleName);
			else {
				// create a dangling package that has to be attached to a parent
				// later
				final Package orphanPackage = newPackage = IRepository.FACTORY.createPackage();
				orphanPackage.setName(simpleName);
				getRefTracker().add(new DeferredReference<Package>(parentName, Literals.PACKAGE, null) {
					@Override
					protected void onBind(Package parentPackage) {
						if (parentPackage == null) {
							problemBuilder.addProblem( new UnknownParentPackage(parentName), node.getSemicolon());
							return;
						}
						if (!MDDUtil.isGenerated(parentPackage)) {
							problemBuilder.addProblem( new UnclassifiedProblem("Cannot modify external packages: "
											+ packageName), node.getQualifiedIdentifier());
							return;
						}
						parentPackage.getNestedPackages().add(orphanPackage);
					}
				}, IReferenceTracker.Step.PACKAGE_STRUCTURE);
			}
		}
		annotationProcessor.process(node.getAnnotations());
		annotationProcessor.applyAnnotations(newPackage, node.getQualifiedIdentifier());
		if (node.getModelComment() != null)
			node.getModelComment().apply(this);
		applyCurrentComment(newPackage);
		namespaceTracker.enterNamespace(newPackage);
	}

	@Override
	public void caseASimpleSignature(final ASimpleSignature node) {
		final Namespace parentSnapshot = namespaceTracker.currentNamespace(null);
		getRefTracker().add(new IDeferredReference() {
			public void resolve(IBasicRepository repository) {
				node.apply(newSignatureProcessor(parentSnapshot));
			}
		}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}
	
	@Override
	public void caseASignature(final ASignature node) {
		final Namespace parentSnapshot = namespaceTracker.currentNamespace(null);
		getRefTracker().add(new IDeferredReference() {
			public void resolve(IBasicRepository repository) {
				node.apply(newSignatureProcessor(parentSnapshot));
			}
		}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}

	@Override
	public void caseASimpleBlock(ASimpleBlock node) {
		// don't go there, the behavior generator will take care of it
	}
	
	@Override
	public final void caseAStart(AStart node) {
		super.caseAStart(node);
		final Namespace currentNamespace = namespaceTracker.currentNamespace(null);
		if (currentNamespace == null)
			return;
		if (currentNamespace.eClass() == IRepository.PACKAGE.getProfile())
			getRefTracker().add(new IDeferredReference() {
				public void resolve(IBasicRepository repository) {
					// defining must be the last thing to be done to the
					// profile, hence must be deferred
					((Profile) currentNamespace).define();
				}
			}, IReferenceTracker.Step.DEFINE_PROFILES);
	}

	
	@Override
	public final void caseAStereotypeDef(AStereotypeDef node) {
		final String stereotypeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getStereotypeDefHeader());
		if (!ensureNameAvailable(stereotypeIdentifier, node, Literals.TYPE))
			return;
		try {
			annotationProcessor.process(node.getAnnotations());
			super.caseAStereotypeDef(node);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}

	@Override
	public final void caseAStereotypeDefHeader(AStereotypeDefHeader node) {
		final Package currentPackage = namespaceTracker.currentPackage();
		final String stereotypeIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
		Stereotype newStereotype =
						(Stereotype) currentPackage.createPackagedElement(stereotypeIdentifier, IRepository.PACKAGE
										.getStereotype());
		if (node.getAbstract() != null)
			newStereotype.setIsAbstract(true);
		annotationProcessor.applyAnnotations(newStereotype, node.getIdentifier());
		applyCurrentComment(newStereotype);
		namespaceTracker.enterNamespace(newStereotype);
		super.caseAStereotypeDefHeader(node);
	}

	@Override
	public final void caseAStereotypeExtension(final AStereotypeExtension node) {
		super.caseAStereotypeExtension(node);
		final Stereotype currentStereotypeSnapshot = (Stereotype) namespaceTracker.currentNamespace(IRepository.PACKAGE.getStereotype());
		String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getQualifiedIdentifier());
		getRefTracker().add(
						new DeferredReference<Class>(qualifiedIdentifier, IRepository.PACKAGE.getClass_(), namespaceTracker.currentPackage()) {
							@Override
							protected void onBind(Class metaClass) {
								if (metaClass == null) {
									problemBuilder.addProblem( new UnresolvedSymbol(getSymbolName()), node
													.getQualifiedIdentifier());
									return;
								}
								if (!metaClass.isMetaclass()) {
									problemBuilder.addProblem( new NotAMetaclass(getSymbolName()), node
													.getQualifiedIdentifier());
									return;
								}
								if (!currentStereotypeSnapshot.getProfile().getReferencedMetamodels().contains(
												metaClass.getModel()))
									currentStereotypeSnapshot.getProfile().createMetamodelReference(
													metaClass.getNearestPackage());
								boolean requiredExtension = node.getRequired() != null;
								currentStereotypeSnapshot.createExtension(metaClass, requiredExtension);
							}
						}, IReferenceTracker.Step.GENERAL_RESOLUTION);
	}

	@Override
	public void caseAStereotypePropertyDecl(AStereotypePropertyDecl node) {
		final String propertyIdentifier = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
		if (!ensureNameAvailable(propertyIdentifier, node, Literals.PROPERTY))
			return;
		// do not call super as we deal with everything here
		final Stereotype currentStereotype = (Stereotype) namespaceTracker.currentNamespace(null);
		final Property attribute = currentStereotype.createOwnedAttribute(propertyIdentifier, null);
		applyCurrentComment(attribute);
		new DeferredTypeSetter(sourceContext, namespaceTracker.currentNamespace(null), attribute).process(node.getTypeIdentifier());
	}

	@Override
	public void caseASubNamespace(ASubNamespace node) {
		Package parentNamespace = namespaceTracker.currentPackage();
		Assert.isNotNull(parentNamespace);
		// this will create the package and set the current namespace, or abort
		// the compilation
		node.getPackageHeading().apply(this);
		try {
			node.getNamespaceContents().apply(this);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}

	@Override
	public void caseAWordyBlock(AWordyBlock node) {
		// don't go there, the behavior generator will take care of it
	}

	@Override
	public void caseTModelComment(TModelComment node) {
		currentComment = CommentUtils.collectCommentBody(node);
	}

	protected IReferenceTracker getRefTracker() {
		return context.getReferenceTracker();
	}

	private VisibilityKind getVisibility(Modifier modifier, VisibilityKind defaultVisibility) {
		VisibilityKind found = VisibilityKind.getByName(modifier.name().toLowerCase());
		return found != null ? found : defaultVisibility;
	}

	private void processAnnotations(final PAnnotations annotations, Element target) {
		if (annotations != null) {
			AnnotationProcessor localAnnotationProcessor = new AnnotationProcessor(getRefTracker(), problemBuilder);
			localAnnotationProcessor.process(annotations);
			localAnnotationProcessor.applyAnnotations(target, annotations.parent());
		}
	}
}