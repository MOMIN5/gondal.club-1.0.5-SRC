// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import okio.Sink;
import com.squareup.okhttp.Request;

public interface HttpStream
{
    public static final int DISCARD_STREAM_TIMEOUT_MILLIS = 100;
    
    Sink createRequestBody(final Request p0, final long p1) throws IOException;
    
    void writeRequestHeaders(final Request p0) throws IOException;
    
    void writeRequestBody(final RetryableSink p0) throws IOException;
    
    void finishRequest() throws IOException;
    
    Response.Builder readResponseHeaders() throws IOException;
    
    ResponseBody openResponseBody(final Response p0) throws IOException;
    
    void setHttpEngine(final HttpEngine p0);
    
    void cancel();
}
