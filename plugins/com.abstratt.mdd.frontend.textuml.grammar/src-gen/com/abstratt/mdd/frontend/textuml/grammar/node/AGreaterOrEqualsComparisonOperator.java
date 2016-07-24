/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.frontend.textuml.grammar.node;

import com.abstratt.mdd.frontend.textuml.grammar.analysis.*;

@SuppressWarnings("nls")
public final class AGreaterOrEqualsComparisonOperator extends PComparisonOperator
{
    private TRabEquals _rabEquals_;

    public AGreaterOrEqualsComparisonOperator()
    {
        // Constructor
    }

    public AGreaterOrEqualsComparisonOperator(
        @SuppressWarnings("hiding") TRabEquals _rabEquals_)
    {
        // Constructor
        setRabEquals(_rabEquals_);

    }

    @Override
    public Object clone()
    {
        return new AGreaterOrEqualsComparisonOperator(
            cloneNode(this._rabEquals_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAGreaterOrEqualsComparisonOperator(this);
    }

    public TRabEquals getRabEquals()
    {
        return this._rabEquals_;
    }

    public void setRabEquals(TRabEquals node)
    {
        if(this._rabEquals_ != null)
        {
            this._rabEquals_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._rabEquals_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._rabEquals_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._rabEquals_ == child)
        {
            this._rabEquals_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._rabEquals_ == oldChild)
        {
            setRabEquals((TRabEquals) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}