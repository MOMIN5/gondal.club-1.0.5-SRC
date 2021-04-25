// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.io.IOException;

public interface Interceptor
{
    Response intercept(final Chain p0) throws IOException;
    
    public interface Chain
    {
        Request request();
        
        Response proceed(final Request p0) throws IOException;
        
        Connection connection();
    }
}
