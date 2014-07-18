/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.internal.frontend.textuml.node;

import com.abstratt.mdd.internal.frontend.textuml.analysis.*;

@SuppressWarnings("nls")
public final class AWithIfStatementResolved extends PStatementResolved
{
    private PIfStatement _ifStatement_;

    public AWithIfStatementResolved()
    {
        // Constructor
    }

    public AWithIfStatementResolved(
        @SuppressWarnings("hiding") PIfStatement _ifStatement_)
    {
        // Constructor
        setIfStatement(_ifStatement_);

    }

    @Override
    public Object clone()
    {
        return new AWithIfStatementResolved(
            cloneNode(this._ifStatement_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAWithIfStatementResolved(this);
    }

    public PIfStatement getIfStatement()
    {
        return this._ifStatement_;
    }

    public void setIfStatement(PIfStatement node)
    {
        if(this._ifStatement_ != null)
        {
            this._ifStatement_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._ifStatement_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._ifStatement_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._ifStatement_ == child)
        {
            this._ifStatement_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._ifStatement_ == oldChild)
        {
            setIfStatement((PIfStatement) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}