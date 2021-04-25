// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import okio.Buffer;

public final class FormEncodingBuilder
{
    private static final MediaType CONTENT_TYPE;
    private final Buffer content;
    
    public FormEncodingBuilder() {
        this.content = new Buffer();
    }
    
    public FormEncodingBuilder add(final String name, final String value) {
        if (this.content.size() > 0L) {
            this.content.writeByte(38);
        }
        HttpUrl.canonicalize(this.content, name, 0, name.length(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, true, true);
        this.content.writeByte(61);
        HttpUrl.canonicalize(this.content, value, 0, value.length(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, true, true);
        return this;
    }
    
    public FormEncodingBuilder addEncoded(final String name, final String value) {
        if (this.content.size() > 0L) {
            this.content.writeByte(38);
        }
        HttpUrl.canonicalize(this.content, name, 0, name.length(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", true, true, true);
        this.content.writeByte(61);
        HttpUrl.canonicalize(this.content, value, 0, value.length(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", true, true, true);
        return this;
    }
    
    public RequestBody build() {
        return RequestBody.create(FormEncodingBuilder.CONTENT_TYPE, this.content.snapshot());
    }
    
    static {
        CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded");
    }
}
