package com.abstratt.mdd.core.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.MessageEvent;
import org.eclipse.uml2.uml.SignalEvent;

public class EventUtils {

    public static <M> M getMessage(MessageEvent event) {
        if (event instanceof CallEvent)
            return (M) ((CallEvent) event).getOperation();
        if (event instanceof SignalEvent)
            return (M) ((SignalEvent) event).getSignal();
        Assert.isLegal(false, event.getClass().getName());
        return null;
    }

}
