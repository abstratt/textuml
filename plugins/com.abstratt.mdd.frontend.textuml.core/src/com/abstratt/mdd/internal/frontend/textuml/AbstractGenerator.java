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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.util.BasicTypeUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;
import com.abstratt.mdd.frontend.core.spi.NamespaceTracker;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.internal.frontend.textuml.analysis.DepthFirstAdapter;
import com.abstratt.mdd.internal.frontend.textuml.node.AModelPackageType;
import com.abstratt.mdd.internal.frontend.textuml.node.APackagePackageType;
import com.abstratt.mdd.internal.frontend.textuml.node.AProfilePackageType;
import com.abstratt.mdd.internal.frontend.textuml.node.AStart;
import com.abstratt.mdd.internal.frontend.textuml.node.ATypeIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.PPackageType;
import com.abstratt.mdd.internal.frontend.textuml.node.TIdentifier;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;

public abstract class AbstractGenerator extends DepthFirstAdapter {

	protected CompilationContext context;
	protected NamespaceTracker namespaceTracker;
	protected SCCTextUMLSourceMiner sourceMiner;
	protected ProblemBuilder<Node> problemBuilder;
	protected SourceCompilationContext<Node> sourceContext;

	public AbstractGenerator(CompilationContext context) {
		namespaceTracker = new NamespaceTracker();
		sourceMiner = new SCCTextUMLSourceMiner();
		this.context = context;
		this.problemBuilder = new ProblemBuilder<Node>(context.getProblemTracker(), sourceMiner);
		this.sourceContext = new SourceCompilationContext<Node>(context, namespaceTracker, sourceMiner, problemBuilder);
	}
	
	public AbstractGenerator(SourceCompilationContext<Node> sourceContext) {
		this.context = sourceContext.getContext();
		this.namespaceTracker = sourceContext.getNamespaceTracker();
		this.sourceMiner = (SCCTextUMLSourceMiner) sourceContext.getSourceMiner();
		this.problemBuilder = sourceContext.getProblemBuilder();
		this.sourceContext = sourceContext;
	}

	@Override
	public void caseAStart(AStart node) {
		try {
			super.caseAStart(node);
		} catch (AbortedScopeCompilationException e) {
			// aborted top level element compilation...
		}
	}

	@Override
	public final void caseATypeIdentifier(ATypeIdentifier node) {
		// forbid processing type identifiers (and corresponding
		// multiplicities),
		// they should be processed using TypeSetters
	}

	protected IProblemTracker getProblems() {
		return context.getProblemTracker();
	}

	protected IRepository getRepository() {
		return context.getRepository();
	}
	

	protected void fillDebugInfo(Element element, Node node) {
		if (!context.isDebug())
			return;
		Token token = sourceMiner.findToken(node);
		int lineNumber = token.getLine();
        String sourceFile = context.getSourcePath() != null ? context.getSourcePath() : null;
		MDDExtensionUtils.addDebugInfo(element, sourceFile, lineNumber);
	}

	protected SignatureProcessor newSignatureProcessor(Namespace target) {
		if (target instanceof Behavior)
			return new BehaviorSignatureProcessor(context, (Behavior) target);
		if (target instanceof BehavioralFeature)
			return new BehavioralFeatureSignatureProcessor(context,
					(BehavioralFeature) target);
		throw new IllegalArgumentException("" + target);
	}
	
	protected Classifier findBuiltInType(String typeName, Node node) {
		Classifier builtInType = BasicTypeUtils.findBuiltInType(typeName);
		if (builtInType == null) {
			problemBuilder.addProblem(new UnresolvedSymbol(
					typeName), node);
			throw new AbortedStatementCompilationException();
		}
		return builtInType;
	}

	/**
	 * Figures what kind of package we have in the package type node.
	 */
	public EClass determinePackageType(PPackageType node) {
		if (node instanceof APackagePackageType)
			return UMLPackage.Literals.PACKAGE;
		if (node instanceof AModelPackageType)
			return UMLPackage.Literals.MODEL;
		if (node instanceof AProfilePackageType)
			return UMLPackage.Literals.PROFILE;
		return null;
    }
}