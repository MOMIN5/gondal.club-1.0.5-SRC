// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.util.logging.Level;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.NamedRunnable;
import java.net.ProtocolException;
import okio.Sink;
import com.squareup.okhttp.internal.http.RouteException;
import com.squareup.okhttp.internal.http.RequestException;
import com.squareup.okhttp.internal.http.RetryableSink;
import com.squareup.okhttp.internal.http.StreamAllocation;
import java.io.IOException;
import com.squareup.okhttp.internal.http.HttpEngine;

public class Call
{
    private final OkHttpClient client;
    private boolean executed;
    volatile boolean canceled;
    Request originalRequest;
    HttpEngine engine;
    
    protected Call(final OkHttpClient client, final Request originalRequest) {
        this.client = client.copyWithDefaults();
        this.originalRequest = originalRequest;
    }
    
    public Response execute() throws IOException {
        synchronized (this) {
            if (this.executed) {
                throw new IllegalStateException("Already Executed");
            }
            this.executed = true;
        }
        try {
            this.client.getDispatcher().executed(this);
            final Response result = this.getResponseWithInterceptorChain(false);
            if (result == null) {
                throw new IOException("Canceled");
            }
            return result;
        }
        finally {
            this.client.getDispatcher().finished(this);
        }
    }
    
    Object tag() {
        return this.originalRequest.tag();
    }
    
    public void enqueue(final Callback responseCallback) {
        this.enqueue(responseCallback, false);
    }
    
    void enqueue(final Callback responseCallback, final boolean forWebSocket) {
        synchronized (this) {
            if (this.executed) {
                throw new IllegalStateException("Already Executed");
            }
            this.executed = true;
        }
        this.client.getDispatcher().enqueue(new AsyncCall(responseCallback, forWebSocket));
    }
    
    public void cancel() {
        this.canceled = true;
        if (this.engine != null) {
            this.engine.cancel();
        }
    }
    
    public synchronized boolean isExecuted() {
        return this.executed;
    }
    
    public boolean isCanceled() {
        return this.canceled;
    }
    
    private String toLoggableString() {
        final String string = this.canceled ? "canceled call" : "call";
        final HttpUrl redactedUrl = this.originalRequest.httpUrl().resolve("/...");
        return string + " to " + redactedUrl;
    }
    
    private Response getResponseWithInterceptorChain(final boolean forWebSocket) throws IOException {
        final Interceptor.Chain chain = new ApplicationInterceptorChain(0, this.originalRequest, forWebSocket);
        return chain.proceed(this.originalRequest);
    }
    
    Response getResponse(Request request, final boolean forWebSocket) throws IOException {
        final RequestBody body = request.body();
        if (body != null) {
            final Request.Builder requestBuilder = request.newBuilder();
            final MediaType contentType = body.contentType();
            if (contentType != null) {
                requestBuilder.header("Content-Type", contentType.toString());
            }
            final long contentLength = body.contentLength();
            if (contentLength != -1L) {
                requestBuilder.header("Content-Length", Long.toString(contentLength));
                requestBuilder.removeHeader("Transfer-Encoding");
            }
            else {
                requestBuilder.header("Transfer-Encoding", "chunked");
                requestBuilder.removeHeader("Content-Length");
            }
            request = requestBuilder.build();
        }
        this.engine = new HttpEngine(this.client, request, false, false, forWebSocket, null, null, null);
        int followUpCount = 0;
        while (!this.canceled) {
            boolean releaseConnection = true;
            try {
                this.engine.sendRequest();
                this.engine.readResponse();
                releaseConnection = false;
            }
            catch (RequestException e) {
                throw e.getCause();
            }
            catch (RouteException e2) {
                final HttpEngine retryEngine = this.engine.recover(e2);
                if (retryEngine != null) {
                    releaseConnection = false;
                    this.engine = retryEngine;
                    continue;
                }
                throw e2.getLastConnectException();
            }
            catch (IOException e3) {
                final HttpEngine retryEngine = this.engine.recover(e3, null);
                if (retryEngine != null) {
                    releaseConnection = false;
                    this.engine = retryEngine;
                    continue;
                }
                throw e3;
            }
            finally {
                if (releaseConnection) {
                    final StreamAllocation streamAllocation = this.engine.close();
                    streamAllocation.release();
                }
            }
            final Response response = this.engine.getResponse();
            final Request followUp = this.engine.followUpRequest();
            if (followUp == null) {
                if (!forWebSocket) {
                    this.engine.releaseStreamAllocation();
                }
                return response;
            }
            StreamAllocation streamAllocation2 = this.engine.close();
            if (++followUpCount > 20) {
                streamAllocation2.release();
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }
            if (!this.engine.sameConnection(followUp.httpUrl())) {
                streamAllocation2.release();
                streamAllocation2 = null;
            }
            request = followUp;
            this.engine = new HttpEngine(this.client, request, false, false, forWebSocket, streamAllocation2, null, response);
        }
        this.engine.releaseStreamAllocation();
        throw new IOException("Canceled");
    }
    
