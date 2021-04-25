// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import com.squareup.okhttp.internal.Internal;
import java.lang.ref.Reference;
import java.util.List;
import java.util.ArrayList;
import com.squareup.okhttp.internal.http.StreamAllocation;
import java.util.Iterator;
import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import com.squareup.okhttp.internal.Util;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.squareup.okhttp.internal.RouteDatabase;
import com.squareup.okhttp.internal.io.RealConnection;
import java.util.Deque;
import java.util.concurrent.Executor;

public final class ConnectionPool
{
    private static final long DEFAULT_KEEP_ALIVE_DURATION_MS = 300000L;
    private static final ConnectionPool systemDefault;
    private final Executor executor;
    private final int maxIdleConnections;
    private final long keepAliveDurationNs;
    private Runnable cleanupRunnable;
    private final Deque<RealConnection> connections;
    final RouteDatabase routeDatabase;
    
    public ConnectionPool(final int maxIdleConnections, final long keepAliveDurationMs) {
        this(maxIdleConnections, keepAliveDurationMs, TimeUnit.MILLISECONDS);
    }
    
    public ConnectionPool(final int maxIdleConnections, final long keepAliveDuration, final TimeUnit timeUnit) {
        this.executor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), Util.threadFactory("OkHttp ConnectionPool", true));
        this.cleanupRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    long waitNanos = ConnectionPool.this.cleanup(System.nanoTime());
                    if (waitNanos == -1L) {
                        break;
                    }
                    if (waitNanos <= 0L) {
                        continue;
                    }
                    final long waitMillis = waitNanos / 1000000L;
                    waitNanos -= waitMillis * 1000000L;
                    synchronized (ConnectionPool.this) {
                        try {
                            ConnectionPool.this.wait(waitMillis, (int)waitNanos);
                        }
                        catch (InterruptedException ex) {}
                    }
                }
            }
        };
        this.connections = new ArrayDeque<RealConnection>();
        this.routeDatabase = new RouteDatabase();
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);
        if (keepAliveDuration <= 0L) {
            throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
        }
    }
    
    public static ConnectionPool getDefault() {
        return ConnectionPool.systemDefault;
    }
    
    public synchronized int getIdleConnectionCount() {
        int total = 0;
        for (final RealConnection connection : this.connections) {
            if (connection.allocations.isEmpty()) {
                ++total;
            }
        }
        return total;
    }
    
    public synchronized int getConnectionCount() {
        return this.connections.size();
    }
    
    @Deprecated
    public synchronized int getSpdyConnectionCount() {
        return this.getMultiplexedConnectionCount();
    }
    
    public synchronized int getMultiplexedConnectionCount() {
        int total = 0;
        for (final RealConnection connection : this.connections) {
            if (connection.isMultiplexed()) {
                ++total;
            }
        }
        return total;
    }
    
    public synchronized int getHttpConnectionCount() {
        return this.connections.size() - this.getMultiplexedConnectionCount();
    }
    
    RealConnection get(final Address address, final StreamAllocation streamAllocation) {
        assert Thread.holdsLock(this);
        for (final RealConnection connection : this.connections) {
            if (connection.allocations.size() < connection.allocationLimit() && address.equals(connection.getRoute().address) && !connection.noNewStreams) {
                streamAllocation.acquire(connection);
                return connection;
            }
        }
        return null;
    }
    
    void put(final RealConnection connection) {
        assert Thread.holdsLock(this);
        if (this.connections.isEmpty()) {
            this.executor.execute(this.cleanupRunnable);
        }
        this.connections.add(connection);
    }
    
    boolean connectionBecameIdle(final RealConnection connection) {
        assert Thread.holdsLock(this);
        if (connection.noNewStreams || this.maxIdleConnections == 0) {
            this.connections.remove(connection);
            return true;
        }
        this.notifyAll();
        return false;
    }
    
    public void evictAll() {
        final List<RealConnection> evictedConnections = new ArrayList<RealConnection>();
        synchronized (this) {
            final Iterator<RealConnection> i = this.connections.iterator();
            while (i.hasNext()) {
                final RealConnection connection = i.next();
                if (connection.allocations.isEmpty()) {
                    connection.noNewStreams = true;
                    evictedConnections.add(connection);
                    i.remove();
                }
            }
        }
        for (final RealConnection connection2 : evictedConnections) {
            Util.closeQuietly(connection2.getSocket());
        }
    }
    
    long cleanup(final long now) {
        int inUseConnectionCount = 0;
        int idleConnectionCount = 0;
        RealConnection longestIdleConnection = null;
        long longestIdleDurationNs = Long.MIN_VALUE;
        synchronized (this) {
            for (final RealConnection connection : this.connections) {
                if (this.pruneAndGetAllocationCount(connection, now) > 0) {
                    ++inUseConnectionCount;
                }
                else {
                    ++idleConnectionCount;
                    final long idleDurationNs = now - connection.idleAtNanos;
                    if (idleDurationNs <= longestIdleDurationNs) {
                        continue;
                    }
                    longestIdleDurationNs = idleDurationNs;
                    longestIdleConnection = connection;
                }
            }
            if (longestIdleDurationNs >= this.keepAliveDurationNs || idleConnectionCount > this.maxIdleConnections) {
                this.connections.remove(longestIdleConnection);
            }
            else {
                if (idleConnectionCount > 0) {
                    return this.keepAliveDurationNs - longestIdleDurationNs;
                }
                if (inUseConnectionCount > 0) {
                    return this.keepAliveDurationNs;
                }
                return -1L;
            }
        }
        Util.closeQuietly(longestIdleConnection.getSocket());
        return 0L;
    }
    
    private int pruneAndGetAllocationCount(final RealConnection connection, final long now) {
        final List<Reference<StreamAllocation>> references = connection.allocations;
        int i = 0;
        while (i < references.size()) {
            final Reference<StreamAllocation> reference = references.get(i);
            if (reference.get() != null) {
                ++i;
            }
            else {
                Internal.logger.warning("A connection to " + connection.getRoute().getAddress().url() + " was leaked. Did you forget to close a response body?");
                references.remove(i);
                connection.noNewStreams = true;
                if (references.isEmpty()) {
                    connection.idleAtNanos = now - this.keepAliveDurationNs;
                    return 0;
                }
                continue;
            }
        }
        return references.size();
    }
    
    void setCleanupRunnableForTest(final Runnable cleanupRunnable) {
        this.cleanupRunnable = cleanupRunnable;
    }
    
    static {
        final String keepAlive = System.getProperty("http.keepAlive");
        final String keepAliveDuration = System.getProperty("http.keepAliveDuration");
        final String maxIdleConnections = System.getProperty("http.maxConnections");
        final long keepAliveDurationMs = (keepAliveDuration != null) ? Long.parseLong(keepAliveDuration) : 300000L;
        if (keepAlive != null && !Boolean.parseBoolean(keepAlive)) {
            systemDefault = new ConnectionPool(0, keepAliveDurationMs);
        }
        else if (maxIdleConnections != null) {
            systemDefault = new ConnectionPool(Integer.parseInt(maxIdleConnections), keepAliveDurationMs);
        }
        else {
            systemDefault = new ConnectionPool(5, keepAliveDurationMs);
        }
    }
}
