// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.framed;

import java.io.Closeable;
import okio.Source;
import okio.Okio;
import okio.Sink;
import okio.DeflaterSink;
import java.util.zip.Deflater;
import okio.Buffer;
import okio.ByteString;
import java.util.List;
import java.net.ProtocolException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import com.squareup.okhttp.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import com.squareup.okhttp.Protocol;

public final class Spdy3 implements Variant
{
    static final int TYPE_DATA = 0;
    static final int TYPE_SYN_STREAM = 1;
    static final int TYPE_SYN_REPLY = 2;
    static final int TYPE_RST_STREAM = 3;
    static final int TYPE_SETTINGS = 4;
    static final int TYPE_PING = 6;
    static final int TYPE_GOAWAY = 7;
    static final int TYPE_HEADERS = 8;
    static final int TYPE_WINDOW_UPDATE = 9;
    static final int FLAG_FIN = 1;
    static final int FLAG_UNIDIRECTIONAL = 2;
    static final int VERSION = 3;
    static final byte[] DICTIONARY;
    
    @Override
    public Protocol getProtocol() {
        return Protocol.SPDY_3;
    }
    
    @Override
    public FrameReader newReader(final BufferedSource source, final boolean client) {
        return new Reader(source, client);
    }
    
    @Override
    public FrameWriter newWriter(final BufferedSink sink, final boolean client) {
        return new Writer(sink, client);
    }
    
    static {
        try {
            DICTIONARY = "\u0000\u0000\u0000\u0007options\u0000\u0000\u0000\u0004head\u0000\u0000\u0000\u0004post\u0000\u0000\u0000\u0003put\u0000\u0000\u0000\u0006delete\u0000\u0000\u0000\u0005trace\u0000\u0000\u0000\u0006accept\u0000\u0000\u0000\u000eaccept-charset\u0000\u0000\u0000\u000faccept-encoding\u0000\u0000\u0000\u000faccept-language\u0000\u0000\u0000\raccept-ranges\u0000\u0000\u0000\u0003age\u0000\u0000\u0000\u0005allow\u0000\u0000\u0000\rauthorization\u0000\u0000\u0000\rcache-control\u0000\u0000\u0000\nconnection\u0000\u0000\u0000\fcontent-base\u0000\u0000\u0000\u0010content-encoding\u0000\u0000\u0000\u0010content-language\u0000\u0000\u0000\u000econtent-length\u0000\u0000\u0000\u0010content-location\u0000\u0000\u0000\u000bcontent-md5\u0000\u0000\u0000\rcontent-range\u0000\u0000\u0000\fcontent-type\u0000\u0000\u0000\u0004date\u0000\u0000\u0000\u0004etag\u0000\u0000\u0000\u0006expect\u0000\u0000\u0000\u0007expires\u0000\u0000\u0000\u0004from\u0000\u0000\u0000\u0004host\u0000\u0000\u0000\bif-match\u0000\u0000\u0000\u0011if-modified-since\u0000\u0000\u0000\rif-none-match\u0000\u0000\u0000\bif-range\u0000\u0000\u0000\u0013if-unmodified-since\u0000\u0000\u0000\rlast-modified\u0000\u0000\u0000\blocation\u0000\u0000\u0000\fmax-forwards\u0000\u0000\u0000\u0006pragma\u0000\u0000\u0000\u0012proxy-authenticate\u0000\u0000\u0000\u0013proxy-authorization\u0000\u0000\u0000\u0005range\u0000\u0000\u0000\u0007referer\u0000\u0000\u0000\u000bretry-after\u0000\u0000\u0000\u0006server\u0000\u0000\u0000\u0002te\u0000\u0000\u0000\u0007trailer\u0000\u0000\u0000\u0011transfer-encoding\u0000\u0000\u0000\u0007upgrade\u0000\u0000\u0000\nuser-agent\u0000\u0000\u0000\u0004vary\u0000\u0000\u0000\u0003via\u0000\u0000\u0000\u0007warning\u0000\u0000\u0000\u0010www-authenticate\u0000\u0000\u0000\u0006method\u0000\u0000\u0000\u0003get\u0000\u0000\u0000\u0006status\u0000\u0000\u0000\u0006200 OK\u0000\u0000\u0000\u0007version\u0000\u0000\u0000\bHTTP/1.1\u0000\u0000\u0000\u0003url\u0000\u0000\u0000\u0006public\u0000\u0000\u0000\nset-cookie\u0000\u0000\u0000\nkeep-alive\u0000\u0000\u0000\u0006origin100101201202205206300302303304305306307402405406407408409410411412413414415416417502504505203 Non-Authoritative Information204 No Content301 Moved Permanently400 Bad Request401 Unauthorized403 Forbidden404 Not Found500 Internal Server Error501 Not Implemented503 Service UnavailableJan Feb Mar Apr May Jun Jul Aug Sept Oct Nov Dec 00:00:00 Mon, Tue, Wed, Thu, Fri, Sat, Sun, GMTchunked,text/html,image/png,image/jpg,image/gif,application/xml,application/xhtml+xml,text/plain,text/javascript,publicprivatemax-age=gzip,deflate,sdchcharset=utf-8charset=iso-8859-1,utf-,*,enq=0.".getBytes(Util.UTF_8.name());
        }
        catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }
    
