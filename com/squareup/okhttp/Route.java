// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.net.InetSocketAddress;
import java.net.Proxy;

public final class Route
{
    final Address address;
    final Proxy proxy;
    final InetSocketAddress inetSocketAddress;
    
    public Route(final Address address, final Proxy proxy, final InetSocketAddress inetSocketAddress) {
        if (address == null) {
            throw new NullPointerException("address == null");
        }
        if (proxy == null) {
            throw new NullPointerException("proxy == null");
        }
        if (inetSocketAddress == null) {
            throw new NullPointerException("inetSocketAddress == null");
        }
        this.address = address;
        this.proxy = proxy;
        this.inetSocketAddress = inetSocketAddress;
    }
    
    public Address getAddress() {
        return this.address;
    }
    
    public Proxy getProxy() {
        return this.proxy;
    }
    
    public InetSocketAddress getSocketAddress() {
        return this.inetSocketAddress;
    }
    
    public boolean requiresTunnel() {
        return this.address.sslSocketFactory != null && this.proxy.type() == Proxy.Type.HTTP;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Route) {
            final Route other = (Route)obj;
            return this.address.equals(other.address) && this.proxy.equals(other.proxy) && this.inetSocketAddress.equals(other.inetSocketAddress);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.address.hashCode();
        result = 31 * result + this.proxy.hashCode();
        result = 31 * result + this.inetSocketAddress.hashCode();
        return result;
    }
}
