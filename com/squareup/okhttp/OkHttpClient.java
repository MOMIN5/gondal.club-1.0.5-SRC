// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.net.UnknownHostException;
import java.net.MalformedURLException;
import javax.net.ssl.SSLSocket;
import com.squareup.okhttp.internal.http.StreamAllocation;
import com.squareup.okhttp.internal.io.RealConnection;
import com.squareup.okhttp.internal.Internal;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import com.squareup.okhttp.internal.http.AuthenticatorAdapter;
import com.squareup.okhttp.internal.tls.OkHostnameVerifier;
import com.squareup.okhttp.internal.Util;
import java.util.concurrent.TimeUnit;
import java.util.Collection;
import java.util.ArrayList;
import javax.net.ssl.HostnameVerifier;
import javax.net.SocketFactory;
import com.squareup.okhttp.internal.InternalCache;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.Proxy;
import com.squareup.okhttp.internal.RouteDatabase;
import javax.net.ssl.SSLSocketFactory;
import java.util.List;

public class OkHttpClient implements Cloneable
{
    private static final List<Protocol> DEFAULT_PROTOCOLS;
    private static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS;
    private static SSLSocketFactory defaultSslSocketFactory;
    private final RouteDatabase routeDatabase;
    private Dispatcher dispatcher;
    private Proxy proxy;
    private List<Protocol> protocols;
    private List<ConnectionSpec> connectionSpecs;
    private final List<Interceptor> interceptors;
    private final List<Interceptor> networkInterceptors;
    private ProxySelector proxySelector;
    private CookieHandler cookieHandler;
    private InternalCache internalCache;
    private Cache cache;
    private SocketFactory socketFactory;
    private SSLSocketFactory sslSocketFactory;
    private HostnameVerifier hostnameVerifier;
    private CertificatePinner certificatePinner;
    private Authenticator authenticator;
    private ConnectionPool connectionPool;
    private Dns dns;
    private boolean followSslRedirects;
    private boolean followRedirects;
    private boolean retryOnConnectionFailure;
    private int connectTimeout;
    private int readTimeout;
    private int writeTimeout;
    
    public OkHttpClient() {
        this.interceptors = new ArrayList<Interceptor>();
        this.networkInterceptors = new ArrayList<Interceptor>();
        this.followSslRedirects = true;
        this.followRedirects = true;
        this.retryOnConnectionFailure = true;
        this.connectTimeout = 10000;
        this.readTimeout = 10000;
        this.writeTimeout = 10000;
        this.routeDatabase = new RouteDatabase();
        this.dispatcher = new Dispatcher();
    }
    
    private OkHttpClient(final OkHttpClient okHttpClient) {
        this.interceptors = new ArrayList<Interceptor>();
        this.networkInterceptors = new ArrayList<Interceptor>();
        this.followSslRedirects = true;
        this.followRedirects = true;
        this.retryOnConnectionFailure = true;
        this.connectTimeout = 10000;
        this.readTimeout = 10000;
        this.writeTimeout = 10000;
        this.routeDatabase = okHttpClient.routeDatabase;
        this.dispatcher = okHttpClient.dispatcher;
        this.proxy = okHttpClient.proxy;
        this.protocols = okHttpClient.protocols;
        this.connectionSpecs = okHttpClient.connectionSpecs;
        this.interceptors.addAll(okHttpClient.interceptors);
        this.networkInterceptors.addAll(okHttpClient.networkInterceptors);
        this.proxySelector = okHttpClient.proxySelector;
        this.cookieHandler = okHttpClient.cookieHandler;
        this.cache = okHttpClient.cache;
        this.internalCache = ((this.cache != null) ? this.cache.internalCache : okHttpClient.internalCache);
        this.socketFactory = okHttpClient.socketFactory;
        this.sslSocketFactory = okHttpClient.sslSocketFactory;
        this.hostnameVerifier = okHttpClient.hostnameVerifier;
        this.certificatePinner = okHttpClient.certificatePinner;
        this.authenticator = okHttpClient.authenticator;
        this.connectionPool = okHttpClient.connectionPool;
        this.dns = okHttpClient.dns;
        this.followSslRedirects = okHttpClient.followSslRedirects;
        this.followRedirects = okHttpClient.followRedirects;
        this.retryOnConnectionFailure = okHttpClient.retryOnConnectionFailure;
        this.connectTimeout = okHttpClient.connectTimeout;
        this.readTimeout = okHttpClient.readTimeout;
        this.writeTimeout = okHttpClient.writeTimeout;
    }
    
    public void setConnectTimeout(final long timeout, final TimeUnit unit) {
        if (timeout < 0L) {
            throw new IllegalArgumentException("timeout < 0");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit == null");
        }
        final long millis = unit.toMillis(timeout);
        if (millis > 2147483647L) {
            throw new IllegalArgumentException("Timeout too large.");
        }
        if (millis == 0L && timeout > 0L) {
            throw new IllegalArgumentException("Timeout too small.");
        }
        this.connectTimeout = (int)millis;
    }
    
