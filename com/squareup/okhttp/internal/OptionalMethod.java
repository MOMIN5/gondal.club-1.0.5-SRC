// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class OptionalMethod<T>
{
    private final Class<?> returnType;
    private final String methodName;
    private final Class[] methodParams;
    
    public OptionalMethod(final Class<?> returnType, final String methodName, final Class... methodParams) {
        this.returnType = returnType;
        this.methodName = methodName;
        this.methodParams = methodParams;
    }
    
    public boolean isSupported(final T target) {
        return this.getMethod(target.getClass()) != null;
    }
    
    public Object invokeOptional(final T target, final Object... args) throws InvocationTargetException {
        final Method m = this.getMethod(target.getClass());
        if (m == null) {
            return null;
        }
        try {
            return m.invoke(target, args);
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }
    
    public Object invokeOptionalWithoutCheckedException(final T target, final Object... args) {
        try {
            return this.invokeOptional(target, args);
        }
        catch (InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException)targetException;
            }
            final AssertionError error = new AssertionError((Object)"Unexpected exception");
            error.initCause(targetException);
            throw error;
        }
    }
    
    public Object invoke(final T target, final Object... args) throws InvocationTargetException {
        final Method m = this.getMethod(target.getClass());
        if (m == null) {
            throw new AssertionError((Object)("Method " + this.methodName + " not supported for object " + target));
        }
        try {
            return m.invoke(target, args);
        }
        catch (IllegalAccessException e) {
            final AssertionError error = new AssertionError((Object)("Unexpectedly could not call: " + m));
            error.initCause(e);
            throw error;
        }
    }
    
    public Object invokeWithoutCheckedException(final T target, final Object... args) {
        try {
            return this.invoke(target, args);
        }
        catch (InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException)targetException;
            }
            final AssertionError error = new AssertionError((Object)"Unexpected exception");
            error.initCause(targetException);
            throw error;
        }
    }
    
    private Method getMethod(final Class<?> clazz) {
        Method method = null;
        if (this.methodName != null) {
            method = getPublicMethod(clazz, this.methodName, this.methodParams);
            if (method != null && this.returnType != null && !this.returnType.isAssignableFrom(method.getReturnType())) {
                method = null;
            }
        }
        return method;
    }
    
    private static Method getPublicMethod(final Class<?> clazz, final String methodName, final Class[] parameterTypes) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName, (Class<?>[])parameterTypes);
            if ((method.getModifiers() & 0x1) == 0x0) {
                method = null;
            }
        }
        catch (NoSuchMethodException ex) {}
        return method;
    }
}
