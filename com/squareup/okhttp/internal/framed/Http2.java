// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.framed;

import okio.Timeout;
import okio.Buffer;
import java.util.List;
import java.util.logging.Level;
import okio.Source;
import java.io.IOException;
import okio.BufferedSink;
import okio.BufferedSource;
import com.squareup.okhttp.Protocol;
import okio.ByteString;
import java.util.logging.Logger;

public final class Http2 implements Variant
{
    private static final Logger logger;
    private static final ByteString CONNECTION_PREFACE;
    static final int INITIAL_MAX_FRAME_SIZE = 16384;
    static final byte TYPE_DATA = 0;
    static final byte TYPE_HEADERS = 1;
    static final byte TYPE_PRIORITY = 2;
    static final byte TYPE_RST_STREAM = 3;
    static final byte TYPE_SETTINGS = 4;
    static final byte TYPE_PUSH_PROMISE = 5;
    static final byte TYPE_PING = 6;
    static final byte TYPE_GOAWAY = 7;
    static final byte TYPE_WINDOW_UPDATE = 8;
    static final byte TYPE_CONTINUATION = 9;
    static final byte FLAG_NONE = 0;
    static final byte FLAG_ACK = 1;
    static final byte FLAG_END_STREAM = 1;
    static final byte FLAG_END_HEADERS = 4;
    static final byte FLAG_END_PUSH_PROMISE = 4;
    static final byte FLAG_PADDED = 8;
    static final byte FLAG_PRIORITY = 32;
    static final byte FLAG_COMPRESSED = 32;
    
    @Override
    public Protocol getProtocol() {
        return Protocol.HTTP_2;
    }
    
    @Override
    public FrameReader newReader(final BufferedSource source, final boolean client) {
        return new Reader(source, 4096, client);
    }
    
    @Override
    public FrameWriter newWriter(final BufferedSink sink, final boolean client) {
        return new Writer(sink, client);
    }
    
    private static IllegalArgumentException illegalArgument(final String message, final Object... args) {
        throw new IllegalArgumentException(String.format(message, args));
    }
    
    private static IOException ioException(final String message, final Object... args) throws IOException {
        throw new IOException(String.format(message, args));
    }
    
    private static int lengthWithoutPadding(int length, final byte flags, final short padding) throws IOException {
        if ((flags & 0x8) != 0x0) {
            --length;
        }
        if (padding > length) {
            throw ioException("PROTOCOL_ERROR padding %s > remaining length %s", padding, length);
        }
        return (short)(length - padding);
    }
    
    private static int readMedium(final BufferedSource source) throws IOException {
        return (source.readByte() & 0xFF) << 16 | (source.readByte() & 0xFF) << 8 | (source.readByte() & 0xFF);
    }
    
    private static void writeMedium(final BufferedSink sink, final int i) throws IOException {
        sink.writeByte(i >>> 16 & 0xFF);
        sink.writeByte(i >>> 8 & 0xFF);
        sink.writeByte(i & 0xFF);
    }
    
    static {
        logger = Logger.getLogger(FrameLogger.class.getName());
        CONNECTION_PREFACE = ByteString.encodeUtf8("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n");
    }
    
    static final class Reader implements FrameReader
    {
        private final BufferedSource source;
        private final ContinuationSource continuation;
        private final boolean client;
        final Hpack.Reader hpackReader;
        
        Reader(final BufferedSource source, final int headerTableSize, final boolean client) {
            this.source = source;
            this.client = client;
            this.continuation = new ContinuationSource(this.source);
            this.hpackReader = new Hpack.Reader(headerTableSize, this.continuation);
        }
        
