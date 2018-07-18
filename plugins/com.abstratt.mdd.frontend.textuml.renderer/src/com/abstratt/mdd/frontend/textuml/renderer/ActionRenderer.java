package com.abstratt.mdd.frontend.textuml.renderer;

import org.eclipse.uml2.uml.Action;

import com.abstratt.mdd.modelrenderer.IEObjectRenderer;
import com.abstratt.mdd.modelrenderer.IRenderer;
import com.abstratt.mdd.modelrenderer.IRendererSelector;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class ActionRenderer<A extends Action> implements IEObjectRenderer<A> {

    @Override
    final public boolean renderObject(A element, IndentedPrintWriter out, IRenderingSession session) {
        out.println(renderAction(element, session.getSelector()));
        return true;
    }
    
    protected CharSequence renderAnotherAction(Action anotherAction, IRendererSelector<Action> selector) {
		IRenderer<Action> anotherRenderer = selector.select(anotherAction);
		ActionRenderer<Action> anotherActionRenderer = (ActionRenderer<Action>) anotherRenderer;
		CharSequence rendered = anotherActionRenderer.renderAction(anotherAction, selector);
    	return rendered;
    }
    
    protected CharSequence renderAction(A action, IRendererSelector<Action> selector) {
    	return "/* TBD: " + action.eClass().getName() + " */";
    }
    
    protected static <A extends Action> ActionRenderer<A> findRenderer(A action, IRendererSelector<Action> selector) {
    	return (ActionRenderer<A>) selector.select(action); 
    }

}
