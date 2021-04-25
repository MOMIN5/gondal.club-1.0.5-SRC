// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import okio.ForwardingSource;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import okio.ByteString;
import okio.Buffer;
import java.util.ArrayList;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import okio.BufferedSink;
import java.security.cert.Certificate;
import java.util.List;
import com.squareup.okhttp.internal.http.StatusLine;
import okio.Source;
import okio.ForwardingSink;
import okio.Sink;
import java.util.NoSuchElementException;
import okio.BufferedSource;
import okio.Okio;
import java.util.Iterator;
import com.squareup.okhttp.internal.http.OkHeaders;
import com.squareup.okhttp.internal.http.HttpMethod;
import java.io.Closeable;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.http.CacheStrategy;
import com.squareup.okhttp.internal.http.CacheRequest;
import java.io.IOException;
import com.squareup.okhttp.internal.io.FileSystem;
import java.io.File;
import com.squareup.okhttp.internal.DiskLruCache;
import com.squareup.okhttp.internal.InternalCache;

public final class Cache
{
    private static final int VERSION = 201105;
    private static final int ENTRY_METADATA = 0;
    private static final int ENTRY_BODY = 1;
    private static final int ENTRY_COUNT = 2;
    final InternalCache internalCache;
    private final DiskLruCache cache;
    private int writeSuccessCount;
    private int writeAbortCount;
    private int networkCount;
    private int hitCount;
    private int requestCount;
    
    public Cache(final File directory, final long maxSize) {
        this(directory, maxSize, FileSystem.SYSTEM);
    }
    
    Cache(final File directory, final long maxSize, final FileSystem fileSystem) {
        this.internalCache = new InternalCache() {
            @Override
            public Response get(final Request request) throws IOException {
                return Cache.this.get(request);
            }
            
            @Override
            public CacheRequest put(final Response response) throws IOException {
                return Cache.this.put(response);
            }
            
            @Override
            public void remove(final Request request) throws IOException {
                Cache.this.remove(request);
            }
            
            @Override
            public void update(final Response cached, final Response network) throws IOException {
                Cache.this.update(cached, network);
            }
            
            @Override
            public void trackConditionalCacheHit() {
                Cache.this.trackConditionalCacheHit();
            }
            
            @Override
            public void trackResponse(final CacheStrategy cacheStrategy) {
                Cache.this.trackResponse(cacheStrategy);
            }
        };
        this.cache = DiskLruCache.create(fileSystem, directory, 201105, 2, maxSize);
    }
    
    private static String urlToKey(final Request request) {
        return Util.md5Hex(request.urlString());
    }
    
    Response get(final Request request) {
        final String key = urlToKey(request);
        DiskLruCache.Snapshot snapshot;
        try {
            snapshot = this.cache.get(key);
            if (snapshot == null) {
                return null;
            }
        }
        catch (IOException e) {
            return null;
        }
        Entry entry;
        try {
            entry = new Entry(snapshot.getSource(0));
        }
        catch (IOException e) {
            Util.closeQuietly(snapshot);
            return null;
        }
        final Response response = entry.response(request, snapshot);
        if (!entry.matches(request, response)) {
            Util.closeQuietly(response.body());
            return null;
        }
        return response;
    }
    
    private CacheRequest put(final Response response) throws IOException {
        final String requestMethod = response.request().method();
        if (HttpMethod.invalidatesCache(response.request().method())) {
            try {
                this.remove(response.request());
            }
            catch (IOException ex) {}
            return null;
        }
        if (!requestMethod.equals("GET")) {
            return null;
        }
        if (OkHeaders.hasVaryAll(response)) {
            return null;
        }
        final Entry entry = new Entry(response);
        DiskLruCache.Editor editor = null;
        try {
            editor = this.cache.edit(urlToKey(response.request()));
            if (editor == null) {
                return null;
            }
            entry.writeTo(editor);
            return new CacheRequestImpl(editor);
        }
        catch (IOException e) {
            this.abortQuietly(editor);
            return null;
        }
    }
    
