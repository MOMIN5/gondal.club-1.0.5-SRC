// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import okio.ForwardingSource;
import com.squareup.okhttp.internal.framed.ErrorCode;
import okio.Source;
import okio.Okio;
import com.squareup.okhttp.ResponseBody;
import java.net.ProtocolException;
import java.util.Set;
import com.squareup.okhttp.Headers;
import java.util.Locale;
import java.util.LinkedHashSet;
import com.squareup.okhttp.internal.Util;
import java.util.ArrayList;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.framed.Header;
import java.util.concurrent.TimeUnit;
import com.squareup.okhttp.Protocol;
import java.io.IOException;
import okio.Sink;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.internal.framed.FramedStream;
import com.squareup.okhttp.internal.framed.FramedConnection;
import java.util.List;
import okio.ByteString;

public final class Http2xStream implements HttpStream
{
    private static final ByteString CONNECTION;
    private static final ByteString HOST;
    private static final ByteString KEEP_ALIVE;
    private static final ByteString PROXY_CONNECTION;
    private static final ByteString TRANSFER_ENCODING;
    private static final ByteString TE;
    private static final ByteString ENCODING;
    private static final ByteString UPGRADE;
    private static final List<ByteString> SPDY_3_SKIPPED_REQUEST_HEADERS;
    private static final List<ByteString> SPDY_3_SKIPPED_RESPONSE_HEADERS;
    private static final List<ByteString> HTTP_2_SKIPPED_REQUEST_HEADERS;
    private static final List<ByteString> HTTP_2_SKIPPED_RESPONSE_HEADERS;
    private final StreamAllocation streamAllocation;
    private final FramedConnection framedConnection;
    private HttpEngine httpEngine;
    private FramedStream stream;
    
    public Http2xStream(final StreamAllocation streamAllocation, final FramedConnection framedConnection) {
        this.streamAllocation = streamAllocation;
        this.framedConnection = framedConnection;
    }
    
    @Override
    public void setHttpEngine(final HttpEngine httpEngine) {
        this.httpEngine = httpEngine;
    }
    
    @Override
    public Sink createRequestBody(final Request request, final long contentLength) throws IOException {
        return this.stream.getSink();
    }
    