    static final class Reader implements FrameReader
    {
        private final BufferedSource source;
        private final boolean client;
        private final NameValueBlockReader headerBlockReader;
        
        Reader(final BufferedSource source, final boolean client) {
            this.source = source;
            this.headerBlockReader = new NameValueBlockReader(this.source);
            this.client = client;
        }
        
        @Override
        public void readConnectionPreface() {
        }
        
        @Override
        public boolean nextFrame(final Handler handler) throws IOException {
            int w1;
            int w2;
            try {
                w1 = this.source.readInt();
                w2 = this.source.readInt();
            }
            catch (IOException e) {
                return false;
            }
            final boolean control = (w1 & Integer.MIN_VALUE) != 0x0;
            final int flags = (w2 & 0xFF000000) >>> 24;
            final int length = w2 & 0xFFFFFF;
            if (!control) {
                final int streamId = w1 & Integer.MAX_VALUE;
                final boolean inFinished = (flags & 0x1) != 0x0;
                handler.data(inFinished, streamId, this.source, length);
                return true;
            }
            final int version = (w1 & 0x7FFF0000) >>> 16;
            final int type = w1 & 0xFFFF;
            if (version != 3) {
                throw new ProtocolException("version != 3: " + version);
            }
            switch (type) {
                case 1: {
                    this.readSynStream(handler, flags, length);
                    return true;
                }
                case 2: {
                    this.readSynReply(handler, flags, length);
                    return true;
                }
                case 3: {
                    this.readRstStream(handler, flags, length);
                    return true;
                }
                case 4: {
                    this.readSettings(handler, flags, length);
                    return true;
                }
                case 6: {
                    this.readPing(handler, flags, length);
                    return true;
                }
                case 7: {
                    this.readGoAway(handler, flags, length);
                    return true;
                }
                case 8: {
                    this.readHeaders(handler, flags, length);
                    return true;
                }
                case 9: {
                    this.readWindowUpdate(handler, flags, length);
                    return true;
                }
                default: {
                    this.source.skip(length);
                    return true;
                }
            }
        }
        
        private void readSynStream(final Handler handler, final int flags, final int length) throws IOException {
            final int w1 = this.source.readInt();
            final int w2 = this.source.readInt();
            final int streamId = w1 & Integer.MAX_VALUE;
            final int associatedStreamId = w2 & Integer.MAX_VALUE;
            this.source.readShort();
            final List<Header> headerBlock = this.headerBlockReader.readNameValueBlock(length - 10);
            final boolean inFinished = (flags & 0x1) != 0x0;
            final boolean outFinished = (flags & 0x2) != 0x0;
            handler.headers(outFinished, inFinished, streamId, associatedStreamId, headerBlock, HeadersMode.SPDY_SYN_STREAM);
        }
        
