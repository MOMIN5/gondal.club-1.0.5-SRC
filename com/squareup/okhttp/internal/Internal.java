// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Call;
import java.net.UnknownHostException;
import java.net.MalformedURLException;
import com.squareup.okhttp.HttpUrl;
import javax.net.ssl.SSLSocket;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.internal.io.RealConnection;
import com.squareup.okhttp.internal.http.StreamAllocation;
import com.squareup.okhttp.Address;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import java.util.logging.Logger;

public abstract class Internal
{
    public static final Logger logger;
    public static Internal instance;
    
    public static void initializeInstanceForTests() {
        new OkHttpClient();
    }
    
    public abstract void addLenient(final Headers.Builder p0, final String p1);
    
    public abstract void addLenient(final Headers.Builder p0, final String p1, final String p2);
    
    public abstract void setCache(final OkHttpClient p0, final InternalCache p1);
    
    public abstract InternalCache internalCache(final OkHttpClient p0);
    
    public abstract RealConnection get(final ConnectionPool p0, final Address p1, final StreamAllocation p2);
    
    public abstract void put(final ConnectionPool p0, final RealConnection p1);
    
    public abstract boolean connectionBecameIdle(final ConnectionPool p0, final RealConnection p1);
    
    public abstract RouteDatabase routeDatabase(final ConnectionPool p0);
    
    public abstract void apply(final ConnectionSpec p0, final SSLSocket p1, final boolean p2);
    
    public abstract HttpUrl getHttpUrlChecked(final String p0) throws MalformedURLException, UnknownHostException;
    
    public abstract void callEnqueue(final Call p0, final Callback p1, final boolean p2);
    
    public abstract StreamAllocation callEngineGetStreamAllocation(final Call p0);
    
    static {
        logger = Logger.getLogger(OkHttpClient.class.getName());
    }
}
