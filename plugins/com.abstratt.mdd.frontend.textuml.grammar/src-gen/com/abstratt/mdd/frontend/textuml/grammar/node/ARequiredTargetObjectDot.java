/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.frontend.textuml.grammar.node;

import com.abstratt.mdd.frontend.textuml.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ARequiredTargetObjectDot extends PObjectDot
{
    private TDot _dot_;

    public ARequiredTargetObjectDot()
    {
        // Constructor
    }

    public ARequiredTargetObjectDot(
        @SuppressWarnings("hiding") TDot _dot_)
    {
        // Constructor
        setDot(_dot_);

    }

    @Override
    public Object clone()
    {
        return new ARequiredTargetObjectDot(
            cloneNode(this._dot_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseARequiredTargetObjectDot(this);
    }

    public TDot getDot()
    {
        return this._dot_;
    }

    public void setDot(TDot node)
    {
        if(this._dot_ != null)
        {
            this._dot_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dot_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._dot_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._dot_ == child)
        {
            this._dot_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._dot_ == oldChild)
        {
            setDot((TDot) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
