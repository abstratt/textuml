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

import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.abstratt.mdd.internal.ui.model.ModelChildrenComparator;

public class UIModelObjectViewerComparator extends ViewerComparator {

    public UIModelObjectViewerComparator() {
        super(new ModelChildrenComparator());
    }

    @SuppressWarnings("unchecked")
    public int compare(Viewer viewer, Object e1, Object e2) {
        TreeNode node1 = (TreeNode) e1;
        TreeNode node2 = (TreeNode) e2;
        return getComparator().compare(node1.getValue(), node2.getValue());
    }

}