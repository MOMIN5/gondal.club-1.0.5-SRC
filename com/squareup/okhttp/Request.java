// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import com.squareup.okhttp.internal.http.HttpMethod;
import java.util.List;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public final class Request
{
    private final HttpUrl url;
    private final String method;
    private final Headers headers;
    private final RequestBody body;
    private final Object tag;
    private volatile URL javaNetUrl;
    private volatile URI javaNetUri;
    private volatile CacheControl cacheControl;
    
    private Request(final Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.tag = ((builder.tag != null) ? builder.tag : this);
    }
    
    public HttpUrl httpUrl() {
        return this.url;
    }
    
    public URL url() {
        final URL result = this.javaNetUrl;
        return (result != null) ? result : (this.javaNetUrl = this.url.url());
    }
    
    public URI uri() throws IOException {
        try {
            final URI result = this.javaNetUri;
            return (result != null) ? result : (this.javaNetUri = this.url.uri());
        }
        catch (IllegalStateException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public String urlString() {
        return this.url.toString();
    }
    
    public String method() {
        return this.method;
    }
    
    public Headers headers() {
        return this.headers;
    }
    
    public String header(final String name) {
        return this.headers.get(name);
    }
    
    public List<String> headers(final String name) {
        return this.headers.values(name);
    }
    
    public RequestBody body() {
        return this.body;
    }
    
    public Object tag() {
        return this.tag;
    }
    
    public Builder newBuilder() {
        return new Builder(this);
    }
    
    public CacheControl cacheControl() {
        final CacheControl result = this.cacheControl;
        return (result != null) ? result : (this.cacheControl = CacheControl.parse(this.headers));
    }
    
    public boolean isHttps() {
        return this.url.isHttps();
    }
    
    @Override
    public String toString() {
        return "Request{method=" + this.method + ", url=" + this.url + ", tag=" + ((this.tag != this) ? this.tag : null) + '}';
    }
    
    public static class Builder
    {
        private HttpUrl url;
        private String method;
        private Headers.Builder headers;
        private RequestBody body;
        private Object tag;
        
        public Builder() {
            this.method = "GET";
            this.headers = new Headers.Builder();
        }
        
        private Builder(final Request request) {
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            this.tag = request.tag;
            this.headers = request.headers.newBuilder();
        }
        
        public Builder url(final HttpUrl url) {
            if (url == null) {
                throw new IllegalArgumentException("url == null");
            }
            this.url = url;
            return this;
        }
        
        public Builder url(String url) {
            if (url == null) {
                throw new IllegalArgumentException("url == null");
            }
            if (url.regionMatches(true, 0, "ws:", 0, 3)) {
                url = "http:" + url.substring(3);
            }
            else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
                url = "https:" + url.substring(4);
            }
            final HttpUrl parsed = HttpUrl.parse(url);
            if (parsed == null) {
                throw new IllegalArgumentException("unexpected url: " + url);
            }
            return this.url(parsed);
        }
        
        public Builder url(final URL url) {
            if (url == null) {
                throw new IllegalArgumentException("url == null");
            }
            final HttpUrl parsed = HttpUrl.get(url);
            if (parsed == null) {
                throw new IllegalArgumentException("unexpected url: " + url);
            }
            return this.url(parsed);
        }
        
        public Builder header(final String name, final String value) {
            this.headers.set(name, value);
            return this;
        }
        
        public Builder addHeader(final String name, final String value) {
            this.headers.add(name, value);
            return this;
        }
        
        public Builder removeHeader(final String name) {
            this.headers.removeAll(name);
            return this;
        }
        
        public Builder headers(final Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }
        
        public Builder cacheControl(final CacheControl cacheControl) {
            final String value = cacheControl.toString();
            if (value.isEmpty()) {
                return this.removeHeader("Cache-Control");
            }
            return this.header("Cache-Control", value);
        }
        
        public Builder get() {
            return this.method("GET", null);
        }
        
        public Builder head() {
            return this.method("HEAD", null);
        }
        
        public Builder post(final RequestBody body) {
            return this.method("POST", body);
        }
        
        public Builder delete(final RequestBody body) {
            return this.method("DELETE", body);
        }
        
        public Builder delete() {
            return this.delete(RequestBody.create(null, new byte[0]));
        }
        
        public Builder put(final RequestBody body) {
            return this.method("PUT", body);
        }
        
        public Builder patch(final RequestBody body) {
            return this.method("PATCH", body);
        }
        
        public Builder method(final String method, final RequestBody body) {
            if (method == null || method.length() == 0) {
                throw new IllegalArgumentException("method == null || method.length() == 0");
            }
            if (body != null && !HttpMethod.permitsRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must not have a request body.");
            }
            if (body == null && HttpMethod.requiresRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must have a request body.");
            }
            this.method = method;
            this.body = body;
            return this;
        }
        
        public Builder tag(final Object tag) {
            this.tag = tag;
            return this;
        }
        
        public Request build() {
            if (this.url == null) {
                throw new IllegalStateException("url == null");
            }
            return new Request(this, null);
        }
    }
}
