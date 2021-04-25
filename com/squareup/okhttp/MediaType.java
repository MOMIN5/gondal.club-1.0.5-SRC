// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MediaType
{
    private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
    private static final String QUOTED = "\"([^\"]*)\"";
    private static final Pattern TYPE_SUBTYPE;
    private static final Pattern PARAMETER;
    private final String mediaType;
    private final String type;
    private final String subtype;
    private final String charset;
    
    private MediaType(final String mediaType, final String type, final String subtype, final String charset) {
        this.mediaType = mediaType;
        this.type = type;
        this.subtype = subtype;
        this.charset = charset;
    }
    
    public static MediaType parse(final String string) {
        final Matcher typeSubtype = MediaType.TYPE_SUBTYPE.matcher(string);
        if (!typeSubtype.lookingAt()) {
            return null;
        }
        final String type = typeSubtype.group(1).toLowerCase(Locale.US);
        final String subtype = typeSubtype.group(2).toLowerCase(Locale.US);
        String charset = null;
        final Matcher parameter = MediaType.PARAMETER.matcher(string);
        for (int s = typeSubtype.end(); s < string.length(); s = parameter.end()) {
            parameter.region(s, string.length());
            if (!parameter.lookingAt()) {
                return null;
            }
            final String name = parameter.group(1);
            if (name != null) {
                if (name.equalsIgnoreCase("charset")) {
                    final String charsetParameter = (parameter.group(2) != null) ? parameter.group(2) : parameter.group(3);
                    if (charset != null && !charsetParameter.equalsIgnoreCase(charset)) {
                        throw new IllegalArgumentException("Multiple different charsets: " + string);
                    }
                    charset = charsetParameter;
                }
            }
        }
        return new MediaType(string, type, subtype, charset);
    }
    
    public String type() {
        return this.type;
    }
    
    public String subtype() {
        return this.subtype;
    }
    
    public Charset charset() {
        return (this.charset != null) ? Charset.forName(this.charset) : null;
    }
    
    public Charset charset(final Charset defaultValue) {
        return (this.charset != null) ? Charset.forName(this.charset) : defaultValue;
    }
    
    @Override
    public String toString() {
        return this.mediaType;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof MediaType && ((MediaType)o).mediaType.equals(this.mediaType);
    }
    
    @Override
    public int hashCode() {
        return this.mediaType.hashCode();
    }
    
    static {
        TYPE_SUBTYPE = Pattern.compile("([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)/([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)");
        PARAMETER = Pattern.compile(";\\s*(?:([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)=(?:([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)|\"([^\"]*)\"))?");
    }
}
