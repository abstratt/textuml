/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.internal.frontend.textuml.node;

import com.abstratt.mdd.internal.frontend.textuml.analysis.*;

@SuppressWarnings("nls")
public final class ARaisedExceptionListTail extends PRaisedExceptionListTail
{
    private TComma _comma_;
    private PRaisedExceptionItem _raisedExceptionItem_;

    public ARaisedExceptionListTail()
    {
        // Constructor
    }

    public ARaisedExceptionListTail(
        @SuppressWarnings("hiding") TComma _comma_,
        @SuppressWarnings("hiding") PRaisedExceptionItem _raisedExceptionItem_)
    {
        // Constructor
        setComma(_comma_);

        setRaisedExceptionItem(_raisedExceptionItem_);

    }

    @Override
    public Object clone()
    {
        return new ARaisedExceptionListTail(
            cloneNode(this._comma_),
            cloneNode(this._raisedExceptionItem_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseARaisedExceptionListTail(this);
    }

    public TComma getComma()
    {
        return this._comma_;
    }

    public void setComma(TComma node)
    {
        if(this._comma_ != null)
        {
            this._comma_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._comma_ = node;
    }

    public PRaisedExceptionItem getRaisedExceptionItem()
    {
        return this._raisedExceptionItem_;
    }

    public void setRaisedExceptionItem(PRaisedExceptionItem node)
    {
        if(this._raisedExceptionItem_ != null)
        {
            this._raisedExceptionItem_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._raisedExceptionItem_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._comma_)
            + toString(this._raisedExceptionItem_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._comma_ == child)
        {
            this._comma_ = null;
            return;
        }

        if(this._raisedExceptionItem_ == child)
        {
            this._raisedExceptionItem_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._comma_ == oldChild)
        {
            setComma((TComma) newChild);
            return;
        }

        if(this._raisedExceptionItem_ == oldChild)
        {
            setRaisedExceptionItem((PRaisedExceptionItem) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}