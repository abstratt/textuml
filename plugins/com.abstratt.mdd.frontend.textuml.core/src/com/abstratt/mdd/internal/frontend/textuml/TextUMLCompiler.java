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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.uml2.uml.NamedElement;

import com.abstratt.mdd.core.IBasicRepository;
import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.frontend.core.SyntaxProblem;
import com.abstratt.mdd.frontend.core.spi.AbortedCompilationException;
import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.core.spi.ICompiler;
import com.abstratt.mdd.frontend.core.spi.IDeferredReference;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.ISourceAnalyzer;
import com.abstratt.mdd.frontend.core.spi.ISourceMiner;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.frontend.textuml.core.TextUMLConstants;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.AnalysisAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.lexer.Lexer;
import com.abstratt.mdd.frontend.textuml.grammar.lexer.LexerException;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAggregationReferenceType;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationMemberEnd;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAssociationOwnedEnd;
import com.abstratt.mdd.frontend.textuml.grammar.node.AAttributeDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AClassHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.AComponentClassType;
import com.abstratt.mdd.frontend.textuml.grammar.node.ACompositionReferenceType;
import com.abstratt.mdd.frontend.textuml.grammar.node.AEnumerationClassType;
import com.abstratt.mdd.frontend.textuml.grammar.node.AFeatureDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AInterfaceClassType;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AOperationHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.APackageHeading;
import com.abstratt.mdd.frontend.textuml.grammar.node.AQueryOperationKeyword;
import com.abstratt.mdd.frontend.textuml.grammar.node.AReceptionDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AReferenceDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASignalClassType;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASignature;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleOptionalReturnType;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASimpleParamDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStart;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStateDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStateMachineDecl;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStereotypeDef;
import com.abstratt.mdd.frontend.textuml.grammar.node.AStereotypeDefHeader;
import com.abstratt.mdd.frontend.textuml.grammar.node.ASubNamespace;
import com.abstratt.mdd.frontend.textuml.grammar.node.ATypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.EOF;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PAssociationKind;
import com.abstratt.mdd.frontend.textuml.grammar.node.PClassType;
import com.abstratt.mdd.frontend.textuml.grammar.node.PReferenceType;
import com.abstratt.mdd.frontend.textuml.grammar.node.Start;
import com.abstratt.mdd.frontend.textuml.grammar.node.Switch;
import com.abstratt.mdd.frontend.textuml.grammar.node.TIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.TPrivate;
import com.abstratt.mdd.frontend.textuml.grammar.node.TSemicolon;
import com.abstratt.mdd.frontend.textuml.grammar.node.TWhiteSpace;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;
import com.abstratt.mdd.frontend.textuml.grammar.parser.Parser;
import com.abstratt.mdd.frontend.textuml.grammar.parser.ParserException;

/**
 * The compiler for the TextUML language (aka DIL).
 */
