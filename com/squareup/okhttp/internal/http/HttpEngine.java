// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.CertificatePinner;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import com.squareup.okhttp.Address;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Route;
import com.squareup.okhttp.RequestBody;
import java.net.ProtocolException;
import java.net.Proxy;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import okio.Timeout;
import okio.Buffer;
import okio.BufferedSource;
import java.util.List;
import java.util.Map;
import java.net.CookieHandler;
import com.squareup.okhttp.internal.Version;
import com.squareup.okhttp.Headers;
import okio.Source;
import okio.GzipSource;
import com.squareup.okhttp.Connection;
import okio.Okio;
import java.io.IOException;
import com.squareup.okhttp.internal.InternalCache;
import com.squareup.okhttp.Protocol;
import java.io.Closeable;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.Internal;
import okio.BufferedSink;
import okio.Sink;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.ResponseBody;

public final class HttpEngine
{
    public static final int MAX_FOLLOW_UPS = 20;
    private static final ResponseBody EMPTY_BODY;
    final OkHttpClient client;
    public final StreamAllocation streamAllocation;
    private final Response priorResponse;
    private HttpStream httpStream;
    long sentRequestMillis;
    private boolean transparentGzip;
    public final boolean bufferRequestBody;
    private final Request userRequest;
    private Request networkRequest;
    private Response cacheResponse;
    private Response userResponse;
    private Sink requestBodyOut;
    private BufferedSink bufferedRequestBody;
    private final boolean callerWritesRequestBody;
    private final boolean forWebSocket;
    private CacheRequest storeRequest;
    private CacheStrategy cacheStrategy;
    
    public HttpEngine(final OkHttpClient client, final Request request, final boolean bufferRequestBody, final boolean callerWritesRequestBody, final boolean forWebSocket, final StreamAllocation streamAllocation, final RetryableSink requestBodyOut, final Response priorResponse) {
        this.sentRequestMillis = -1L;
        this.client = client;
        this.userRequest = request;
        this.bufferRequestBody = bufferRequestBody;
        this.callerWritesRequestBody = callerWritesRequestBody;
        this.forWebSocket = forWebSocket;
        this.streamAllocation = ((streamAllocation != null) ? streamAllocation : new StreamAllocation(client.getConnectionPool(), createAddress(client, request)));
        this.requestBodyOut = requestBodyOut;
        this.priorResponse = priorResponse;
    }
    
