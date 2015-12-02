package com.abstratt.mdd.target.base;

import com.abstratt.mdd.target.base.IBehaviorGenerator.IExecutionContext
import com.abstratt.mdd.target.base.IBehaviorGenerator.SimpleContext
import java.util.Arrays
import java.util.Deque
import java.util.LinkedList
import java.util.function.Supplier
import java.util.stream.Stream
import org.eclipse.uml2.uml.Action

import static com.abstratt.mdd.target.base.IBehaviorGenerator.*
import java.util.function.Function

public interface IBehaviorGenerator extends IBasicBehaviorGenerator {
    
    public final static ThreadLocal<Deque<IExecutionContext>> currentContextStack = new ThreadLocal<Deque<IExecutionContext>>() {
        override protected initialValue() {
            new LinkedList(Arrays.asList(new SimpleContext("this")))
        }
    }
    
    final static ThreadLocal<Deque<Action>> currentActionStack = new ThreadLocal<Deque<Action>>() {
        override protected initialValue() {
            new LinkedList()
        }
    }
    
    public interface IExecutionContext {
        public def CharSequence generateCurrentReference();
        public def IBasicBehaviorGenerator getDelegate() {
            currentContextStack.get().findLast[it.delegate != null].delegate
        }        
    }
    
    public def Action getCurrentAction() {
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
    
    public override def generateAction(Action node, boolean delegate) {
        runWithAction(node, [action | 
            if (delegate && context.delegate != null)
                context.delegate.generateAction(node, false)
            else
                generateActionProper(node)
        ])
    }
    
    abstract def CharSequence generateActionProper(Action action)
    
    public def Stream<Action> getRecentActions() {
        return currentActionStack.get().stream().sequential()
    }
    
    class SimpleContext implements IExecutionContext {
        private final CharSequence reference;
        private final IBasicBehaviorGenerator delegate

        public new(String reference) {
            this(reference, null);
        }
        public new(String reference, IBasicBehaviorGenerator delegate) {
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
