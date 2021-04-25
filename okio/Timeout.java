// 
// Decompiled by Procyon v0.5.36
// 

package okio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

public class Timeout
{
    public static final Timeout NONE;
    private boolean hasDeadline;
    private long deadlineNanoTime;
    private long timeoutNanos;
    
    public Timeout timeout(final long timeout, final TimeUnit unit) {
        if (timeout < 0L) {
            throw new IllegalArgumentException("timeout < 0: " + timeout);
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit == null");
        }
        this.timeoutNanos = unit.toNanos(timeout);
        return this;
    }
    
    public long timeoutNanos() {
        return this.timeoutNanos;
    }
    
    public boolean hasDeadline() {
        return this.hasDeadline;
    }
    
    public long deadlineNanoTime() {
        if (!this.hasDeadline) {
            throw new IllegalStateException("No deadline");
        }
        return this.deadlineNanoTime;
    }
    
    public Timeout deadlineNanoTime(final long deadlineNanoTime) {
        this.hasDeadline = true;
        this.deadlineNanoTime = deadlineNanoTime;
        return this;
    }
    
    public final Timeout deadline(final long duration, final TimeUnit unit) {
        if (duration <= 0L) {
            throw new IllegalArgumentException("duration <= 0: " + duration);
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit == null");
        }
        return this.deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
    }
    
    public Timeout clearTimeout() {
        this.timeoutNanos = 0L;
        return this;
    }
    
    public Timeout clearDeadline() {
        this.hasDeadline = false;
        return this;
    }
    
    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            throw new InterruptedIOException("thread interrupted");
        }
        if (this.hasDeadline && this.deadlineNanoTime - System.nanoTime() <= 0L) {
            throw new InterruptedIOException("deadline reached");
        }
    }
    
    static {
        NONE = new Timeout() {
            @Override
            public Timeout timeout(final long timeout, final TimeUnit unit) {
                return this;
            }
            
            @Override
            public Timeout deadlineNanoTime(final long deadlineNanoTime) {
                return this;
            }
            
            @Override
            public void throwIfReached() throws IOException {
            }
        };
    }
}
