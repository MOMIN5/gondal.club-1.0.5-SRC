// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.util.Arrays;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.List;

public interface Dns
{
    public static final Dns SYSTEM = new Dns() {
        @Override
        public List<InetAddress> lookup(final String hostname) throws UnknownHostException {
            if (hostname == null) {
                throw new UnknownHostException("hostname == null");
            }
            return Arrays.asList(InetAddress.getAllByName(hostname));
        }
    };
    
    List<InetAddress> lookup(final String p0) throws UnknownHostException;
}
