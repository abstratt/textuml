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
package com.abstratt.mdd.internal.ui.editors.source;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.internal.frontend.textuml.node.Node;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;
import com.abstratt.mdd.internal.ui.editors.TextUMLLabelProvider;
import com.abstratt.mdd.internal.ui.editors.TextUMLTreeNode;
import com.abstratt.mdd.internal.ui.editors.UIModelObjectViewerComparator;
import com.abstratt.mdd.internal.ui.editors.WorkingCopy;
import com.abstratt.mdd.internal.ui.model.UIModelObject;
import com.abstratt.mdd.ui.UIUtils;

public class SourceContentOutlinePage extends ContentOutlinePage {

	protected IContentProvider contentProvider;
	protected SourceEditor editor;
	protected ILabelProvider labelProvider;
	protected TreeViewer viewer;

	public SourceContentOutlinePage(SourceEditor editor) {
		super();
		this.editor = editor;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		viewer = getTreeViewer();
		contentProvider = new TreeNodeContentProvider();
		viewer.setContentProvider(contentProvider);
		labelProvider = new TextUMLLabelProvider();
		viewer.setLabelProvider(labelProvider);
//      disabled: used to make elements to show sorted by type        
//		viewer.setComparator(new UIModelObjectViewerComparator());
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

		// tracks selections in the outline and reflects them in the editor
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				TreeSelection selection = (TreeSelection) event.getSelection();
				if (!selection.isEmpty()) {
					TreeNode treeNode = (TreeNode) selection.getFirstElement();
					UIModelObject model = (UIModelObject) treeNode.getValue();
					selectInEditor(model.getToken());
				}
			}
		});

		refresh();
	}

	@Override
	public void dispose() {
		contentProvider.dispose();
		contentProvider = null;
		labelProvider.dispose();
		labelProvider = null;
		editor = null;
		super.dispose();
	}

	public void refresh() {
		if (editor == null)
			return;
		WorkingCopy workingCopy = editor.getWorkingCopy();
		if (workingCopy == null)
			return;
		ASTNode<Token, Node> root = workingCopy.getRootASTNode();
		if (root == null)
			return;
		TextUMLTreeNode node = new TextUMLTreeNode(UIModelObject
				.createModelObject(null, root));
		viewer.setInput(node.getChildren());
	}

	protected void selectInEditor(Token token) {
		int line = token.getLine();
		line--; // -1 because SableCC lines are 1-based
		WorkingCopy workingCopy = editor.getWorkingCopy();
		IDocument document = workingCopy.getDocument();
		try {
			int start = document.getLineOffset(line);
			editor.selectAndReveal(start, 0);
		} catch (BadLocationException e) {
			UIUtils.log(e);
		}
	}
}