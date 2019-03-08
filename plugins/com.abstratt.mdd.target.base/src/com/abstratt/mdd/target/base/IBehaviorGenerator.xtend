package com.abstratt.mdd.target.base;

import com.abstratt.mdd.target.base.IBehaviorGenerator.IExecutionContext
import com.abstratt.mdd.target.base.IBehaviorGenerator.SimpleContext
import java.util.Arrays
import java.util.Deque
import java.util.LinkedList
import java.util.function.Supplier
import java.util.stream.Stream
import org.eclipse.uml2.uml.Action

import java.util.function.Function

interface IBehaviorGenerator extends IBasicBehaviorGenerator {
    
    final static ThreadLocal<Deque<IExecutionContext>> currentContextStack = new ThreadLocal<Deque<IExecutionContext>>() {
        override protected initialValue() {
            new LinkedList(Arrays.asList(new SimpleContext("this")))
        }
    }
    
    final static ThreadLocal<Deque<Action>> currentActionStack = new ThreadLocal<Deque<Action>>() {
        override protected initialValue() {
            new LinkedList()
        }
    }
    
    interface IExecutionContext {
        def CharSequence generateCurrentReference();
        def IBasicBehaviorGenerator getDelegate() {
            currentContextStack.get().findLast[it.delegate !== null].delegate
        }
    }
    
    def Action getCurrentAction() {
        return currentActionStack.get().peek()
    }
    
    def runWithAction(Action node, Function<Action, CharSequence> function) {
        currentActionStack.get().push(node)
        try {
            function.apply(node)
        } finally {
            currentActionStack.get().pop()    
        }
    }
    
    override generateAction(Action node, boolean delegate) {
        runWithAction(node, [action | 
            if (delegate && context.delegate !== null)
                context.delegate.generateAction(node, false)
            else
                generateActionProper(node)
        ])
    }
    
    abstract def CharSequence generateActionProper(Action action)
    
    def Stream<Action> getRecentActions() {
        return currentActionStack.get().stream().sequential()
    }
    
    class SimpleContext implements IExecutionContext {
		final CharSequence reference;
		final IBasicBehaviorGenerator delegate

        new(String reference) {
            this(reference, null);
        }
        new(String reference, IBasicBehaviorGenerator delegate) {
            this.reference = reference;
            this.delegate = delegate;
        }
        
        override CharSequence generateCurrentReference() {
            return reference;
        }
        
        override getDelegate() {
            return delegate;
        }
    }
    
    def getContextStack() {
        currentContextStack.get()
    }

    def enterContext(IExecutionContext context) {
        contextStack.push(context)
    }

    def leaveContext(IExecutionContext context) {
        val top = contextStack.peek
        if (context != top)
            throw new IllegalStateException
        contextStack.pop
    }
    
    def <R> R runInContext(IExecutionContext context, Supplier<R> p) {
        enterContext(context)
        try {
            return p.get()
        } finally {
            leaveContext(context)
        }
    }

    def getContext() {
        contextStack.peek
    }
}
