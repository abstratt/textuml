/* This file was generated by SableCC (http://www.sablecc.org/). */

package com.abstratt.mdd.internal.frontend.textuml.node;

import com.abstratt.mdd.internal.frontend.textuml.analysis.*;

@SuppressWarnings("nls")
public final class TFinally extends Token
{
    public TFinally()
    {
        super.setText("finally");
    }

    public TFinally(int line, int pos)
    {
        super.setText("finally");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TFinally(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTFinally(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TFinally text.");
    }
}