// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal;

import com.squareup.okhttp.internal.http.CacheStrategy;
import com.squareup.okhttp.internal.http.CacheRequest;
import java.io.IOException;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Request;

public interface InternalCache
{
    Response get(final Request p0) throws IOException;
    
    CacheRequest put(final Response p0) throws IOException;
    
    void remove(final Request p0) throws IOException;
    
    void update(final Response p0, final Response p1) throws IOException;
    
    void trackConditionalCacheHit();
    
    void trackResponse(final CacheStrategy p0);
}
