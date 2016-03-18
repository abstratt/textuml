/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.frontend.textuml.grammar.node;

import com.abstratt.mdd.frontend.textuml.grammar.analysis.*;

@SuppressWarnings("nls")
public final class APermissionConstraint extends PPermissionConstraint
{
    private TAllow _allow_;
    private PPermissionRoles _permissionRoles_;
    private PAccessCapabilities _accessCapabilities_;
    private PPermissionExpression _permissionExpression_;

    public APermissionConstraint()
    {
        // Constructor
    }

    public APermissionConstraint(
        @SuppressWarnings("hiding") TAllow _allow_,
        @SuppressWarnings("hiding") PPermissionRoles _permissionRoles_,
        @SuppressWarnings("hiding") PAccessCapabilities _accessCapabilities_,
        @SuppressWarnings("hiding") PPermissionExpression _permissionExpression_)
    {
        // Constructor
        setAllow(_allow_);

        setPermissionRoles(_permissionRoles_);

        setAccessCapabilities(_accessCapabilities_);

        setPermissionExpression(_permissionExpression_);

    }

    @Override
    public Object clone()
    {
        return new APermissionConstraint(
            cloneNode(this._allow_),
            cloneNode(this._permissionRoles_),
            cloneNode(this._accessCapabilities_),
            cloneNode(this._permissionExpression_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAPermissionConstraint(this);
    }

    public TAllow getAllow()
    {
        return this._allow_;
    }

    public void setAllow(TAllow node)
    {
        if(this._allow_ != null)
        {
            this._allow_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._allow_ = node;
    }

    public PPermissionRoles getPermissionRoles()
    {
        return this._permissionRoles_;
    }

    public void setPermissionRoles(PPermissionRoles node)
    {
        if(this._permissionRoles_ != null)
        {
            this._permissionRoles_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._permissionRoles_ = node;
    }

    public PAccessCapabilities getAccessCapabilities()
    {
        return this._accessCapabilities_;
    }

    public void setAccessCapabilities(PAccessCapabilities node)
    {
        if(this._accessCapabilities_ != null)
        {
            this._accessCapabilities_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._accessCapabilities_ = node;
    }

    public PPermissionExpression getPermissionExpression()
    {
        return this._permissionExpression_;
    }

    public void setPermissionExpression(PPermissionExpression node)
    {
        if(this._permissionExpression_ != null)
        {
            this._permissionExpression_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._permissionExpression_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._allow_)
            + toString(this._permissionRoles_)
            + toString(this._accessCapabilities_)
            + toString(this._permissionExpression_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._allow_ == child)
        {
            this._allow_ = null;
            return;
        }

        if(this._permissionRoles_ == child)
        {
            this._permissionRoles_ = null;
            return;
        }

        if(this._accessCapabilities_ == child)
        {
            this._accessCapabilities_ = null;
            return;
        }

        if(this._permissionExpression_ == child)
        {
            this._permissionExpression_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._allow_ == oldChild)
        {
            setAllow((TAllow) newChild);
            return;
        }

        if(this._permissionRoles_ == oldChild)
        {
            setPermissionRoles((PPermissionRoles) newChild);
            return;
        }

        if(this._accessCapabilities_ == oldChild)
        {
            setAccessCapabilities((PAccessCapabilities) newChild);
            return;
        }

        if(this._permissionExpression_ == oldChild)
        {
            setPermissionExpression((PPermissionExpression) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
