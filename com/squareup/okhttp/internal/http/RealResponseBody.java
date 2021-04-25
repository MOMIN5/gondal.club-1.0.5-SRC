// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import com.squareup.okhttp.MediaType;
import okio.BufferedSource;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.ResponseBody;

public final class RealResponseBody extends ResponseBody
{
    private final Headers headers;
    private final BufferedSource source;
    
    public RealResponseBody(final Headers headers, final BufferedSource source) {
        this.headers = headers;
        this.source = source;
    }
    
    @Override
    public MediaType contentType() {
        final String contentType = this.headers.get("Content-Type");
        return (contentType != null) ? MediaType.parse(contentType) : null;
    }
    
    @Override
    public long contentLength() {
        return OkHeaders.contentLength(this.headers);
    }
    
    @Override
    public BufferedSource source() {
        return this.source;
    }
}
