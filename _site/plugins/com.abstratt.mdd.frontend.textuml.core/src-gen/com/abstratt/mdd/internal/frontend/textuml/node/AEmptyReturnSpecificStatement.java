/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.internal.frontend.textuml.node;

import com.abstratt.mdd.internal.frontend.textuml.analysis.*;

@SuppressWarnings("nls")
public final class AEmptyReturnSpecificStatement extends PSpecificStatement
{
    private TReturn _return_;

    public AEmptyReturnSpecificStatement()
    {
        // Constructor
    }

    public AEmptyReturnSpecificStatement(
        @SuppressWarnings("hiding") TReturn _return_)
    {
        // Constructor
        setReturn(_return_);

    }

    @Override
    public Object clone()
    {
        return new AEmptyReturnSpecificStatement(
            cloneNode(this._return_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAEmptyReturnSpecificStatement(this);
    }

    public TReturn getReturn()
    {
        return this._return_;
    }

    public void setReturn(TReturn node)
    {
        if(this._return_ != null)
        {
            this._return_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._return_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._return_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._return_ == child)
        {
            this._return_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._return_ == oldChild)
        {
            setReturn((TReturn) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}