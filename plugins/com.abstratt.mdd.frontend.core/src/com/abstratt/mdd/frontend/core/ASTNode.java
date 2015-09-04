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
package com.abstratt.mdd.frontend.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class ASTNode<T, N> {

    public enum VisitorResult {
        CONTINUE, SKIP, STOP
    }

    public static <T, N> ASTNode<T, N> buildTree(ASTNode<T, N> parent, N startingPoint) {
        return new ASTNode<T, N>(parent, startingPoint);
    }

    public static <T, N> ASTNode<T, N> buildTree(N startingPoint) {
        return new ASTNode<T, N>(startingPoint);
    }

    private N baseNode;

    private List<ASTNode<T, N>> children;

    private ASTNode<T, N> parent;

    private ASTNode(ASTNode<T, N> parent, N node) {
        this.baseNode = node;
        this.parent = parent;
        children = computeChildren();
    }

    private ASTNode(N node) {
        Assert.isNotNull(node);
        this.baseNode = node;
        children = computeChildren();
    }

    /**
     * Visits the current node and its parent.
     */
    public void accept(ASTReverseVisitor<T, N> visitor) {
        if (visitor.visit(this)) {
            ASTNode<T, N> parent = getParent();
            if (parent != null)
                parent.accept(visitor);
        }
    }

    /**
     * Visits the current node and its children.
     */
    public boolean accept(ASTVisitor<T, N> visitor) {
        final VisitorResult outcome = visitor.visit(this);
        if (outcome == VisitorResult.STOP)
            return false;
        if (outcome == VisitorResult.SKIP)
            return true;
        List<ASTNode<T, N>> children = this.getChildren();
        for (Iterator<ASTNode<T, N>> iter = children.iterator(); iter.hasNext();) {
            ASTNode<T, N> child = iter.next();
            if (!child.accept(visitor))
                return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<ASTNode<T, N>> computeChildren() {
        Field[] fields = baseNode.getClass().getDeclaredFields();
        List<ASTNode<T, N>> children = new ArrayList<ASTNode<T, N>>();
        for (int i = 0; i < fields.length; i++) {
            try {
                Method accessor = baseNode.getClass().getMethod(getAccessorName(fields[i].getName()), new Class[0]);
                if (accessor == null)
                    continue;
                Object value = accessor.invoke(baseNode);
                if (value == null)
                    continue;
                if (isNodeType(value.getClass())) {
                    final N nodeValue = (N) value;
                    children.add(buildTree(this, nodeValue));
                } else if (Iterable.class.isAssignableFrom(value.getClass())) {
                    final Iterable<N> nodeValueSequence = (Iterable<N>) value;
                    for (N n : nodeValueSequence)
                        children.add(buildTree(this, n));
                } else
                    // ignore
                    ;
            } catch (IllegalAccessException e) {
                // never happens - we only process public methods
            } catch (InvocationTargetException e) {
                // not likely
            } catch (NoSuchMethodException e) {
                // not likely
            }
        }
        return children;
    }

    public N findNode(final Class<? extends N> nodeType) {
        return findNode(nodeType, null);
    }

    public N findNode(final Class<? extends N> nodeType, final String text) {
        final Object[] found = { null };
        this.accept(new ASTVisitor<T, N>() {
            public ASTNode.VisitorResult visit(ASTNode<T, N> node) {
                if (nodeType != null && !nodeType.isAssignableFrom(node.getBaseNode().getClass()))
                    return ASTNode.VisitorResult.CONTINUE;
                if (text != null && !text.equals(node.getBaseNode().toString().trim()))
                    return ASTNode.VisitorResult.CONTINUE;
                found[0] = node.getBaseNode();
                return ASTNode.VisitorResult.STOP;
            }
        });
        return (N) found[0];
    }

    public N findNode(final String text) {
        return findNode(null, text);
    }

    private String getAccessorName(String name) {
        name = name.substring(1, name.length() - 1);
        return "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public N getBaseNode() {
        return baseNode;
    }

    public List<ASTNode<T, N>> getChildren() {
        return children;
    }

    public ASTNode<T, N> getParent() {
        return parent;
    }

    private String getSimpleName(Class<?> clazz) {
        String fullName = clazz.getName();
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    public boolean hasChildren() {
        return children.isEmpty();
    }

    /**
     * Checks if the node contents are of the given type.
     */
    public boolean instanceOf(Class<? extends N> target) {
        return target.isInstance(baseNode);
    }

    private boolean isNodeType(Class<?> fieldType) {
        Class<?> current = fieldType;
        while (current != null) {
            if ("Node".equals(current.getSimpleName()))
                return true;
            current = current.getSuperclass();
        }
        return false;
    }

    public boolean isToken() {
        return "Token".equals(baseNode.getClass().getSuperclass().getSimpleName());
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(getSimpleName(baseNode.getClass()));
        if (isToken())
            result.append(" " + baseNode);
        else if (!hasChildren())
            result.append(getChildren());
        return result.toString();
    }

}