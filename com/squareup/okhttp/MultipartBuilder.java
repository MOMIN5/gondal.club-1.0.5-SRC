// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import okio.Buffer;
import java.io.IOException;
import okio.BufferedSink;
import com.squareup.okhttp.internal.Util;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import okio.ByteString;

public final class MultipartBuilder
{
    public static final MediaType MIXED;
    public static final MediaType ALTERNATIVE;
    public static final MediaType DIGEST;
    public static final MediaType PARALLEL;
    public static final MediaType FORM;
    private static final byte[] COLONSPACE;
    private static final byte[] CRLF;
    private static final byte[] DASHDASH;
    private final ByteString boundary;
    private MediaType type;
    private final List<Headers> partHeaders;
    private final List<RequestBody> partBodies;
    
    public MultipartBuilder() {
        this(UUID.randomUUID().toString());
    }
    
    public MultipartBuilder(final String boundary) {
        this.type = MultipartBuilder.MIXED;
        this.partHeaders = new ArrayList<Headers>();
        this.partBodies = new ArrayList<RequestBody>();
        this.boundary = ByteString.encodeUtf8(boundary);
    }
    
    public MultipartBuilder type(final MediaType type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        if (!type.type().equals("multipart")) {
            throw new IllegalArgumentException("multipart != " + type);
        }
        this.type = type;
        return this;
    }
    
    public MultipartBuilder addPart(final RequestBody body) {
        return this.addPart(null, body);
    }
    
    public MultipartBuilder addPart(final Headers headers, final RequestBody body) {
        if (body == null) {
            throw new NullPointerException("body == null");
        }
        if (headers != null && headers.get("Content-Type") != null) {
            throw new IllegalArgumentException("Unexpected header: Content-Type");
        }
        if (headers != null && headers.get("Content-Length") != null) {
            throw new IllegalArgumentException("Unexpected header: Content-Length");
        }
        this.partHeaders.add(headers);
        this.partBodies.add(body);
        return this;
    }
    
    private static StringBuilder appendQuotedString(final StringBuilder target, final String key) {
        target.append('\"');
        for (int i = 0, len = key.length(); i < len; ++i) {
            final char ch = key.charAt(i);
            switch (ch) {
                case '\n': {
                    target.append("%0A");
                    break;
                }
                case '\r': {
                    target.append("%0D");
                    break;
                }
                case '\"': {
                    target.append("%22");
                    break;
                }
                default: {
                    target.append(ch);
                    break;
                }
            }
        }
        target.append('\"');
        return target;
    }
    
    public MultipartBuilder addFormDataPart(final String name, final String value) {
        return this.addFormDataPart(name, null, RequestBody.create(null, value));
    }
    
    public MultipartBuilder addFormDataPart(final String name, final String filename, final RequestBody value) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        final StringBuilder disposition = new StringBuilder("form-data; name=");
        appendQuotedString(disposition, name);
        if (filename != null) {
            disposition.append("; filename=");
            appendQuotedString(disposition, filename);
        }
        return this.addPart(Headers.of("Content-Disposition", disposition.toString()), value);
    }
    
    public RequestBody build() {
        if (this.partHeaders.isEmpty()) {
            throw new IllegalStateException("Multipart body must have at least one part.");
        }
        return new MultipartRequestBody(this.type, this.boundary, this.partHeaders, this.partBodies);
    }
    
    static {
        MIXED = MediaType.parse("multipart/mixed");
        ALTERNATIVE = MediaType.parse("multipart/alternative");
        DIGEST = MediaType.parse("multipart/digest");
        PARALLEL = MediaType.parse("multipart/parallel");
        FORM = MediaType.parse("multipart/form-data");
        COLONSPACE = new byte[] { 58, 32 };
        CRLF = new byte[] { 13, 10 };
        DASHDASH = new byte[] { 45, 45 };
    }
    
    private static final class MultipartRequestBody extends RequestBody
    {
        private final ByteString boundary;
        private final MediaType contentType;
        private final List<Headers> partHeaders;
        private final List<RequestBody> partBodies;
        private long contentLength;
        
        public MultipartRequestBody(final MediaType type, final ByteString boundary, final List<Headers> partHeaders, final List<RequestBody> partBodies) {
            this.contentLength = -1L;
            if (type == null) {
                throw new NullPointerException("type == null");
            }
            this.boundary = boundary;
            this.contentType = MediaType.parse(type + "; boundary=" + boundary.utf8());
            this.partHeaders = Util.immutableList(partHeaders);
            this.partBodies = Util.immutableList(partBodies);
        }
        
        @Override
        public MediaType contentType() {
            return this.contentType;
        }
        
        @Override
        public long contentLength() throws IOException {
            final long result = this.contentLength;
            if (result != -1L) {
                return result;
            }
            return this.contentLength = this.writeOrCountBytes(null, true);
        }
        
        private long writeOrCountBytes(BufferedSink sink, final boolean countBytes) throws IOException {
            long byteCount = 0L;
            Buffer byteCountBuffer = null;
            if (countBytes) {
                byteCountBuffer = (Buffer)(sink = new Buffer());
            }
            for (int p = 0, partCount = this.partHeaders.size(); p < partCount; ++p) {
                final Headers headers = this.partHeaders.get(p);
                final RequestBody body = this.partBodies.get(p);
                sink.write(MultipartBuilder.DASHDASH);
                sink.write(this.boundary);
                sink.write(MultipartBuilder.CRLF);
                if (headers != null) {
                    for (int h = 0, headerCount = headers.size(); h < headerCount; ++h) {
                        sink.writeUtf8(headers.name(h)).write(MultipartBuilder.COLONSPACE).writeUtf8(headers.value(h)).write(MultipartBuilder.CRLF);
                    }
                }
                final MediaType contentType = body.contentType();
                if (contentType != null) {
                    sink.writeUtf8("Content-Type: ").writeUtf8(contentType.toString()).write(MultipartBuilder.CRLF);
                }
                final long contentLength = body.contentLength();
                if (contentLength != -1L) {
                    sink.writeUtf8("Content-Length: ").writeDecimalLong(contentLength).write(MultipartBuilder.CRLF);
                }
                else if (countBytes) {
                    byteCountBuffer.clear();
                    return -1L;
                }
                sink.write(MultipartBuilder.CRLF);
                if (countBytes) {
                    byteCount += contentLength;
                }
                else {
                    this.partBodies.get(p).writeTo(sink);
                }
                sink.write(MultipartBuilder.CRLF);
            }
            sink.write(MultipartBuilder.DASHDASH);
            sink.write(this.boundary);
            sink.write(MultipartBuilder.DASHDASH);
            sink.write(MultipartBuilder.CRLF);
            if (countBytes) {
                byteCount += byteCountBuffer.size();
                byteCountBuffer.clear();
            }
            return byteCount;
        }
        
        @Override
        public void writeTo(final BufferedSink sink) throws IOException {
            this.writeOrCountBytes(sink, false);
        }
    }
}
