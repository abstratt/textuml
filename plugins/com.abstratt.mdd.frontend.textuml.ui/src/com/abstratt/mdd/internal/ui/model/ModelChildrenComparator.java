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
package com.abstratt.mdd.internal.ui.model;

import java.util.Comparator;

/**
 * Sorts the model object children according to the order returned by
 * UIModelObject.getChildrenTypes().
 */
public class ModelChildrenComparator implements Comparator {

    public int compare(Object arg0, Object arg1) {
        UIModelObject model0 = (UIModelObject) arg0;
        UIModelObject model1 = (UIModelObject) arg1;
        int i0 = getIndex(model0);
        int i1 = getIndex(model1);
        return i0 - i1;
    }

    protected int getIndex(UIModelObject target) {
        Class targetType = target.getNode().getBaseNode().getClass();
        Class[] types = target.getParent().getChildrenTypes();
        for (int i = 0; i < types.length; i++) {
            Class type = types[i];
            if (type == targetType) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }
}