        private void readSynReply(final Handler handler, final int flags, final int length) throws IOException {
            final int w1 = this.source.readInt();
            final int streamId = w1 & Integer.MAX_VALUE;
            final List<Header> headerBlock = this.headerBlockReader.readNameValueBlock(length - 4);
            final boolean inFinished = (flags & 0x1) != 0x0;
            handler.headers(false, inFinished, streamId, -1, headerBlock, HeadersMode.SPDY_REPLY);
        }
        
        private void readRstStream(final Handler handler, final int flags, final int length) throws IOException {
            if (length != 8) {
                throw ioException("TYPE_RST_STREAM length: %d != 8", length);
            }
            final int streamId = this.source.readInt() & Integer.MAX_VALUE;
            final int errorCodeInt = this.source.readInt();
            final ErrorCode errorCode = ErrorCode.fromSpdy3Rst(errorCodeInt);
            if (errorCode == null) {
                throw ioException("TYPE_RST_STREAM unexpected error code: %d", errorCodeInt);
            }
            handler.rstStream(streamId, errorCode);
        }
        
        private void readHeaders(final Handler handler, final int flags, final int length) throws IOException {
            final int w1 = this.source.readInt();
            final int streamId = w1 & Integer.MAX_VALUE;
            final List<Header> headerBlock = this.headerBlockReader.readNameValueBlock(length - 4);
            handler.headers(false, false, streamId, -1, headerBlock, HeadersMode.SPDY_HEADERS);
        }
        
        private void readWindowUpdate(final Handler handler, final int flags, final int length) throws IOException {
            if (length != 8) {
                throw ioException("TYPE_WINDOW_UPDATE length: %d != 8", length);
            }
            final int w1 = this.source.readInt();
            final int w2 = this.source.readInt();
            final int streamId = w1 & Integer.MAX_VALUE;
            final long increment = w2 & Integer.MAX_VALUE;
            if (increment == 0L) {
                throw ioException("windowSizeIncrement was 0", increment);
            }
            handler.windowUpdate(streamId, increment);
        }
        
        private void readPing(final Handler handler, final int flags, final int length) throws IOException {
            if (length != 4) {
                throw ioException("TYPE_PING length: %d != 4", length);
            }
            final int id = this.source.readInt();
            final boolean ack = this.client == ((id & 0x1) == 0x1);
            handler.ping(ack, id, 0);
        }
        
        private void readGoAway(final Handler handler, final int flags, final int length) throws IOException {
            if (length != 8) {
                throw ioException("TYPE_GOAWAY length: %d != 8", length);
            }
            final int lastGoodStreamId = this.source.readInt() & Integer.MAX_VALUE;
            final int errorCodeInt = this.source.readInt();
            final ErrorCode errorCode = ErrorCode.fromSpdyGoAway(errorCodeInt);
            if (errorCode == null) {
                throw ioException("TYPE_GOAWAY unexpected error code: %d", errorCodeInt);
            }
            handler.goAway(lastGoodStreamId, errorCode, ByteString.EMPTY);
        }
        
        private void readSettings(final Handler handler, final int flags, final int length) throws IOException {
            final int numberOfEntries = this.source.readInt();
            if (length != 4 + 8 * numberOfEntries) {
                throw ioException("TYPE_SETTINGS length: %d != 4 + 8 * %d", length, numberOfEntries);
            }
            final Settings settings = new Settings();
            for (int i = 0; i < numberOfEntries; ++i) {
                final int w1 = this.source.readInt();
                final int value = this.source.readInt();
                final int idFlags = (w1 & 0xFF000000) >>> 24;
                final int id = w1 & 0xFFFFFF;
                settings.set(id, idFlags, value);
            }
            final boolean clearPrevious = (flags & 0x1) != 0x0;
            handler.settings(clearPrevious, settings);
        }
        
        private static IOException ioException(final String message, final Object... args) throws IOException {
            throw new IOException(String.format(message, args));
        }
        
