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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.internal.ui.model.ASTUtils;

/**
 * Keeps track of all the current working copies. Working copies are created for
 * IDocument objects.
 */
public class WorkingCopyRegistry {

	protected static WorkingCopyRegistry instance = new WorkingCopyRegistry();

	protected Set<WorkingCopy> copies;

	public static WorkingCopyRegistry getInstance() {
		return instance;
	}

	protected WorkingCopyRegistry() {
		copies = new HashSet<WorkingCopy>();
	}

	public IFile getFile(ASTNode node) {
		WorkingCopy copy = getWorkingCopy(node);
		if (copy != null) {
			return copy.getFile();
		}
		return null;
	}

	public IFile getFile(IDocument document) {
		WorkingCopy copy = getWorkingCopy(document);
		if (copy != null) {
			return copy.getFile();
		}
		return null;
	}

	public WorkingCopy getWorkingCopy(ASTNode node) {
		ASTNode root = ASTUtils.getRootNode(node);
		for (Iterator<WorkingCopy> iter = copies.iterator(); iter.hasNext();) {
			WorkingCopy copy = iter.next();
			if (root == copy.getRootASTNode()) {
				return copy;
			}
		}
		return null;
	}

	public WorkingCopy getWorkingCopy(IDocument document) {
		for (Iterator<WorkingCopy> iter = copies.iterator(); iter.hasNext();) {
			WorkingCopy copy = iter.next();
			if (document == copy.getDocument()) {
				return copy;
			}
		}
		return null;
	}

	public WorkingCopy getWorkingCopy(IFile file) {
		for (Iterator<WorkingCopy> iter = copies.iterator(); iter.hasNext();) {
			WorkingCopy copy = iter.next();
			if (file == copy.getFile()) {
				return copy;
			}
		}
		return null;
	}

	/**
	 * Registers this document with the working copies. If a working copy for
	 * this document does not exist, a new one will be created.
	 */
	public void register(IDocument document, IFile file) {
		synchronized (copies) {
			WorkingCopy copy = getWorkingCopy(document);
			if (copy == null) {
				copy = new WorkingCopy(document, file);
				copies.add(copy);
			}
		}
	}

	public void unregister(IDocument document) {
		synchronized (copies) {
			WorkingCopy copy = getWorkingCopy(document);
			if (copy != null) {
				copies.remove(copy);
				copy.dispose();
			}
		}
	}
}