// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.framed;

import okio.ByteString;
import java.util.logging.Level;
import com.squareup.okhttp.internal.Internal;
import okio.Okio;
import java.net.InetSocketAddress;
import okio.BufferedSink;
import java.util.concurrent.SynchronousQueue;
import okio.BufferedSource;
import com.squareup.okhttp.internal.NamedRunnable;
import java.io.InterruptedIOException;
import okio.Buffer;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import com.squareup.okhttp.internal.Util;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Set;
import java.net.Socket;
import java.util.Map;
import com.squareup.okhttp.Protocol;
import java.util.concurrent.ExecutorService;
import java.io.Closeable;

public final class FramedConnection implements Closeable
{
    private static final ExecutorService executor;
    final Protocol protocol;
    final boolean client;
    private final Listener listener;
    private final Map<Integer, FramedStream> streams;
    private final String hostName;
    private int lastGoodStreamId;
    private int nextStreamId;
    private boolean shutdown;
    private long idleStartTimeNs;
    private final ExecutorService pushExecutor;
    private Map<Integer, Ping> pings;
    private final PushObserver pushObserver;
    private int nextPingId;
    long unacknowledgedBytesRead;
    long bytesLeftInWriteWindow;
    Settings okHttpSettings;
    private static final int OKHTTP_CLIENT_WINDOW_SIZE = 16777216;
    final Settings peerSettings;
    private boolean receivedInitialPeerSettings;
    final Variant variant;
    final Socket socket;
    final FrameWriter frameWriter;
    final Reader readerRunnable;
    private final Set<Integer> currentPushRequests;
    
