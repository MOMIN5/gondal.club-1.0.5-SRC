// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import okio.Sink;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.RouteDatabase;
import com.squareup.okhttp.Route;
import com.squareup.okhttp.internal.Internal;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.squareup.okhttp.internal.io.RealConnection;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.Address;

public final class StreamAllocation
{
    public final Address address;
    private final ConnectionPool connectionPool;
    private RouteSelector routeSelector;
    private RealConnection connection;
    private boolean released;
    private boolean canceled;
    private HttpStream stream;
    
    public StreamAllocation(final ConnectionPool connectionPool, final Address address) {
        this.connectionPool = connectionPool;
        this.address = address;
    }
    
    public HttpStream newStream(final int connectTimeout, final int readTimeout, final int writeTimeout, final boolean connectionRetryEnabled, final boolean doExtensiveHealthChecks) throws RouteException, IOException {
        try {
            final RealConnection resultConnection = this.findHealthyConnection(connectTimeout, readTimeout, writeTimeout, connectionRetryEnabled, doExtensiveHealthChecks);
            HttpStream resultStream;
            if (resultConnection.framedConnection != null) {
                resultStream = new Http2xStream(this, resultConnection.framedConnection);
            }
            else {
                resultConnection.getSocket().setSoTimeout(readTimeout);
                resultConnection.source.timeout().timeout(readTimeout, TimeUnit.MILLISECONDS);
                resultConnection.sink.timeout().timeout(writeTimeout, TimeUnit.MILLISECONDS);
                resultStream = new Http1xStream(this, resultConnection.source, resultConnection.sink);
            }
            synchronized (this.connectionPool) {
                final RealConnection realConnection = resultConnection;
                ++realConnection.streamCount;
                return this.stream = resultStream;
            }
        }
        catch (IOException e) {
            throw new RouteException(e);
        }
    }
    
    private RealConnection findHealthyConnection(final int connectTimeout, final int readTimeout, final int writeTimeout, final boolean connectionRetryEnabled, final boolean doExtensiveHealthChecks) throws IOException, RouteException {
        RealConnection candidate;
        while (true) {
            candidate = this.findConnection(connectTimeout, readTimeout, writeTimeout, connectionRetryEnabled);
            synchronized (this.connectionPool) {
                if (candidate.streamCount == 0) {
                    return candidate;
                }
            }
            if (candidate.isHealthy(doExtensiveHealthChecks)) {
                break;
            }
            this.connectionFailed();
        }
        return candidate;
    }
    
    private RealConnection findConnection(final int connectTimeout, final int readTimeout, final int writeTimeout, final boolean connectionRetryEnabled) throws IOException, RouteException {
        synchronized (this.connectionPool) {
            if (this.released) {
                throw new IllegalStateException("released");
            }
            if (this.stream != null) {
                throw new IllegalStateException("stream != null");
            }
            if (this.canceled) {
                throw new IOException("Canceled");
            }
            final RealConnection allocatedConnection = this.connection;
            if (allocatedConnection != null && !allocatedConnection.noNewStreams) {
                return allocatedConnection;
            }
            final RealConnection pooledConnection = Internal.instance.get(this.connectionPool, this.address, this);
            if (pooledConnection != null) {
                return this.connection = pooledConnection;
            }
            if (this.routeSelector == null) {
                this.routeSelector = new RouteSelector(this.address, this.routeDatabase());
            }
        }
        final Route route = this.routeSelector.next();
        final RealConnection newConnection = new RealConnection(route);
        this.acquire(newConnection);
        synchronized (this.connectionPool) {
            Internal.instance.put(this.connectionPool, newConnection);
            this.connection = newConnection;
            if (this.canceled) {
                throw new IOException("Canceled");
            }
        }
        newConnection.connect(connectTimeout, readTimeout, writeTimeout, this.address.getConnectionSpecs(), connectionRetryEnabled);
        this.routeDatabase().connected(newConnection.getRoute());
        return newConnection;
    }
    
    public void streamFinished(final HttpStream stream) {
        synchronized (this.connectionPool) {
            if (stream == null || stream != this.stream) {
                throw new IllegalStateException("expected " + this.stream + " but was " + stream);
            }
        }
        this.deallocate(false, false, true);
    }
    
