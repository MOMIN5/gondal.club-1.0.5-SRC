// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.io.IOException;

public enum Protocol
{
    HTTP_1_0("http/1.0"), 
    HTTP_1_1("http/1.1"), 
    SPDY_3("spdy/3.1"), 
    HTTP_2("h2");
    
    private final String protocol;
    
    private Protocol(final String protocol) {
        this.protocol = protocol;
    }
    
    public static Protocol get(final String protocol) throws IOException {
        if (protocol.equals(Protocol.HTTP_1_0.protocol)) {
            return Protocol.HTTP_1_0;
        }
        if (protocol.equals(Protocol.HTTP_1_1.protocol)) {
            return Protocol.HTTP_1_1;
        }
        if (protocol.equals(Protocol.HTTP_2.protocol)) {
            return Protocol.HTTP_2;
        }
        if (protocol.equals(Protocol.SPDY_3.protocol)) {
            return Protocol.SPDY_3;
        }
        throw new IOException("Unexpected protocol: " + protocol);
    }
    
    @Override
    public String toString() {
        return this.protocol;
    }
}
