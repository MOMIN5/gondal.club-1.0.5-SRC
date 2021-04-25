// 
// Decompiled by Procyon v0.5.36
// 

package okio;

import java.io.OutputStream;
import java.io.EOFException;
import java.nio.charset.Charset;
import java.io.IOException;

final class RealBufferedSink implements BufferedSink
{
    public final Buffer buffer;
    public final Sink sink;
    private boolean closed;
    
    public RealBufferedSink(final Sink sink, final Buffer buffer) {
        if (sink == null) {
            throw new IllegalArgumentException("sink == null");
        }
        this.buffer = buffer;
        this.sink = sink;
    }
    
    public RealBufferedSink(final Sink sink) {
        this(sink, new Buffer());
    }
    
    @Override
    public Buffer buffer() {
        return this.buffer;
    }
    
    @Override
    public void write(final Buffer source, final long byteCount) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.write(source, byteCount);
        this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink write(final ByteString byteString) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.write(byteString);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeUtf8(final String string) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeUtf8(string);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeUtf8(final String string, final int beginIndex, final int endIndex) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeUtf8(string, beginIndex, endIndex);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeUtf8CodePoint(final int codePoint) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeUtf8CodePoint(codePoint);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeString(final String string, final Charset charset) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeString(string, charset);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeString(final String string, final int beginIndex, final int endIndex, final Charset charset) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeString(string, beginIndex, endIndex, charset);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink write(final byte[] source) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.write(source);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink write(final byte[] source, final int offset, final int byteCount) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.write(source, offset, byteCount);
        return this.emitCompleteSegments();
    }
    
    @Override
    public long writeAll(final Source source) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        long totalBytesRead = 0L;
        long readCount;
        while ((readCount = source.read(this.buffer, 2048L)) != -1L) {
            totalBytesRead += readCount;
            this.emitCompleteSegments();
        }
        return totalBytesRead;
    }
    
    @Override
    public BufferedSink write(final Source source, long byteCount) throws IOException {
        while (byteCount > 0L) {
            final long read = source.read(this.buffer, byteCount);
            if (read == -1L) {
                throw new EOFException();
            }
            byteCount -= read;
            this.emitCompleteSegments();
        }
        return this;
    }
    
    @Override
    public BufferedSink writeByte(final int b) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeByte(b);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeShort(final int s) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeShort(s);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeShortLe(final int s) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeShortLe(s);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeInt(final int i) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeInt(i);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeIntLe(final int i) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeIntLe(i);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeLong(final long v) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeLong(v);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeLongLe(final long v) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeLongLe(v);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeDecimalLong(final long v) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeDecimalLong(v);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink writeHexadecimalUnsignedLong(final long v) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        this.buffer.writeHexadecimalUnsignedLong(v);
        return this.emitCompleteSegments();
    }
    
    @Override
    public BufferedSink emitCompleteSegments() throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        final long byteCount = this.buffer.completeSegmentByteCount();
        if (byteCount > 0L) {
            this.sink.write(this.buffer, byteCount);
        }
        return this;
    }
    
    @Override
    public BufferedSink emit() throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        final long byteCount = this.buffer.size();
        if (byteCount > 0L) {
            this.sink.write(this.buffer, byteCount);
        }
        return this;
    }
    
    @Override
    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                if (RealBufferedSink.this.closed) {
                    throw new IOException("closed");
                }
                RealBufferedSink.this.buffer.writeByte((int)(byte)b);
                RealBufferedSink.this.emitCompleteSegments();
            }
            
            @Override
            public void write(final byte[] data, final int offset, final int byteCount) throws IOException {
                if (RealBufferedSink.this.closed) {
                    throw new IOException("closed");
                }
                RealBufferedSink.this.buffer.write(data, offset, byteCount);
                RealBufferedSink.this.emitCompleteSegments();
            }
            
            @Override
            public void flush() throws IOException {
                if (!RealBufferedSink.this.closed) {
                    RealBufferedSink.this.flush();
                }
            }
            
            @Override
            public void close() throws IOException {
                RealBufferedSink.this.close();
            }
            
            @Override
            public String toString() {
                return RealBufferedSink.this + ".outputStream()";
            }
        };
    }
    
    @Override
    public void flush() throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        if (this.buffer.size > 0L) {
            this.sink.write(this.buffer, this.buffer.size);
        }
        this.sink.flush();
    }
    
    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        Throwable thrown = null;
        try {
            if (this.buffer.size > 0L) {
                this.sink.write(this.buffer, this.buffer.size);
            }
        }
        catch (Throwable e) {
            thrown = e;
        }
        try {
            this.sink.close();
        }
        catch (Throwable e) {
            if (thrown == null) {
                thrown = e;
            }
        }
        this.closed = true;
        if (thrown != null) {
            Util.sneakyRethrow(thrown);
        }
    }
    
    @Override
    public Timeout timeout() {
        return this.sink.timeout();
    }
    
    @Override
    public String toString() {
        return "buffer(" + this.sink + ")";
    }
}