    @Override
    public void writeRequestHeaders(final Request request) throws IOException {
        if (this.stream != null) {
            return;
        }
        this.httpEngine.writingRequestHeaders();
        final boolean permitsRequestBody = this.httpEngine.permitsRequestBody(request);
        final List<Header> requestHeaders = (this.framedConnection.getProtocol() == Protocol.HTTP_2) ? http2HeadersList(request) : spdy3HeadersList(request);
        final boolean hasResponseBody = true;
        this.stream = this.framedConnection.newStream(requestHeaders, permitsRequestBody, hasResponseBody);
        this.stream.readTimeout().timeout(this.httpEngine.client.getReadTimeout(), TimeUnit.MILLISECONDS);
        this.stream.writeTimeout().timeout(this.httpEngine.client.getWriteTimeout(), TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void writeRequestBody(final RetryableSink requestBody) throws IOException {
        requestBody.writeToSocket(this.stream.getSink());
    }
    
    @Override
    public void finishRequest() throws IOException {
        this.stream.getSink().close();
    }
    
    @Override
    public Response.Builder readResponseHeaders() throws IOException {
        return (this.framedConnection.getProtocol() == Protocol.HTTP_2) ? readHttp2HeadersList(this.stream.getResponseHeaders()) : readSpdy3HeadersList(this.stream.getResponseHeaders());
    }
    
    public static List<Header> spdy3HeadersList(final Request request) {
        final Headers headers = request.headers();
        final List<Header> result = new ArrayList<Header>(headers.size() + 5);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.httpUrl())));
        result.add(new Header(Header.VERSION, "HTTP/1.1"));
        result.add(new Header(Header.TARGET_HOST, Util.hostHeader(request.httpUrl())));
        result.add(new Header(Header.TARGET_SCHEME, request.httpUrl().scheme()));
        final Set<ByteString> names = new LinkedHashSet<ByteString>();
        for (int i = 0, size = headers.size(); i < size; ++i) {
            final ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!Http2xStream.SPDY_3_SKIPPED_REQUEST_HEADERS.contains(name)) {
                final String value = headers.value(i);
                if (names.add(name)) {
                    result.add(new Header(name, value));
                }
                else {
                    for (int j = 0; j < result.size(); ++j) {
                        if (result.get(j).name.equals(name)) {
                            final String concatenated = joinOnNull(result.get(j).value.utf8(), value);
                            result.set(j, new Header(name, concatenated));
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private static String joinOnNull(final String first, final String second) {
        return first + '\0' + second;
    }
    
    public static List<Header> http2HeadersList(final Request request) {
        final Headers headers = request.headers();
        final List<Header> result = new ArrayList<Header>(headers.size() + 4);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.httpUrl())));
        result.add(new Header(Header.TARGET_AUTHORITY, Util.hostHeader(request.httpUrl())));
        result.add(new Header(Header.TARGET_SCHEME, request.httpUrl().scheme()));
        for (int i = 0, size = headers.size(); i < size; ++i) {
            final ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!Http2xStream.HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name)) {
                result.add(new Header(name, headers.value(i)));
            }
        }
        return result;
    }
    
    public static Response.Builder readSpdy3HeadersList(final List<Header> headerBlock) throws IOException {
        String status = null;
        String version = "HTTP/1.1";
        final Headers.Builder headersBuilder = new Headers.Builder();
        for (int i = 0, size = headerBlock.size(); i < size; ++i) {
            final ByteString name = headerBlock.get(i).name;
            final String values = headerBlock.get(i).value.utf8();
            int end;
            for (int start = 0; start < values.length(); start = end + 1) {
                end = values.indexOf(0, start);
                if (end == -1) {
                    end = values.length();
                }
                final String value = values.substring(start, end);
                if (name.equals(Header.RESPONSE_STATUS)) {
                    status = value;
                }
                else if (name.equals(Header.VERSION)) {
                    version = value;
                }
                else if (!Http2xStream.SPDY_3_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                    headersBuilder.add(name.utf8(), value);
                }
            }
        }
        if (status == null) {
            throw new ProtocolException("Expected ':status' header not present");
        }
        final StatusLine statusLine = StatusLine.parse(version + " " + status);
        return new Response.Builder().protocol(Protocol.SPDY_3).code(statusLine.code).message(statusLine.message).headers(headersBuilder.build());
    }
    
    public static Response.Builder readHttp2HeadersList(final List<Header> headerBlock) throws IOException {
        String status = null;
        final Headers.Builder headersBuilder = new Headers.Builder();
        for (int i = 0, size = headerBlock.size(); i < size; ++i) {
            final ByteString name = headerBlock.get(i).name;
            final String value = headerBlock.get(i).value.utf8();
            if (name.equals(Header.RESPONSE_STATUS)) {
                status = value;
            }
            else if (!Http2xStream.HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                headersBuilder.add(name.utf8(), value);
            }
        }
        if (status == null) {
            throw new ProtocolException("Expected ':status' header not present");
        }
        final StatusLine statusLine = StatusLine.parse("HTTP/1.1 " + status);
        return new Response.Builder().protocol(Protocol.HTTP_2).code(statusLine.code).message(statusLine.message).headers(headersBuilder.build());
    }
    
    @Override
    public ResponseBody openResponseBody(final Response response) throws IOException {
        final Source source = new StreamFinishingSource(this.stream.getSource());
        return new RealResponseBody(response.headers(), Okio.buffer(source));
    }
    
    @Override
    public void cancel() {
        if (this.stream != null) {
            this.stream.closeLater(ErrorCode.CANCEL);
        }
    }
    
    static {
        CONNECTION = ByteString.encodeUtf8("connection");
        HOST = ByteString.encodeUtf8("host");
        KEEP_ALIVE = ByteString.encodeUtf8("keep-alive");
        PROXY_CONNECTION = ByteString.encodeUtf8("proxy-connection");
        TRANSFER_ENCODING = ByteString.encodeUtf8("transfer-encoding");
        TE = ByteString.encodeUtf8("te");
        ENCODING = ByteString.encodeUtf8("encoding");
        UPGRADE = ByteString.encodeUtf8("upgrade");
        SPDY_3_SKIPPED_REQUEST_HEADERS = Util.immutableList(Http2xStream.CONNECTION, Http2xStream.HOST, Http2xStream.KEEP_ALIVE, Http2xStream.PROXY_CONNECTION, Http2xStream.TRANSFER_ENCODING, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY, Header.TARGET_HOST, Header.VERSION);
        SPDY_3_SKIPPED_RESPONSE_HEADERS = Util.immutableList(Http2xStream.CONNECTION, Http2xStream.HOST, Http2xStream.KEEP_ALIVE, Http2xStream.PROXY_CONNECTION, Http2xStream.TRANSFER_ENCODING);
        HTTP_2_SKIPPED_REQUEST_HEADERS = Util.immutableList(Http2xStream.CONNECTION, Http2xStream.HOST, Http2xStream.KEEP_ALIVE, Http2xStream.PROXY_CONNECTION, Http2xStream.TE, Http2xStream.TRANSFER_ENCODING, Http2xStream.ENCODING, Http2xStream.UPGRADE, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY, Header.TARGET_HOST, Header.VERSION);
        HTTP_2_SKIPPED_RESPONSE_HEADERS = Util.immutableList(Http2xStream.CONNECTION, Http2xStream.HOST, Http2xStream.KEEP_ALIVE, Http2xStream.PROXY_CONNECTION, Http2xStream.TE, Http2xStream.TRANSFER_ENCODING, Http2xStream.ENCODING, Http2xStream.UPGRADE);
    }
    
    class StreamFinishingSource extends ForwardingSource
    {
        public StreamFinishingSource(final Source delegate) {
            super(delegate);
        }
        
        @Override
        public void close() throws IOException {
            Http2xStream.this.streamAllocation.streamFinished(Http2xStream.this);
            super.close();
        }
    }
}
