// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal;

import java.util.LinkedHashSet;
import com.squareup.okhttp.Route;
import java.util.Set;

public final class RouteDatabase
{
    private final Set<Route> failedRoutes;
    
    public RouteDatabase() {
        this.failedRoutes = new LinkedHashSet<Route>();
    }
    
    public synchronized void failed(final Route failedRoute) {
        this.failedRoutes.add(failedRoute);
    }
    
    public synchronized void connected(final Route route) {
        this.failedRoutes.remove(route);
    }
    
    public synchronized boolean shouldPostpone(final Route route) {
        return this.failedRoutes.contains(route);
    }
    
    public synchronized int failedRoutesCount() {
        return this.failedRoutes.size();
    }
}