    public int getConnectTimeout() {
        return this.connectTimeout;
    }
    
    public void setReadTimeout(final long timeout, final TimeUnit unit) {
        if (timeout < 0L) {
            throw new IllegalArgumentException("timeout < 0");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit == null");
        }
        final long millis = unit.toMillis(timeout);
        if (millis > 2147483647L) {
            throw new IllegalArgumentException("Timeout too large.");
        }
        if (millis == 0L && timeout > 0L) {
            throw new IllegalArgumentException("Timeout too small.");
        }
        this.readTimeout = (int)millis;
    }
    
    public int getReadTimeout() {
        return this.readTimeout;
    }
    
    public void setWriteTimeout(final long timeout, final TimeUnit unit) {
        if (timeout < 0L) {
            throw new IllegalArgumentException("timeout < 0");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit == null");
        }
        final long millis = unit.toMillis(timeout);
        if (millis > 2147483647L) {
            throw new IllegalArgumentException("Timeout too large.");
        }
        if (millis == 0L && timeout > 0L) {
            throw new IllegalArgumentException("Timeout too small.");
        }
        this.writeTimeout = (int)millis;
    }
    
    public int getWriteTimeout() {
        return this.writeTimeout;
    }
    
    public OkHttpClient setProxy(final Proxy proxy) {
        this.proxy = proxy;
        return this;
    }
    
    public Proxy getProxy() {
        return this.proxy;
    }
    
    public OkHttpClient setProxySelector(final ProxySelector proxySelector) {
        this.proxySelector = proxySelector;
        return this;
    }
    
    public ProxySelector getProxySelector() {
        return this.proxySelector;
    }
    
    public OkHttpClient setCookieHandler(final CookieHandler cookieHandler) {
        this.cookieHandler = cookieHandler;
        return this;
    }
    
    public CookieHandler getCookieHandler() {
        return this.cookieHandler;
    }
    
    void setInternalCache(final InternalCache internalCache) {
        this.internalCache = internalCache;
        this.cache = null;
    }
    
    InternalCache internalCache() {
        return this.internalCache;
    }
    
    public OkHttpClient setCache(final Cache cache) {
        this.cache = cache;
        this.internalCache = null;
        return this;
    }
    
    public Cache getCache() {
        return this.cache;
    }
    
    public OkHttpClient setDns(final Dns dns) {
        this.dns = dns;
        return this;
    }
    
    public Dns getDns() {
        return this.dns;
    }
    
    public OkHttpClient setSocketFactory(final SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        return this;
    }
    
    public SocketFactory getSocketFactory() {
        return this.socketFactory;
    }
    
    public OkHttpClient setSslSocketFactory(final SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }
    
    public SSLSocketFactory getSslSocketFactory() {
        return this.sslSocketFactory;
    }
    
