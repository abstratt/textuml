/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.frontend.textuml.grammar.node;

import com.abstratt.mdd.frontend.textuml.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ACallAccessCapability extends PAccessCapability
{
    private TCall _call_;

    public ACallAccessCapability()
    {
        // Constructor
    }

    public ACallAccessCapability(
        @SuppressWarnings("hiding") TCall _call_)
    {
        // Constructor
        setCall(_call_);

    }

    @Override
    public Object clone()
    {
        return new ACallAccessCapability(
            cloneNode(this._call_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseACallAccessCapability(this);
    }

    public TCall getCall()
    {
        return this._call_;
    }

    public void setCall(TCall node)
    {
        if(this._call_ != null)
        {
            this._call_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._call_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._call_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._call_ == child)
        {
            this._call_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._call_ == oldChild)
        {
            setCall((TCall) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
