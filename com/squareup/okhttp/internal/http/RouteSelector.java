// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import java.net.SocketAddress;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import com.squareup.okhttp.HttpUrl;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Collections;
import com.squareup.okhttp.Route;
import java.util.List;
import java.net.InetSocketAddress;
import java.net.Proxy;
import com.squareup.okhttp.internal.RouteDatabase;
import com.squareup.okhttp.Address;

public final class RouteSelector
{
    private final Address address;
    private final RouteDatabase routeDatabase;
    private Proxy lastProxy;
    private InetSocketAddress lastInetSocketAddress;
    private List<Proxy> proxies;
    private int nextProxyIndex;
    private List<InetSocketAddress> inetSocketAddresses;
    private int nextInetSocketAddressIndex;
    private final List<Route> postponedRoutes;
    
    public RouteSelector(final Address address, final RouteDatabase routeDatabase) {
        this.proxies = Collections.emptyList();
        this.inetSocketAddresses = Collections.emptyList();
        this.postponedRoutes = new ArrayList<Route>();
        this.address = address;
        this.routeDatabase = routeDatabase;
        this.resetNextProxy(address.url(), address.getProxy());
    }
    
    public boolean hasNext() {
        return this.hasNextInetSocketAddress() || this.hasNextProxy() || this.hasNextPostponed();
    }
    
    public Route next() throws IOException {
        if (!this.hasNextInetSocketAddress()) {
            if (!this.hasNextProxy()) {
                if (!this.hasNextPostponed()) {
                    throw new NoSuchElementException();
                }
                return this.nextPostponed();
            }
            else {
                this.lastProxy = this.nextProxy();
            }
        }
        this.lastInetSocketAddress = this.nextInetSocketAddress();
        final Route route = new Route(this.address, this.lastProxy, this.lastInetSocketAddress);
        if (this.routeDatabase.shouldPostpone(route)) {
            this.postponedRoutes.add(route);
            return this.next();
        }
        return route;
    }
    
    public void connectFailed(final Route failedRoute, final IOException failure) {
        if (failedRoute.getProxy().type() != Proxy.Type.DIRECT && this.address.getProxySelector() != null) {
            this.address.getProxySelector().connectFailed(this.address.url().uri(), failedRoute.getProxy().address(), failure);
        }
        this.routeDatabase.failed(failedRoute);
    }
    
    private void resetNextProxy(final HttpUrl url, final Proxy proxy) {
        if (proxy != null) {
            this.proxies = Collections.singletonList(proxy);
        }
        else {
            this.proxies = new ArrayList<Proxy>();
            final List<Proxy> selectedProxies = this.address.getProxySelector().select(url.uri());
            if (selectedProxies != null) {
                this.proxies.addAll(selectedProxies);
            }
            this.proxies.removeAll(Collections.singleton(Proxy.NO_PROXY));
            this.proxies.add(Proxy.NO_PROXY);
        }
        this.nextProxyIndex = 0;
    }
    
    private boolean hasNextProxy() {
        return this.nextProxyIndex < this.proxies.size();
    }
    
    private Proxy nextProxy() throws IOException {
        if (!this.hasNextProxy()) {
            throw new SocketException("No route to " + this.address.getUriHost() + "; exhausted proxy configurations: " + this.proxies);
        }
        final Proxy result = this.proxies.get(this.nextProxyIndex++);
        this.resetNextInetSocketAddress(result);
        return result;
    }
    
    private void resetNextInetSocketAddress(final Proxy proxy) throws IOException {
        this.inetSocketAddresses = new ArrayList<InetSocketAddress>();
        String socketHost;
        int socketPort;
        if (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.SOCKS) {
            socketHost = this.address.getUriHost();
            socketPort = this.address.getUriPort();
        }
        else {
            final SocketAddress proxyAddress = proxy.address();
            if (!(proxyAddress instanceof InetSocketAddress)) {
                throw new IllegalArgumentException("Proxy.address() is not an InetSocketAddress: " + proxyAddress.getClass());
            }
            final InetSocketAddress proxySocketAddress = (InetSocketAddress)proxyAddress;
            socketHost = getHostString(proxySocketAddress);
            socketPort = proxySocketAddress.getPort();
        }
        if (socketPort < 1 || socketPort > 65535) {
            throw new SocketException("No route to " + socketHost + ":" + socketPort + "; port is out of range");
        }
        if (proxy.type() == Proxy.Type.SOCKS) {
            this.inetSocketAddresses.add(InetSocketAddress.createUnresolved(socketHost, socketPort));
        }
        else {
            final List<InetAddress> addresses = this.address.getDns().lookup(socketHost);
            for (int i = 0, size = addresses.size(); i < size; ++i) {
                final InetAddress inetAddress = addresses.get(i);
                this.inetSocketAddresses.add(new InetSocketAddress(inetAddress, socketPort));
            }
        }
        this.nextInetSocketAddressIndex = 0;
    }
    
    static String getHostString(final InetSocketAddress socketAddress) {
        final InetAddress address = socketAddress.getAddress();
        if (address == null) {
            return socketAddress.getHostName();
        }
        return address.getHostAddress();
    }
    
    private boolean hasNextInetSocketAddress() {
        return this.nextInetSocketAddressIndex < this.inetSocketAddresses.size();
    }
    
    private InetSocketAddress nextInetSocketAddress() throws IOException {
        if (!this.hasNextInetSocketAddress()) {
            throw new SocketException("No route to " + this.address.getUriHost() + "; exhausted inet socket addresses: " + this.inetSocketAddresses);
        }
        return this.inetSocketAddresses.get(this.nextInetSocketAddressIndex++);
    }
    
    private boolean hasNextPostponed() {
        return !this.postponedRoutes.isEmpty();
    }
    
    private Route nextPostponed() {
        return this.postponedRoutes.remove(0);
    }
}
