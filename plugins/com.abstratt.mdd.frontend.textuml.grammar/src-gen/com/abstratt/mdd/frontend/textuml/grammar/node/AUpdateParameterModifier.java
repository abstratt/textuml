/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.frontend.textuml.grammar.node;

import com.abstratt.mdd.frontend.textuml.grammar.analysis.*;

@SuppressWarnings("nls")
public final class AUpdateParameterModifier extends PParameterModifier
{
    private TUpdate _update_;

    public AUpdateParameterModifier()
    {
        // Constructor
    }

    public AUpdateParameterModifier(
        @SuppressWarnings("hiding") TUpdate _update_)
    {
        // Constructor
        setUpdate(_update_);

    }

    @Override
    public Object clone()
    {
        return new AUpdateParameterModifier(
            cloneNode(this._update_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAUpdateParameterModifier(this);
    }

    public TUpdate getUpdate()
    {
        return this._update_;
    }

    public void setUpdate(TUpdate node)
    {
        if(this._update_ != null)
        {
            this._update_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._update_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._update_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._update_ == child)
        {
            this._update_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._update_ == oldChild)
        {
            setUpdate((TUpdate) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}