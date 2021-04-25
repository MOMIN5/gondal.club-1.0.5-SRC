// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.io.IOException;
import java.net.Proxy;

public interface Authenticator
{
    Request authenticate(final Proxy p0, final Response p1) throws IOException;
    
    Request authenticateProxy(final Proxy p0, final Response p1) throws IOException;
}