        @Override
        public void readConnectionPreface() throws IOException {
            if (this.client) {
                return;
            }
            final ByteString connectionPreface = this.source.readByteString(Http2.CONNECTION_PREFACE.size());
            if (Http2.logger.isLoggable(Level.FINE)) {
                Http2.logger.fine(String.format("<< CONNECTION %s", connectionPreface.hex()));
            }
            if (!Http2.CONNECTION_PREFACE.equals(connectionPreface)) {
                throw ioException("Expected a connection header but was %s", new Object[] { connectionPreface.utf8() });
            }
        }
        
        @Override
        public boolean nextFrame(final Handler handler) throws IOException {
            try {
                this.source.require(9L);
            }
            catch (IOException e) {
                return false;
            }
            final int length = readMedium(this.source);
            if (length < 0 || length > 16384) {
                throw ioException("FRAME_SIZE_ERROR: %s", new Object[] { length });
            }
            final byte type = (byte)(this.source.readByte() & 0xFF);
            final byte flags = (byte)(this.source.readByte() & 0xFF);
            final int streamId = this.source.readInt() & Integer.MAX_VALUE;
            if (Http2.logger.isLoggable(Level.FINE)) {
                Http2.logger.fine(FrameLogger.formatHeader(true, streamId, length, type, flags));
            }
            switch (type) {
                case 0: {
                    this.readData(handler, length, flags, streamId);
                    break;
                }
                case 1: {
                    this.readHeaders(handler, length, flags, streamId);
                    break;
                }
                case 2: {
                    this.readPriority(handler, length, flags, streamId);
                    break;
                }
                case 3: {
                    this.readRstStream(handler, length, flags, streamId);
                    break;
                }
                case 4: {
                    this.readSettings(handler, length, flags, streamId);
                    break;
                }
                case 5: {
                    this.readPushPromise(handler, length, flags, streamId);
                    break;
                }
                case 6: {
                    this.readPing(handler, length, flags, streamId);
                    break;
                }
                case 7: {
                    this.readGoAway(handler, length, flags, streamId);
                    break;
                }
                case 8: {
                    this.readWindowUpdate(handler, length, flags, streamId);
                    break;
                }
                default: {
                    this.source.skip(length);
                    break;
                }
            }
            return true;
        }
        
        private void readHeaders(final Handler handler, int length, final byte flags, final int streamId) throws IOException {
            if (streamId == 0) {
                throw ioException("PROTOCOL_ERROR: TYPE_HEADERS streamId == 0", new Object[0]);
            }
            final boolean endStream = (flags & 0x1) != 0x0;
            final short padding = (short)(((flags & 0x8) != 0x0) ? ((short)(this.source.readByte() & 0xFF)) : 0);
            if ((flags & 0x20) != 0x0) {
                this.readPriority(handler, streamId);
                length -= 5;
            }
            length = lengthWithoutPadding(length, flags, padding);
            final List<Header> headerBlock = this.readHeaderBlock(length, padding, flags, streamId);
            handler.headers(false, endStream, streamId, -1, headerBlock, HeadersMode.HTTP_20_HEADERS);
        }
        
        private List<Header> readHeaderBlock(final int length, final short padding, final byte flags, final int streamId) throws IOException {
            final ContinuationSource continuation = this.continuation;
            this.continuation.left = length;
            continuation.length = length;
            this.continuation.padding = padding;
            this.continuation.flags = flags;
            this.continuation.streamId = streamId;
            this.hpackReader.readHeaders();
            return this.hpackReader.getAndResetHeaderList();
        }
        
        private void readData(final Handler handler, int length, final byte flags, final int streamId) throws IOException {
            final boolean inFinished = (flags & 0x1) != 0x0;
            final boolean gzipped = (flags & 0x20) != 0x0;
            if (gzipped) {
                throw ioException("PROTOCOL_ERROR: FLAG_COMPRESSED without SETTINGS_COMPRESS_DATA", new Object[0]);
            }
            final short padding = (short)(((flags & 0x8) != 0x0) ? ((short)(this.source.readByte() & 0xFF)) : 0);
            length = lengthWithoutPadding(length, flags, padding);
            handler.data(inFinished, streamId, this.source, length);
            this.source.skip(padding);
        }
        
