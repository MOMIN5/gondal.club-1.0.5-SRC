// 
// Decompiled by Procyon v0.5.36
// 

package okio;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.nio.charset.Charset;
import java.io.IOException;
import java.io.InputStream;
import java.io.EOFException;
import java.io.OutputStream;

public final class Buffer implements BufferedSource, BufferedSink, Cloneable
{
    private static final byte[] DIGITS;
    static final int REPLACEMENT_CHARACTER = 65533;
    Segment head;
    long size;
    
    public long size() {
        return this.size;
    }
    
    @Override
    public Buffer buffer() {
        return this;
    }
    
    @Override
    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(final int b) {
                Buffer.this.writeByte((int)(byte)b);
            }
            
            @Override
            public void write(final byte[] data, final int offset, final int byteCount) {
                Buffer.this.write(data, offset, byteCount);
            }
            
            @Override
            public void flush() {
            }
            
            @Override
            public void close() {
            }
            
            @Override
            public String toString() {
                return this + ".outputStream()";
            }
        };
    }
    
    @Override
    public Buffer emitCompleteSegments() {
        return this;
    }
    
    @Override
    public BufferedSink emit() {
        return this;
    }
    
    @Override
    public boolean exhausted() {
        return this.size == 0L;
    }
    
    @Override
    public void require(final long byteCount) throws EOFException {
        if (this.size < byteCount) {
            throw new EOFException();
        }
    }
    
    @Override
    public boolean request(final long byteCount) {
        return this.size >= byteCount;
    }
    
    @Override
    public InputStream inputStream() {
        return new InputStream() {
            @Override
            public int read() {
                if (Buffer.this.size > 0L) {
                    return Buffer.this.readByte() & 0xFF;
                }
                return -1;
            }
            
            @Override
            public int read(final byte[] sink, final int offset, final int byteCount) {
                return Buffer.this.read(sink, offset, byteCount);
            }
            
            @Override
            public int available() {
                return (int)Math.min(Buffer.this.size, 2147483647L);
            }
            
            @Override
            public void close() {
            }
            
            @Override
            public String toString() {
                return Buffer.this + ".inputStream()";
            }
        };
    }
    
    public Buffer copyTo(final OutputStream out) throws IOException {
        return this.copyTo(out, 0L, this.size);
    }
    
    public Buffer copyTo(final OutputStream out, long offset, long byteCount) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        }
        Util.checkOffsetAndCount(this.size, offset, byteCount);
        if (byteCount == 0L) {
            return this;
        }
        Segment s;
        for (s = this.head; offset >= s.limit - s.pos; offset -= s.limit - s.pos, s = s.next) {}
        while (byteCount > 0L) {
            final int pos = (int)(s.pos + offset);
            final int toCopy = (int)Math.min(s.limit - pos, byteCount);
            out.write(s.data, pos, toCopy);
            byteCount -= toCopy;
            offset = 0L;
            s = s.next;
        }
        return this;
    }
    
    public Buffer copyTo(final Buffer out, long offset, long byteCount) {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        }
        Util.checkOffsetAndCount(this.size, offset, byteCount);
        if (byteCount == 0L) {
            return this;
        }
        out.size += byteCount;
        Segment s;
        for (s = this.head; offset >= s.limit - s.pos; offset -= s.limit - s.pos, s = s.next) {}
        while (byteCount > 0L) {
            final Segment segment;
            final Segment copy = segment = new Segment(s);
            segment.pos += (int)offset;
            copy.limit = Math.min(copy.pos + (int)byteCount, copy.limit);
            if (out.head == null) {
                final Segment segment2 = copy;
                final Segment segment3 = copy;
                final Segment head = copy;
                segment3.prev = head;
                segment2.next = head;
                out.head = head;
            }
            else {
                out.head.prev.push(copy);
            }
            byteCount -= copy.limit - copy.pos;
            offset = 0L;
            s = s.next;
        }
        return this;
    }
    
    public Buffer writeTo(final OutputStream out) throws IOException {
        return this.writeTo(out, this.size);
    }
    
    public Buffer writeTo(final OutputStream out, long byteCount) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        }
        Util.checkOffsetAndCount(this.size, 0L, byteCount);
        Segment s = this.head;
        while (byteCount > 0L) {
            final int toCopy = (int)Math.min(byteCount, s.limit - s.pos);
            out.write(s.data, s.pos, toCopy);
            final Segment segment = s;
            segment.pos += toCopy;
            this.size -= toCopy;
            byteCount -= toCopy;
            if (s.pos == s.limit) {
                final Segment toRecycle = s;
                s = (this.head = toRecycle.pop());
                SegmentPool.recycle(toRecycle);
            }
        }
        return this;
    }
    
    public Buffer readFrom(final InputStream in) throws IOException {
        this.readFrom(in, Long.MAX_VALUE, true);
        return this;
    }
    
    public Buffer readFrom(final InputStream in, final long byteCount) throws IOException {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        this.readFrom(in, byteCount, false);
        return this;
    }
    
    private void readFrom(final InputStream in, long byteCount, final boolean forever) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in == null");
        }
        while (byteCount > 0L || forever) {
            final Segment tail = this.writableSegment(1);
            final int maxToCopy = (int)Math.min(byteCount, 2048 - tail.limit);
            final int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
            if (bytesRead == -1) {
                if (forever) {
                    return;
                }
                throw new EOFException();
            }
            else {
                final Segment segment = tail;
                segment.limit += bytesRead;
                this.size += bytesRead;
                byteCount -= bytesRead;
            }
        }
    }
    
    public long completeSegmentByteCount() {
        long result = this.size;
        if (result == 0L) {
            return 0L;
        }
        final Segment tail = this.head.prev;
        if (tail.limit < 2048 && tail.owner) {
            result -= tail.limit - tail.pos;
        }
        return result;
    }
    
    @Override
    public byte readByte() {
        if (this.size == 0L) {
            throw new IllegalStateException("size == 0");
        }
        final Segment segment = this.head;
        int pos = segment.pos;
        final int limit = segment.limit;
        final byte[] data = segment.data;
        final byte b = data[pos++];
        --this.size;
        if (pos == limit) {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        else {
            segment.pos = pos;
        }
        return b;
    }
    
    public byte getByte(long pos) {
        Util.checkOffsetAndCount(this.size, pos, 1L);
        Segment s = this.head;
        while (true) {
            final int segmentByteCount = s.limit - s.pos;
            if (pos < segmentByteCount) {
                break;
            }
            pos -= segmentByteCount;
            s = s.next;
        }
        return s.data[s.pos + (int)pos];
    }
    
    @Override
    public short readShort() {
        if (this.size < 2L) {
            throw new IllegalStateException("size < 2: " + this.size);
        }
        final Segment segment = this.head;
        int pos = segment.pos;
        final int limit = segment.limit;
        if (limit - pos < 2) {
            final int s = (this.readByte() & 0xFF) << 8 | (this.readByte() & 0xFF);
            return (short)s;
        }
        final byte[] data = segment.data;
        final int s2 = (data[pos++] & 0xFF) << 8 | (data[pos++] & 0xFF);
        this.size -= 2L;
        if (pos == limit) {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        else {
            segment.pos = pos;
        }
        return (short)s2;
    }
    
    @Override
    public int readInt() {
        if (this.size < 4L) {
            throw new IllegalStateException("size < 4: " + this.size);
        }
        final Segment segment = this.head;
        int pos = segment.pos;
        final int limit = segment.limit;
        if (limit - pos < 4) {
            return (this.readByte() & 0xFF) << 24 | (this.readByte() & 0xFF) << 16 | (this.readByte() & 0xFF) << 8 | (this.readByte() & 0xFF);
        }
        final byte[] data = segment.data;
        final int i = (data[pos++] & 0xFF) << 24 | (data[pos++] & 0xFF) << 16 | (data[pos++] & 0xFF) << 8 | (data[pos++] & 0xFF);
        this.size -= 4L;
        if (pos == limit) {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        else {
            segment.pos = pos;
        }
        return i;
    }
    
    @Override
    public long readLong() {
        if (this.size < 8L) {
            throw new IllegalStateException("size < 8: " + this.size);
        }
        final Segment segment = this.head;
        int pos = segment.pos;
        final int limit = segment.limit;
        if (limit - pos < 8) {
            return ((long)this.readInt() & 0xFFFFFFFFL) << 32 | ((long)this.readInt() & 0xFFFFFFFFL);
        }
        final byte[] data = segment.data;
        final long v = ((long)data[pos++] & 0xFFL) << 56 | ((long)data[pos++] & 0xFFL) << 48 | ((long)data[pos++] & 0xFFL) << 40 | ((long)data[pos++] & 0xFFL) << 32 | ((long)data[pos++] & 0xFFL) << 24 | ((long)data[pos++] & 0xFFL) << 16 | ((long)data[pos++] & 0xFFL) << 8 | ((long)data[pos++] & 0xFFL);
        this.size -= 8L;
        if (pos == limit) {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        else {
            segment.pos = pos;
        }
        return v;
    }
    
    @Override
    public short readShortLe() {
        return Util.reverseBytesShort(this.readShort());
    }
    
    @Override
    public int readIntLe() {
        return Util.reverseBytesInt(this.readInt());
    }
    
    @Override
    public long readLongLe() {
        return Util.reverseBytesLong(this.readLong());
    }
    
    @Override
    public long readDecimalLong() {
        if (this.size == 0L) {
            throw new IllegalStateException("size == 0");
        }
        long value = 0L;
        int seen = 0;
        boolean negative = false;
        boolean done = false;
        final long overflowZone = -922337203685477580L;
        long overflowDigit = -7L;
        do {
            final Segment segment = this.head;
            final byte[] data = segment.data;
            int pos;
            int limit;
            for (pos = segment.pos, limit = segment.limit; pos < limit; ++pos, ++seen) {
                final byte b = data[pos];
                if (b >= 48 && b <= 57) {
                    final int digit = 48 - b;
                    if (value < overflowZone || (value == overflowZone && digit < overflowDigit)) {
                        final Buffer buffer = new Buffer().writeDecimalLong(value).writeByte((int)b);
                        if (!negative) {
                            buffer.readByte();
                        }
                        throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                    }
                    value *= 10L;
                    value += digit;
                }
                else if (b == 45 && seen == 0) {
                    negative = true;
                    --overflowDigit;
                }
                else {
                    if (seen == 0) {
                        throw new NumberFormatException("Expected leading [0-9] or '-' character but was 0x" + Integer.toHexString(b));
                    }
                    done = true;
                    break;
                }
            }
            if (pos == limit) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            }
            else {
                segment.pos = pos;
            }
        } while (!done && this.head != null);
        this.size -= seen;
        return negative ? value : (-value);
    }
    
    @Override
    public long readHexadecimalUnsignedLong() {
        if (this.size == 0L) {
            throw new IllegalStateException("size == 0");
        }
        long value = 0L;
        int seen = 0;
        boolean done = false;
        do {
            final Segment segment = this.head;
            final byte[] data = segment.data;
            int pos;
            int limit;
            for (pos = segment.pos, limit = segment.limit; pos < limit; ++pos, ++seen) {
                final byte b = data[pos];
                int digit;
                if (b >= 48 && b <= 57) {
                    digit = b - 48;
                }
                else if (b >= 97 && b <= 102) {
                    digit = b - 97 + 10;
                }
                else if (b >= 65 && b <= 70) {
                    digit = b - 65 + 10;
                }
                else {
                    if (seen == 0) {
                        throw new NumberFormatException("Expected leading [0-9a-fA-F] character but was 0x" + Integer.toHexString(b));
                    }
                    done = true;
                    break;
                }
                if ((value & 0xF000000000000000L) != 0x0L) {
                    final Buffer buffer = new Buffer().writeHexadecimalUnsignedLong(value).writeByte((int)b);
                    throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                }
                value <<= 4;
                value |= digit;
            }
            if (pos == limit) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            }
            else {
                segment.pos = pos;
            }
        } while (!done && this.head != null);
        this.size -= seen;
        return value;
    }
    
    @Override
    public ByteString readByteString() {
        return new ByteString(this.readByteArray());
    }
    
    @Override
    public ByteString readByteString(final long byteCount) throws EOFException {
        return new ByteString(this.readByteArray(byteCount));
    }
    
    @Override
    public void readFully(final Buffer sink, final long byteCount) throws EOFException {
        if (this.size < byteCount) {
            sink.write(this, this.size);
            throw new EOFException();
        }
        sink.write(this, byteCount);
    }
    
    @Override
    public long readAll(final Sink sink) throws IOException {
        final long byteCount = this.size;
        if (byteCount > 0L) {
            sink.write(this, byteCount);
        }
        return byteCount;
    }
    
    @Override
    public String readUtf8() {
        try {
            return this.readString(this.size, Util.UTF_8);
        }
        catch (EOFException e) {
            throw new AssertionError((Object)e);
        }
    }
    
    @Override
    public String readUtf8(final long byteCount) throws EOFException {
        return this.readString(byteCount, Util.UTF_8);
    }
    
    @Override
    public String readString(final Charset charset) {
        try {
            return this.readString(this.size, charset);
        }
        catch (EOFException e) {
            throw new AssertionError((Object)e);
        }
    }
    
    @Override
    public String readString(final long byteCount, final Charset charset) throws EOFException {
        Util.checkOffsetAndCount(this.size, 0L, byteCount);
        if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        }
        if (byteCount > 2147483647L) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        }
        if (byteCount == 0L) {
            return "";
        }
        final Segment s = this.head;
        if (s.pos + byteCount > s.limit) {
            return new String(this.readByteArray(byteCount), charset);
        }
        final String result = new String(s.data, s.pos, (int)byteCount, charset);
        final Segment segment = s;
        segment.pos += (int)byteCount;
        this.size -= byteCount;
        if (s.pos == s.limit) {
            this.head = s.pop();
            SegmentPool.recycle(s);
        }
        return result;
    }
    
    @Override
    public String readUtf8Line() throws EOFException {
        final long newline = this.indexOf((byte)10);
        if (newline == -1L) {
            return (this.size != 0L) ? this.readUtf8(this.size) : null;
        }
        return this.readUtf8Line(newline);
    }
    
    @Override
    public String readUtf8LineStrict() throws EOFException {
        final long newline = this.indexOf((byte)10);
        if (newline == -1L) {
            final Buffer data = new Buffer();
            this.copyTo(data, 0L, Math.min(32L, this.size));
            throw new EOFException("\\n not found: size=" + this.size() + " content=" + data.readByteString().hex() + "...");
        }
        return this.readUtf8Line(newline);
    }
    
    String readUtf8Line(final long newline) throws EOFException {
        if (newline > 0L && this.getByte(newline - 1L) == 13) {
            final String result = this.readUtf8(newline - 1L);
            this.skip(2L);
            return result;
        }
        final String result = this.readUtf8(newline);
        this.skip(1L);
        return result;
    }
    
    @Override
    public int readUtf8CodePoint() throws EOFException {
        if (this.size == 0L) {
            throw new EOFException();
        }
        final byte b0 = this.getByte(0L);
        int codePoint;
        int byteCount;
        int min;
        if ((b0 & 0x80) == 0x0) {
            codePoint = (b0 & 0x7F);
            byteCount = 1;
            min = 0;
        }
        else if ((b0 & 0xE0) == 0xC0) {
            codePoint = (b0 & 0x1F);
            byteCount = 2;
            min = 128;
        }
        else if ((b0 & 0xF0) == 0xE0) {
            codePoint = (b0 & 0xF);
            byteCount = 3;
            min = 2048;
        }
        else {
            if ((b0 & 0xF8) != 0xF0) {
                this.skip(1L);
                return 65533;
            }
            codePoint = (b0 & 0x7);
            byteCount = 4;
            min = 65536;
        }
        if (this.size < byteCount) {
            throw new EOFException("size < " + byteCount + ": " + this.size + " (to read code point prefixed 0x" + Integer.toHexString(b0) + ")");
        }
        for (int i = 1; i < byteCount; ++i) {
            final byte b2 = this.getByte(i);
            if ((b2 & 0xC0) != 0x80) {
                this.skip(i);
                return 65533;
            }
            codePoint <<= 6;
            codePoint |= (b2 & 0x3F);
        }
        this.skip(byteCount);
        if (codePoint > 1114111) {
            return 65533;
        }
        if (codePoint >= 55296 && codePoint <= 57343) {
            return 65533;
        }
        if (codePoint < min) {
            return 65533;
        }
        return codePoint;
    }
    
    @Override
    public byte[] readByteArray() {
        try {
            return this.readByteArray(this.size);
        }
        catch (EOFException e) {
            throw new AssertionError((Object)e);
        }
    }
    
    @Override
    public byte[] readByteArray(final long byteCount) throws EOFException {
        Util.checkOffsetAndCount(this.size, 0L, byteCount);
        if (byteCount > 2147483647L) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        }
        final byte[] result = new byte[(int)byteCount];
        this.readFully(result);
        return result;
    }
    
    @Override
    public int read(final byte[] sink) {
        return this.read(sink, 0, sink.length);
    }
    
    @Override
    public void readFully(final byte[] sink) throws EOFException {
        int read;
        for (int offset = 0; offset < sink.length; offset += read) {
            read = this.read(sink, offset, sink.length - offset);
            if (read == -1) {
                throw new EOFException();
            }
        }
    }
    
    @Override
    public int read(final byte[] sink, final int offset, final int byteCount) {
        Util.checkOffsetAndCount(sink.length, offset, byteCount);
        final Segment s = this.head;
        if (s == null) {
            return -1;
        }
        final int toCopy = Math.min(byteCount, s.limit - s.pos);
        System.arraycopy(s.data, s.pos, sink, offset, toCopy);
        final Segment segment = s;
        segment.pos += toCopy;
        this.size -= toCopy;
        if (s.pos == s.limit) {
            this.head = s.pop();
            SegmentPool.recycle(s);
        }
        return toCopy;
    }
    
    public void clear() {
        try {
            this.skip(this.size);
        }
        catch (EOFException e) {
            throw new AssertionError((Object)e);
        }
    }
    
    @Override
    public void skip(long byteCount) throws EOFException {
        while (byteCount > 0L) {
            if (this.head == null) {
                throw new EOFException();
            }
            final int toSkip = (int)Math.min(byteCount, this.head.limit - this.head.pos);
            this.size -= toSkip;
            byteCount -= toSkip;
            final Segment head = this.head;
            head.pos += toSkip;
            if (this.head.pos != this.head.limit) {
                continue;
            }
            final Segment toRecycle = this.head;
            this.head = toRecycle.pop();
            SegmentPool.recycle(toRecycle);
        }
    }
    
    @Override
    public Buffer write(final ByteString byteString) {
        if (byteString == null) {
            throw new IllegalArgumentException("byteString == null");
        }
        byteString.write(this);
        return this;
    }
    
    @Override
    public Buffer writeUtf8(final String string) {
        return this.writeUtf8(string, 0, string.length());
    }
    
    @Override
    public Buffer writeUtf8(final String string, final int beginIndex, final int endIndex) {
        if (string == null) {
            throw new IllegalArgumentException("string == null");
        }
        if (beginIndex < 0) {
            throw new IllegalAccessError("beginIndex < 0: " + beginIndex);
        }
        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        }
        if (endIndex > string.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        }
        int i = beginIndex;
        while (i < endIndex) {
            int c = string.charAt(i);
            if (c < 128) {
                final Segment tail = this.writableSegment(1);
                final byte[] data = tail.data;
                final int segmentOffset = tail.limit - i;
                final int runLimit = Math.min(endIndex, 2048 - segmentOffset);
                data[segmentOffset + i++] = (byte)c;
                while (i < runLimit) {
                    c = string.charAt(i);
                    if (c >= 128) {
                        break;
                    }
                    data[segmentOffset + i++] = (byte)c;
                }
                final int runSize = i + segmentOffset - tail.limit;
                final Segment segment = tail;
                segment.limit += runSize;
                this.size += runSize;
            }
            else if (c < 2048) {
                this.writeByte(c >> 6 | 0xC0);
                this.writeByte((c & 0x3F) | 0x80);
                ++i;
            }
            else if (c < 55296 || c > 57343) {
                this.writeByte(c >> 12 | 0xE0);
                this.writeByte((c >> 6 & 0x3F) | 0x80);
                this.writeByte((c & 0x3F) | 0x80);
                ++i;
            }
            else {
                final int low = (i + 1 < endIndex) ? string.charAt(i + 1) : '\0';
                if (c > 56319 || low < 56320 || low > 57343) {
                    this.writeByte(63);
                    ++i;
                }
                else {
                    final int codePoint = 65536 + ((c & 0xFFFF27FF) << 10 | (low & 0xFFFF23FF));
                    this.writeByte(codePoint >> 18 | 0xF0);
                    this.writeByte((codePoint >> 12 & 0x3F) | 0x80);
                    this.writeByte((codePoint >> 6 & 0x3F) | 0x80);
                    this.writeByte((codePoint & 0x3F) | 0x80);
                    i += 2;
                }
            }
        }
        return this;
    }
    
    @Override
    public Buffer writeUtf8CodePoint(final int codePoint) {
        if (codePoint < 128) {
            this.writeByte(codePoint);
        }
        else if (codePoint < 2048) {
            this.writeByte(codePoint >> 6 | 0xC0);
            this.writeByte((codePoint & 0x3F) | 0x80);
        }
        else if (codePoint < 65536) {
            if (codePoint >= 55296 && codePoint <= 57343) {
                throw new IllegalArgumentException("Unexpected code point: " + Integer.toHexString(codePoint));
            }
            this.writeByte(codePoint >> 12 | 0xE0);
            this.writeByte((codePoint >> 6 & 0x3F) | 0x80);
            this.writeByte((codePoint & 0x3F) | 0x80);
        }
        else {
            if (codePoint > 1114111) {
                throw new IllegalArgumentException("Unexpected code point: " + Integer.toHexString(codePoint));
            }
            this.writeByte(codePoint >> 18 | 0xF0);
            this.writeByte((codePoint >> 12 & 0x3F) | 0x80);
            this.writeByte((codePoint >> 6 & 0x3F) | 0x80);
            this.writeByte((codePoint & 0x3F) | 0x80);
        }
        return this;
    }
    
    @Override
    public Buffer writeString(final String string, final Charset charset) {
        return this.writeString(string, 0, string.length(), charset);
    }
    
    @Override
    public Buffer writeString(final String string, final int beginIndex, final int endIndex, final Charset charset) {
        if (string == null) {
            throw new IllegalArgumentException("string == null");
        }
        if (beginIndex < 0) {
            throw new IllegalAccessError("beginIndex < 0: " + beginIndex);
        }
        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        }
        if (endIndex > string.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        }
        if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        }
        if (charset.equals(Util.UTF_8)) {
            return this.writeUtf8(string);
        }
        final byte[] data = string.substring(beginIndex, endIndex).getBytes(charset);
        return this.write(data, 0, data.length);
    }
    
    @Override
    public Buffer write(final byte[] source) {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        return this.write(source, 0, source.length);
    }
    
    @Override
    public Buffer write(final byte[] source, int offset, final int byteCount) {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        Util.checkOffsetAndCount(source.length, offset, byteCount);
        Segment tail;
        int toCopy;
        Segment segment;
        for (int limit = offset + byteCount; offset < limit; offset += toCopy, segment = tail, segment.limit += toCopy) {
            tail = this.writableSegment(1);
            toCopy = Math.min(limit - offset, 2048 - tail.limit);
            System.arraycopy(source, offset, tail.data, tail.limit, toCopy);
        }
        this.size += byteCount;
        return this;
    }
    
    @Override
    public long writeAll(final Source source) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        long totalBytesRead = 0L;
        long readCount;
        while ((readCount = source.read(this, 2048L)) != -1L) {
            totalBytesRead += readCount;
        }
        return totalBytesRead;
    }
    
    @Override
    public BufferedSink write(final Source source, long byteCount) throws IOException {
        while (byteCount > 0L) {
            final long read = source.read(this, byteCount);
            if (read == -1L) {
                throw new EOFException();
            }
            byteCount -= read;
        }
        return this;
    }
    
    @Override
    public Buffer writeByte(final int b) {
        final Segment tail = this.writableSegment(1);
        tail.data[tail.limit++] = (byte)b;
        ++this.size;
        return this;
    }
    
    @Override
    public Buffer writeShort(final int s) {
        final Segment tail = this.writableSegment(2);
        final byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte)(s >>> 8 & 0xFF);
        data[limit++] = (byte)(s & 0xFF);
        tail.limit = limit;
        this.size += 2L;
        return this;
    }
    
    @Override
    public Buffer writeShortLe(final int s) {
        return this.writeShort((int)Util.reverseBytesShort((short)s));
    }
    
    @Override
    public Buffer writeInt(final int i) {
        final Segment tail = this.writableSegment(4);
        final byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte)(i >>> 24 & 0xFF);
        data[limit++] = (byte)(i >>> 16 & 0xFF);
        data[limit++] = (byte)(i >>> 8 & 0xFF);
        data[limit++] = (byte)(i & 0xFF);
        tail.limit = limit;
        this.size += 4L;
        return this;
    }
    
    @Override
    public Buffer writeIntLe(final int i) {
        return this.writeInt(Util.reverseBytesInt(i));
    }
    
    @Override
    public Buffer writeLong(final long v) {
        final Segment tail = this.writableSegment(8);
        final byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte)(v >>> 56 & 0xFFL);
        data[limit++] = (byte)(v >>> 48 & 0xFFL);
        data[limit++] = (byte)(v >>> 40 & 0xFFL);
        data[limit++] = (byte)(v >>> 32 & 0xFFL);
        data[limit++] = (byte)(v >>> 24 & 0xFFL);
        data[limit++] = (byte)(v >>> 16 & 0xFFL);
        data[limit++] = (byte)(v >>> 8 & 0xFFL);
        data[limit++] = (byte)(v & 0xFFL);
        tail.limit = limit;
        this.size += 8L;
        return this;
    }
    
    @Override
    public Buffer writeLongLe(final long v) {
        return this.writeLong(Util.reverseBytesLong(v));
    }
    
    @Override
    public Buffer writeDecimalLong(long v) {
        if (v == 0L) {
            return this.writeByte(48);
        }
        boolean negative = false;
        if (v < 0L) {
            v = -v;
            if (v < 0L) {
                return this.writeUtf8("-9223372036854775808");
            }
            negative = true;
        }
        int width = (v < 100000000L) ? ((v < 10000L) ? ((v < 100L) ? ((v < 10L) ? 1 : 2) : ((v < 1000L) ? 3 : 4)) : ((v < 1000000L) ? ((v < 100000L) ? 5 : 6) : ((v < 10000000L) ? 7 : 8))) : ((v < 1000000000000L) ? ((v < 10000000000L) ? ((v < 1000000000L) ? 9 : 10) : ((v < 100000000000L) ? 11 : 12)) : ((v < 1000000000000000L) ? ((v < 10000000000000L) ? 13 : ((v < 100000000000000L) ? 14 : 15)) : ((v < 100000000000000000L) ? ((v < 10000000000000000L) ? 16 : 17) : ((v < 1000000000000000000L) ? 18 : 19))));
        if (negative) {
            ++width;
        }
        final Segment tail = this.writableSegment(width);
        final byte[] data = tail.data;
        int pos = tail.limit + width;
        while (v != 0L) {
            final int digit = (int)(v % 10L);
            data[--pos] = Buffer.DIGITS[digit];
            v /= 10L;
        }
        if (negative) {
            data[--pos] = 45;
        }
        final Segment segment = tail;
        segment.limit += width;
        this.size += width;
        return this;
    }
    
    @Override
    public Buffer writeHexadecimalUnsignedLong(long v) {
        if (v == 0L) {
            return this.writeByte(48);
        }
        final int width = Long.numberOfTrailingZeros(Long.highestOneBit(v)) / 4 + 1;
        final Segment tail = this.writableSegment(width);
        final byte[] data = tail.data;
        for (int pos = tail.limit + width - 1, start = tail.limit; pos >= start; --pos) {
            data[pos] = Buffer.DIGITS[(int)(v & 0xFL)];
            v >>>= 4;
        }
        final Segment segment = tail;
        segment.limit += width;
        this.size += width;
        return this;
    }
    
    Segment writableSegment(final int minimumCapacity) {
        if (minimumCapacity < 1 || minimumCapacity > 2048) {
            throw new IllegalArgumentException();
        }
        if (this.head == null) {
            this.head = SegmentPool.take();
            final Segment head = this.head;
            final Segment head2 = this.head;
            final Segment head3 = this.head;
            head2.prev = head3;
            return head.next = head3;
        }
        Segment tail = this.head.prev;
        if (tail.limit + minimumCapacity > 2048 || !tail.owner) {
            tail = tail.push(SegmentPool.take());
        }
        return tail;
    }
    
    @Override
    public void write(final Buffer source, long byteCount) {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        if (source == this) {
            throw new IllegalArgumentException("source == this");
        }
        Util.checkOffsetAndCount(source.size, 0L, byteCount);
        while (byteCount > 0L) {
            if (byteCount < source.head.limit - source.head.pos) {
                final Segment tail = (this.head != null) ? this.head.prev : null;
                if (tail != null && tail.owner && byteCount + tail.limit - (tail.shared ? 0 : tail.pos) <= 2048L) {
                    source.head.writeTo(tail, (int)byteCount);
                    source.size -= byteCount;
                    this.size += byteCount;
                    return;
                }
                source.head = source.head.split((int)byteCount);
            }
            final Segment segmentToMove = source.head;
            final long movedByteCount = segmentToMove.limit - segmentToMove.pos;
            source.head = segmentToMove.pop();
            if (this.head == null) {
                this.head = segmentToMove;
                final Segment head = this.head;
                final Segment head2 = this.head;
                final Segment head3 = this.head;
                head2.prev = head3;
                head.next = head3;
            }
            else {
                Segment tail2 = this.head.prev;
                tail2 = tail2.push(segmentToMove);
                tail2.compact();
            }
            source.size -= movedByteCount;
            this.size += movedByteCount;
            byteCount -= movedByteCount;
        }
    }
    
    @Override
    public long read(final Buffer sink, long byteCount) {
        if (sink == null) {
            throw new IllegalArgumentException("sink == null");
        }
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (this.size == 0L) {
            return -1L;
        }
        if (byteCount > this.size) {
            byteCount = this.size;
        }
        sink.write(this, byteCount);
        return byteCount;
    }
    
    @Override
    public long indexOf(final byte b) {
        return this.indexOf(b, 0L);
    }
    
    @Override
    public long indexOf(final byte b, long fromIndex) {
        if (fromIndex < 0L) {
            throw new IllegalArgumentException("fromIndex < 0");
        }
        Segment s = this.head;
        if (s == null) {
            return -1L;
        }
        long offset = 0L;
        do {
            final int segmentByteCount = s.limit - s.pos;
            if (fromIndex >= segmentByteCount) {
                fromIndex -= segmentByteCount;
            }
            else {
                final byte[] data = s.data;
                for (int pos = (int)(s.pos + fromIndex), limit = s.limit; pos < limit; ++pos) {
                    if (data[pos] == b) {
                        return offset + pos - s.pos;
                    }
                }
                fromIndex = 0L;
            }
            offset += segmentByteCount;
            s = s.next;
        } while (s != this.head);
        return -1L;
    }
    
    @Override
    public long indexOf(final ByteString bytes) throws IOException {
        return this.indexOf(bytes, 0L);
    }
    
    @Override
    public long indexOf(final ByteString bytes, long fromIndex) throws IOException {
        if (bytes.size() == 0) {
            throw new IllegalArgumentException("bytes is empty");
        }
        while (true) {
            fromIndex = this.indexOf(bytes.getByte(0), fromIndex);
            if (fromIndex == -1L) {
                return -1L;
            }
            if (this.rangeEquals(fromIndex, bytes)) {
                return fromIndex;
            }
            ++fromIndex;
        }
    }
    
    @Override
    public long indexOfElement(final ByteString targetBytes) {
        return this.indexOfElement(targetBytes, 0L);
    }
    
    @Override
    public long indexOfElement(final ByteString targetBytes, long fromIndex) {
        if (fromIndex < 0L) {
            throw new IllegalArgumentException("fromIndex < 0");
        }
        Segment s = this.head;
        if (s == null) {
            return -1L;
        }
        long offset = 0L;
        final byte[] toFind = targetBytes.toByteArray();
        do {
            final int segmentByteCount = s.limit - s.pos;
            if (fromIndex >= segmentByteCount) {
                fromIndex -= segmentByteCount;
            }
            else {
                final byte[] data = s.data;
                for (long pos = s.pos + fromIndex, limit = s.limit; pos < limit; ++pos) {
                    final byte b = data[(int)pos];
                    for (final byte targetByte : toFind) {
                        if (b == targetByte) {
                            return offset + pos - s.pos;
                        }
                    }
                }
                fromIndex = 0L;
            }
            offset += segmentByteCount;
            s = s.next;
        } while (s != this.head);
        return -1L;
    }
    
    boolean rangeEquals(final long offset, final ByteString bytes) {
        final int byteCount = bytes.size();
        if (this.size - offset < byteCount) {
            return false;
        }
        for (int i = 0; i < byteCount; ++i) {
            if (this.getByte(offset + i) != bytes.getByte(i)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void flush() {
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }
    
    List<Integer> segmentSizes() {
        if (this.head == null) {
            return Collections.emptyList();
        }
        final List<Integer> result = new ArrayList<Integer>();
        result.add(this.head.limit - this.head.pos);
        for (Segment s = this.head.next; s != this.head; s = s.next) {
            result.add(s.limit - s.pos);
        }
        return result;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Buffer)) {
            return false;
        }
        final Buffer that = (Buffer)o;
        if (this.size != that.size) {
            return false;
        }
        if (this.size == 0L) {
            return true;
        }
        Segment sa = this.head;
        Segment sb = that.head;
        int posA = sa.pos;
        int posB = sb.pos;
        long count;
        for (long pos = 0L; pos < this.size; pos += count) {
            count = Math.min(sa.limit - posA, sb.limit - posB);
            for (int i = 0; i < count; ++i) {
                if (sa.data[posA++] != sb.data[posB++]) {
                    return false;
                }
            }
            if (posA == sa.limit) {
                sa = sa.next;
                posA = sa.pos;
            }
            if (posB == sb.limit) {
                sb = sb.next;
                posB = sb.pos;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        Segment s = this.head;
        if (s == null) {
            return 0;
        }
        int result = 1;
        do {
            for (int pos = s.pos, limit = s.limit; pos < limit; ++pos) {
                result = 31 * result + s.data[pos];
            }
            s = s.next;
        } while (s != this.head);
        return result;
    }
    
    @Override
    public String toString() {
        if (this.size == 0L) {
            return "Buffer[size=0]";
        }
        if (this.size <= 16L) {
            final ByteString data = this.clone().readByteString();
            return String.format("Buffer[size=%s data=%s]", this.size, data.hex());
        }
        try {
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
            for (Segment s = this.head.next; s != this.head; s = s.next) {
                md5.update(s.data, s.pos, s.limit - s.pos);
            }
            return String.format("Buffer[size=%s md5=%s]", this.size, ByteString.of(md5.digest()).hex());
        }
        catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }
    
    public Buffer clone() {
        final Buffer result = new Buffer();
        if (this.size == 0L) {
            return result;
        }
        result.head = new Segment(this.head);
        final Segment head = result.head;
        final Segment head2 = result.head;
        final Segment head3 = result.head;
        head2.prev = head3;
        head.next = head3;
        for (Segment s = this.head.next; s != this.head; s = s.next) {
            result.head.prev.push(new Segment(s));
        }
        result.size = this.size;
        return result;
    }
    
    public ByteString snapshot() {
        if (this.size > 2147483647L) {
            throw new IllegalArgumentException("size > Integer.MAX_VALUE: " + this.size);
        }
        return this.snapshot((int)this.size);
    }
    
    public ByteString snapshot(final int byteCount) {
        if (byteCount == 0) {
            return ByteString.EMPTY;
        }
        return new SegmentedByteString(this, byteCount);
    }
    
    static {
        DIGITS = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };
    }
}
