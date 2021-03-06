/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.frontend.textuml.grammar.node;

import com.abstratt.mdd.frontend.textuml.grammar.analysis.*;

@SuppressWarnings("nls")
public final class AEnumerationLiteralSlotValues extends PEnumerationLiteralSlotValues
{
    private TLParen _lParen_;
    private PNamedSimpleValueList _namedSimpleValueList_;
    private TRParen _rParen_;

    public AEnumerationLiteralSlotValues()
    {
        // Constructor
    }

    public AEnumerationLiteralSlotValues(
        @SuppressWarnings("hiding") TLParen _lParen_,
        @SuppressWarnings("hiding") PNamedSimpleValueList _namedSimpleValueList_,
        @SuppressWarnings("hiding") TRParen _rParen_)
    {
        // Constructor
        setLParen(_lParen_);

        setNamedSimpleValueList(_namedSimpleValueList_);

        setRParen(_rParen_);

    }

    @Override
    public Object clone()
    {
        return new AEnumerationLiteralSlotValues(
            cloneNode(this._lParen_),
            cloneNode(this._namedSimpleValueList_),
            cloneNode(this._rParen_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAEnumerationLiteralSlotValues(this);
    }

    public TLParen getLParen()
    {
        return this._lParen_;
    }

    public void setLParen(TLParen node)
    {
        if(this._lParen_ != null)
        {
            this._lParen_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._lParen_ = node;
    }

    public PNamedSimpleValueList getNamedSimpleValueList()
    {
        return this._namedSimpleValueList_;
    }

    public void setNamedSimpleValueList(PNamedSimpleValueList node)
    {
        if(this._namedSimpleValueList_ != null)
        {
            this._namedSimpleValueList_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._namedSimpleValueList_ = node;
    }

    public TRParen getRParen()
    {
        return this._rParen_;
    }

    public void setRParen(TRParen node)
    {
        if(this._rParen_ != null)
        {
            this._rParen_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._rParen_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._lParen_)
            + toString(this._namedSimpleValueList_)
            + toString(this._rParen_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._lParen_ == child)
        {
            this._lParen_ = null;
            return;
        }

        if(this._namedSimpleValueList_ == child)
        {
            this._namedSimpleValueList_ = null;
            return;
        }

        if(this._rParen_ == child)
        {
            this._rParen_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._lParen_ == oldChild)
        {
            setLParen((TLParen) newChild);
            return;
        }

        if(this._namedSimpleValueList_ == oldChild)
        {
            setNamedSimpleValueList((PNamedSimpleValueList) newChild);
            return;
        }

        if(this._rParen_ == oldChild)
        {
            setRParen((TRParen) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