        private void readPriority(final Handler handler, final int length, final byte flags, final int streamId) throws IOException {
            if (length != 5) {
                throw ioException("TYPE_PRIORITY length: %d != 5", new Object[] { length });
            }
            if (streamId == 0) {
                throw ioException("TYPE_PRIORITY streamId == 0", new Object[0]);
            }
            this.readPriority(handler, streamId);
        }
        
        private void readPriority(final Handler handler, final int streamId) throws IOException {
            final int w1 = this.source.readInt();
            final boolean exclusive = (w1 & Integer.MIN_VALUE) != 0x0;
            final int streamDependency = w1 & Integer.MAX_VALUE;
            final int weight = (this.source.readByte() & 0xFF) + 1;
            handler.priority(streamId, streamDependency, weight, exclusive);
        }
        
        private void readRstStream(final Handler handler, final int length, final byte flags, final int streamId) throws IOException {
            if (length != 4) {
                throw ioException("TYPE_RST_STREAM length: %d != 4", new Object[] { length });
            }
            if (streamId == 0) {
                throw ioException("TYPE_RST_STREAM streamId == 0", new Object[0]);
            }
            final int errorCodeInt = this.source.readInt();
            final ErrorCode errorCode = ErrorCode.fromHttp2(errorCodeInt);
            if (errorCode == null) {
                throw ioException("TYPE_RST_STREAM unexpected error code: %d", new Object[] { errorCodeInt });
            }
            handler.rstStream(streamId, errorCode);
        }
        
        private void readSettings(final Handler handler, final int length, final byte flags, final int streamId) throws IOException {
            if (streamId != 0) {
                throw ioException("TYPE_SETTINGS streamId != 0", new Object[0]);
            }
            if ((flags & 0x1) != 0x0) {
                if (length != 0) {
                    throw ioException("FRAME_SIZE_ERROR ack frame should be empty!", new Object[0]);
                }
                handler.ackSettings();
            }
            else {
                if (length % 6 != 0) {
                    throw ioException("TYPE_SETTINGS length %% 6 != 0: %s", new Object[] { length });
                }
                final Settings settings = new Settings();
                for (int i = 0; i < length; i += 6) {
                    short id = this.source.readShort();
                    final int value = this.source.readInt();
                    switch (id) {
                        case 1: {
                            break;
                        }
                        case 2: {
                            if (value != 0 && value != 1) {
                                throw ioException("PROTOCOL_ERROR SETTINGS_ENABLE_PUSH != 0 or 1", new Object[0]);
                            }
                            break;
                        }
                        case 3: {
                            id = 4;
                            break;
                        }
                        case 4: {
                            id = 7;
                            if (value < 0) {
                                throw ioException("PROTOCOL_ERROR SETTINGS_INITIAL_WINDOW_SIZE > 2^31 - 1", new Object[0]);
                            }
                            break;
                        }
                        case 5: {
                            if (value < 16384 || value > 16777215) {
                                throw ioException("PROTOCOL_ERROR SETTINGS_MAX_FRAME_SIZE: %s", new Object[] { value });
                            }
                            break;
                        }
                        case 6: {
                            break;
                        }
                        default: {
                            throw ioException("PROTOCOL_ERROR invalid settings id: %s", new Object[] { id });
                        }
                    }
                    settings.set(id, 0, value);
                }
                handler.settings(false, settings);
                if (settings.getHeaderTableSize() >= 0) {
                    this.hpackReader.headerTableSizeSetting(settings.getHeaderTableSize());
                }
            }
        }
        
