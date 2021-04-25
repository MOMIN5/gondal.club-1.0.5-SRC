// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.framed;

import okio.Buffer;
import java.util.List;
import java.io.IOException;
import java.io.Closeable;

public interface FrameWriter extends Closeable
{
    void connectionPreface() throws IOException;
    
    void ackSettings(final Settings p0) throws IOException;
    
    void pushPromise(final int p0, final int p1, final List<Header> p2) throws IOException;
    
    void flush() throws IOException;
    
    void synStream(final boolean p0, final boolean p1, final int p2, final int p3, final List<Header> p4) throws IOException;
    
    void synReply(final boolean p0, final int p1, final List<Header> p2) throws IOException;
    
    void headers(final int p0, final List<Header> p1) throws IOException;
    
    void rstStream(final int p0, final ErrorCode p1) throws IOException;
    
    int maxDataLength();
    
    void data(final boolean p0, final int p1, final Buffer p2, final int p3) throws IOException;
    
    void settings(final Settings p0) throws IOException;
    
    void ping(final boolean p0, final int p1, final int p2) throws IOException;
    
    void goAway(final int p0, final ErrorCode p1, final byte[] p2) throws IOException;
    
    void windowUpdate(final int p0, final long p1) throws IOException;
}
