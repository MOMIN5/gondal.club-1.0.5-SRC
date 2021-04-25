// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.lang.reflect.Method;

public final class RouteException extends Exception
{
    private static final Method addSuppressedExceptionMethod;
    private IOException lastException;
    
    public RouteException(final IOException cause) {
        super(cause);
        this.lastException = cause;
    }
    
    public IOException getLastConnectException() {
        return this.lastException;
    }
    
    public void addConnectException(final IOException e) {
        this.addSuppressedIfPossible(e, this.lastException);
        this.lastException = e;
    }
    
    private void addSuppressedIfPossible(final IOException e, final IOException suppressed) {
        if (RouteException.addSuppressedExceptionMethod != null) {
            try {
                RouteException.addSuppressedExceptionMethod.invoke(e, suppressed);
            }
            catch (InvocationTargetException ex) {}
            catch (IllegalAccessException ex2) {}
        }
    }
    
    static {
        Method m;
        try {
            m = Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class);
        }
        catch (Exception e) {
            m = null;
        }
        addSuppressedExceptionMethod = m;
    }
}
