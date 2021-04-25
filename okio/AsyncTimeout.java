// 
// Decompiled by Procyon v0.5.36
// 

package okio;

import java.io.InterruptedIOException;
import java.io.IOException;

public class AsyncTimeout extends Timeout
{
    private static AsyncTimeout head;
    private boolean inQueue;
    private AsyncTimeout next;
    private long timeoutAt;
    
    public final void enter() {
        if (this.inQueue) {
            throw new IllegalStateException("Unbalanced enter/exit");
        }
        final long timeoutNanos = this.timeoutNanos();
        final boolean hasDeadline = this.hasDeadline();
        if (timeoutNanos == 0L && !hasDeadline) {
            return;
        }
        this.inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
    }
    
    private static synchronized void scheduleTimeout(final AsyncTimeout node, final long timeoutNanos, final boolean hasDeadline) {
        if (AsyncTimeout.head == null) {
            AsyncTimeout.head = new AsyncTimeout();
            new Watchdog().start();
        }
        final long now = System.nanoTime();
        if (timeoutNanos != 0L && hasDeadline) {
            node.timeoutAt = now + Math.min(timeoutNanos, node.deadlineNanoTime() - now);
        }
        else if (timeoutNanos != 0L) {
            node.timeoutAt = now + timeoutNanos;
        }
        else {
            if (!hasDeadline) {
                throw new AssertionError();
            }
            node.timeoutAt = node.deadlineNanoTime();
        }
        long remainingNanos;
        AsyncTimeout prev;
        for (remainingNanos = node.remainingNanos(now), prev = AsyncTimeout.head; prev.next != null && remainingNanos >= prev.next.remainingNanos(now); prev = prev.next) {}
        node.next = prev.next;
        prev.next = node;
        if (prev == AsyncTimeout.head) {
            AsyncTimeout.class.notify();
        }
    }
    
    public final boolean exit() {
        if (!this.inQueue) {
            return false;
        }
        this.inQueue = false;
        return cancelScheduledTimeout(this);
    }
    
    private static synchronized boolean cancelScheduledTimeout(final AsyncTimeout node) {
        for (AsyncTimeout prev = AsyncTimeout.head; prev != null; prev = prev.next) {
            if (prev.next == node) {
                prev.next = node.next;
                node.next = null;
                return false;
            }
        }
        return true;
    }
    
    private long remainingNanos(final long now) {
        return this.timeoutAt - now;
    }
    
    protected void timedOut() {
    }
    
    public final Sink sink(final Sink sink) {
        return new Sink() {
            @Override
            public void write(final Buffer source, final long byteCount) throws IOException {
                boolean throwOnTimeout = false;
                AsyncTimeout.this.enter();
                try {
                    sink.write(source, byteCount);
                    throwOnTimeout = true;
                }
                catch (IOException e) {
                    throw AsyncTimeout.this.exit(e);
                }
                finally {
                    AsyncTimeout.this.exit(throwOnTimeout);
                }
            }
            
            @Override
            public void flush() throws IOException {
                boolean throwOnTimeout = false;
                AsyncTimeout.this.enter();
                try {
                    sink.flush();
                    throwOnTimeout = true;
                }
                catch (IOException e) {
                    throw AsyncTimeout.this.exit(e);
                }
                finally {
                    AsyncTimeout.this.exit(throwOnTimeout);
                }
            }
            
            @Override
            public void close() throws IOException {
                boolean throwOnTimeout = false;
                AsyncTimeout.this.enter();
                try {
                    sink.close();
                    throwOnTimeout = true;
                }
                catch (IOException e) {
                    throw AsyncTimeout.this.exit(e);
                }
                finally {
                    AsyncTimeout.this.exit(throwOnTimeout);
                }
            }
            
            @Override
            public Timeout timeout() {
                return AsyncTimeout.this;
            }
            
            @Override
            public String toString() {
                return "AsyncTimeout.sink(" + sink + ")";
            }
        };
    }
    
    public final Source source(final Source source) {
        return new Source() {
            @Override
            public long read(final Buffer sink, final long byteCount) throws IOException {
                boolean throwOnTimeout = false;
                AsyncTimeout.this.enter();
                try {
                    final long result = source.read(sink, byteCount);
                    throwOnTimeout = true;
                    return result;
                }
                catch (IOException e) {
                    throw AsyncTimeout.this.exit(e);
                }
                finally {
                    AsyncTimeout.this.exit(throwOnTimeout);
                }
            }
            
            @Override
            public void close() throws IOException {
                boolean throwOnTimeout = false;
                try {
                    source.close();
                    throwOnTimeout = true;
                }
                catch (IOException e) {
                    throw AsyncTimeout.this.exit(e);
                }
                finally {
                    AsyncTimeout.this.exit(throwOnTimeout);
                }
            }
            
            @Override
            public Timeout timeout() {
                return AsyncTimeout.this;
            }
            
            @Override
            public String toString() {
                return "AsyncTimeout.source(" + source + ")";
            }
        };
    }
    
    final void exit(final boolean throwOnTimeout) throws IOException {
        final boolean timedOut = this.exit();
        if (timedOut && throwOnTimeout) {
            throw this.newTimeoutException(null);
        }
    }
    
    final IOException exit(final IOException cause) throws IOException {
        if (!this.exit()) {
            return cause;
        }
        return this.newTimeoutException(cause);
    }
    
    protected IOException newTimeoutException(final IOException cause) {
        final InterruptedIOException e = new InterruptedIOException("timeout");
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }
    
    private static synchronized AsyncTimeout awaitTimeout() throws InterruptedException {
        final AsyncTimeout node = AsyncTimeout.head.next;
        if (node == null) {
            AsyncTimeout.class.wait();
            return null;
        }
        long waitNanos = node.remainingNanos(System.nanoTime());
        if (waitNanos > 0L) {
            final long waitMillis = waitNanos / 1000000L;
            waitNanos -= waitMillis * 1000000L;
            AsyncTimeout.class.wait(waitMillis, (int)waitNanos);
            return null;
        }
        AsyncTimeout.head.next = node.next;
        node.next = null;
        return node;
    }
    
    private static final class Watchdog extends Thread
    {
        public Watchdog() {
            super("Okio Watchdog");
            this.setDaemon(true);
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    while (true) {
                        final AsyncTimeout timedOut = awaitTimeout();
                        if (timedOut == null) {
                            continue;
                        }
                        timedOut.timedOut();
                    }
                }
                catch (InterruptedException ex) {
                    continue;
                }
                break;
            }
        }
    }
}