        @Override
        public void close() throws IOException {
            this.headerBlockReader.close();
        }
    }
    
    static final class Writer implements FrameWriter
    {
        private final BufferedSink sink;
        private final Buffer headerBlockBuffer;
        private final BufferedSink headerBlockOut;
        private final boolean client;
        private boolean closed;
        
        Writer(final BufferedSink sink, final boolean client) {
            this.sink = sink;
            this.client = client;
            final Deflater deflater = new Deflater();
            deflater.setDictionary(Spdy3.DICTIONARY);
            this.headerBlockBuffer = new Buffer();
            this.headerBlockOut = Okio.buffer(new DeflaterSink((Sink)this.headerBlockBuffer, deflater));
        }
        
        @Override
        public void ackSettings(final Settings peerSettings) {
        }
        
        @Override
        public void pushPromise(final int streamId, final int promisedStreamId, final List<Header> requestHeaders) throws IOException {
        }
        
        @Override
        public synchronized void connectionPreface() {
        }
        
        @Override
        public synchronized void flush() throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.sink.flush();
        }
        
        @Override
        public synchronized void synStream(final boolean outFinished, final boolean inFinished, final int streamId, final int associatedStreamId, final List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.writeNameValueBlockToBuffer(headerBlock);
            final int length = (int)(10L + this.headerBlockBuffer.size());
            final int type = 1;
            final int flags = (outFinished ? 1 : 0) | (inFinished ? 2 : 0);
            final int unused = 0;
            this.sink.writeInt(0x80030000 | (type & 0xFFFF));
            this.sink.writeInt((flags & 0xFF) << 24 | (length & 0xFFFFFF));
            this.sink.writeInt(streamId & Integer.MAX_VALUE);
            this.sink.writeInt(associatedStreamId & Integer.MAX_VALUE);
            this.sink.writeShort((unused & 0x7) << 13 | (unused & 0x1F) << 8 | (unused & 0xFF));
            this.sink.writeAll(this.headerBlockBuffer);
            this.sink.flush();
        }
        
        @Override
        public synchronized void synReply(final boolean outFinished, final int streamId, final List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.writeNameValueBlockToBuffer(headerBlock);
            final int type = 2;
            final int flags = outFinished ? 1 : 0;
            final int length = (int)(this.headerBlockBuffer.size() + 4L);
            this.sink.writeInt(0x80030000 | (type & 0xFFFF));
            this.sink.writeInt((flags & 0xFF) << 24 | (length & 0xFFFFFF));
            this.sink.writeInt(streamId & Integer.MAX_VALUE);
            this.sink.writeAll(this.headerBlockBuffer);
            this.sink.flush();
        }
        
        @Override
        public synchronized void headers(final int streamId, final List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.writeNameValueBlockToBuffer(headerBlock);
            final int flags = 0;
            final int type = 8;
            final int length = (int)(this.headerBlockBuffer.size() + 4L);
            this.sink.writeInt(0x80030000 | (type & 0xFFFF));
            this.sink.writeInt((flags & 0xFF) << 24 | (length & 0xFFFFFF));
            this.sink.writeInt(streamId & Integer.MAX_VALUE);
            this.sink.writeAll(this.headerBlockBuffer);
        }
        
