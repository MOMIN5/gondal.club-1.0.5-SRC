// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.framed;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

public final class Ping
{
    private final CountDownLatch latch;
    private long sent;
    private long received;
    
    Ping() {
        this.latch = new CountDownLatch(1);
        this.sent = -1L;
        this.received = -1L;
    }
    
    void send() {
        if (this.sent != -1L) {
            throw new IllegalStateException();
        }
        this.sent = System.nanoTime();
    }
    
    void receive() {
        if (this.received != -1L || this.sent == -1L) {
            throw new IllegalStateException();
        }
        this.received = System.nanoTime();
        this.latch.countDown();
    }
    
    void cancel() {
        if (this.received != -1L || this.sent == -1L) {
            throw new IllegalStateException();
        }
        this.received = this.sent - 1L;
        this.latch.countDown();
    }
    
    public long roundTripTime() throws InterruptedException {
        this.latch.await();
        return this.received - this.sent;
    }
    
    public long roundTripTime(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (this.latch.await(timeout, unit)) {
            return this.received - this.sent;
        }
        return -2L;
    }
}
