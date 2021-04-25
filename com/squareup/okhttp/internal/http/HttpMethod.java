// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

public final class HttpMethod
{
    public static boolean invalidatesCache(final String method) {
        return method.equals("POST") || method.equals("PATCH") || method.equals("PUT") || method.equals("DELETE") || method.equals("MOVE");
    }
    
    public static boolean requiresRequestBody(final String method) {
        return method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("PROPPATCH") || method.equals("REPORT");
    }
    
    public static boolean permitsRequestBody(final String method) {
        return requiresRequestBody(method) || method.equals("OPTIONS") || method.equals("DELETE") || method.equals("PROPFIND") || method.equals("MKCOL") || method.equals("LOCK");
    }
    
    public static boolean redirectsToGet(final String method) {
        return !method.equals("PROPFIND");
    }
    
    private HttpMethod() {
    }
}