    public HttpStream stream() {
        synchronized (this.connectionPool) {
            return this.stream;
        }
    }
    
    private RouteDatabase routeDatabase() {
        return Internal.instance.routeDatabase(this.connectionPool);
    }
    
    public synchronized RealConnection connection() {
        return this.connection;
    }
    
    public void release() {
        this.deallocate(false, true, false);
    }
    
    public void noNewStreams() {
        this.deallocate(true, false, false);
    }
    
    private void deallocate(final boolean noNewStreams, final boolean released, final boolean streamFinished) {
        RealConnection connectionToClose = null;
        synchronized (this.connectionPool) {
            if (streamFinished) {
                this.stream = null;
            }
            if (released) {
                this.released = true;
            }
            if (this.connection != null) {
                if (noNewStreams) {
                    this.connection.noNewStreams = true;
                }
                if (this.stream == null && (this.released || this.connection.noNewStreams)) {
                    this.release(this.connection);
                    if (this.connection.streamCount > 0) {
                        this.routeSelector = null;
                    }
                    if (this.connection.allocations.isEmpty()) {
                        this.connection.idleAtNanos = System.nanoTime();
                        if (Internal.instance.connectionBecameIdle(this.connectionPool, this.connection)) {
                            connectionToClose = this.connection;
                        }
                    }
                    this.connection = null;
                }
            }
        }
        if (connectionToClose != null) {
            Util.closeQuietly(connectionToClose.getSocket());
        }
    }
    
    public void cancel() {
        final HttpStream streamToCancel;
        final RealConnection connectionToCancel;
        synchronized (this.connectionPool) {
            this.canceled = true;
            streamToCancel = this.stream;
            connectionToCancel = this.connection;
        }
        if (streamToCancel != null) {
            streamToCancel.cancel();
        }
        else if (connectionToCancel != null) {
            connectionToCancel.cancel();
        }
    }
    
    private void connectionFailed(final IOException e) {
        synchronized (this.connectionPool) {
            if (this.routeSelector != null) {
                if (this.connection.streamCount == 0) {
                    final Route failedRoute = this.connection.getRoute();
                    this.routeSelector.connectFailed(failedRoute, e);
                }
                else {
                    this.routeSelector = null;
                }
            }
        }
        this.connectionFailed();
    }
    
    public void connectionFailed() {
        this.deallocate(true, false, true);
    }
    
    public void acquire(final RealConnection connection) {
        connection.allocations.add(new WeakReference<StreamAllocation>(this));
    }
    
    private void release(final RealConnection connection) {
        for (int i = 0, size = connection.allocations.size(); i < size; ++i) {
            final Reference<StreamAllocation> reference = connection.allocations.get(i);
            if (reference.get() == this) {
                connection.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }
    
    public boolean recover(final RouteException e) {
        if (this.connection != null) {
            this.connectionFailed(e.getLastConnectException());
        }
        return (this.routeSelector == null || this.routeSelector.hasNext()) && this.isRecoverable(e);
    }
    
    public boolean recover(final IOException e, final Sink requestBodyOut) {
        if (this.connection != null) {
            final int streamCount = this.connection.streamCount;
            this.connectionFailed(e);
            if (streamCount == 1) {
                return false;
            }
        }
        final boolean canRetryRequestBody = requestBodyOut == null || requestBodyOut instanceof RetryableSink;
        return (this.routeSelector == null || this.routeSelector.hasNext()) && this.isRecoverable(e) && canRetryRequestBody;
    }
    
    private boolean isRecoverable(final IOException e) {
        return !(e instanceof ProtocolException) && !(e instanceof InterruptedIOException);
    }
    
    private boolean isRecoverable(final RouteException e) {
        final IOException ioe = e.getLastConnectException();
        if (ioe instanceof ProtocolException) {
            return false;
        }
        if (ioe instanceof InterruptedIOException) {
            return ioe instanceof SocketTimeoutException;
        }
        return (!(ioe instanceof SSLHandshakeException) || !(ioe.getCause() instanceof CertificateException)) && !(ioe instanceof SSLPeerUnverifiedException);
    }
    
    @Override
    public String toString() {
        return this.address.toString();
    }
}