    public void sendRequest() throws RequestException, RouteException, IOException {
        if (this.cacheStrategy != null) {
            return;
        }
        if (this.httpStream != null) {
            throw new IllegalStateException();
        }
        final Request request = this.networkRequest(this.userRequest);
        final InternalCache responseCache = Internal.instance.internalCache(this.client);
        final Response cacheCandidate = (responseCache != null) ? responseCache.get(request) : null;
        final long now = System.currentTimeMillis();
        this.cacheStrategy = new CacheStrategy.Factory(now, request, cacheCandidate).get();
        this.networkRequest = this.cacheStrategy.networkRequest;
        this.cacheResponse = this.cacheStrategy.cacheResponse;
        if (responseCache != null) {
            responseCache.trackResponse(this.cacheStrategy);
        }
        if (cacheCandidate != null && this.cacheResponse == null) {
            Util.closeQuietly(cacheCandidate.body());
        }
        if (this.networkRequest != null) {
            (this.httpStream = this.connect()).setHttpEngine(this);
            if (this.callerWritesRequestBody && this.permitsRequestBody(this.networkRequest) && this.requestBodyOut == null) {
                final long contentLength = OkHeaders.contentLength(request);
                if (this.bufferRequestBody) {
                    if (contentLength > 2147483647L) {
                        throw new IllegalStateException("Use setFixedLengthStreamingMode() or setChunkedStreamingMode() for requests larger than 2 GiB.");
                    }
                    if (contentLength != -1L) {
                        this.httpStream.writeRequestHeaders(this.networkRequest);
                        this.requestBodyOut = new RetryableSink((int)contentLength);
                    }
                    else {
                        this.requestBodyOut = new RetryableSink();
                    }
                }
                else {
                    this.httpStream.writeRequestHeaders(this.networkRequest);
                    this.requestBodyOut = this.httpStream.createRequestBody(this.networkRequest, contentLength);
                }
            }
        }
        else {
            if (this.cacheResponse != null) {
                this.userResponse = this.cacheResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).cacheResponse(stripBody(this.cacheResponse)).build();
            }
            else {
                this.userResponse = new Response.Builder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).protocol(Protocol.HTTP_1_1).code(504).message("Unsatisfiable Request (only-if-cached)").body(HttpEngine.EMPTY_BODY).build();
            }
            this.userResponse = this.unzip(this.userResponse);
        }
    }
    
    private HttpStream connect() throws RouteException, RequestException, IOException {
        final boolean doExtensiveHealthChecks = !this.networkRequest.method().equals("GET");
        return this.streamAllocation.newStream(this.client.getConnectTimeout(), this.client.getReadTimeout(), this.client.getWriteTimeout(), this.client.getRetryOnConnectionFailure(), doExtensiveHealthChecks);
    }
    
    private static Response stripBody(final Response response) {
        return (response != null && response.body() != null) ? response.newBuilder().body(null).build() : response;
    }
    
    public void writingRequestHeaders() {
        if (this.sentRequestMillis != -1L) {
            throw new IllegalStateException();
        }
        this.sentRequestMillis = System.currentTimeMillis();
    }
    
    boolean permitsRequestBody(final Request request) {
        return HttpMethod.permitsRequestBody(request.method());
    }
    
    public Sink getRequestBody() {
        if (this.cacheStrategy == null) {
            throw new IllegalStateException();
        }
        return this.requestBodyOut;
    }
    
    public BufferedSink getBufferedRequestBody() {
        final BufferedSink result = this.bufferedRequestBody;
        if (result != null) {
            return result;
        }
        final Sink requestBody = this.getRequestBody();
        return (requestBody != null) ? (this.bufferedRequestBody = Okio.buffer(requestBody)) : null;
    }
    
    public boolean hasResponse() {
        return this.userResponse != null;
    }
    
    public Request getRequest() {
        return this.userRequest;
    }
    
    public Response getResponse() {
        if (this.userResponse == null) {
            throw new IllegalStateException();
        }
        return this.userResponse;
    }
    
    public Connection getConnection() {
        return this.streamAllocation.connection();
    }
    
    public HttpEngine recover(final RouteException e) {
        if (!this.streamAllocation.recover(e)) {
            return null;
        }
        if (!this.client.getRetryOnConnectionFailure()) {
            return null;
        }
        final StreamAllocation streamAllocation = this.close();
        return new HttpEngine(this.client, this.userRequest, this.bufferRequestBody, this.callerWritesRequestBody, this.forWebSocket, streamAllocation, (RetryableSink)this.requestBodyOut, this.priorResponse);
    }
    
    public HttpEngine recover(final IOException e, final Sink requestBodyOut) {
        if (!this.streamAllocation.recover(e, requestBodyOut)) {
            return null;
        }
        if (!this.client.getRetryOnConnectionFailure()) {
            return null;
        }
        final StreamAllocation streamAllocation = this.close();
        return new HttpEngine(this.client, this.userRequest, this.bufferRequestBody, this.callerWritesRequestBody, this.forWebSocket, streamAllocation, (RetryableSink)requestBodyOut, this.priorResponse);
    }
    
    public HttpEngine recover(final IOException e) {
        return this.recover(e, this.requestBodyOut);
    }
    
    private void maybeCache() throws IOException {
        final InternalCache responseCache = Internal.instance.internalCache(this.client);
        if (responseCache == null) {
            return;
        }
        if (!CacheStrategy.isCacheable(this.userResponse, this.networkRequest)) {
            if (HttpMethod.invalidatesCache(this.networkRequest.method())) {
                try {
                    responseCache.remove(this.networkRequest);
                }
                catch (IOException ex) {}
            }
            return;
        }
        this.storeRequest = responseCache.put(stripBody(this.userResponse));
    }
    
    public void releaseStreamAllocation() throws IOException {
        this.streamAllocation.release();
    }
    
    public void cancel() {
        this.streamAllocation.cancel();
    }
    
    public StreamAllocation close() {
        if (this.bufferedRequestBody != null) {
            Util.closeQuietly(this.bufferedRequestBody);
        }
        else if (this.requestBodyOut != null) {
            Util.closeQuietly(this.requestBodyOut);
        }
        if (this.userResponse != null) {
            Util.closeQuietly(this.userResponse.body());
        }
        else {
            this.streamAllocation.connectionFailed();
        }
        return this.streamAllocation;
    }
    
    private Response unzip(final Response response) throws IOException {
        if (!this.transparentGzip || !"gzip".equalsIgnoreCase(this.userResponse.header("Content-Encoding"))) {
            return response;
        }
        if (response.body() == null) {
            return response;
        }
        final GzipSource responseBody = new GzipSource(response.body().source());
        final Headers strippedHeaders = response.headers().newBuilder().removeAll("Content-Encoding").removeAll("Content-Length").build();
        return response.newBuilder().headers(strippedHeaders).body(new RealResponseBody(strippedHeaders, Okio.buffer(responseBody))).build();
    }
    
    public static boolean hasBody(final Response response) {
        if (response.request().method().equals("HEAD")) {
            return false;
        }
        final int responseCode = response.code();
        return ((responseCode < 100 || responseCode >= 200) && responseCode != 204 && responseCode != 304) || (OkHeaders.contentLength(response) != -1L || "chunked".equalsIgnoreCase(response.header("Transfer-Encoding")));
    }
    
    private Request networkRequest(final Request request) throws IOException {
        final Request.Builder result = request.newBuilder();
        if (request.header("Host") == null) {
            result.header("Host", Util.hostHeader(request.httpUrl()));
        }
        if (request.header("Connection") == null) {
            result.header("Connection", "Keep-Alive");
        }
        if (request.header("Accept-Encoding") == null) {
            this.transparentGzip = true;
            result.header("Accept-Encoding", "gzip");
        }
        final CookieHandler cookieHandler = this.client.getCookieHandler();
        if (cookieHandler != null) {
            final Map<String, List<String>> headers = OkHeaders.toMultimap(result.build().headers(), null);
            final Map<String, List<String>> cookies = cookieHandler.get(request.uri(), headers);
            OkHeaders.addCookies(result, cookies);
        }
        if (request.header("User-Agent") == null) {
            result.header("User-Agent", Version.userAgent());
        }
        return result.build();
    }
    
    public void readResponse() throws IOException {
        if (this.userResponse != null) {
            return;
        }
        if (this.networkRequest == null && this.cacheResponse == null) {
            throw new IllegalStateException("call sendRequest() first!");
        }
        if (this.networkRequest == null) {
            return;
        }
        Response networkResponse;
        if (this.forWebSocket) {
            this.httpStream.writeRequestHeaders(this.networkRequest);
            networkResponse = this.readNetworkResponse();
        }
        else if (!this.callerWritesRequestBody) {
            networkResponse = new NetworkInterceptorChain(0, this.networkRequest).proceed(this.networkRequest);
        }
        else {
            if (this.bufferedRequestBody != null && this.bufferedRequestBody.buffer().size() > 0L) {
                this.bufferedRequestBody.emit();
            }
            if (this.sentRequestMillis == -1L) {
                if (OkHeaders.contentLength(this.networkRequest) == -1L && this.requestBodyOut instanceof RetryableSink) {
                    final long contentLength = ((RetryableSink)this.requestBodyOut).contentLength();
                    this.networkRequest = this.networkRequest.newBuilder().header("Content-Length", Long.toString(contentLength)).build();
                }
                this.httpStream.writeRequestHeaders(this.networkRequest);
            }
            if (this.requestBodyOut != null) {
                if (this.bufferedRequestBody != null) {
                    this.bufferedRequestBody.close();
                }
                else {
                    this.requestBodyOut.close();
                }
                if (this.requestBodyOut instanceof RetryableSink) {
                    this.httpStream.writeRequestBody((RetryableSink)this.requestBodyOut);
                }
            }
            networkResponse = this.readNetworkResponse();
        }
        this.receiveHeaders(networkResponse.headers());
        if (this.cacheResponse != null) {
            if (validate(this.cacheResponse, networkResponse)) {
                this.userResponse = this.cacheResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).headers(combine(this.cacheResponse.headers(), networkResponse.headers())).cacheResponse(stripBody(this.cacheResponse)).networkResponse(stripBody(networkResponse)).build();
                networkResponse.body().close();
                this.releaseStreamAllocation();
                final InternalCache responseCache = Internal.instance.internalCache(this.client);
                responseCache.trackConditionalCacheHit();
                responseCache.update(this.cacheResponse, stripBody(this.userResponse));
                this.userResponse = this.unzip(this.userResponse);
                return;
            }
            Util.closeQuietly(this.cacheResponse.body());
        }
        this.userResponse = networkResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).cacheResponse(stripBody(this.cacheResponse)).networkResponse(stripBody(networkResponse)).build();
        if (hasBody(this.userResponse)) {
            this.maybeCache();
            this.userResponse = this.unzip(this.cacheWritingResponse(this.storeRequest, this.userResponse));
        }
    }
    
    private Response readNetworkResponse() throws IOException {
        this.httpStream.finishRequest();
        Response networkResponse = this.httpStream.readResponseHeaders().request(this.networkRequest).handshake(this.streamAllocation.connection().getHandshake()).header(OkHeaders.SENT_MILLIS, Long.toString(this.sentRequestMillis)).header(OkHeaders.RECEIVED_MILLIS, Long.toString(System.currentTimeMillis())).build();
        if (!this.forWebSocket) {
            networkResponse = networkResponse.newBuilder().body(this.httpStream.openResponseBody(networkResponse)).build();
        }
        if ("close".equalsIgnoreCase(networkResponse.request().header("Connection")) || "close".equalsIgnoreCase(networkResponse.header("Connection"))) {
            this.streamAllocation.noNewStreams();
        }
        return networkResponse;
    }
    
    private Response cacheWritingResponse(final CacheRequest cacheRequest, final Response response) throws IOException {
        if (cacheRequest == null) {
            return response;
        }
        final Sink cacheBodyUnbuffered = cacheRequest.body();
        if (cacheBodyUnbuffered == null) {
            return response;
        }
        final BufferedSource source = response.body().source();
        final BufferedSink cacheBody = Okio.buffer(cacheBodyUnbuffered);
        final Source cacheWritingSource = new Source() {
            boolean cacheRequestClosed;
            
            @Override
            public long read(final Buffer sink, final long byteCount) throws IOException {
                long bytesRead;
                try {
                    bytesRead = source.read(sink, byteCount);
                }
                catch (IOException e) {
                    if (!this.cacheRequestClosed) {
                        this.cacheRequestClosed = true;
                        cacheRequest.abort();
                    }
                    throw e;
                }
                if (bytesRead == -1L) {
                    if (!this.cacheRequestClosed) {
                        this.cacheRequestClosed = true;
                        cacheBody.close();
                    }
                    return -1L;
                }
                sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
                cacheBody.emitCompleteSegments();
                return bytesRead;
            }
            
            @Override
            public Timeout timeout() {
                return source.timeout();
            }
            
            @Override
            public void close() throws IOException {
                if (!this.cacheRequestClosed && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    this.cacheRequestClosed = true;
                    cacheRequest.abort();
                }
                source.close();
            }
        };
        return response.newBuilder().body(new RealResponseBody(response.headers(), Okio.buffer(cacheWritingSource))).build();
    }
    
    private static boolean validate(final Response cached, final Response network) {
        if (network.code() == 304) {
            return true;
        }
        final Date lastModified = cached.headers().getDate("Last-Modified");
        if (lastModified != null) {
            final Date networkLastModified = network.headers().getDate("Last-Modified");
            if (networkLastModified != null && networkLastModified.getTime() < lastModified.getTime()) {
                return true;
            }
        }
        return false;
    }
    
    private static Headers combine(final Headers cachedHeaders, final Headers networkHeaders) throws IOException {
        final Headers.Builder result = new Headers.Builder();
        for (int i = 0, size = cachedHeaders.size(); i < size; ++i) {
            final String fieldName = cachedHeaders.name(i);
            final String value = cachedHeaders.value(i);
            if (!"Warning".equalsIgnoreCase(fieldName) || !value.startsWith("1")) {
                if (!OkHeaders.isEndToEnd(fieldName) || networkHeaders.get(fieldName) == null) {
                    result.add(fieldName, value);
                }
            }
        }
        for (int i = 0, size = networkHeaders.size(); i < size; ++i) {
            final String fieldName = networkHeaders.name(i);
            if (!"Content-Length".equalsIgnoreCase(fieldName)) {
                if (OkHeaders.isEndToEnd(fieldName)) {
                    result.add(fieldName, networkHeaders.value(i));
                }
            }
        }
        return result.build();
    }
    
    public void receiveHeaders(final Headers headers) throws IOException {
        final CookieHandler cookieHandler = this.client.getCookieHandler();
        if (cookieHandler != null) {
            cookieHandler.put(this.userRequest.uri(), OkHeaders.toMultimap(headers, null));
        }
    }
    
    public Request followUpRequest() throws IOException {
        if (this.userResponse == null) {
            throw new IllegalStateException();
        }
        final Connection connection = this.streamAllocation.connection();
        final Route route = (connection != null) ? connection.getRoute() : null;
        final Proxy selectedProxy = (route != null) ? route.getProxy() : this.client.getProxy();
        final int responseCode = this.userResponse.code();
        final String method = this.userRequest.method();
        switch (responseCode) {
            case 407: {
                if (selectedProxy.type() != Proxy.Type.HTTP) {
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                }
                return OkHeaders.processAuthHeader(this.client.getAuthenticator(), this.userResponse, selectedProxy);
            }
            case 401: {
                return OkHeaders.processAuthHeader(this.client.getAuthenticator(), this.userResponse, selectedProxy);
            }
            case 307:
            case 308: {
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    return null;
                }
            }
            case 300:
            case 301:
            case 302:
            case 303: {
                if (!this.client.getFollowRedirects()) {
                    return null;
                }
                final String location = this.userResponse.header("Location");
                if (location == null) {
                    return null;
                }
                final HttpUrl url = this.userRequest.httpUrl().resolve(location);
                if (url == null) {
                    return null;
                }
                final boolean sameScheme = url.scheme().equals(this.userRequest.httpUrl().scheme());
                if (!sameScheme && !this.client.getFollowSslRedirects()) {
                    return null;
                }
                final Request.Builder requestBuilder = this.userRequest.newBuilder();
                if (HttpMethod.permitsRequestBody(method)) {
                    if (HttpMethod.redirectsToGet(method)) {
                        requestBuilder.method("GET", null);
                    }
                    else {
                        requestBuilder.method(method, null);
                    }
                    requestBuilder.removeHeader("Transfer-Encoding");
                    requestBuilder.removeHeader("Content-Length");
                    requestBuilder.removeHeader("Content-Type");
                }
                if (!this.sameConnection(url)) {
                    requestBuilder.removeHeader("Authorization");
                }
                return requestBuilder.url(url).build();
            }
            default: {
                return null;
            }
        }
    }
    
    public boolean sameConnection(final HttpUrl followUp) {
        final HttpUrl url = this.userRequest.httpUrl();
        return url.host().equals(followUp.host()) && url.port() == followUp.port() && url.scheme().equals(followUp.scheme());
    }
    
    private static Address createAddress(final OkHttpClient client, final Request request) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (request.isHttps()) {
            sslSocketFactory = client.getSslSocketFactory();
            hostnameVerifier = client.getHostnameVerifier();
            certificatePinner = client.getCertificatePinner();
        }
        return new Address(request.httpUrl().host(), request.httpUrl().port(), client.getDns(), client.getSocketFactory(), sslSocketFactory, hostnameVerifier, certificatePinner, client.getAuthenticator(), client.getProxy(), client.getProtocols(), client.getConnectionSpecs(), client.getProxySelector());
    }
    
    static {
        EMPTY_BODY = new ResponseBody() {
            @Override
            public MediaType contentType() {
                return null;
            }
            
            @Override
            public long contentLength() {
                return 0L;
            }
            
            @Override
            public BufferedSource source() {
                return new Buffer();
            }
        };
    }
    
    class NetworkInterceptorChain implements Interceptor.Chain
    {
        private final int index;
        private final Request request;
        private int calls;
        
        NetworkInterceptorChain(final int index, final Request request) {
            this.index = index;
            this.request = request;
        }
        
        @Override
        public Connection connection() {
            return HttpEngine.this.streamAllocation.connection();
        }
        
        @Override
        public Request request() {
            return this.request;
        }
        
        @Override
        public Response proceed(final Request request) throws IOException {
            ++this.calls;
            if (this.index > 0) {
                final Interceptor caller = HttpEngine.this.client.networkInterceptors().get(this.index - 1);
                final Address address = this.connection().getRoute().getAddress();
                if (!request.httpUrl().host().equals(address.getUriHost()) || request.httpUrl().port() != address.getUriPort()) {
                    throw new IllegalStateException("network interceptor " + caller + " must retain the same host and port");
                }
                if (this.calls > 1) {
                    throw new IllegalStateException("network interceptor " + caller + " must call proceed() exactly once");
                }
            }
            if (this.index < HttpEngine.this.client.networkInterceptors().size()) {
                final NetworkInterceptorChain chain = new NetworkInterceptorChain(this.index + 1, request);
                final Interceptor interceptor = HttpEngine.this.client.networkInterceptors().get(this.index);
                final Response interceptedResponse = interceptor.intercept(chain);
                if (chain.calls != 1) {
                    throw new IllegalStateException("network interceptor " + interceptor + " must call proceed() exactly once");
                }
                if (interceptedResponse == null) {
                    throw new NullPointerException("network interceptor " + interceptor + " returned null");
                }
                return interceptedResponse;
            }
            else {
                HttpEngine.this.httpStream.writeRequestHeaders(request);
                HttpEngine.this.networkRequest = request;
                if (HttpEngine.this.permitsRequestBody(request) && request.body() != null) {
                    final Sink requestBodyOut = HttpEngine.this.httpStream.createRequestBody(request, request.body().contentLength());
                    final BufferedSink bufferedRequestBody = Okio.buffer(requestBodyOut);
                    request.body().writeTo(bufferedRequestBody);
                    bufferedRequestBody.close();
                }
                final Response response = HttpEngine.this.readNetworkResponse();
                final int code = response.code();
                if ((code == 204 || code == 205) && response.body().contentLength() > 0L) {
                    throw new ProtocolException("HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
                }
                return response;
            }
        }
    }
}