public class TextUMLCompiler implements ICompiler, ISourceAnalyzer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.spi.ICompiler#compile(java.io.Reader,
	 *      com.abstratt.mdd.core.IRepository,
	 *      com.abstratt.mdd.core.frontend.spi.IReferenceTracker,
	 *      com.abstratt.mdd.core.frontend.spi.IProblemTracker, boolean)
	 */
	public void compile(Reader source, CompilationContext context) throws CoreException {
		Start tree = parse(source, context.getProblemTracker());
		if (tree != null)
			compile(tree, context);
	}

	public void compile(final Start tree, final CompilationContext context) {
		try {
			// generate structure
			tree.apply(new StructureGenerator(context));
			context.getReferenceTracker().add(new IDeferredReference() {
				public void resolve(IBasicRepository repository) {
					if (!context.getProblemTracker().hasProblems(Severity.ERROR)) {
						// schedule behavior generation as the last step
						final Switch behaviorGenerator = new StructureBehaviorGenerator(context);
						context.getReferenceTracker().add(new IDeferredReference() {
							public void resolve(IBasicRepository repository) {
								tree.apply(behaviorGenerator);
							}
						}, IReferenceTracker.Step.LAST);
					}
				}
			}, IReferenceTracker.Step.GENERAL_RESOLUTION);
		} catch (AbortedCompilationException e) {
			// just abort...
		}
	}

	public String findModelName(String toParse) {
		PushbackReader in = new PushbackReader(new StringReader(toParse), 1);
		Lexer lexer = new Lexer(in);
		Parser parser = new Parser(lexer);
		try {
			Start parsed = parser.parse();
			final String[] packageIdentifier = { null };
			parsed.getPStart().apply(new DepthFirstAdapter() {
				@Override
				public void caseAStart(AStart node) {
					((APackageHeading) node.getPackageHeading()).getQualifiedIdentifier().apply(new DepthFirstAdapter() {
						@Override
						public void caseTIdentifier(TIdentifier node) {
							if (packageIdentifier[0] == null)
								packageIdentifier[0] = Util.stripEscaping(node.getText());
						}
					});
				}
			});
			return packageIdentifier[0];
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// fall through
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// not interested in failures while closing
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.spi.ICompiler#format(java.lang.String)
	 */
	public String format(String toFormat) {
		PushbackReader in = new PushbackReader(new StringReader(toFormat), 64 * 1024);
		Lexer lexer = new Lexer(in);
		Parser parser = new Parser(lexer);
		try {
			Start parsed = parser.parse();
			return new TextUMLFormatter().format(parsed.getPStart(), parser.ignoredTokens);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// fall through
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// not interested in failures while closing
			}
		}
		return toFormat;
	}

	private Start parse(Reader source, IProblemTracker problems) throws CoreException {
		ProblemBuilder<Node> problemBuilder = new ProblemBuilder<Node>(problems, new SCCTextUMLSourceMiner());
		PushbackReader in = new PushbackReader(source, 64 * 1024);
		Lexer lexer = new Lexer(in);
		Parser parser = new Parser(lexer);
		try {
			return parser.parse();
		} catch (ParserException e) {
			if (problems != null)
				problemBuilder.addProblem(new SyntaxProblem("Found: '" + e.getToken().getText() + "'. " + e.getMessage()),
							e.getToken());
		} catch (LexerException e) {
			if (problems != null) {
				SyntaxProblem problem = new SyntaxProblem(e.getMessage());
				problem.setAttribute(IProblem.LINE_NUMBER, SCCTextUMLSourceMiner.parseLineNumber(e.getMessage()));
				problemBuilder.addProblem(problem, null);
			}
		} catch (IOException e) {
			IStatus status =
							new Status(IStatus.ERROR, TextUMLConstants.PLUGIN_ID, 0, "Error reading source unit: "
											+ source.toString(), e);
			throw new CoreException(status);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// not interested in failures while closing
			}
		}
		return null;
	}
	
	/**
     * Parses the given compilation unit, returning the parsed syntax tree.
     *
     * @param toParse source of compilation unit to be parsed
     * @return compilation unit syntax tree
     */
	public Start parse(String toParse) {
		try {
			return parse(new StringReader(toParse), null);
		} catch (CoreException e) {
			return null;
		}
	}
	
	/**
	 * Given a position in a compilation unit, finds the contextual package name. 
	 * 
	 * @param toParse source of compilation unit
	 * @param line line number, starting from 1
	 * @param col column number, starting from 1
	 * @return the name of the contextual package
	 */
	public String findPackageName(String toParse, final int line, final int col) {
		Token token = findTokenAt(toParse, line, col);
		if (token == null)
			return null;
		final Stack<String> segments = new Stack<String>();
        for (Node current = token;current != null;current = current.parent()) {
            current.apply(new AnalysisAdapter() {
            	@Override
            	public void caseAStart(AStart node) {
            		segments.push(TextUMLCore.getSourceMiner().getQualifiedIdentifier(((APackageHeading) node.getPackageHeading()).getQualifiedIdentifier()));
            	}
            	public void caseASubNamespace(ASubNamespace node) {
            		segments.push(TextUMLCore.getSourceMiner().getQualifiedIdentifier(((APackageHeading) node.getPackageHeading())));
            	}
            });
        }
        if (segments.isEmpty())
        	return null;
        StringBuffer result = new StringBuffer();
        while (!segments.isEmpty()) {
        	result.append(segments.pop());
        	result.append(NamedElement.SEPARATOR);
        }
        result.delete((result.length() - NamedElement.SEPARATOR.length()), result.length());
        return result.toString();
	}
	
	/**
	 * Given a position in a compilation unit, finds the contextual namespace. 
	 * 
	 * @param toParse source of compilation unit
	 * @param line line number, starting from 1
	 * @param col column number, starting from 1
	 * @return the name of the contextual namespace
	 */
	public String findNamespace(String toParse, final int line, final int col) {
		Token token = findTokenAt(toParse, line, col, false, true);
		if (token == null)
			return null;
		final Stack<String> segments = new Stack<String>();
        for (Node current = token;current != null;current = current.parent()) {
            current.apply(new AnalysisAdapter() {
            	@Override
            	public void caseAStart(AStart node) {
            		segments.push(TextUMLCore.getSourceMiner().getQualifiedIdentifier(((APackageHeading) node.getPackageHeading()).getQualifiedIdentifier()));
            	}
            	public void caseASubNamespace(ASubNamespace node) {
            		segments.push(TextUMLCore.getSourceMiner().getQualifiedIdentifier(((APackageHeading) node.getPackageHeading())));
            	}
            	@Override
            	public void caseAClassDef(AClassDef node) {
            		segments.push(((AClassHeader) node.getClassHeader()).getIdentifier().getText());
            	}
            	@Override
            	public void caseAStereotypeDef(AStereotypeDef node) {
            		segments.push(((AStereotypeDefHeader) node.getStereotypeDefHeader()).getIdentifier().getText());
            	}
            	@Override
            	public void caseAAssociationDef(AAssociationDef node) {
            		final String associationName = ((AAssociationHeader) node.getAssociationHeader()).getIdentifier().getText();
            		if (associationName.length() > 0)
            			segments.push(associationName);
            	}
            });
        }
        if (segments.isEmpty())
        	return null;
        StringBuffer result = new StringBuffer();
        while (!segments.isEmpty()) {
        	result.append(segments.pop());
        	result.append(NamedElement.SEPARATOR);
        }
        result.delete((result.length() - NamedElement.SEPARATOR.length()), result.length());
        return result.toString();
	}

	/**
	 * Returns the token found at the given position in the given compilation unit.
	 * 
	 * @param toParse source of compilation unit
	 * @param line line number, starting from 1
	 * @param col column number, starting from 1
	 * @return the token found
	 */
	public Token findTokenAt(String toParse, final int line, final int col) {
		return findTokenAt(toParse, line, col, true, false);
	}
	
	/**
	 * Returns the token found at the given position in the given compilation unit.
	 * 
	 * @param toParse source of compilation unit
	 * @param line line number, starting from 1
	 * @param col column number, starting from 1
     * @param ignoreWhitespace whether whitespaces should be ignored
     * @param ignoreSemicolon whether semicolons should be ignored
	 * @return the token found
	 */
	public Token findTokenAt(String toParse, final int line, final int col, final boolean ignoreWhitespace, final boolean ignoreSemicolon) {
		Start parsed = parse(toParse);
		final Token[] target = {null};
		parsed.apply(new DepthFirstAdapter() {
			@Override
			public void defaultCase(Node node) {
				if (!(node instanceof Token))
				    return;
				Token asToken = (Token) node;
				if (asToken instanceof EOF)
					return;
				if (ignoreWhitespace && asToken instanceof TWhiteSpace)
					return;
				if (ignoreSemicolon && asToken instanceof TSemicolon)
					return;
				if (line < asToken.getLine())
					return;
				int start = asToken.getPos();
				int end = start + asToken.getText().length() - 1;
			    if (line > asToken.getLine() || col >= start)
					target[0] = asToken;
			}
		});
		return target[0];
	}
	
	@Override
	public List<SourceElement> analyze(String source) {
		final ISourceMiner<Node> sourceMiner = TextUMLCore.getSourceMiner();
		final List<SourceElement> elements = new ArrayList<SourceElement>();
		Start parsed = parse(source);
		if (parsed == null) 
			return Collections.emptyList();
		final List<SourceElement> parent = new LinkedList<ISourceAnalyzer.SourceElement>();
		parsed.apply(new DepthFirstAdapter() {
			@Override
			public void caseAClassDef(AClassDef node) {
				PClassType classType = sourceMiner.findChild(node.getClassHeader(), PClassType.class, true);
				String classTypeName = sourceMiner.getText(classType);
				String className = sourceMiner.getIdentifier(node.getClassHeader());
				ElementKind kind;
				if (classType instanceof ASignalClassType)
					kind = ElementKind.Signals;
				else if (classType instanceof AInterfaceClassType)
					kind = ElementKind.Interfaces;
				else if (classType instanceof AComponentClassType)
					kind = ElementKind.Components;
				else if (classType instanceof AInterfaceClassType)
					kind = ElementKind.Interfaces;
				else if (classType instanceof AEnumerationClassType)
                    kind = ElementKind.Enumerations;
				else
					kind = ElementKind.Classes;
				SourceElement element = new SourceElement(classTypeName + " " + className, sourceMiner.getLineNumber(node.getClassHeader()), kind);
				elements.add(element);
				parent.add(element);
				super.caseAClassDef(node);
				parent.remove(parent.size()-1);
			}
			@Override
			public void caseAAssociationDef(AAssociationDef node) {
				PAssociationKind assocKind = sourceMiner.findChild(node.getAssociationHeader(), PAssociationKind.class, true);
				String assocKindTypeName = sourceMiner.getText(assocKind);
				String assocName = sourceMiner.getIdentifier(node.getAssociationHeader());
				SourceElement element = new SourceElement(assocKindTypeName + " " + StringUtils.trimToEmpty(assocName), sourceMiner.getLineNumber(node.getAssociationHeader()), ElementKind.Associations);
				elements.add(element);
				parent.add(element);
				super.caseAAssociationDef(node);
				parent.remove(parent.size()-1);
			}
			@Override
			public void caseAAssociationOwnedEnd(AAssociationOwnedEnd node) {
				String attributeName = node.getIdentifier().getText();
				SourceElement element = new SourceElement(attributeName, node.getRole().getLine(), ElementKind.Ends);
				parent.get(parent.size()-1).getChildren().add(element);
			}
			@Override
			public void caseAAssociationMemberEnd(AAssociationMemberEnd node) {
				String attributeName = node.getProperty().getText();
				SourceElement element = new SourceElement(attributeName, node.getRole().getLine(), ElementKind.Ends);
				parent.get(parent.size()-1).getChildren().add(element);
			}
			@Override
			public void caseAStateMachineDecl(AStateMachineDecl node) {
				String smName = sourceMiner.getIdentifier(node.getIdentifier());
				SourceElement element = new SourceElement("statemachine" + " " + smName, sourceMiner.getLineNumber(node.getIdentifier()), ElementKind.StateMachines);
				parent.get(parent.size()-1).getChildren().add(element);
				parent.add(element);
				super.caseAStateMachineDecl(node);
				parent.remove(parent.size()-1);
			}
			
			@Override
			public void caseAStateDecl(AStateDecl node) {
				String stateName = sourceMiner.getText(node.getIdentifier());
				String modifier = sourceMiner.getText(node.getStateModifierList());
				String label = StringUtils.isBlank(stateName) ? modifier : (StringUtils.isBlank(modifier) ? stateName : (stateName + " (" + modifier  + ")"));
				SourceElement element = new SourceElement(label, sourceMiner.getLineNumber(node.getState()), ElementKind.States);
				parent.get(parent.size()-1).getChildren().add(element);
			}
			
			@Override
			public void caseAReferenceDecl(AReferenceDecl node) {
				PReferenceType refType = node.getReferenceType();
				ElementKind elementKind = refType instanceof ACompositionReferenceType ? ElementKind.Compositions : (refType instanceof AAggregationReferenceType ? ElementKind.Aggregations : ElementKind.References);
				addAttribute(isPrivateFeature(node), node.getIdentifier(), node.getTypeIdentifier(), elementKind);
			}
			
			@Override
			public void caseAAttributeDecl(AAttributeDecl node) {
				addAttribute(isPrivateFeature(node), node.getIdentifier(), node.getTypeIdentifier(), ElementKind.Attributes);
			}
			private void addAttribute(boolean isPrivate, Node identifierNode, Node typeNode, ElementKind elementKind) {
				String attributeName = sourceMiner.getText(identifierNode);
				String attributeType = sourceMiner.getText(typeNode);
				SourceElement element = new SourceElement((isPrivate ? "-" : "") + attributeName + " : " + attributeType, sourceMiner.getLineNumber(identifierNode), elementKind);
				parent.get(parent.size()-1).getChildren().add(element);
			}
			@Override
			public void caseAOperationDecl(AOperationDecl node) {
				AOperationHeader header = (AOperationHeader) node.getOperationHeader();
				boolean isQuery = header.getOperationKeyword() instanceof AQueryOperationKeyword;
				boolean isPrivate = isPrivateFeature(node);
				ASignature signature = (ASignature) header.getSignature();
				ASimpleOptionalReturnType returnType = sourceMiner.findChild(header.getSignature(), ASimpleOptionalReturnType.class, true);
				String operationName = header.getIdentifier().getText();
				
				String operationType = returnType == null ? "" : sourceMiner.getText(returnType);
				
				List<String> parameterTypes = new ArrayList<String>();
				for (ASimpleParamDecl paramDecl : sourceMiner.findChildren(signature.getParamDeclList(), ASimpleParamDecl.class))
					parameterTypes.add(sourceMiner.getText(paramDecl.getTypeIdentifier()));
				String allParameters = StringUtils.join(parameterTypes, ", ");
				String representation = (isPrivate ? "-": "") + operationName + (isQuery ? "?" : "") + "(" + allParameters + ")" + operationType;
				SourceElement element = new SourceElement(representation, sourceMiner.getLineNumber(header.getOperationKeyword()), ElementKind.Operations);
				parent.get(parent.size()-1).getChildren().add(element);
			}
			protected boolean isPrivateFeature(Node node) {
				return sourceMiner.findChild(sourceMiner.findParent(node, AFeatureDecl.class).getModifiers(), TPrivate.class, true) != null;
			}
			@Override
			public void caseAReceptionDecl(AReceptionDecl node) {
				ATypeIdentifier signalType = sourceMiner.findChild(node.getSimpleParamDecl(), ATypeIdentifier.class, true);
				String operationName = node.getReceptionName().getText();
				String representation = operationName + "(" + signalType + ")";
				SourceElement element = new SourceElement(representation, node.getReception().getLine(), ElementKind.Receptions);
				parent.get(parent.size()-1).getChildren().add(element);
			}
		});
		Collections.sort(elements, SourceElementComparator.INSTANCE);
		
		List<SourceElement> result = elements;
		if (elements.size() > 10) {
			Map<ElementKind, SourceElement> groups = new TreeMap<ElementKind, SourceElement>();
	        for (SourceElement sourceElement : elements) {
	        	SourceElement group = groups.get(sourceElement.getKind());
				if (group == null)
					groups.put(sourceElement.getKind(), group = new SourceElement(sourceElement.getKind().name(), sourceElement.getLine(), ElementKind.Groups));
				group.getChildren().add(sourceElement);
			}
	        if (groups.size() > 2)
			    result = new ArrayList<SourceElement>(groups.values());
		}		
		return result;
	}
}