        @Override
        public synchronized void rstStream(final int streamId, final ErrorCode errorCode) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            if (errorCode.spdyRstCode == -1) {
                throw new IllegalArgumentException();
            }
            final int flags = 0;
            final int type = 3;
            final int length = 8;
            this.sink.writeInt(0x80030000 | (type & 0xFFFF));
            this.sink.writeInt((flags & 0xFF) << 24 | (length & 0xFFFFFF));
            this.sink.writeInt(streamId & Integer.MAX_VALUE);
            this.sink.writeInt(errorCode.spdyRstCode);
            this.sink.flush();
        }
        
        @Override
        public int maxDataLength() {
            return 16383;
        }
        
        @Override
        public synchronized void data(final boolean outFinished, final int streamId, final Buffer source, final int byteCount) throws IOException {
            final int flags = outFinished ? 1 : 0;
            this.sendDataFrame(streamId, flags, source, byteCount);
        }
        
        void sendDataFrame(final int streamId, final int flags, final Buffer buffer, final int byteCount) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            if (byteCount > 16777215L) {
                throw new IllegalArgumentException("FRAME_TOO_LARGE max size is 16Mib: " + byteCount);
            }
            this.sink.writeInt(streamId & Integer.MAX_VALUE);
            this.sink.writeInt((flags & 0xFF) << 24 | (byteCount & 0xFFFFFF));
            if (byteCount > 0) {
                this.sink.write(buffer, byteCount);
            }
        }
        
        private void writeNameValueBlockToBuffer(final List<Header> headerBlock) throws IOException {
            this.headerBlockOut.writeInt(headerBlock.size());
            for (int i = 0, size = headerBlock.size(); i < size; ++i) {
                final ByteString name = headerBlock.get(i).name;
                this.headerBlockOut.writeInt(name.size());
                this.headerBlockOut.write(name);
                final ByteString value = headerBlock.get(i).value;
                this.headerBlockOut.writeInt(value.size());
                this.headerBlockOut.write(value);
            }
            this.headerBlockOut.flush();
        }
        
        @Override
        public synchronized void settings(final Settings settings) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            final int type = 4;
            final int flags = 0;
            final int size = settings.size();
            final int length = 4 + size * 8;
            this.sink.writeInt(0x80030000 | (type & 0xFFFF));
            this.sink.writeInt((flags & 0xFF) << 24 | (length & 0xFFFFFF));
            this.sink.writeInt(size);
            for (int i = 0; i <= 10; ++i) {
                if (settings.isSet(i)) {
                    final int settingsFlags = settings.flags(i);
                    this.sink.writeInt((settingsFlags & 0xFF) << 24 | (i & 0xFFFFFF));
                    this.sink.writeInt(settings.get(i));
                }
            }
            this.sink.flush();
        }
        
        @Override
        public synchronized void ping(final boolean reply, final int payload1, final int payload2) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            final boolean payloadIsReply = this.client != ((payload1 & 0x1) == 0x1);
            if (reply != payloadIsReply) {
                throw new IllegalArgumentException("payload != reply");
            }
            final int type = 6;
            final int flags = 0;
            final int length = 4;
            this.sink.writeInt(0x80030000 | (type & 0xFFFF));
            this.sink.writeInt((flags & 0xFF) << 24 | (length & 0xFFFFFF));
            this.sink.writeInt(payload1);
            this.sink.flush();
        }
        
        @Override
        public synchronized void goAway(final int lastGoodStreamId, final ErrorCode errorCode, final byte[] ignored) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            if (errorCode.spdyGoAwayCode == -1) {
                throw new IllegalArgumentException("errorCode.spdyGoAwayCode == -1");
            }
            final int type = 7;
            final int flags = 0;
            final int length = 8;
            this.sink.writeInt(0x80030000 | (type & 0xFFFF));
            this.sink.writeInt((flags & 0xFF) << 24 | (length & 0xFFFFFF));
            this.sink.writeInt(lastGoodStreamId);
            this.sink.writeInt(errorCode.spdyGoAwayCode);
            this.sink.flush();
        }
        
        @Override
        public synchronized void windowUpdate(final int streamId, final long increment) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            if (increment == 0L || increment > 2147483647L) {
                throw new IllegalArgumentException("windowSizeIncrement must be between 1 and 0x7fffffff: " + increment);
            }
            final int type = 9;
            final int flags = 0;
            final int length = 8;
            this.sink.writeInt(0x80030000 | (type & 0xFFFF));
            this.sink.writeInt((flags & 0xFF) << 24 | (length & 0xFFFFFF));
            this.sink.writeInt(streamId);
            this.sink.writeInt((int)increment);
            this.sink.flush();
        }
        
        @Override
        public synchronized void close() throws IOException {
            this.closed = true;
            Util.closeAll(this.sink, this.headerBlockOut);
        }
    }
}