        private void readPushPromise(final Handler handler, int length, final byte flags, final int streamId) throws IOException {
            if (streamId == 0) {
                throw ioException("PROTOCOL_ERROR: TYPE_PUSH_PROMISE streamId == 0", new Object[0]);
            }
            final short padding = (short)(((flags & 0x8) != 0x0) ? ((short)(this.source.readByte() & 0xFF)) : 0);
            final int promisedStreamId = this.source.readInt() & Integer.MAX_VALUE;
            length -= 4;
            length = lengthWithoutPadding(length, flags, padding);
            final List<Header> headerBlock = this.readHeaderBlock(length, padding, flags, streamId);
            handler.pushPromise(streamId, promisedStreamId, headerBlock);
        }
        
        private void readPing(final Handler handler, final int length, final byte flags, final int streamId) throws IOException {
            if (length != 8) {
                throw ioException("TYPE_PING length != 8: %s", new Object[] { length });
            }
            if (streamId != 0) {
                throw ioException("TYPE_PING streamId != 0", new Object[0]);
            }
            final int payload1 = this.source.readInt();
            final int payload2 = this.source.readInt();
            final boolean ack = (flags & 0x1) != 0x0;
            handler.ping(ack, payload1, payload2);
        }
        
        private void readGoAway(final Handler handler, final int length, final byte flags, final int streamId) throws IOException {
            if (length < 8) {
                throw ioException("TYPE_GOAWAY length < 8: %s", new Object[] { length });
            }
            if (streamId != 0) {
                throw ioException("TYPE_GOAWAY streamId != 0", new Object[0]);
            }
            final int lastStreamId = this.source.readInt();
            final int errorCodeInt = this.source.readInt();
            final int opaqueDataLength = length - 8;
            final ErrorCode errorCode = ErrorCode.fromHttp2(errorCodeInt);
            if (errorCode == null) {
                throw ioException("TYPE_GOAWAY unexpected error code: %d", new Object[] { errorCodeInt });
            }
            ByteString debugData = ByteString.EMPTY;
            if (opaqueDataLength > 0) {
                debugData = this.source.readByteString(opaqueDataLength);
            }
            handler.goAway(lastStreamId, errorCode, debugData);
        }
        
        private void readWindowUpdate(final Handler handler, final int length, final byte flags, final int streamId) throws IOException {
            if (length != 4) {
                throw ioException("TYPE_WINDOW_UPDATE length !=4: %s", new Object[] { length });
            }
            final long increment = (long)this.source.readInt() & 0x7FFFFFFFL;
            if (increment == 0L) {
                throw ioException("windowSizeIncrement was 0", new Object[] { increment });
            }
            handler.windowUpdate(streamId, increment);
        }
        
        @Override
        public void close() throws IOException {
            this.source.close();
        }
    }
    
    static final class Writer implements FrameWriter
    {
        private final BufferedSink sink;
        private final boolean client;
        private final Buffer hpackBuffer;
        private final Hpack.Writer hpackWriter;
        private int maxFrameSize;
        private boolean closed;
        
        Writer(final BufferedSink sink, final boolean client) {
            this.sink = sink;
            this.client = client;
            this.hpackBuffer = new Buffer();
            this.hpackWriter = new Hpack.Writer(this.hpackBuffer);
            this.maxFrameSize = 16384;
        }
        
        @Override
        public synchronized void flush() throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.sink.flush();
        }
        
