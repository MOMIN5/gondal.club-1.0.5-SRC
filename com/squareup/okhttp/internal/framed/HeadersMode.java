// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.framed;

public enum HeadersMode
{
    SPDY_SYN_STREAM, 
    SPDY_REPLY, 
    SPDY_HEADERS, 
    HTTP_20_HEADERS;
    
    public boolean failIfStreamAbsent() {
        return this == HeadersMode.SPDY_REPLY || this == HeadersMode.SPDY_HEADERS;
    }
    
    public boolean failIfStreamPresent() {
        return this == HeadersMode.SPDY_SYN_STREAM;
    }
    
    public boolean failIfHeadersAbsent() {
        return this == HeadersMode.SPDY_HEADERS;
    }
    
    public boolean failIfHeadersPresent() {
        return this == HeadersMode.SPDY_REPLY;
    }
}
