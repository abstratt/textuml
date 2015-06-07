package com.abstratt.mdd.target.base;

import com.abstratt.mdd.target.base.IBehaviorGenerator.IExecutionContext
import com.abstratt.mdd.target.base.IBehaviorGenerator.SimpleContext
import java.util.Arrays
import java.util.Deque
import java.util.LinkedList

import static com.abstratt.mdd.target.base.IBehaviorGenerator.*

public interface IBehaviorGenerator extends IBasicBehaviorGenerator {
    
    public final static ThreadLocal<Deque<IExecutionContext>> currentContextStack = new ThreadLocal<Deque<IExecutionContext>>() {
        override protected initialValue() {
            new LinkedList(Arrays.asList(new SimpleContext("this")))
        }
    }
    
    public interface IExecutionContext {
        public def CharSequence generateCurrentReference();
        public def IBasicBehaviorGenerator getDelegate() {
            currentContextStack.get().findLast[it.delegate != null].delegate
        }        
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

    def getContext() {
        contextStack.peek
    }
}
