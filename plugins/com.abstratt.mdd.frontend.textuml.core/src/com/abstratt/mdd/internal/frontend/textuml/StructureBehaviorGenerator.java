package com.abstratt.mdd.internal.frontend.textuml;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils.AccessCapability;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.ReceptionUtils;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.AbortedCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAccessCapabilities;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAllAccessCapabilities;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAttributeDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAttributeInvariant;
import com.abstratt.mdd.frontend.textuml.grammar.node.ABehavioralFeatureBody;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassInvariantDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AConstraintException;
import com.abstratt.mdd.frontend.textuml.grammar.node.ADetachedOperationDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.ADetachedOperationHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.AEmptyAccessCapabilities;
import com.abstratt.mdd.frontend.textuml.grammar.node.AFeatureDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.ANoneAccessCapabilities;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationConstraint;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationPrecondition;
import com.abstratt.mdd.frontend.textuml.grammar.node.APackageHeading;
import com.abstratt.mdd.frontend.textuml.grammar.node.APermissionConstraint;
import com.abstratt.mdd.frontend.textuml.grammar.node.APermissionOperationConstraintKernel;
import com.abstratt.mdd.frontend.textuml.grammar.node.APreconditionOperationConstraintKernel;
import com.abstratt.mdd.frontend.textuml.grammar.node.AQualifiedIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.AReceptionDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.ARegularInvariantConstraint;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStart;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASubNamespace;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PAccessCapability;
import com.abstratt.mdd.frontend.textuml.grammar.node.PAttributeInvariant;
import com.abstratt.mdd.frontend.textuml.grammar.node.TIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.TModelComment;

public class StructureBehaviorGenerator extends AbstractGenerator {

	BehaviorGenerator behaviorGenerator = new BehaviorGenerator(sourceContext);

	public StructureBehaviorGenerator(CompilationContext context) {
		super(context);
	}

	@Override
	public void caseADetachedOperationDef(ADetachedOperationDef node) {
		// find behavioral feature
		final BehavioralFeature[] behavioralFeature = { null };
		node.apply(new DepthFirstAdapter() {
			@Override
			public void caseADetachedOperationHeader(ADetachedOperationHeader node) {
				super.caseADetachedOperationHeader(node);
				String qualifiedIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getType());
				Classifier classifier = (Classifier) getRepository().findNamedElement(qualifiedIdentifier,
						IRepository.PACKAGE.getClassifier(), namespaceTracker.currentPackage());
				if (classifier == null) {
					problemBuilder.addError("Unknown classifier: " + qualifiedIdentifier, node.getType());
					throw new AbortedScopeCompilationException();
				}
				String operationName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
				behavioralFeature[0] = classifier.getOperation(operationName, null, null);
				// no behavioral feature found
				if (behavioralFeature[0] == null) {
					problemBuilder.addError("Unknown behavioral feature: " + operationName, node.getOperation());
					throw new AbortedScopeCompilationException();
				}
			}
		});
		Namespace toEnter = behavioralFeature[0];
		namespaceTracker.enterNamespace(toEnter);
		try {
			node.getBehavioralFeatureBody().apply(this);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}

	@Override
	public void caseAOperationConstraint(AOperationConstraint node) {
		final Operation operation = (Operation) namespaceTracker.currentNamespace(IRepository.PACKAGE.getOperation());

		final Constraint[] created = { null };
		node.getOperationConstraintKernel().apply(new DepthFirstAdapter() {
			@Override
			public void caseAPreconditionOperationConstraintKernel(
					APreconditionOperationConstraintKernel preconditionNode) {
				created[0] = buildOperationPreconditionConstraint(
						(AOperationPrecondition) preconditionNode.getOperationPrecondition(), operation);
			}

			@Override
			public void caseAPermissionOperationConstraintKernel(APermissionOperationConstraintKernel permissionNode) {
				created[0] = buildPermissionConstraint((APermissionConstraint) permissionNode.getPermissionConstraint(),
						(BehavioredClassifier) operation.getClass_(), operation);
			}
		});
		CommentUtils.applyComment(node.getModelComment(), created[0]);
	}