    public OkHttpClient setHostnameVerifier(final HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }
    
    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }
    
    public OkHttpClient setCertificatePinner(final CertificatePinner certificatePinner) {
        this.certificatePinner = certificatePinner;
        return this;
    }
    
    public CertificatePinner getCertificatePinner() {
        return this.certificatePinner;
    }
    
    public OkHttpClient setAuthenticator(final Authenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }
    
    public Authenticator getAuthenticator() {
        return this.authenticator;
    }
    
    public OkHttpClient setConnectionPool(final ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        return this;
    }
    
    public ConnectionPool getConnectionPool() {
        return this.connectionPool;
    }
    
    public OkHttpClient setFollowSslRedirects(final boolean followProtocolRedirects) {
        this.followSslRedirects = followProtocolRedirects;
        return this;
    }
    
    public boolean getFollowSslRedirects() {
        return this.followSslRedirects;
    }
    
    public void setFollowRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
    }
    
    public boolean getFollowRedirects() {
        return this.followRedirects;
    }
    
    public void setRetryOnConnectionFailure(final boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
    }
    
    public boolean getRetryOnConnectionFailure() {
        return this.retryOnConnectionFailure;
    }
    
    RouteDatabase routeDatabase() {
        return this.routeDatabase;
    }
    
    public OkHttpClient setDispatcher(final Dispatcher dispatcher) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("dispatcher == null");
        }
        this.dispatcher = dispatcher;
        return this;
    }
    
    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }
    
    public OkHttpClient setProtocols(List<Protocol> protocols) {
        protocols = Util.immutableList(protocols);
        if (!protocols.contains(Protocol.HTTP_1_1)) {
            throw new IllegalArgumentException("protocols doesn't contain http/1.1: " + protocols);
        }
        if (protocols.contains(Protocol.HTTP_1_0)) {
            throw new IllegalArgumentException("protocols must not contain http/1.0: " + protocols);
        }
        if (protocols.contains(null)) {
            throw new IllegalArgumentException("protocols must not contain null");
        }
        this.protocols = Util.immutableList(protocols);
        return this;
    }
    
    public List<Protocol> getProtocols() {
        return this.protocols;
    }
    
    public OkHttpClient setConnectionSpecs(final List<ConnectionSpec> connectionSpecs) {
        this.connectionSpecs = Util.immutableList(connectionSpecs);
        return this;
    }
    
    public List<ConnectionSpec> getConnectionSpecs() {
        return this.connectionSpecs;
    }
    
    public List<Interceptor> interceptors() {
        return this.interceptors;
    }
    
    public List<Interceptor> networkInterceptors() {
        return this.networkInterceptors;
    }
    
    public Call newCall(final Request request) {
        return new Call(this, request);
    }
    
    public OkHttpClient cancel(final Object tag) {
        this.getDispatcher().cancel(tag);
        return this;
    }
    
    OkHttpClient copyWithDefaults() {
        final OkHttpClient result = new OkHttpClient(this);
        if (result.proxySelector == null) {
            result.proxySelector = ProxySelector.getDefault();
        }
        if (result.cookieHandler == null) {
            result.cookieHandler = CookieHandler.getDefault();
        }
        if (result.socketFactory == null) {
            result.socketFactory = SocketFactory.getDefault();
        }
        if (result.sslSocketFactory == null) {
            result.sslSocketFactory = this.getDefaultSSLSocketFactory();
        }
        if (result.hostnameVerifier == null) {
            result.hostnameVerifier = OkHostnameVerifier.INSTANCE;
        }
        if (result.certificatePinner == null) {
            result.certificatePinner = CertificatePinner.DEFAULT;
        }
        if (result.authenticator == null) {
            result.authenticator = AuthenticatorAdapter.INSTANCE;
        }
        if (result.connectionPool == null) {
            result.connectionPool = ConnectionPool.getDefault();
        }
        if (result.protocols == null) {
            result.protocols = OkHttpClient.DEFAULT_PROTOCOLS;
        }
        if (result.connectionSpecs == null) {
            result.connectionSpecs = OkHttpClient.DEFAULT_CONNECTION_SPECS;
        }
        if (result.dns == null) {
            result.dns = Dns.SYSTEM;
        }
        return result;
    }
    
    private synchronized SSLSocketFactory getDefaultSSLSocketFactory() {
        if (OkHttpClient.defaultSslSocketFactory == null) {
            try {
                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                OkHttpClient.defaultSslSocketFactory = sslContext.getSocketFactory();
            }
            catch (GeneralSecurityException e) {
                throw new AssertionError();
            }
        }
        return OkHttpClient.defaultSslSocketFactory;
    }
    
    public OkHttpClient clone() {
        return new OkHttpClient(this);
    }
    
    static {
        DEFAULT_PROTOCOLS = Util.immutableList(Protocol.HTTP_2, Protocol.SPDY_3, Protocol.HTTP_1_1);
        DEFAULT_CONNECTION_SPECS = Util.immutableList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT);
        Internal.instance = new Internal() {
            @Override
            public void addLenient(final Headers.Builder builder, final String line) {
                builder.addLenient(line);
            }
            
            @Override
            public void addLenient(final Headers.Builder builder, final String name, final String value) {
                builder.addLenient(name, value);
            }
            
            @Override
            public void setCache(final OkHttpClient client, final InternalCache internalCache) {
                client.setInternalCache(internalCache);
            }
            
            @Override
            public InternalCache internalCache(final OkHttpClient client) {
                return client.internalCache();
            }
            
            @Override
            public boolean connectionBecameIdle(final ConnectionPool pool, final RealConnection connection) {
                return pool.connectionBecameIdle(connection);
            }
            
            @Override
            public RealConnection get(final ConnectionPool pool, final Address address, final StreamAllocation streamAllocation) {
                return pool.get(address, streamAllocation);
            }
            
            @Override
            public void put(final ConnectionPool pool, final RealConnection connection) {
                pool.put(connection);
            }
            
            @Override
            public RouteDatabase routeDatabase(final ConnectionPool connectionPool) {
                return connectionPool.routeDatabase;
            }
            
            @Override
            public void callEnqueue(final Call call, final Callback responseCallback, final boolean forWebSocket) {
                call.enqueue(responseCallback, forWebSocket);
            }
            
            @Override
            public StreamAllocation callEngineGetStreamAllocation(final Call call) {
                return call.engine.streamAllocation;
            }
            
            @Override
            public void apply(final ConnectionSpec tlsConfiguration, final SSLSocket sslSocket, final boolean isFallback) {
                tlsConfiguration.apply(sslSocket, isFallback);
            }
            
            @Override
            public HttpUrl getHttpUrlChecked(final String url) throws MalformedURLException, UnknownHostException {
                return HttpUrl.getChecked(url);
            }
        };
    }
}