    private FramedConnection(final Builder builder) throws IOException {
        this.streams = new HashMap<Integer, FramedStream>();
        this.idleStartTimeNs = System.nanoTime();
        this.unacknowledgedBytesRead = 0L;
        this.okHttpSettings = new Settings();
        this.peerSettings = new Settings();
        this.receivedInitialPeerSettings = false;
        this.currentPushRequests = new LinkedHashSet<Integer>();
        this.protocol = builder.protocol;
        this.pushObserver = builder.pushObserver;
        this.client = builder.client;
        this.listener = builder.listener;
        this.nextStreamId = (builder.client ? 1 : 2);
        if (builder.client && this.protocol == Protocol.HTTP_2) {
            this.nextStreamId += 2;
        }
        this.nextPingId = (builder.client ? 1 : 2);
        if (builder.client) {
            this.okHttpSettings.set(7, 0, 16777216);
        }
        this.hostName = builder.hostName;
        if (this.protocol == Protocol.HTTP_2) {
            this.variant = new Http2();
            this.pushExecutor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), Util.threadFactory(String.format("OkHttp %s Push Observer", this.hostName), true));
            this.peerSettings.set(7, 0, 65535);
            this.peerSettings.set(5, 0, 16384);
        }
        else {
            if (this.protocol != Protocol.SPDY_3) {
                throw new AssertionError(this.protocol);
            }
            this.variant = new Spdy3();
            this.pushExecutor = null;
        }
        this.bytesLeftInWriteWindow = this.peerSettings.getInitialWindowSize(65536);
        this.socket = builder.socket;
        this.frameWriter = this.variant.newWriter(builder.sink, this.client);
        this.readerRunnable = new Reader(this.variant.newReader(builder.source, this.client));
        new Thread(this.readerRunnable).start();
    }
    
    public Protocol getProtocol() {
        return this.protocol;
    }
    
    public synchronized int openStreamCount() {
        return this.streams.size();
    }
    
    synchronized FramedStream getStream(final int id) {
        return this.streams.get(id);
    }
    
    synchronized FramedStream removeStream(final int streamId) {
        final FramedStream stream = this.streams.remove(streamId);
        if (stream != null && this.streams.isEmpty()) {
            this.setIdle(true);
        }
        this.notifyAll();
        return stream;
    }
    
    private synchronized void setIdle(final boolean value) {
        this.idleStartTimeNs = (value ? System.nanoTime() : Long.MAX_VALUE);
    }
    
    public synchronized boolean isIdle() {
        return this.idleStartTimeNs != Long.MAX_VALUE;
    }
    
    public synchronized int maxConcurrentStreams() {
        return this.peerSettings.getMaxConcurrentStreams(Integer.MAX_VALUE);
    }
    
    public synchronized long getIdleStartTimeNs() {
        return this.idleStartTimeNs;
    }
    
    public FramedStream pushStream(final int associatedStreamId, final List<Header> requestHeaders, final boolean out) throws IOException {
        if (this.client) {
            throw new IllegalStateException("Client cannot push requests.");
        }
        if (this.protocol != Protocol.HTTP_2) {
            throw new IllegalStateException("protocol != HTTP_2");
        }
        return this.newStream(associatedStreamId, requestHeaders, out, false);
    }
    
    public FramedStream newStream(final List<Header> requestHeaders, final boolean out, final boolean in) throws IOException {
        return this.newStream(0, requestHeaders, out, in);
    }
    
    private FramedStream newStream(final int associatedStreamId, final List<Header> requestHeaders, final boolean out, final boolean in) throws IOException {
        final boolean outFinished = !out;
        final boolean inFinished = !in;
        final FramedStream stream;
        synchronized (this.frameWriter) {
            final int streamId;
            synchronized (this) {
                if (this.shutdown) {
                    throw new IOException("shutdown");
                }
                streamId = this.nextStreamId;
                this.nextStreamId += 2;
                stream = new FramedStream(streamId, this, outFinished, inFinished, requestHeaders);
                if (stream.isOpen()) {
                    this.streams.put(streamId, stream);
                    this.setIdle(false);
                }
            }
            if (associatedStreamId == 0) {
                this.frameWriter.synStream(outFinished, inFinished, streamId, associatedStreamId, requestHeaders);
            }
            else {
                if (this.client) {
                    throw new IllegalArgumentException("client streams shouldn't have associated stream IDs");
                }
                this.frameWriter.pushPromise(associatedStreamId, streamId, requestHeaders);
            }
        }
        if (!out) {
            this.frameWriter.flush();
        }
        return stream;
    }
    
    void writeSynReply(final int streamId, final boolean outFinished, final List<Header> alternating) throws IOException {
        this.frameWriter.synReply(outFinished, streamId, alternating);
    }
    
    public void writeData(final int streamId, final boolean outFinished, final Buffer buffer, long byteCount) throws IOException {
        if (byteCount == 0L) {
            this.frameWriter.data(outFinished, streamId, buffer, 0);
            return;
        }
        while (byteCount > 0L) {
            int toWrite;
            synchronized (this) {
                try {
                    while (this.bytesLeftInWriteWindow <= 0L) {
                        if (!this.streams.containsKey(streamId)) {
                            throw new IOException("stream closed");
                        }
                        this.wait();
                    }
                }
                catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
                toWrite = (int)Math.min(byteCount, this.bytesLeftInWriteWindow);
                toWrite = Math.min(toWrite, this.frameWriter.maxDataLength());
                this.bytesLeftInWriteWindow -= toWrite;
            }
            byteCount -= toWrite;
            this.frameWriter.data(outFinished && byteCount == 0L, streamId, buffer, toWrite);
        }
    }
    
    void addBytesToWriteWindow(final long delta) {
        this.bytesLeftInWriteWindow += delta;
        if (delta > 0L) {
            this.notifyAll();
        }
    }
    
    void writeSynResetLater(final int streamId, final ErrorCode errorCode) {
        FramedConnection.executor.submit(new NamedRunnable("OkHttp %s stream %d", new Object[] { this.hostName, streamId }) {
            public void execute() {
                try {
                    FramedConnection.this.writeSynReset(streamId, errorCode);
                }
                catch (IOException ex) {}
            }
        });
    }
    
    void writeSynReset(final int streamId, final ErrorCode statusCode) throws IOException {
        this.frameWriter.rstStream(streamId, statusCode);
    }
    
    void writeWindowUpdateLater(final int streamId, final long unacknowledgedBytesRead) {
        FramedConnection.executor.execute(new NamedRunnable("OkHttp Window Update %s stream %d", new Object[] { this.hostName, streamId }) {
            public void execute() {
                try {
                    FramedConnection.this.frameWriter.windowUpdate(streamId, unacknowledgedBytesRead);
                }
                catch (IOException ex) {}
            }
        });
    }
    
    public Ping ping() throws IOException {
        final Ping ping = new Ping();
        final int pingId;
        synchronized (this) {
            if (this.shutdown) {
                throw new IOException("shutdown");
            }
            pingId = this.nextPingId;
            this.nextPingId += 2;
            if (this.pings == null) {
                this.pings = new HashMap<Integer, Ping>();
            }
            this.pings.put(pingId, ping);
        }
        this.writePing(false, pingId, 1330343787, ping);
        return ping;
    }
    
    private void writePingLater(final boolean reply, final int payload1, final int payload2, final Ping ping) {
        FramedConnection.executor.execute(new NamedRunnable("OkHttp %s ping %08x%08x", new Object[] { this.hostName, payload1, payload2 }) {
            public void execute() {
                try {
                    FramedConnection.this.writePing(reply, payload1, payload2, ping);
                }
                catch (IOException ex) {}
            }
        });
    }
    
    private void writePing(final boolean reply, final int payload1, final int payload2, final Ping ping) throws IOException {
        synchronized (this.frameWriter) {
            if (ping != null) {
                ping.send();
            }
            this.frameWriter.ping(reply, payload1, payload2);
        }
    }
    
    private synchronized Ping removePing(final int id) {
        return (this.pings != null) ? this.pings.remove(id) : null;
    }
    
    public void flush() throws IOException {
        this.frameWriter.flush();
    }
    
    public void shutdown(final ErrorCode statusCode) throws IOException {
        synchronized (this.frameWriter) {
            final int lastGoodStreamId;
            synchronized (this) {
                if (this.shutdown) {
                    return;
                }
                this.shutdown = true;
                lastGoodStreamId = this.lastGoodStreamId;
            }
            this.frameWriter.goAway(lastGoodStreamId, statusCode, Util.EMPTY_BYTE_ARRAY);
        }
    }
    
    @Override
    public void close() throws IOException {
        this.close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
    }
    
    private void close(final ErrorCode connectionCode, final ErrorCode streamCode) throws IOException {
        assert !Thread.holdsLock(this);
        IOException thrown = null;
        try {
            this.shutdown(connectionCode);
        }
        catch (IOException e) {
            thrown = e;
        }
        FramedStream[] streamsToClose = null;
        Ping[] pingsToCancel = null;
        synchronized (this) {
            if (!this.streams.isEmpty()) {
                streamsToClose = this.streams.values().toArray(new FramedStream[this.streams.size()]);
                this.streams.clear();
                this.setIdle(false);
            }
            if (this.pings != null) {
                pingsToCancel = this.pings.values().toArray(new Ping[this.pings.size()]);
                this.pings = null;
            }
        }
        if (streamsToClose != null) {
            for (final FramedStream stream : streamsToClose) {
                try {
                    stream.close(streamCode);
                }
                catch (IOException e2) {
                    if (thrown != null) {
                        thrown = e2;
                    }
                }
            }
        }
        if (pingsToCancel != null) {
            for (final Ping ping : pingsToCancel) {
                ping.cancel();
            }
        }
        try {
            this.frameWriter.close();
        }
        catch (IOException e3) {
            if (thrown == null) {
                thrown = e3;
            }
        }
        try {
            this.socket.close();
        }
        catch (IOException e3) {
            thrown = e3;
        }
        if (thrown != null) {
            throw thrown;
        }
    }
    
    public void sendConnectionPreface() throws IOException {
        this.frameWriter.connectionPreface();
        this.frameWriter.settings(this.okHttpSettings);
        final int windowSize = this.okHttpSettings.getInitialWindowSize(65536);
        if (windowSize != 65536) {
            this.frameWriter.windowUpdate(0, windowSize - 65536);
        }
    }
    
    public void setSettings(final Settings settings) throws IOException {
        synchronized (this.frameWriter) {
            synchronized (this) {
                if (this.shutdown) {
                    throw new IOException("shutdown");
                }
                this.okHttpSettings.merge(settings);
                this.frameWriter.settings(settings);
            }
        }
    }
    
    private boolean pushedStream(final int streamId) {
        return this.protocol == Protocol.HTTP_2 && streamId != 0 && (streamId & 0x1) == 0x0;
    }
    
    private void pushRequestLater(final int streamId, final List<Header> requestHeaders) {
        synchronized (this) {
            if (this.currentPushRequests.contains(streamId)) {
                this.writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
                return;
            }
            this.currentPushRequests.add(streamId);
        }
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Request[%s]", new Object[] { this.hostName, streamId }) {
            public void execute() {
                final boolean cancel = FramedConnection.this.pushObserver.onRequest(streamId, requestHeaders);
                try {
                    if (cancel) {
                        FramedConnection.this.frameWriter.rstStream(streamId, ErrorCode.CANCEL);
                        synchronized (FramedConnection.this) {
                            FramedConnection.this.currentPushRequests.remove(streamId);
                        }
                    }
                }
                catch (IOException ex) {}
            }
        });
    }
    
    private void pushHeadersLater(final int streamId, final List<Header> requestHeaders, final boolean inFinished) {
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Headers[%s]", new Object[] { this.hostName, streamId }) {
            public void execute() {
                final boolean cancel = FramedConnection.this.pushObserver.onHeaders(streamId, requestHeaders, inFinished);
                try {
                    if (cancel) {
                        FramedConnection.this.frameWriter.rstStream(streamId, ErrorCode.CANCEL);
                    }
                    if (cancel || inFinished) {
                        synchronized (FramedConnection.this) {
                            FramedConnection.this.currentPushRequests.remove(streamId);
                        }
                    }
                }
                catch (IOException ex) {}
            }
        });
    }
    
    private void pushDataLater(final int streamId, final BufferedSource source, final int byteCount, final boolean inFinished) throws IOException {
        final Buffer buffer = new Buffer();
        source.require(byteCount);
        source.read(buffer, byteCount);
        if (buffer.size() != byteCount) {
            throw new IOException(buffer.size() + " != " + byteCount);
        }
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Data[%s]", new Object[] { this.hostName, streamId }) {
            public void execute() {
                try {
                    final boolean cancel = FramedConnection.this.pushObserver.onData(streamId, buffer, byteCount, inFinished);
                    if (cancel) {
                        FramedConnection.this.frameWriter.rstStream(streamId, ErrorCode.CANCEL);
                    }
                    if (cancel || inFinished) {
                        synchronized (FramedConnection.this) {
                            FramedConnection.this.currentPushRequests.remove(streamId);
                        }
                    }
                }
                catch (IOException ex) {}
            }
        });
    }
    
    private void pushResetLater(final int streamId, final ErrorCode errorCode) {
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Reset[%s]", new Object[] { this.hostName, streamId }) {
            public void execute() {
                FramedConnection.this.pushObserver.onReset(streamId, errorCode);
                synchronized (FramedConnection.this) {
                    FramedConnection.this.currentPushRequests.remove(streamId);
                }
            }
        });
    }
    
    static {
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp FramedConnection", true));
    }
    
    public static class Builder
    {
        private Socket socket;
        private String hostName;
        private BufferedSource source;
        private BufferedSink sink;
        private Listener listener;
        private Protocol protocol;
        private PushObserver pushObserver;
        private boolean client;
        
        public Builder(final boolean client) throws IOException {
            this.listener = Listener.REFUSE_INCOMING_STREAMS;
            this.protocol = Protocol.SPDY_3;
            this.pushObserver = PushObserver.CANCEL;
            this.client = client;
        }
        
        public Builder socket(final Socket socket) throws IOException {
            return this.socket(socket, ((InetSocketAddress)socket.getRemoteSocketAddress()).getHostName(), Okio.buffer(Okio.source(socket)), Okio.buffer(Okio.sink(socket)));
        }
        
        public Builder socket(final Socket socket, final String hostName, final BufferedSource source, final BufferedSink sink) {
            this.socket = socket;
            this.hostName = hostName;
            this.source = source;
            this.sink = sink;
            return this;
        }
        
        public Builder listener(final Listener listener) {
            this.listener = listener;
            return this;
        }
        
        public Builder protocol(final Protocol protocol) {
            this.protocol = protocol;
            return this;
        }
        
        public Builder pushObserver(final PushObserver pushObserver) {
            this.pushObserver = pushObserver;
            return this;
        }
        
        public FramedConnection build() throws IOException {
            return new FramedConnection(this, null);
        }
    }
    
    class Reader extends NamedRunnable implements FrameReader.Handler
    {
        final FrameReader frameReader;
        
        private Reader(final FrameReader frameReader) {
            super("OkHttp %s", new Object[] { FramedConnection.this.hostName });
            this.frameReader = frameReader;
        }
        
        @Override
        protected void execute() {
            ErrorCode connectionErrorCode = ErrorCode.INTERNAL_ERROR;
            ErrorCode streamErrorCode = ErrorCode.INTERNAL_ERROR;
            try {
                if (!FramedConnection.this.client) {
                    this.frameReader.readConnectionPreface();
                }
                while (this.frameReader.nextFrame(this)) {}
                connectionErrorCode = ErrorCode.NO_ERROR;
                streamErrorCode = ErrorCode.CANCEL;
            }
            catch (IOException e) {
                connectionErrorCode = ErrorCode.PROTOCOL_ERROR;
                streamErrorCode = ErrorCode.PROTOCOL_ERROR;
            }
            finally {
                try {
                    FramedConnection.this.close(connectionErrorCode, streamErrorCode);
                }
                catch (IOException ex) {}
                Util.closeQuietly(this.frameReader);
            }
        }
        
        @Override
        public void data(final boolean inFinished, final int streamId, final BufferedSource source, final int length) throws IOException {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushDataLater(streamId, source, length, inFinished);
                return;
            }
            final FramedStream dataStream = FramedConnection.this.getStream(streamId);
            if (dataStream == null) {
                FramedConnection.this.writeSynResetLater(streamId, ErrorCode.INVALID_STREAM);
                source.skip(length);
                return;
            }
            dataStream.receiveData(source, length);
            if (inFinished) {
                dataStream.receiveFin();
            }
        }
        
        @Override
        public void headers(final boolean outFinished, final boolean inFinished, final int streamId, final int associatedStreamId, final List<Header> headerBlock, final HeadersMode headersMode) {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushHeadersLater(streamId, headerBlock, inFinished);
                return;
            }
            final FramedStream stream;
            synchronized (FramedConnection.this) {
                if (FramedConnection.this.shutdown) {
                    return;
                }
                stream = FramedConnection.this.getStream(streamId);
                if (stream == null) {
                    if (headersMode.failIfStreamAbsent()) {
                        FramedConnection.this.writeSynResetLater(streamId, ErrorCode.INVALID_STREAM);
                        return;
                    }
                    if (streamId <= FramedConnection.this.lastGoodStreamId) {
                        return;
                    }
                    if (streamId % 2 == FramedConnection.this.nextStreamId % 2) {
                        return;
                    }
                    final FramedStream newStream = new FramedStream(streamId, FramedConnection.this, outFinished, inFinished, headerBlock);
                    FramedConnection.this.lastGoodStreamId = streamId;
                    FramedConnection.this.streams.put(streamId, newStream);
                    FramedConnection.executor.execute(new NamedRunnable("OkHttp %s stream %d", new Object[] { FramedConnection.this.hostName, streamId }) {
                        public void execute() {
                            try {
                                FramedConnection.this.listener.onStream(newStream);
                            }
                            catch (IOException e) {
                                Internal.logger.log(Level.INFO, "FramedConnection.Listener failure for " + FramedConnection.this.hostName, e);
                                try {
                                    newStream.close(ErrorCode.PROTOCOL_ERROR);
                                }
                                catch (IOException ex) {}
                            }
                        }
                    });
                    return;
                }
            }
            if (headersMode.failIfStreamPresent()) {
                stream.closeLater(ErrorCode.PROTOCOL_ERROR);
                FramedConnection.this.removeStream(streamId);
                return;
            }
            stream.receiveHeaders(headerBlock, headersMode);
            if (inFinished) {
                stream.receiveFin();
            }
        }
        
        @Override
        public void rstStream(final int streamId, final ErrorCode errorCode) {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushResetLater(streamId, errorCode);
                return;
            }
            final FramedStream rstStream = FramedConnection.this.removeStream(streamId);
            if (rstStream != null) {
                rstStream.receiveRstStream(errorCode);
            }
        }
        
        @Override
        public void settings(final boolean clearPrevious, final Settings newSettings) {
            long delta = 0L;
            FramedStream[] streamsToNotify = null;
            synchronized (FramedConnection.this) {
                final int priorWriteWindowSize = FramedConnection.this.peerSettings.getInitialWindowSize(65536);
                if (clearPrevious) {
                    FramedConnection.this.peerSettings.clear();
                }
                FramedConnection.this.peerSettings.merge(newSettings);
                if (FramedConnection.this.getProtocol() == Protocol.HTTP_2) {
                    this.ackSettingsLater(newSettings);
                }
                final int peerInitialWindowSize = FramedConnection.this.peerSettings.getInitialWindowSize(65536);
                if (peerInitialWindowSize != -1 && peerInitialWindowSize != priorWriteWindowSize) {
                    delta = peerInitialWindowSize - priorWriteWindowSize;
                    if (!FramedConnection.this.receivedInitialPeerSettings) {
                        FramedConnection.this.addBytesToWriteWindow(delta);
                        FramedConnection.this.receivedInitialPeerSettings = true;
                    }
                    if (!FramedConnection.this.streams.isEmpty()) {
                        streamsToNotify = (FramedStream[])FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                    }
                }
                FramedConnection.executor.execute(new NamedRunnable("OkHttp %s settings", new Object[] { FramedConnection.this.hostName }) {
                    public void execute() {
                        FramedConnection.this.listener.onSettings(FramedConnection.this);
                    }
                });
            }
            if (streamsToNotify != null && delta != 0L) {
                for (final FramedStream stream : streamsToNotify) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(delta);
                    }
                }
            }
        }
        
        private void ackSettingsLater(final Settings peerSettings) {
            FramedConnection.executor.execute(new NamedRunnable("OkHttp %s ACK Settings", new Object[] { FramedConnection.this.hostName }) {
                public void execute() {
                    try {
                        FramedConnection.this.frameWriter.ackSettings(peerSettings);
                    }
                    catch (IOException ex) {}
                }
            });
        }
        
        @Override
        public void ackSettings() {
        }
        
        @Override
        public void ping(final boolean reply, final int payload1, final int payload2) {
            if (reply) {
                final Ping ping = FramedConnection.this.removePing(payload1);
                if (ping != null) {
                    ping.receive();
                }
            }
            else {
                FramedConnection.this.writePingLater(true, payload1, payload2, null);
            }
        }
        
        @Override
        public void goAway(final int lastGoodStreamId, final ErrorCode errorCode, final ByteString debugData) {
            if (debugData.size() > 0) {}
            final FramedStream[] streamsCopy;
            synchronized (FramedConnection.this) {
                streamsCopy = (FramedStream[])FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                FramedConnection.this.shutdown = true;
            }
            for (final FramedStream framedStream : streamsCopy) {
                if (framedStream.getId() > lastGoodStreamId && framedStream.isLocallyInitiated()) {
                    framedStream.receiveRstStream(ErrorCode.REFUSED_STREAM);
                    FramedConnection.this.removeStream(framedStream.getId());
                }
            }
        }
        
        @Override
        public void windowUpdate(final int streamId, final long windowSizeIncrement) {
            if (streamId == 0) {
                synchronized (FramedConnection.this) {
                    final FramedConnection this$0 = FramedConnection.this;
                    this$0.bytesLeftInWriteWindow += windowSizeIncrement;
                    FramedConnection.this.notifyAll();
                }
            }
            else {
                final FramedStream stream = FramedConnection.this.getStream(streamId);
                if (stream != null) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(windowSizeIncrement);
                    }
                }
            }
        }
        
        @Override
        public void priority(final int streamId, final int streamDependency, final int weight, final boolean exclusive) {
        }
        
        @Override
        public void pushPromise(final int streamId, final int promisedStreamId, final List<Header> requestHeaders) {
            FramedConnection.this.pushRequestLater(promisedStreamId, requestHeaders);
        }
        
        @Override
        public void alternateService(final int streamId, final String origin, final ByteString protocol, final String host, final int port, final long maxAge) {
        }
    }
    
    public abstract static class Listener
    {
        public static final Listener REFUSE_INCOMING_STREAMS;
        
        public abstract void onStream(final FramedStream p0) throws IOException;
        
        public void onSettings(final FramedConnection connection) {
        }
        
        static {
            REFUSE_INCOMING_STREAMS = new Listener() {
                @Override
                public void onStream(final FramedStream stream) throws IOException {
                    stream.close(ErrorCode.REFUSED_STREAM);
                }
            };
        }
    }
}
