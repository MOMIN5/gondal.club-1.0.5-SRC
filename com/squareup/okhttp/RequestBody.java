// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import okio.Source;
import java.io.Closeable;
import okio.Okio;
import java.io.File;
import okio.ByteString;
import java.nio.charset.Charset;
import com.squareup.okhttp.internal.Util;
import okio.BufferedSink;
import java.io.IOException;

public abstract class RequestBody
{
    public abstract MediaType contentType();
    
    public long contentLength() throws IOException {
        return -1L;
    }
    
    public abstract void writeTo(final BufferedSink p0) throws IOException;
    
    public static RequestBody create(MediaType contentType, final String content) {
        Charset charset = Util.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Util.UTF_8;
                contentType = MediaType.parse(contentType + "; charset=utf-8");
            }
        }
        final byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }
    
    public static RequestBody create(final MediaType contentType, final ByteString content) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }
            
            @Override
            public long contentLength() throws IOException {
                return content.size();
            }
            
            @Override
            public void writeTo(final BufferedSink sink) throws IOException {
                sink.write(content);
            }
        };
    }
    
    public static RequestBody create(final MediaType contentType, final byte[] content) {
        return create(contentType, content, 0, content.length);
    }
    
    public static RequestBody create(final MediaType contentType, final byte[] content, final int offset, final int byteCount) {
        if (content == null) {
            throw new NullPointerException("content == null");
        }
        Util.checkOffsetAndCount(content.length, offset, byteCount);
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }
            
            @Override
            public long contentLength() {
                return byteCount;
            }
            
            @Override
            public void writeTo(final BufferedSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }
        };
    }
    
    public static RequestBody create(final MediaType contentType, final File file) {
        if (file == null) {
            throw new NullPointerException("content == null");
        }
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }
            
            @Override
            public long contentLength() {
                return file.length();
            }
            
            @Override
            public void writeTo(final BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(file);
                    sink.writeAll(source);
                }
                finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }
}
