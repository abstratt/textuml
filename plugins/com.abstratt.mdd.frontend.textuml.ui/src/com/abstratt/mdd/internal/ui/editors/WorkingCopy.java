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
package com.abstratt.mdd.internal.ui.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextUtilities;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.frontend.core.ASTNode.VisitorResult;
import com.abstratt.mdd.frontend.core.ASTVisitor;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.Start;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;
import com.abstratt.mdd.internal.frontend.textuml.core.TextUMLCompiler;
import com.abstratt.mdd.ui.UIUtils;

/**
 * Contains information about a working copy of a Text UML file.
 */
public class WorkingCopy {

	// data structure that holds the offset range in a line
	class Line {
		int end;
		int start;

		public String toString() {
			// debug string
			return start + ", " + end;
		}
	}

	protected TextUMLCompiler compiler;
	protected IDocument document;
	protected IDocumentListener documentListener;
	protected IFile file;
	protected String lineDelimiter;
	protected Line[] lines;
	protected Start root;
	protected ASTNode<Token,Node> rootNode;
	protected String source;

	public WorkingCopy(IDocument document, IFile file) {
		this.document = document;
		this.file = file;
		this.compiler = new TextUMLCompiler();
		setSource(document.get());

		documentListener = new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
				// nothing for now
			}

			public void documentChanged(DocumentEvent event) {
				setSource(event.getDocument().get());
			}
		};

		document.addDocumentListener(documentListener);
	}

	public void dispose() {
		document.removeDocumentListener(documentListener);
		document = null;
	}

	public ASTNode<Token,Node> getASTNode(int offset) throws CoreException {
		final ASTNode<Token,Node>[] found = new ASTNode[1];
		int[] position = transformOffsetInLineAndPos(offset);
		if (position.length == 0)
			return null;
		final int line = position[0];
		final int col = position[1];
		ASTNode<Token,Node> root = getRootASTNode();
		root.accept(new ASTVisitor<Token, Node>() {
			public ASTNode.VisitorResult visit(ASTNode<Token,Node> node) {
				if (node.isToken()) {
					Token token = (Token) node.getBaseNode();
					int tokenLine = token.getLine();
					int tokenPosStart = token.getPos();
					int tokenPosEnd = tokenPosStart + token.getText().length();
					//					System.out.println("Token: " + token + " line: " + tokenLine + " colStart: " + tokenPosStart + " colEnd: " + tokenPosEnd);
					if (tokenLine == line && col >= tokenPosStart && col <= tokenPosEnd) {
						found[0] = node;
						return VisitorResult.STOP;
					}
				}
				return VisitorResult.CONTINUE;
			}
		});
		return found[0];
	}

	public IDocument getDocument() {
		return document;
	}

	public IFile getFile() {
		return file;
	}

	public String getLineDelimiter() {
		return lineDelimiter;
	}

	/**
	 * Returns a matrix where the lines represent the line numbers in a
	 * source code and the columms represent the offset range in that line. 
	 */
	protected Line[] getLines() throws CoreException {
		if (lines == null) {
			List<Line> result = new ArrayList<Line>();
			BufferedReader reader = new BufferedReader(new StringReader(getSource()));
			String sourceLine = null;
			int offset = 0;
			try {
				while ((sourceLine = reader.readLine()) != null) {
					Line line = new Line();
					line.start = offset;
					offset += sourceLine.length();
					offset += lineDelimiter.length();
					line.end = offset;
					result.add(line);
				}
			} catch (IOException e) {
				UIUtils.throwException(e);
			}
			lines = result.toArray(new Line[result.size()]);
		}
		return lines;
	}

	public ASTNode<Token,Node> getRootASTNode() {
		if (rootNode == null) {
			recompile();
			rootNode = (root != null) ? ASTNode.<Token, Node> buildTree(root) : null;
		}
		return rootNode;
	}

	public String getSource() {
		return source;
	}

	protected void recompile() {
		root = compiler.parse(source);
	}

	public void setLineDelimiter(String lineDelimiter) {
		this.lineDelimiter = lineDelimiter;
	}

	public void setSource(String source) {
		this.source = source;
		setLineDelimiter(TextUtilities.getDefaultLineDelimiter(document));
		root = null;
		rootNode = null;
		lines = null;
	}

	/**
	 * Returns an array where the first value is the line number and the
	 * second is the position in the line.
	 * Returns null if the given offset is invalid.
	 */
	protected int[] transformOffsetInLineAndPos(int offset) throws CoreException {
		Line[] lines = getLines();
		if (offset == 0 && lines.length == 0)
			return new int[0];
		for (int i = 0; i < lines.length; i++) {
			Line line = lines[i];
			if (offset >= line.start && offset <= line.end) {
				int pos = offset - line.start;
				return new int[] {i + 1, pos + 1}; // +1 because SableCC lines and columns start at 1
			}
		}
		return null;
	}
}