    final class AsyncCall extends NamedRunnable
    {
        private final Callback responseCallback;
        private final boolean forWebSocket;
        
        private AsyncCall(final Callback responseCallback, final boolean forWebSocket) {
            super("OkHttp %s", new Object[] { Call.this.originalRequest.urlString() });
            this.responseCallback = responseCallback;
            this.forWebSocket = forWebSocket;
        }
        
        String host() {
            return Call.this.originalRequest.httpUrl().host();
        }
        
        Request request() {
            return Call.this.originalRequest;
        }
        
        Object tag() {
            return Call.this.originalRequest.tag();
        }
        
        void cancel() {
            Call.this.cancel();
        }
        
        Call get() {
            return Call.this;
        }
        
        @Override
        protected void execute() {
            boolean signalledCallback = false;
            try {
                final Response response = Call.this.getResponseWithInterceptorChain(this.forWebSocket);
                if (Call.this.canceled) {
                    signalledCallback = true;
                    this.responseCallback.onFailure(Call.this.originalRequest, new IOException("Canceled"));
                }
                else {
                    signalledCallback = true;
                    this.responseCallback.onResponse(response);
                }
            }
            catch (IOException e) {
                if (signalledCallback) {
                    Internal.logger.log(Level.INFO, "Callback failure for " + Call.this.toLoggableString(), e);
                }
                else {
                    final Request request = (Call.this.engine == null) ? Call.this.originalRequest : Call.this.engine.getRequest();
                    this.responseCallback.onFailure(request, e);
                }
            }
            finally {
                Call.this.client.getDispatcher().finished(this);
            }
        }
    }
    
    class ApplicationInterceptorChain implements Interceptor.Chain
    {
        private final int index;
        private final Request request;
        private final boolean forWebSocket;
        
        ApplicationInterceptorChain(final int index, final Request request, final boolean forWebSocket) {
            this.index = index;
            this.request = request;
            this.forWebSocket = forWebSocket;
        }
        
        @Override
        public Connection connection() {
            return null;
        }
        
        @Override
        public Request request() {
            return this.request;
        }
        
        @Override
        public Response proceed(final Request request) throws IOException {
            if (this.index >= Call.this.client.interceptors().size()) {
                return Call.this.getResponse(request, this.forWebSocket);
            }
            final Interceptor.Chain chain = new ApplicationInterceptorChain(this.index + 1, request, this.forWebSocket);
            final Interceptor interceptor = Call.this.client.interceptors().get(this.index);
            final Response interceptedResponse = interceptor.intercept(chain);
            if (interceptedResponse == null) {
                throw new NullPointerException("application interceptor " + interceptor + " returned null");
            }
            return interceptedResponse;
        }
    }
}
