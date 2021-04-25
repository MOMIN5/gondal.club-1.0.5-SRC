// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import com.squareup.okhttp.internal.Platform;
import java.io.IOException;
import java.net.Proxy;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Challenge;
import java.util.TreeSet;
import java.util.Set;
import com.squareup.okhttp.internal.Util;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Request;
import java.util.Comparator;

public final class OkHeaders
{
    private static final Comparator<String> FIELD_NAME_COMPARATOR;
    static final String PREFIX;
    public static final String SENT_MILLIS;
    public static final String RECEIVED_MILLIS;
    public static final String SELECTED_PROTOCOL;
    public static final String RESPONSE_SOURCE;
    
    private OkHeaders() {
    }
    
    public static long contentLength(final Request request) {
        return contentLength(request.headers());
    }
    
    public static long contentLength(final Response response) {
        return contentLength(response.headers());
    }
    
    public static long contentLength(final Headers headers) {
        return stringToLong(headers.get("Content-Length"));
    }
    
    private static long stringToLong(final String s) {
        if (s == null) {
            return -1L;
        }
        try {
            return Long.parseLong(s);
        }
        catch (NumberFormatException e) {
            return -1L;
        }
    }
    
    public static Map<String, List<String>> toMultimap(final Headers headers, final String valueForNullKey) {
        final Map<String, List<String>> result = new TreeMap<String, List<String>>(OkHeaders.FIELD_NAME_COMPARATOR);
        for (int i = 0, size = headers.size(); i < size; ++i) {
            final String fieldName = headers.name(i);
            final String value = headers.value(i);
            final List<String> allValues = new ArrayList<String>();
            final List<String> otherValues = result.get(fieldName);
            if (otherValues != null) {
                allValues.addAll(otherValues);
            }
            allValues.add(value);
            result.put(fieldName, Collections.unmodifiableList((List<? extends String>)allValues));
        }
        if (valueForNullKey != null) {
            result.put(null, Collections.unmodifiableList((List<? extends String>)Collections.singletonList((T)valueForNullKey)));
        }
        return Collections.unmodifiableMap((Map<? extends String, ? extends List<String>>)result);
    }
    
    public static void addCookies(final Request.Builder builder, final Map<String, List<String>> cookieHeaders) {
        for (final Map.Entry<String, List<String>> entry : cookieHeaders.entrySet()) {
            final String key = entry.getKey();
            if (("Cookie".equalsIgnoreCase(key) || "Cookie2".equalsIgnoreCase(key)) && !entry.getValue().isEmpty()) {
                builder.addHeader(key, buildCookieHeader(entry.getValue()));
            }
        }
    }
    
    private static String buildCookieHeader(final List<String> cookies) {
        if (cookies.size() == 1) {
            return cookies.get(0);
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, size = cookies.size(); i < size; ++i) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(cookies.get(i));
        }
        return sb.toString();
    }
    
    public static boolean varyMatches(final Response cachedResponse, final Headers cachedRequest, final Request newRequest) {
        for (final String field : varyFields(cachedResponse)) {
            if (!Util.equal(cachedRequest.values(field), newRequest.headers(field))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean hasVaryAll(final Response response) {
        return hasVaryAll(response.headers());
    }
    
    public static boolean hasVaryAll(final Headers responseHeaders) {
        return varyFields(responseHeaders).contains("*");
    }
    
    private static Set<String> varyFields(final Response response) {
        return varyFields(response.headers());
    }
    
    public static Set<String> varyFields(final Headers responseHeaders) {
        Set<String> result = Collections.emptySet();
        for (int i = 0, size = responseHeaders.size(); i < size; ++i) {
            if ("Vary".equalsIgnoreCase(responseHeaders.name(i))) {
                final String value = responseHeaders.value(i);
                if (result.isEmpty()) {
                    result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                }
                for (final String varyField : value.split(",")) {
                    result.add(varyField.trim());
                }
            }
        }
        return result;
    }
    
    public static Headers varyHeaders(final Response response) {
        final Headers requestHeaders = response.networkResponse().request().headers();
        final Headers responseHeaders = response.headers();
        return varyHeaders(requestHeaders, responseHeaders);
    }
    
    public static Headers varyHeaders(final Headers requestHeaders, final Headers responseHeaders) {
        final Set<String> varyFields = varyFields(responseHeaders);
        if (varyFields.isEmpty()) {
            return new Headers.Builder().build();
        }
        final Headers.Builder result = new Headers.Builder();
        for (int i = 0, size = requestHeaders.size(); i < size; ++i) {
            final String fieldName = requestHeaders.name(i);
            if (varyFields.contains(fieldName)) {
                result.add(fieldName, requestHeaders.value(i));
            }
        }
        return result.build();
    }
    
    static boolean isEndToEnd(final String fieldName) {
        return !"Connection".equalsIgnoreCase(fieldName) && !"Keep-Alive".equalsIgnoreCase(fieldName) && !"Proxy-Authenticate".equalsIgnoreCase(fieldName) && !"Proxy-Authorization".equalsIgnoreCase(fieldName) && !"TE".equalsIgnoreCase(fieldName) && !"Trailers".equalsIgnoreCase(fieldName) && !"Transfer-Encoding".equalsIgnoreCase(fieldName) && !"Upgrade".equalsIgnoreCase(fieldName);
    }
    
    public static List<Challenge> parseChallenges(final Headers responseHeaders, final String challengeHeader) {
        final List<Challenge> result = new ArrayList<Challenge>();
        for (int i = 0, size = responseHeaders.size(); i < size; ++i) {
            if (challengeHeader.equalsIgnoreCase(responseHeaders.name(i))) {
                final String value = responseHeaders.value(i);
                int pos = 0;
                while (pos < value.length()) {
                    final int tokenStart = pos;
                    pos = HeaderParser.skipUntil(value, pos, " ");
                    final String scheme = value.substring(tokenStart, pos).trim();
                    pos = HeaderParser.skipWhitespace(value, pos);
                    if (!value.regionMatches(true, pos, "realm=\"", 0, "realm=\"".length())) {
                        break;
                    }
                    final int realmStart;
                    pos = (realmStart = pos + "realm=\"".length());
                    pos = HeaderParser.skipUntil(value, pos, "\"");
                    final String realm = value.substring(realmStart, pos);
                    ++pos;
                    pos = HeaderParser.skipUntil(value, pos, ",");
                    ++pos;
                    pos = HeaderParser.skipWhitespace(value, pos);
                    result.add(new Challenge(scheme, realm));
                }
            }
        }
        return result;
    }
    
    public static Request processAuthHeader(final Authenticator authenticator, final Response response, final Proxy proxy) throws IOException {
        return (response.code() == 407) ? authenticator.authenticateProxy(proxy, response) : authenticator.authenticate(proxy, response);
    }
    
    static {
        FIELD_NAME_COMPARATOR = new Comparator<String>() {
            @Override
            public int compare(final String a, final String b) {
                if (a == b) {
                    return 0;
                }
                if (a == null) {
                    return -1;
                }
                if (b == null) {
                    return 1;
                }
                return String.CASE_INSENSITIVE_ORDER.compare(a, b);
            }
        };
        PREFIX = Platform.get().getPrefix();
        SENT_MILLIS = OkHeaders.PREFIX + "-Sent-Millis";
        RECEIVED_MILLIS = OkHeaders.PREFIX + "-Received-Millis";
        SELECTED_PROTOCOL = OkHeaders.PREFIX + "-Selected-Protocol";
        RESPONSE_SOURCE = OkHeaders.PREFIX + "-Response-Source";
    }
}
