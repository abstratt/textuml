/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Massimiliano Federici - bug 2127735 - multi-line comments lose highlighting
 *    Attila Bak - feature 2075236 - Support content assist. 
 *******************************************************************************/
package com.abstratt.mdd.internal.ui.editors.source;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import com.abstratt.mdd.internal.ui.TextUMLCompletionProcessor;

public class TextUMLSourceViewerConfiguration extends
		TextSourceViewerConfiguration {

	private SourceEditor editor;

	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		final PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		final SyntaxHighlighter scanner = new SyntaxHighlighter(
				com.abstratt.mdd.frontend.textuml.core.TextUMLConstants.KEYWORDS);
		final DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr, ContentTypes.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, ContentTypes.DEFAULT_CONTENT_TYPE);
		
		// fix bug 2127735 --multiline comment is broken
		final ITokenScanner commentScanner = scanner.getCommentScanner();
		final DefaultDamagerRepairer commentDamagerRepairer = new DefaultDamagerRepairer(commentScanner);
		reconciler.setDamager(commentDamagerRepairer, ContentTypes.COMMENT_CONTENT_TYPE);
		reconciler.setRepairer(commentDamagerRepairer, ContentTypes.COMMENT_CONTENT_TYPE);

		return reconciler;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return ContentTypes.CONFIGURED_CONTENT_TYPES;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(
			new TextUMLCompletionProcessor(editor),
			IDocument.DEFAULT_CONTENT_TYPE);

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(
			IContentAssistant.PROPOSAL_OVERLAY);
		return assistant;
	}

	public TextUMLSourceViewerConfiguration(SourceEditor editor) {
		// failing to pass the preference store in causes annotation hovering not to work
		super(EditorsUI.getPreferenceStore());
		this.editor = editor;
	}

}