    private void remove(final Request request) throws IOException {
        this.cache.remove(urlToKey(request));
    }
    
    private void update(final Response cached, final Response network) {
        final Entry entry = new Entry(network);
        final DiskLruCache.Snapshot snapshot = ((CacheResponseBody)cached.body()).snapshot;
        DiskLruCache.Editor editor = null;
        try {
            editor = snapshot.edit();
            if (editor != null) {
                entry.writeTo(editor);
                editor.commit();
            }
        }
        catch (IOException e) {
            this.abortQuietly(editor);
        }
    }
    
    private void abortQuietly(final DiskLruCache.Editor editor) {
        try {
            if (editor != null) {
                editor.abort();
            }
        }
        catch (IOException ex) {}
    }
    
    public void initialize() throws IOException {
        this.cache.initialize();
    }
    
    public void delete() throws IOException {
        this.cache.delete();
    }
    
    public void evictAll() throws IOException {
        this.cache.evictAll();
    }
    
    public Iterator<String> urls() throws IOException {
        return new Iterator<String>() {
            final Iterator<DiskLruCache.Snapshot> delegate = Cache.this.cache.snapshots();
            String nextUrl;
            boolean canRemove;
            
            @Override
            public boolean hasNext() {
                if (this.nextUrl != null) {
                    return true;
                }
                this.canRemove = false;
                while (this.delegate.hasNext()) {
                    final DiskLruCache.Snapshot snapshot = this.delegate.next();
                    try {
                        final BufferedSource metadata = Okio.buffer(snapshot.getSource(0));
                        this.nextUrl = metadata.readUtf8LineStrict();
                        return true;
                    }
                    catch (IOException ex) {}
                    finally {
                        snapshot.close();
                    }
                }
                return false;
            }
            
            @Override
            public String next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                final String result = this.nextUrl;
                this.nextUrl = null;
                this.canRemove = true;
                return result;
            }
            
            @Override
            public void remove() {
                if (!this.canRemove) {
                    throw new IllegalStateException("remove() before next()");
                }
                this.delegate.remove();
            }
        };
    }
    
    public synchronized int getWriteAbortCount() {
        return this.writeAbortCount;
    }
    
    public synchronized int getWriteSuccessCount() {
        return this.writeSuccessCount;
    }
    
    public long getSize() throws IOException {
        return this.cache.size();
    }
    
    public long getMaxSize() {
        return this.cache.getMaxSize();
    }
    
    public void flush() throws IOException {
        this.cache.flush();
    }
    
    public void close() throws IOException {
        this.cache.close();
    }
    
    public File getDirectory() {
        return this.cache.getDirectory();
    }
    
    public boolean isClosed() {
        return this.cache.isClosed();
    }
    
    private synchronized void trackResponse(final CacheStrategy cacheStrategy) {
        ++this.requestCount;
        if (cacheStrategy.networkRequest != null) {
            ++this.networkCount;
        }
        else if (cacheStrategy.cacheResponse != null) {
            ++this.hitCount;
        }
    }
    
    private synchronized void trackConditionalCacheHit() {
        ++this.hitCount;
    }
    
    public synchronized int getNetworkCount() {
        return this.networkCount;
    }
    
    public synchronized int getHitCount() {
        return this.hitCount;
    }
    
    public synchronized int getRequestCount() {
        return this.requestCount;
    }
    
    private static int readInt(final BufferedSource source) throws IOException {
        try {
            final long result = source.readDecimalLong();
            final String line = source.readUtf8LineStrict();
            if (result < 0L || result > 2147483647L || !line.isEmpty()) {
                throw new IOException("expected an int but was \"" + result + line + "\"");
            }
            return (int)result;
        }
        catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private final class CacheRequestImpl implements CacheRequest
    {
        private final DiskLruCache.Editor editor;
        private Sink cacheOut;
        private boolean done;
        private Sink body;
        
        public CacheRequestImpl(final DiskLruCache.Editor editor) throws IOException {
            this.editor = editor;
            this.cacheOut = editor.newSink(1);
            this.body = new ForwardingSink(this.cacheOut) {
                @Override
                public void close() throws IOException {
                    synchronized (Cache.this) {
                        if (CacheRequestImpl.this.done) {
                            return;
                        }
                        CacheRequestImpl.this.done = true;
                        Cache.this.writeSuccessCount++;
                    }
                    super.close();
                    editor.commit();
                }
            };
        }
        
        @Override
        public void abort() {
            synchronized (Cache.this) {
                if (this.done) {
                    return;
                }
                this.done = true;
                Cache.this.writeAbortCount++;
            }
            Util.closeQuietly(this.cacheOut);
            try {
                this.editor.abort();
            }
            catch (IOException ex) {}
        }
        
        @Override
        public Sink body() {
            return this.body;
        }
    }
    
    private static final class Entry
    {
        private final String url;
        private final Headers varyHeaders;
        private final String requestMethod;
        private final Protocol protocol;
        private final int code;
        private final String message;
        private final Headers responseHeaders;
        private final Handshake handshake;
        
        public Entry(final Source in) throws IOException {
            try {
                final BufferedSource source = Okio.buffer(in);
                this.url = source.readUtf8LineStrict();
                this.requestMethod = source.readUtf8LineStrict();
                final Headers.Builder varyHeadersBuilder = new Headers.Builder();
                for (int varyRequestHeaderLineCount = readInt(source), i = 0; i < varyRequestHeaderLineCount; ++i) {
                    varyHeadersBuilder.addLenient(source.readUtf8LineStrict());
                }
                this.varyHeaders = varyHeadersBuilder.build();
                final StatusLine statusLine = StatusLine.parse(source.readUtf8LineStrict());
                this.protocol = statusLine.protocol;
                this.code = statusLine.code;
                this.message = statusLine.message;
                final Headers.Builder responseHeadersBuilder = new Headers.Builder();
                for (int responseHeaderLineCount = readInt(source), j = 0; j < responseHeaderLineCount; ++j) {
                    responseHeadersBuilder.addLenient(source.readUtf8LineStrict());
                }
                this.responseHeaders = responseHeadersBuilder.build();
                if (this.isHttps()) {
                    final String blank = source.readUtf8LineStrict();
                    if (blank.length() > 0) {
                        throw new IOException("expected \"\" but was \"" + blank + "\"");
                    }
                    final String cipherSuite = source.readUtf8LineStrict();
                    final List<Certificate> peerCertificates = this.readCertificateList(source);
                    final List<Certificate> localCertificates = this.readCertificateList(source);
                    this.handshake = Handshake.get(cipherSuite, peerCertificates, localCertificates);
                }
                else {
                    this.handshake = null;
                }
            }
            finally {
                in.close();
            }
        }
        
        public Entry(final Response response) {
            this.url = response.request().urlString();
            this.varyHeaders = OkHeaders.varyHeaders(response);
            this.requestMethod = response.request().method();
            this.protocol = response.protocol();
            this.code = response.code();
            this.message = response.message();
            this.responseHeaders = response.headers();
            this.handshake = response.handshake();
        }
        
        public void writeTo(final DiskLruCache.Editor editor) throws IOException {
            final BufferedSink sink = Okio.buffer(editor.newSink(0));
            sink.writeUtf8(this.url);
            sink.writeByte(10);
            sink.writeUtf8(this.requestMethod);
            sink.writeByte(10);
            sink.writeDecimalLong(this.varyHeaders.size());
            sink.writeByte(10);
            for (int i = 0, size = this.varyHeaders.size(); i < size; ++i) {
                sink.writeUtf8(this.varyHeaders.name(i));
                sink.writeUtf8(": ");
                sink.writeUtf8(this.varyHeaders.value(i));
                sink.writeByte(10);
            }
            sink.writeUtf8(new StatusLine(this.protocol, this.code, this.message).toString());
            sink.writeByte(10);
            sink.writeDecimalLong(this.responseHeaders.size());
            sink.writeByte(10);
            for (int i = 0, size = this.responseHeaders.size(); i < size; ++i) {
                sink.writeUtf8(this.responseHeaders.name(i));
                sink.writeUtf8(": ");
                sink.writeUtf8(this.responseHeaders.value(i));
                sink.writeByte(10);
            }
            if (this.isHttps()) {
                sink.writeByte(10);
                sink.writeUtf8(this.handshake.cipherSuite());
                sink.writeByte(10);
                this.writeCertList(sink, this.handshake.peerCertificates());
                this.writeCertList(sink, this.handshake.localCertificates());
            }
            sink.close();
        }
        
        private boolean isHttps() {
            return this.url.startsWith("https://");
        }
        
        private List<Certificate> readCertificateList(final BufferedSource source) throws IOException {
            final int length = readInt(source);
            if (length == -1) {
                return Collections.emptyList();
            }
            try {
                final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                final List<Certificate> result = new ArrayList<Certificate>(length);
                for (int i = 0; i < length; ++i) {
                    final String line = source.readUtf8LineStrict();
                    final Buffer bytes = new Buffer();
                    bytes.write(ByteString.decodeBase64(line));
                    result.add(certificateFactory.generateCertificate(bytes.inputStream()));
                }
                return result;
            }
            catch (CertificateException e) {
                throw new IOException(e.getMessage());
            }
        }
        
        private void writeCertList(final BufferedSink sink, final List<Certificate> certificates) throws IOException {
            try {
                sink.writeDecimalLong(certificates.size());
                sink.writeByte(10);
                for (int i = 0, size = certificates.size(); i < size; ++i) {
                    final byte[] bytes = certificates.get(i).getEncoded();
                    final String line = ByteString.of(bytes).base64();
                    sink.writeUtf8(line);
                    sink.writeByte(10);
                }
            }
            catch (CertificateEncodingException e) {
                throw new IOException(e.getMessage());
            }
        }
        
        public boolean matches(final Request request, final Response response) {
            return this.url.equals(request.urlString()) && this.requestMethod.equals(request.method()) && OkHeaders.varyMatches(response, this.varyHeaders, request);
        }
        
        public Response response(final Request request, final DiskLruCache.Snapshot snapshot) {
            final String contentType = this.responseHeaders.get("Content-Type");
            final String contentLength = this.responseHeaders.get("Content-Length");
            final Request cacheRequest = new Request.Builder().url(this.url).method(this.requestMethod, null).headers(this.varyHeaders).build();
            return new Response.Builder().request(cacheRequest).protocol(this.protocol).code(this.code).message(this.message).headers(this.responseHeaders).body(new CacheResponseBody(snapshot, contentType, contentLength)).handshake(this.handshake).build();
        }
    }
    
    private static class CacheResponseBody extends ResponseBody
    {
        private final DiskLruCache.Snapshot snapshot;
        private final BufferedSource bodySource;
        private final String contentType;
        private final String contentLength;
        
        public CacheResponseBody(final DiskLruCache.Snapshot snapshot, final String contentType, final String contentLength) {
            this.snapshot = snapshot;
            this.contentType = contentType;
            this.contentLength = contentLength;
            final Source source = snapshot.getSource(1);
            this.bodySource = Okio.buffer(new ForwardingSource(source) {
                @Override
                public void close() throws IOException {
                    snapshot.close();
                    super.close();
                }
            });
        }
        
        @Override
        public MediaType contentType() {
            return (this.contentType != null) ? MediaType.parse(this.contentType) : null;
        }
        
        @Override
        public long contentLength() {
            try {
                return (this.contentLength != null) ? Long.parseLong(this.contentLength) : -1L;
            }
            catch (NumberFormatException e) {
                return -1L;
            }
        }
        
        @Override
        public BufferedSource source() {
            return this.bodySource;
        }
    }
}
