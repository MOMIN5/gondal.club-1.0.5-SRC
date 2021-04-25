// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.framed;

import okio.BufferedSink;
import okio.BufferedSource;
import com.squareup.okhttp.Protocol;

public interface Variant
{
    Protocol getProtocol();
    
    FrameReader newReader(final BufferedSource p0, final boolean p1);
    
    FrameWriter newWriter(final BufferedSink p0, final boolean p1);
}
