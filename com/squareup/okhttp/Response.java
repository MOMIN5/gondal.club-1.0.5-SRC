// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import com.squareup.okhttp.internal.http.OkHeaders;
import java.util.Collections;
import java.util.List;

public final class Response
{
    private final Request request;
    private final Protocol protocol;
    private final int code;
    private final String message;
    private final Handshake handshake;
    private final Headers headers;
    private final ResponseBody body;
    private Response networkResponse;
    private Response cacheResponse;
    private final Response priorResponse;
    private volatile CacheControl cacheControl;
    
    private Response(final Builder builder) {
        this.request = builder.request;
        this.protocol = builder.protocol;
        this.code = builder.code;
        this.message = builder.message;
        this.handshake = builder.handshake;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.networkResponse = builder.networkResponse;
        this.cacheResponse = builder.cacheResponse;
        this.priorResponse = builder.priorResponse;
    }
    
    public Request request() {
        return this.request;
    }
    
    public Protocol protocol() {
        return this.protocol;
    }
    
    public int code() {
        return this.code;
    }
    
    public boolean isSuccessful() {
        return this.code >= 200 && this.code < 300;
    }
    
    public String message() {
        return this.message;
    }
    
    public Handshake handshake() {
        return this.handshake;
    }
    
    public List<String> headers(final String name) {
        return this.headers.values(name);
    }
    
    public String header(final String name) {
        return this.header(name, null);
    }
    
    public String header(final String name, final String defaultValue) {
        final String result = this.headers.get(name);
        return (result != null) ? result : defaultValue;
    }
    
    public Headers headers() {
        return this.headers;
    }
    
    public ResponseBody body() {
        return this.body;
    }
    
    public Builder newBuilder() {
        return new Builder(this);
    }
    
    public boolean isRedirect() {
        switch (this.code) {
            case 300:
            case 301:
            case 302:
            case 303:
            case 307:
            case 308: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public Response networkResponse() {
        return this.networkResponse;
    }
    
    public Response cacheResponse() {
        return this.cacheResponse;
    }
    
    public Response priorResponse() {
        return this.priorResponse;
    }
    
    public List<Challenge> challenges() {
        String responseField;
        if (this.code == 401) {
            responseField = "WWW-Authenticate";
        }
        else {
            if (this.code != 407) {
                return Collections.emptyList();
            }
            responseField = "Proxy-Authenticate";
        }
        return OkHeaders.parseChallenges(this.headers(), responseField);
    }
    
    public CacheControl cacheControl() {
        final CacheControl result = this.cacheControl;
        return (result != null) ? result : (this.cacheControl = CacheControl.parse(this.headers));
    }
    
    @Override
    public String toString() {
        return "Response{protocol=" + this.protocol + ", code=" + this.code + ", message=" + this.message + ", url=" + this.request.urlString() + '}';
    }
    
    public static class Builder
    {
        private Request request;
        private Protocol protocol;
        private int code;
        private String message;
        private Handshake handshake;
        private Headers.Builder headers;
        private ResponseBody body;
        private Response networkResponse;
        private Response cacheResponse;
        private Response priorResponse;
        
        public Builder() {
            this.code = -1;
            this.headers = new Headers.Builder();
        }
        
        private Builder(final Response response) {
            this.code = -1;
            this.request = response.request;
            this.protocol = response.protocol;
            this.code = response.code;
            this.message = response.message;
            this.handshake = response.handshake;
            this.headers = response.headers.newBuilder();
            this.body = response.body;
            this.networkResponse = response.networkResponse;
            this.cacheResponse = response.cacheResponse;
            this.priorResponse = response.priorResponse;
        }
        
        public Builder request(final Request request) {
            this.request = request;
            return this;
        }
        
        public Builder protocol(final Protocol protocol) {
            this.protocol = protocol;
            return this;
        }
        
        public Builder code(final int code) {
            this.code = code;
            return this;
        }
        
        public Builder message(final String message) {
            this.message = message;
            return this;
        }
        
        public Builder handshake(final Handshake handshake) {
            this.handshake = handshake;
            return this;
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
        
        public Builder body(final ResponseBody body) {
            this.body = body;
            return this;
        }
        
        public Builder networkResponse(final Response networkResponse) {
            if (networkResponse != null) {
                this.checkSupportResponse("networkResponse", networkResponse);
            }
            this.networkResponse = networkResponse;
            return this;
        }
        
        public Builder cacheResponse(final Response cacheResponse) {
            if (cacheResponse != null) {
                this.checkSupportResponse("cacheResponse", cacheResponse);
            }
            this.cacheResponse = cacheResponse;
            return this;
        }
        
        private void checkSupportResponse(final String name, final Response response) {
            if (response.body != null) {
                throw new IllegalArgumentException(name + ".body != null");
            }
            if (response.networkResponse != null) {
                throw new IllegalArgumentException(name + ".networkResponse != null");
            }
            if (response.cacheResponse != null) {
                throw new IllegalArgumentException(name + ".cacheResponse != null");
            }
            if (response.priorResponse != null) {
                throw new IllegalArgumentException(name + ".priorResponse != null");
            }
        }
        
        public Builder priorResponse(final Response priorResponse) {
            if (priorResponse != null) {
                this.checkPriorResponse(priorResponse);
            }
            this.priorResponse = priorResponse;
            return this;
        }
        
        private void checkPriorResponse(final Response response) {
            if (response.body != null) {
                throw new IllegalArgumentException("priorResponse.body != null");
            }
        }
        
        public Response build() {
            if (this.request == null) {
                throw new IllegalStateException("request == null");
            }
            if (this.protocol == null) {
                throw new IllegalStateException("protocol == null");
            }
            if (this.code < 0) {
                throw new IllegalStateException("code < 0: " + this.code);
            }
            return new Response(this, null);
        }
    }
}