	public Constraint buildOperationPreconditionConstraint(AOperationPrecondition node, Operation operation) {
		final String preconditionName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
		if (operation.isAbstract()) {
			problemBuilder.addError("Operation is abstract", node.getExpressionBlock());
			throw new AbortedStatementCompilationException();
		}

		Constraint constraint = operation.createPrecondition(preconditionName);
		fillDebugInfo(constraint, node);

		if (node.getConstraintException() != null) {
			Classifier exceptionClass = assignConstraintException(constraint,
					(AConstraintException) node.getConstraintException());
			operation.getRaisedExceptions().add(exceptionClass);
		}

		final Class currentClass = (Class) operation.getOwner();

		List<Parameter> parameters = new LinkedList<Parameter>();
		// include selected operation parameters
		for (TIdentifier paramNameToken : sourceMiner.findChildren(node.getPreconditionSignature(),
				TIdentifier.class)) {
			String paramName = sourceMiner.getText(paramNameToken);
			Parameter found = operation.getOwnedParameter(paramName, null);
			if (found == null) {
				problemBuilder.addProblem(new UnresolvedSymbol(paramName, IRepository.PACKAGE.getParameter()),
						paramNameToken);
				throw new AbortedScopeCompilationException();
			}
			if (found.getDirection() != ParameterDirectionKind.IN_LITERAL
					&& found.getDirection() != ParameterDirectionKind.INOUT_LITERAL) {
				problemBuilder.addProblem(new UnclassifiedProblem("Precondition can only apply to input parameters"),
						paramNameToken);
				throw new AbortedScopeCompilationException();
			}
			parameters.add((Parameter) EcoreUtil.copy(found));
		}

		behaviorGenerator.createConstraintBehavior(currentClass, constraint, node.getExpressionBlock(), parameters);

		return constraint;
	}

