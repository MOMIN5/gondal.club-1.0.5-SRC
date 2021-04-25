// 
// Decompiled by Procyon v0.5.36
// 

package okio;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.io.IOException;

public interface BufferedSource extends Source
{
    Buffer buffer();
    
    boolean exhausted() throws IOException;
    
    void require(final long p0) throws IOException;
    
    boolean request(final long p0) throws IOException;
    
    byte readByte() throws IOException;
    
    short readShort() throws IOException;
    
    short readShortLe() throws IOException;
    
    int readInt() throws IOException;
    
    int readIntLe() throws IOException;
    
    long readLong() throws IOException;
    
    long readLongLe() throws IOException;
    
    long readDecimalLong() throws IOException;
    
    long readHexadecimalUnsignedLong() throws IOException;
    
    void skip(final long p0) throws IOException;
    
    ByteString readByteString() throws IOException;
    
    ByteString readByteString(final long p0) throws IOException;
    
    byte[] readByteArray() throws IOException;
    
    byte[] readByteArray(final long p0) throws IOException;
    
    int read(final byte[] p0) throws IOException;
    
    void readFully(final byte[] p0) throws IOException;
    
    int read(final byte[] p0, final int p1, final int p2) throws IOException;
    
    void readFully(final Buffer p0, final long p1) throws IOException;
    
    long readAll(final Sink p0) throws IOException;
    
    String readUtf8() throws IOException;
    
    String readUtf8(final long p0) throws IOException;
    
    String readUtf8Line() throws IOException;
    
    String readUtf8LineStrict() throws IOException;
    
    int readUtf8CodePoint() throws IOException;
    
    String readString(final Charset p0) throws IOException;
    
    String readString(final long p0, final Charset p1) throws IOException;
    
    long indexOf(final byte p0) throws IOException;
    
    long indexOf(final byte p0, final long p1) throws IOException;
    
    long indexOf(final ByteString p0) throws IOException;
    
    long indexOf(final ByteString p0, final long p1) throws IOException;
    
    long indexOfElement(final ByteString p0) throws IOException;
    
    long indexOfElement(final ByteString p0, final long p1) throws IOException;
    
    InputStream inputStream();
}
