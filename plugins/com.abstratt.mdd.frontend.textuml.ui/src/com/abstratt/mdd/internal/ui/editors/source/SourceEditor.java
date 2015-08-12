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
 *******************************************************************************/
package com.abstratt.mdd.internal.ui.editors.source;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.abstratt.mdd.frontend.ui.IMDDEditor;
import com.abstratt.mdd.internal.frontend.textuml.TextUMLCompiler;
import com.abstratt.mdd.internal.ui.TextUMLUIPlugin;
import com.abstratt.mdd.internal.ui.editors.WorkingCopy;
import com.abstratt.mdd.internal.ui.editors.WorkingCopyRegistry;

public class SourceEditor extends TextEditor implements IMDDEditor {

	protected SourceContentOutlinePage outlinePage;

	public SourceEditor() {
		setSourceViewerConfiguration(new TextUMLSourceViewerConfiguration(this));
		// set the document provider to create the partitioner
		setDocumentProvider(new FileDocumentProvider() {
			protected IDocument createDocument(Object element) throws CoreException {
				IDocument document = super.createDocument(element);
				if (document != null) {
					// this will create partitions
					IDocumentPartitioner partitioner = new FastPartitioner(new PartitionScanner(),
					        ContentTypes.CONFIGURED_CONTENT_TYPES);
					partitioner.connect(document);
					document.setDocumentPartitioner(partitioner);
				}
				return document;
			}
		});
	}

	@Override
	protected void createActions() {
		super.createActions();

		IAction contentAssistAction = new ContentAssistAction(Messages.RESOURCE_BUNDLE, "ContentAssistProposal.", this);
		contentAssistAction.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", contentAssistAction);
		markAsStateDependentAction("ContentAssistProposal", true);
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = super.createSourceViewer(parent, ruler, styles);

		// register IDocument with Jabal registry
		viewer.addTextInputListener(new ITextInputListener() {
			public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
				// nothing for now
			}

			public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
				WorkingCopyRegistry registry = WorkingCopyRegistry.getInstance();
				if (oldInput != null) {
					registry.unregister(oldInput);
				}
				if (newInput != null) {
					final IFile file = getFile();
					if (file != null)
						registry.register(newInput, file);
				}
			}
		});

		return viewer;
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		// TODO: we need a better solution to update the outline
		if (outlinePage != null)
			outlinePage.refresh();
	}

	@Override
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		if (TextUMLUIPlugin.getDefault().getPluginPreferences().getBoolean(TextUMLUIPlugin.FORMAT_ON_SAVE))
			doFormat();
		super.performSaveAs(progressMonitor);
	}

	@Override
	protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {
		if (TextUMLUIPlugin.getDefault().getPluginPreferences().getBoolean(TextUMLUIPlugin.FORMAT_ON_SAVE))
			doFormat();
		super.performSave(overwrite, progressMonitor);
	}

	public void format() {
		Display display = null;
		IWorkbenchPartSite site = getSite();
		Shell shell = site.getShell();
		if (shell != null && !shell.isDisposed())
			display = shell.getDisplay();
		BusyIndicator.showWhile(display, new Runnable() {
			public void run() {
				doFormat();
			}
		});
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (outlinePage == null) {
				outlinePage = new SourceContentOutlinePage(this);
			}
			return outlinePage;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Returns the corresponding file, if any.
	 * 
	 * @return
	 */
	protected IFile getFile() {
		if (!(getEditorInput() instanceof IFileEditorInput))
			return null;
		IFileEditorInput input = (IFileEditorInput) getEditorInput();
		return input.getFile();
	}

	public IFile getModelFile() {
		final WorkingCopy workingCopy = getWorkingCopy();
		if (workingCopy == null)
			return null;
		IDocument document = workingCopy.getDocument();
		String toFormat = document.get();
		String modelName = new TextUMLCompiler().findModelName(toFormat);
		if (modelName == null)
			modelName = getFile().getFullPath().removeFileExtension().lastSegment();
		// TODO clean-up
		IFile modelFile = getFile().getProject().getFile(modelName + ".uml");
		return modelFile;
	}

	public WorkingCopy getWorkingCopy() {
		WorkingCopyRegistry registry = WorkingCopyRegistry.getInstance();
		IDocument document = getSourceViewer().getDocument();
		return registry.getWorkingCopy(document);
	}

	private void doFormat() {
		ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();
		IDocument document = getWorkingCopy().getDocument();
		String toFormat = document.get();
		String formatted = new TextUMLCompiler().format(toFormat);
		document.set(formatted);
		getSelectionProvider().setSelection(selection);
	}
}