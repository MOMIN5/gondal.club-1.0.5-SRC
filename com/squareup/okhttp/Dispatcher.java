// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import com.squareup.okhttp.internal.http.HttpEngine;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import com.squareup.okhttp.internal.Util;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;

public final class Dispatcher
{
    private int maxRequests;
    private int maxRequestsPerHost;
    private ExecutorService executorService;
    private final Deque<Call.AsyncCall> readyCalls;
    private final Deque<Call.AsyncCall> runningCalls;
    private final Deque<Call> executedCalls;
    
    public Dispatcher(final ExecutorService executorService) {
        this.maxRequests = 64;
        this.maxRequestsPerHost = 5;
        this.readyCalls = new ArrayDeque<Call.AsyncCall>();
        this.runningCalls = new ArrayDeque<Call.AsyncCall>();
        this.executedCalls = new ArrayDeque<Call>();
        this.executorService = executorService;
    }
    
    public Dispatcher() {
        this.maxRequests = 64;
        this.maxRequestsPerHost = 5;
        this.readyCalls = new ArrayDeque<Call.AsyncCall>();
        this.runningCalls = new ArrayDeque<Call.AsyncCall>();
        this.executedCalls = new ArrayDeque<Call>();
    }
    
    public synchronized ExecutorService getExecutorService() {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false));
        }
        return this.executorService;
    }
    
    public synchronized void setMaxRequests(final int maxRequests) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }
        this.maxRequests = maxRequests;
        this.promoteCalls();
    }
    
    public synchronized int getMaxRequests() {
        return this.maxRequests;
    }
    
    public synchronized void setMaxRequestsPerHost(final int maxRequestsPerHost) {
        if (maxRequestsPerHost < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
        }
        this.maxRequestsPerHost = maxRequestsPerHost;
        this.promoteCalls();
    }
    
    public synchronized int getMaxRequestsPerHost() {
        return this.maxRequestsPerHost;
    }
    
    synchronized void enqueue(final Call.AsyncCall call) {
        if (this.runningCalls.size() < this.maxRequests && this.runningCallsForHost(call) < this.maxRequestsPerHost) {
            this.runningCalls.add(call);
            this.getExecutorService().execute(call);
        }
        else {
            this.readyCalls.add(call);
        }
    }
    
    public synchronized void cancel(final Object tag) {
        for (final Call.AsyncCall call : this.readyCalls) {
            if (Util.equal(tag, call.tag())) {
                call.cancel();
            }
        }
        for (final Call.AsyncCall call : this.runningCalls) {
            if (Util.equal(tag, call.tag())) {
                call.get().canceled = true;
                final HttpEngine engine = call.get().engine;
                if (engine == null) {
                    continue;
                }
                engine.cancel();
            }
        }
        for (final Call call2 : this.executedCalls) {
            if (Util.equal(tag, call2.tag())) {
                call2.cancel();
            }
        }
    }
    
    synchronized void finished(final Call.AsyncCall call) {
        if (!this.runningCalls.remove(call)) {
            throw new AssertionError((Object)"AsyncCall wasn't running!");
        }
        this.promoteCalls();
    }
    
    private void promoteCalls() {
        if (this.runningCalls.size() >= this.maxRequests) {
            return;
        }
        if (this.readyCalls.isEmpty()) {
            return;
        }
        final Iterator<Call.AsyncCall> i = this.readyCalls.iterator();
        while (i.hasNext()) {
            final Call.AsyncCall call = i.next();
            if (this.runningCallsForHost(call) < this.maxRequestsPerHost) {
                i.remove();
                this.runningCalls.add(call);
                this.getExecutorService().execute(call);
            }
            if (this.runningCalls.size() >= this.maxRequests) {
                return;
            }
        }
    }
    
    private int runningCallsForHost(final Call.AsyncCall call) {
        int result = 0;
        for (final Call.AsyncCall c : this.runningCalls) {
            if (c.host().equals(call.host())) {
                ++result;
            }
        }
        return result;
    }
    
    synchronized void executed(final Call call) {
        this.executedCalls.add(call);
    }
    
    synchronized void finished(final Call call) {
        if (!this.executedCalls.remove(call)) {
            throw new AssertionError((Object)"Call wasn't in-flight!");
        }
    }
    
    public synchronized int getRunningCallCount() {
        return this.runningCalls.size();
    }
    
    public synchronized int getQueuedCallCount() {
        return this.readyCalls.size();
    }
}
