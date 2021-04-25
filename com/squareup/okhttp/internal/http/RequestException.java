// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import java.io.IOException;

public final class RequestException extends Exception
{
    public RequestException(final IOException cause) {
        super(cause);
    }
    
    @Override
    public IOException getCause() {
        return (IOException)super.getCause();
    }
}