        @Override
        public synchronized void ackSettings(final Settings peerSettings) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.maxFrameSize = peerSettings.getMaxFrameSize(this.maxFrameSize);
            final int length = 0;
            final byte type = 4;
            final byte flags = 1;
            final int streamId = 0;
            this.frameHeader(streamId, length, type, flags);
            this.sink.flush();
        }
        
        @Override
        public synchronized void connectionPreface() throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            if (!this.client) {
                return;
            }
            if (Http2.logger.isLoggable(Level.FINE)) {
                Http2.logger.fine(String.format(">> CONNECTION %s", Http2.CONNECTION_PREFACE.hex()));
            }
            this.sink.write(Http2.CONNECTION_PREFACE.toByteArray());
            this.sink.flush();
        }
        
        @Override
        public synchronized void synStream(final boolean outFinished, final boolean inFinished, final int streamId, final int associatedStreamId, final List<Header> headerBlock) throws IOException {
            if (inFinished) {
                throw new UnsupportedOperationException();
            }
            if (this.closed) {
                throw new IOException("closed");
            }
            this.headers(outFinished, streamId, headerBlock);
        }
        
        @Override
        public synchronized void synReply(final boolean outFinished, final int streamId, final List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.headers(outFinished, streamId, headerBlock);
        }
        
        @Override
        public synchronized void headers(final int streamId, final List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.headers(false, streamId, headerBlock);
        }
        
        @Override
        public synchronized void pushPromise(final int streamId, final int promisedStreamId, final List<Header> requestHeaders) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.hpackWriter.writeHeaders(requestHeaders);
            final long byteCount = this.hpackBuffer.size();
            final int length = (int)Math.min(this.maxFrameSize - 4, byteCount);
            final byte type = 5;
            final byte flags = (byte)((byteCount == length) ? 4 : 0);
            this.frameHeader(streamId, length + 4, type, flags);
            this.sink.writeInt(promisedStreamId & Integer.MAX_VALUE);
            this.sink.write(this.hpackBuffer, length);
            if (byteCount > length) {
                this.writeContinuationFrames(streamId, byteCount - length);
            }
        }
        
        void headers(final boolean outFinished, final int streamId, final List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.hpackWriter.writeHeaders(headerBlock);
            final long byteCount = this.hpackBuffer.size();
            final int length = (int)Math.min(this.maxFrameSize, byteCount);
            final byte type = 1;
            byte flags = (byte)((byteCount == length) ? 4 : 0);
            if (outFinished) {
                flags |= 0x1;
            }
            this.frameHeader(streamId, length, type, flags);
            this.sink.write(this.hpackBuffer, length);
            if (byteCount > length) {
                this.writeContinuationFrames(streamId, byteCount - length);
            }
        }
        
        private void writeContinuationFrames(final int streamId, long byteCount) throws IOException {
            while (byteCount > 0L) {
                final int length = (int)Math.min(this.maxFrameSize, byteCount);
                byteCount -= length;
                this.frameHeader(streamId, length, (byte)9, (byte)((byteCount == 0L) ? 4 : 0));
                this.sink.write(this.hpackBuffer, length);
            }
        }
        
        @Override
        public synchronized void rstStream(final int streamId, final ErrorCode errorCode) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            if (errorCode.httpCode == -1) {
                throw new IllegalArgumentException();
            }
            final int length = 4;
            final byte type = 3;
            final byte flags = 0;
            this.frameHeader(streamId, length, type, flags);
            this.sink.writeInt(errorCode.httpCode);
            this.sink.flush();
        }
        
        @Override
        public int maxDataLength() {
            return this.maxFrameSize;
        }
        
        @Override
        public synchronized void data(final boolean outFinished, final int streamId, final Buffer source, final int byteCount) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            byte flags = 0;
            if (outFinished) {
                flags |= 0x1;
            }
            this.dataFrame(streamId, flags, source, byteCount);
        }
        
        void dataFrame(final int streamId, final byte flags, final Buffer buffer, final int byteCount) throws IOException {
            final byte type = 0;
            this.frameHeader(streamId, byteCount, type, flags);
            if (byteCount > 0) {
                this.sink.write(buffer, byteCount);
            }
        }
        
        @Override
        public synchronized void settings(final Settings settings) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            final int length = settings.size() * 6;
            final byte type = 4;
            final byte flags = 0;
            final int streamId = 0;
            this.frameHeader(streamId, length, type, flags);
            for (int i = 0; i < 10; ++i) {
                if (settings.isSet(i)) {
                    int id = i;
                    if (id == 4) {
                        id = 3;
                    }
                    else if (id == 7) {
                        id = 4;
                    }
                    this.sink.writeShort(id);
                    this.sink.writeInt(settings.get(i));
                }
            }
            this.sink.flush();
        }
        
        @Override
        public synchronized void ping(final boolean ack, final int payload1, final int payload2) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            final int length = 8;
            final byte type = 6;
            final byte flags = (byte)(ack ? 1 : 0);
            final int streamId = 0;
            this.frameHeader(streamId, length, type, flags);
            this.sink.writeInt(payload1);
            this.sink.writeInt(payload2);
            this.sink.flush();
        }
        
        @Override
        public synchronized void goAway(final int lastGoodStreamId, final ErrorCode errorCode, final byte[] debugData) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            if (errorCode.httpCode == -1) {
                throw illegalArgument("errorCode.httpCode == -1", new Object[0]);
            }
            final int length = 8 + debugData.length;
            final byte type = 7;
            final byte flags = 0;
            final int streamId = 0;
            this.frameHeader(streamId, length, type, flags);
            this.sink.writeInt(lastGoodStreamId);
            this.sink.writeInt(errorCode.httpCode);
            if (debugData.length > 0) {
                this.sink.write(debugData);
            }
            this.sink.flush();
        }
        
        @Override
        public synchronized void windowUpdate(final int streamId, final long windowSizeIncrement) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            if (windowSizeIncrement == 0L || windowSizeIncrement > 2147483647L) {
                throw illegalArgument("windowSizeIncrement == 0 || windowSizeIncrement > 0x7fffffffL: %s", new Object[] { windowSizeIncrement });
            }
            final int length = 4;
            final byte type = 8;
            final byte flags = 0;
            this.frameHeader(streamId, length, type, flags);
            this.sink.writeInt((int)windowSizeIncrement);
            this.sink.flush();
        }
        
        @Override
        public synchronized void close() throws IOException {
            this.closed = true;
            this.sink.close();
        }
        
        void frameHeader(final int streamId, final int length, final byte type, final byte flags) throws IOException {
            if (Http2.logger.isLoggable(Level.FINE)) {
                Http2.logger.fine(FrameLogger.formatHeader(false, streamId, length, type, flags));
            }
            if (length > this.maxFrameSize) {
                throw illegalArgument("FRAME_SIZE_ERROR length > %d: %d", new Object[] { this.maxFrameSize, length });
            }
            if ((streamId & Integer.MIN_VALUE) != 0x0) {
                throw illegalArgument("reserved bit set: %s", new Object[] { streamId });
            }
            writeMedium(this.sink, length);
            this.sink.writeByte(type & 0xFF);
            this.sink.writeByte(flags & 0xFF);
            this.sink.writeInt(streamId & Integer.MAX_VALUE);
        }
    }
    
    static final class ContinuationSource implements Source
    {
        private final BufferedSource source;
        int length;
        byte flags;
        int streamId;
        int left;
        short padding;
        
        public ContinuationSource(final BufferedSource source) {
            this.source = source;
        }
        
        @Override
        public long read(final Buffer sink, final long byteCount) throws IOException {
            while (this.left == 0) {
                this.source.skip(this.padding);
                this.padding = 0;
                if ((this.flags & 0x4) != 0x0) {
                    return -1L;
                }
                this.readContinuationHeader();
            }
            final long read = this.source.read(sink, Math.min(byteCount, this.left));
            if (read == -1L) {
                return -1L;
            }
            this.left -= (int)read;
            return read;
        }
        
        @Override
        public Timeout timeout() {
            return this.source.timeout();
        }
        
        @Override
        public void close() throws IOException {
        }
        
        private void readContinuationHeader() throws IOException {
            final int previousStreamId = this.streamId;
            final int access$300 = readMedium(this.source);
            this.left = access$300;
            this.length = access$300;
            final byte type = (byte)(this.source.readByte() & 0xFF);
            this.flags = (byte)(this.source.readByte() & 0xFF);
            if (Http2.logger.isLoggable(Level.FINE)) {
                Http2.logger.fine(FrameLogger.formatHeader(true, this.streamId, this.length, type, this.flags));
            }
            this.streamId = (this.source.readInt() & Integer.MAX_VALUE);
            if (type != 9) {
                throw ioException("%s != TYPE_CONTINUATION", new Object[] { type });
            }
            if (this.streamId != previousStreamId) {
                throw ioException("TYPE_CONTINUATION streamId changed", new Object[0]);
            }
        }
    }
    
    static final class FrameLogger
    {
        private static final String[] TYPES;
        private static final String[] FLAGS;
        private static final String[] BINARY;
        
        static String formatHeader(final boolean inbound, final int streamId, final int length, final byte type, final byte flags) {
            final String formattedType = (type < FrameLogger.TYPES.length) ? FrameLogger.TYPES[type] : String.format("0x%02x", type);
            final String formattedFlags = formatFlags(type, flags);
            return String.format("%s 0x%08x %5d %-13s %s", inbound ? "<<" : ">>", streamId, length, formattedType, formattedFlags);
        }
        
        static String formatFlags(final byte type, final byte flags) {
            if (flags == 0) {
                return "";
            }
            switch (type) {
                case 4:
                case 6: {
                    return (flags == 1) ? "ACK" : FrameLogger.BINARY[flags];
                }
                case 2:
                case 3:
                case 7:
                case 8: {
                    return FrameLogger.BINARY[flags];
                }
                default: {
                    final String result = (flags < FrameLogger.FLAGS.length) ? FrameLogger.FLAGS[flags] : FrameLogger.BINARY[flags];
                    if (type == 5 && (flags & 0x4) != 0x0) {
                        return result.replace("HEADERS", "PUSH_PROMISE");
                    }
                    if (type == 0 && (flags & 0x20) != 0x0) {
                        return result.replace("PRIORITY", "COMPRESSED");
                    }
                    return result;
                }
            }
        }
        
        static {
            TYPES = new String[] { "DATA", "HEADERS", "PRIORITY", "RST_STREAM", "SETTINGS", "PUSH_PROMISE", "PING", "GOAWAY", "WINDOW_UPDATE", "CONTINUATION" };
            FLAGS = new String[64];
            BINARY = new String[256];
            for (int i = 0; i < FrameLogger.BINARY.length; ++i) {
                FrameLogger.BINARY[i] = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
            }
            FrameLogger.FLAGS[0] = "";
            FrameLogger.FLAGS[1] = "END_STREAM";
            final int[] prefixFlags = { 1 };
            FrameLogger.FLAGS[8] = "PADDED";
            for (final int prefixFlag : prefixFlags) {
                FrameLogger.FLAGS[prefixFlag | 0x8] = FrameLogger.FLAGS[prefixFlag] + "|PADDED";
            }
            FrameLogger.FLAGS[4] = "END_HEADERS";
            FrameLogger.FLAGS[32] = "PRIORITY";
            FrameLogger.FLAGS[36] = "END_HEADERS|PRIORITY";
            final int[] array2;
            final int[] frameFlags = array2 = new int[] { 4, 32, 36 };
            for (final int frameFlag : array2) {
                for (final int prefixFlag2 : prefixFlags) {
                    FrameLogger.FLAGS[prefixFlag2 | frameFlag] = FrameLogger.FLAGS[prefixFlag2] + '|' + FrameLogger.FLAGS[frameFlag];
                    FrameLogger.FLAGS[prefixFlag2 | frameFlag | 0x8] = FrameLogger.FLAGS[prefixFlag2] + '|' + FrameLogger.FLAGS[frameFlag] + "|PADDED";
                }
            }
            for (int j = 0; j < FrameLogger.FLAGS.length; ++j) {
                if (FrameLogger.FLAGS[j] == null) {
                    FrameLogger.FLAGS[j] = FrameLogger.BINARY[j];
                }
            }
        }
    }
}
