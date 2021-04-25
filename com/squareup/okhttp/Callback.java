// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.io.IOException;

public interface Callback
{
    void onFailure(final Request p0, final IOException p1);
    
    void onResponse(final Response p0) throws IOException;
}