	@Override
	public void caseAClassDef(AClassDef node) {
		final Classifier[] currentClassifier = { null };
		node.apply(new DepthFirstAdapter() {
			@Override
			public void caseAClassHeader(AClassHeader node) {
				final String classSimpleName = TextUMLCore.getSourceMiner().getIdentifier(node.getIdentifier());
				final Package currentPackage = namespaceTracker.currentPackage();
				currentClassifier[0] = (Classifier) currentPackage.getOwnedMember(classSimpleName, false,
						IRepository.PACKAGE.getClassifier());
				if (currentClassifier[0] == null) {
					problemBuilder.addError("Class "
							+ MDDUtil.appendSegment(currentPackage.getQualifiedName(), classSimpleName) + " not found",
							node);
				}
			}
		});
		if (currentClassifier[0] == null)
			// skip the entire class declaration
			return;
		Namespace toEnter = currentClassifier[0];
		namespaceTracker.enterNamespace(toEnter);
		try {
			// process operations, derived features, invariants
			node.getFeatureDeclList().apply(new DepthFirstAdapter() {
				@Override
				public void caseAOperationDecl(AOperationDecl node) {
					buildOperationBehaviors(node);
				}

				@Override
				public void caseAReceptionDecl(AReceptionDecl node) {
					buildReceptionBehavior(node);
				}

				@Override
				public void caseAClassInvariantDecl(AClassInvariantDecl node) {
					if (!(currentClassifier[0] instanceof BehavioredClassifier)) {
						problemBuilder.addError(
								"Not a behaviored classifier: " + currentClassifier[0].getQualifiedName(), node);
						return;
					}
					node.apply(StructureBehaviorGenerator.this);
				}

				@Override
				public void caseAAttributeDecl(AAttributeDecl node) {
					// invariants
					if (node.getAttributeInvariant() != null)
						for (PAttributeInvariant invariant : node.getAttributeInvariant())
							invariant.apply(StructureBehaviorGenerator.this);
				}
			});
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}
	
	@Override
	public void caseAClassInvariantDecl(AClassInvariantDecl node) {
		BehavioredClassifier currentClassifier = (Class) namespaceTracker.currentNamespace(UMLPackage.Literals.BEHAVIORED_CLASSIFIER);
		final Constraint[] created = { null };
		node.getInvariantKernel().apply(new DepthFirstAdapter() {
			@Override
			public void caseARegularInvariantConstraint(ARegularInvariantConstraint node) {
				created[0] = buildInvariantConstraint(node,
						currentClassifier, currentClassifier);
			}

			@Override
			public void caseAPermissionConstraint(APermissionConstraint node) {
				created[0] = buildPermissionConstraint(node,
						(BehavioredClassifier) currentClassifier, currentClassifier);
			}
		});
		TModelComment commentNode = sourceMiner.findChild(
				sourceMiner.findParent(node, AFeatureDecl.class), TModelComment.class, true);
		CommentUtils.applyComment(commentNode, created[0]);
	}

	@Override
	public void caseAAttributeInvariant(AAttributeInvariant node) {
		final String attributeName = TextUMLCore.getSourceMiner()
				.getIdentifier(TextUMLCore.getSourceMiner().findParent(node, AAttributeDecl.class).getIdentifier());

		final Class currentClass = (Class) namespaceTracker.currentNamespace(null);
		Property currentAttribute = (Property) currentClass.getAttribute(attributeName, null);
		if (currentAttribute == null)
			// could not find the attribute, probably due to a structural
			// compilation failure
			return;

		if (currentAttribute.isDerived()) {
			problemBuilder.addError("Attribute is derived", node);
			throw new AbortedStatementCompilationException();
		}

		node.getInvariantKernel().apply(new DepthFirstAdapter() {
			@Override
			public void caseARegularInvariantConstraint(ARegularInvariantConstraint constraintNode) {
				Constraint created = buildInvariantConstraint(constraintNode, currentClass, currentAttribute);
				CommentUtils.applyComment(node.getModelComment(), created);
			}

			@Override
			public void caseAPermissionConstraint(APermissionConstraint constraintNode) {
				Constraint created = buildPermissionConstraint(constraintNode, (BehavioredClassifier) currentClass,
						currentAttribute);
				CommentUtils.applyComment(node.getModelComment(), created);
			}
		});
	}

	protected Constraint buildInvariantConstraint(ARegularInvariantConstraint invariantNode,
			BehavioredClassifier currentBehavioredClassifier, NamedElement constrainedElement) {
		final String invariantName = TextUMLCore.getSourceMiner().getIdentifier(invariantNode.getIdentifier());
		Constraint constraint = MDDExtensionUtils.createConstraint(constrainedElement, invariantName,
				MDDExtensionUtils.INVARIANT_STEREOTYPE);
		fillDebugInfo(constraint, invariantNode);
		if (invariantNode.getConstraintException() != null)
			assignConstraintException(constraint, (AConstraintException) invariantNode.getConstraintException());

		behaviorGenerator.createConstraintBehavior(currentBehavioredClassifier, constraint,
				invariantNode.getExpressionBlock(), Collections.<Parameter> emptyList());
		return constraint;
	}

	protected Constraint buildPermissionConstraint(APermissionConstraint constraintNode,
			BehavioredClassifier currentBehavioredClassifier, NamedElement constrainedElement) {

		List<AQualifiedIdentifier> roleIdentifiers = sourceMiner.findChildren(constraintNode.getPermissionRoles(),
				AQualifiedIdentifier.class);
		List<Class> roleClasses = new LinkedList<>();
		roleIdentifiers.forEach(roleId -> {
			String roleClassName = sourceMiner.getQualifiedIdentifier(roleId);
			Class roleClass = getRepository().findNamedElement(roleClassName, IRepository.PACKAGE.getClass_(),
					namespaceTracker.currentPackage());
			if (roleClass == null) {
				problemBuilder.addProblem(new UnresolvedSymbol(roleClassName, IRepository.PACKAGE.getClass_()), roleId);
				throw new AbortedCompilationException();
			}
			if (!MDDExtensionUtils.isRoleClass(roleClass)) {
				problemBuilder.addProblem(new UnclassifiedProblem(roleClassName + " is not a role class"), roleId);
				throw new AbortedCompilationException();
			}
			roleClasses.add(roleClass);
		});

		Set<AccessCapability> allowed = new LinkedHashSet<>();
		constraintNode.apply(new DepthFirstAdapter() {
			@Override
			public void caseAAccessCapabilities(AAccessCapabilities node) {
				List<PAccessCapability> selectedCapabilities = sourceMiner.findChildren(node, PAccessCapability.class);
				selectedCapabilities.forEach(it -> 
				    allowed.addAll(AccessCapability.valueOf(StringUtils.capitalize(sourceMiner.getText(it))).getImplied(true))
				);
			}
			@Override
			public void caseANoneAccessCapabilities(ANoneAccessCapabilities node) {
				// none
			}
			@Override
			public void caseAAllAccessCapabilities(AAllAccessCapabilities node) {
				addAllCapabilities();
			}
			private void addAllCapabilities() {
				allowed.addAll(EnumSet.allOf(AccessCapability.class));
			}
			
			@Override
			public void caseAEmptyAccessCapabilities(AEmptyAccessCapabilities node) {
				addAllCapabilities();
			}
		});
		

		Constraint constraint = MDDExtensionUtils.createAccessConstraint(constrainedElement, null, roleClasses,
				allowed);
		fillDebugInfo(constraint, constraintNode);

		if (constraintNode.getPermissionExpression() != null)
			behaviorGenerator.createConstraintBehavior(currentBehavioredClassifier, constraint,
					constraintNode.getPermissionExpression(), Collections.<Parameter> emptyList());
		return constraint;
	}

	private void createBody(Node bodyNode, Activity activity) {
		behaviorGenerator.createBody(bodyNode, activity);
	}

	private Classifier assignConstraintException(Constraint invariant, AConstraintException constraintException) {
		AQualifiedIdentifier exceptionNode = sourceMiner.findChild(constraintException, AQualifiedIdentifier.class,
				true);
		String exceptionClassName = sourceMiner.getQualifiedIdentifier(exceptionNode);
		Classifier exceptionClass = (Classifier) getRepository().findNamedElement(exceptionClassName,
				IRepository.PACKAGE.getClassifier(), namespaceTracker.currentPackage());
		if (exceptionClass == null) {
			problemBuilder.addProblem(new UnresolvedSymbol(exceptionClassName, IRepository.PACKAGE.getClassifier()),
					exceptionNode);
			throw new AbortedCompilationException();
		}
		MDDExtensionUtils.makeRule(invariant, exceptionClass);
		return exceptionClass;
	}

	private void buildOperationBehaviors(AOperationDecl node) {
		final TIdentifier[] identifier = { null };
		node.apply(new DepthFirstAdapter() {
			@Override
			public void caseAOperationHeader(AOperationHeader node) {
				identifier[0] = node.getIdentifier();
			}
		});

		final String operationSimpleName = TextUMLCore.getSourceMiner().getIdentifier(identifier[0]);
		final Classifier currentClassifier = (Classifier) namespaceTracker.currentNamespace(null);
		Operation currentOperation = (Operation) currentClassifier.getOwnedMember(operationSimpleName, false,
				IRepository.PACKAGE.getOperation());
		if (currentOperation == null) {
			problemBuilder.addError("Operation "
					+ MDDUtil.appendSegment(currentClassifier.getQualifiedName(), operationSimpleName) + " not found",
					node.getOperationHeader());
			// could not find the operation, don't go further deep
			return;
		}
		if (!currentOperation.getMethods().isEmpty()) {
			problemBuilder.addError("Operation overloading is not supported", node.getOperationHeader());
			return;
		}
		namespaceTracker.enterNamespace(currentOperation);
		try {
			// constraints
			for (AOperationConstraint current : sourceMiner.findChildren(node, AOperationConstraint.class))
				current.apply(this);
			// actual operation behavior
			node.getOptionalBehavioralFeatureBody().apply(this);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}

	private void buildReceptionBehavior(AReceptionDecl node) {
		final String signalName = TextUMLCore.getSourceMiner().getQualifiedIdentifier(node.getSimpleParamDecl());
		final Classifier currentClassifier = (Classifier) namespaceTracker.currentNamespace(null);
		Signal currentSignal = (Signal) getRepository().findNamedElement(signalName, UMLPackage.Literals.SIGNAL,
				currentClassifier);

		Reception currentReception = ReceptionUtils.findBySignal(currentClassifier, currentSignal);
		if (currentReception == null) {
			problemBuilder.addError("Reception not found for signal " + signalName, node.getReception());
			// could not find the reception, don't go further
			return;
		}
		namespaceTracker.enterNamespace(currentReception);
		try {
			node.getOptionalBehavioralFeatureBody().apply(this);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}

	/**
	 * {@link AOperationBody} is the first node we visit.
	 */
	@Override
	public void caseABehavioralFeatureBody(ABehavioralFeatureBody node) {
		final BehavioralFeature feature = (BehavioralFeature) namespaceTracker
				.currentNamespace(IRepository.PACKAGE.getBehavioralFeature());
		if (feature.isAbstract()) {
			problemBuilder.addError("Behavioral feature is abstract", node.getBlock());
			throw new AbortedStatementCompilationException();
		}
		if (!(feature.getOwner() instanceof Class)) {
			problemBuilder.addError("Can only define behavioral feature in classes", node.getBlock());
			throw new AbortedStatementCompilationException();
		}
		final Class currentClass = (Class) feature.getOwner();
		Activity activity = (Activity) currentClass.createNestedClassifier(null, IRepository.PACKAGE.getActivity());
		activity.setIsReadOnly(feature instanceof Operation && ((Operation) feature).isQuery());
		activity.setSpecification(feature);
		activity.setName("__activity_" + feature.getName());
		activity.getOwnedParameters().addAll(EcoreUtil.copyAll(feature.getOwnedParameters()));
		createBody(node.getBlock(), activity);
	}

	@Override
	public void caseAStart(AStart node) {
		final Package[] currentPackage = { null };
		node.getPackageHeading().apply(new DepthFirstAdapter() {
			@Override
			public void caseAPackageHeading(APackageHeading node) {
				final String packageName = TextUMLCore.getSourceMiner()
						.getQualifiedIdentifier(node.getQualifiedIdentifier());
				currentPackage[0] = getRepository().findPackage(packageName, null);
				if (currentPackage[0] == null)
					problemBuilder.addError("Package " + packageName + " was not found", node);
			}
		});
		if (currentPackage[0] == null)
			// could not find the referred package, skip the whole thing
			return;
		Namespace toEnter = currentPackage[0];
		namespaceTracker.enterNamespace(toEnter);
		try {
			super.caseAStart(node);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}

	@Override
	public void caseASubNamespace(ASubNamespace node) {
		final Package[] newPackage = { null };
		node.getPackageHeading().apply(new DepthFirstAdapter() {
			@Override
			public void caseAPackageHeading(APackageHeading node) {
				final String packageName = TextUMLCore.getSourceMiner()
						.getQualifiedIdentifier(node.getQualifiedIdentifier());
				newPackage[0] = getRepository().findNamedElement(packageName, Literals.PACKAGE,
						namespaceTracker.currentPackage());
				if (newPackage[0] == null)
					problemBuilder.addError("Package " + packageName + " was not found", node);
			}
		});
		if (newPackage[0] == null)
			// could not find the referred package, skip the whole thing
			return;
		Namespace toEnter = newPackage[0];
		namespaceTracker.enterNamespace(toEnter);
		try {
			node.getNamespaceContents().apply(this);
		} finally {
			namespaceTracker.leaveNamespace();
		}
	}
}
