// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.util.Locale;
import java.net.IDN;
import java.util.Arrays;
import java.net.InetAddress;
import okio.Buffer;
import java.util.Iterator;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import java.util.ArrayList;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class HttpUrl
{
    private static final char[] HEX_DIGITS;
    static final String USERNAME_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    static final String PASSWORD_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";
    static final String PATH_SEGMENT_ENCODE_SET_URI = "[]";
    static final String QUERY_ENCODE_SET = " \"'<>#";
    static final String QUERY_COMPONENT_ENCODE_SET = " \"'<>#&=";
    static final String QUERY_COMPONENT_ENCODE_SET_URI = "\\^`{|}";
    static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
    static final String FRAGMENT_ENCODE_SET = "";
    static final String FRAGMENT_ENCODE_SET_URI = " \"#<>\\^`{|}";
    private final String scheme;
    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final List<String> pathSegments;
    private final List<String> queryNamesAndValues;
    private final String fragment;
    private final String url;
    
    private HttpUrl(final Builder builder) {
        this.scheme = builder.scheme;
        this.username = percentDecode(builder.encodedUsername, false);
        this.password = percentDecode(builder.encodedPassword, false);
        this.host = builder.host;
        this.port = builder.effectivePort();
        this.pathSegments = this.percentDecode(builder.encodedPathSegments, false);
        this.queryNamesAndValues = ((builder.encodedQueryNamesAndValues != null) ? this.percentDecode(builder.encodedQueryNamesAndValues, true) : null);
        this.fragment = ((builder.encodedFragment != null) ? percentDecode(builder.encodedFragment, false) : null);
        this.url = builder.toString();
    }
    
    public URL url() {
        try {
            return new URL(this.url);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public URI uri() {
        try {
            final String uri = this.newBuilder().reencodeForUri().toString();
            return new URI(uri);
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException("not valid as a java.net.URI: " + this.url);
        }
    }
    
    public String scheme() {
        return this.scheme;
    }
    
    public boolean isHttps() {
        return this.scheme.equals("https");
    }
    
    public String encodedUsername() {
        if (this.username.isEmpty()) {
            return "";
        }
        final int usernameStart = this.scheme.length() + 3;
        final int usernameEnd = delimiterOffset(this.url, usernameStart, this.url.length(), ":@");
        return this.url.substring(usernameStart, usernameEnd);
    }
    
    public String username() {
        return this.username;
    }
    
    public String encodedPassword() {
        if (this.password.isEmpty()) {
            return "";
        }
        final int passwordStart = this.url.indexOf(58, this.scheme.length() + 3) + 1;
        final int passwordEnd = this.url.indexOf(64);
        return this.url.substring(passwordStart, passwordEnd);
    }
    
    public String password() {
        return this.password;
    }
    
    public String host() {
        return this.host;
    }
    
    public int port() {
        return this.port;
    }
    
    public static int defaultPort(final String scheme) {
        if (scheme.equals("http")) {
            return 80;
        }
        if (scheme.equals("https")) {
            return 443;
        }
        return -1;
    }
    
    public int pathSize() {
        return this.pathSegments.size();
    }
    
    public String encodedPath() {
        final int pathStart = this.url.indexOf(47, this.scheme.length() + 3);
        final int pathEnd = delimiterOffset(this.url, pathStart, this.url.length(), "?#");
        return this.url.substring(pathStart, pathEnd);
    }
    
    static void pathSegmentsToString(final StringBuilder out, final List<String> pathSegments) {
        for (int i = 0, size = pathSegments.size(); i < size; ++i) {
            out.append('/');
            out.append(pathSegments.get(i));
        }
    }
    
    public List<String> encodedPathSegments() {
        final int pathStart = this.url.indexOf(47, this.scheme.length() + 3);
        final int pathEnd = delimiterOffset(this.url, pathStart, this.url.length(), "?#");
        final List<String> result = new ArrayList<String>();
        int segmentEnd;
        for (int i = pathStart; i < pathEnd; i = segmentEnd) {
            ++i;
            segmentEnd = delimiterOffset(this.url, i, pathEnd, "/");
            result.add(this.url.substring(i, segmentEnd));
        }
        return result;
    }
    
    public List<String> pathSegments() {
        return this.pathSegments;
    }
    
    public String encodedQuery() {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        final int queryStart = this.url.indexOf(63) + 1;
        final int queryEnd = delimiterOffset(this.url, queryStart + 1, this.url.length(), "#");
        return this.url.substring(queryStart, queryEnd);
    }
    
    static void namesAndValuesToQueryString(final StringBuilder out, final List<String> namesAndValues) {
        for (int i = 0, size = namesAndValues.size(); i < size; i += 2) {
            final String name = namesAndValues.get(i);
            final String value = namesAndValues.get(i + 1);
            if (i > 0) {
                out.append('&');
            }
            out.append(name);
            if (value != null) {
                out.append('=');
                out.append(value);
            }
        }
    }
    
    static List<String> queryStringToNamesAndValues(final String encodedQuery) {
        final List<String> result = new ArrayList<String>();
        int ampersandOffset;
        for (int pos = 0; pos <= encodedQuery.length(); pos = ampersandOffset + 1) {
            ampersandOffset = encodedQuery.indexOf(38, pos);
            if (ampersandOffset == -1) {
                ampersandOffset = encodedQuery.length();
            }
            final int equalsOffset = encodedQuery.indexOf(61, pos);
            if (equalsOffset == -1 || equalsOffset > ampersandOffset) {
                result.add(encodedQuery.substring(pos, ampersandOffset));
                result.add(null);
            }
            else {
                result.add(encodedQuery.substring(pos, equalsOffset));
                result.add(encodedQuery.substring(equalsOffset + 1, ampersandOffset));
            }
        }
        return result;
    }
    
    public String query() {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        final StringBuilder result = new StringBuilder();
        namesAndValuesToQueryString(result, this.queryNamesAndValues);
        return result.toString();
    }
    
    public int querySize() {
        return (this.queryNamesAndValues != null) ? (this.queryNamesAndValues.size() / 2) : 0;
    }
    
    public String queryParameter(final String name) {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        for (int i = 0, size = this.queryNamesAndValues.size(); i < size; i += 2) {
            if (name.equals(this.queryNamesAndValues.get(i))) {
                return this.queryNamesAndValues.get(i + 1);
            }
        }
        return null;
    }
    
    public Set<String> queryParameterNames() {
        if (this.queryNamesAndValues == null) {
            return Collections.emptySet();
        }
        final Set<String> result = new LinkedHashSet<String>();
        for (int i = 0, size = this.queryNamesAndValues.size(); i < size; i += 2) {
            result.add(this.queryNamesAndValues.get(i));
        }
        return Collections.unmodifiableSet((Set<? extends String>)result);
    }
    
    public List<String> queryParameterValues(final String name) {
        if (this.queryNamesAndValues == null) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<String>();
        for (int i = 0, size = this.queryNamesAndValues.size(); i < size; i += 2) {
            if (name.equals(this.queryNamesAndValues.get(i))) {
                result.add(this.queryNamesAndValues.get(i + 1));
            }
        }
        return Collections.unmodifiableList((List<? extends String>)result);
    }
    
    public String queryParameterName(final int index) {
        return this.queryNamesAndValues.get(index * 2);
    }
    
    public String queryParameterValue(final int index) {
        return this.queryNamesAndValues.get(index * 2 + 1);
    }
    
    public String encodedFragment() {
        if (this.fragment == null) {
            return null;
        }
        final int fragmentStart = this.url.indexOf(35) + 1;
        return this.url.substring(fragmentStart);
    }
    
    public String fragment() {
        return this.fragment;
    }
    
    public HttpUrl resolve(final String link) {
        final Builder builder = new Builder();
        final Builder.ParseResult result = builder.parse(this, link);
        return (result == Builder.ParseResult.SUCCESS) ? builder.build() : null;
    }
    
    public Builder newBuilder() {
        final Builder result = new Builder();
        result.scheme = this.scheme;
        result.encodedUsername = this.encodedUsername();
        result.encodedPassword = this.encodedPassword();
        result.host = this.host;
        result.port = ((this.port != defaultPort(this.scheme)) ? this.port : -1);
        result.encodedPathSegments.clear();
        result.encodedPathSegments.addAll(this.encodedPathSegments());
        result.encodedQuery(this.encodedQuery());
        result.encodedFragment = this.encodedFragment();
        return result;
    }
    
    public static HttpUrl parse(final String url) {
        final Builder builder = new Builder();
        final Builder.ParseResult result = builder.parse(null, url);
        return (result == Builder.ParseResult.SUCCESS) ? builder.build() : null;
    }
    
    public static HttpUrl get(final URL url) {
        return parse(url.toString());
    }
    
    static HttpUrl getChecked(final String url) throws MalformedURLException, UnknownHostException {
        final Builder builder = new Builder();
        final Builder.ParseResult result = builder.parse(null, url);
        switch (result) {
            case SUCCESS: {
                return builder.build();
            }
            case INVALID_HOST: {
                throw new UnknownHostException("Invalid host: " + url);
            }
            default: {
                throw new MalformedURLException("Invalid URL: " + result + " for " + url);
            }
        }
    }
    
    public static HttpUrl get(final URI uri) {
        return parse(uri.toString());
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof HttpUrl && ((HttpUrl)o).url.equals(this.url);
    }
    
    @Override
    public int hashCode() {
        return this.url.hashCode();
    }
    
    @Override
    public String toString() {
        return this.url;
    }
    
    private static int delimiterOffset(final String input, final int pos, final int limit, final String delimiters) {
        for (int i = pos; i < limit; ++i) {
            if (delimiters.indexOf(input.charAt(i)) != -1) {
                return i;
            }
        }
        return limit;
    }
    
    static String percentDecode(final String encoded, final boolean plusIsSpace) {
        return percentDecode(encoded, 0, encoded.length(), plusIsSpace);
    }
    
    private List<String> percentDecode(final List<String> list, final boolean plusIsSpace) {
        final List<String> result = new ArrayList<String>(list.size());
        for (final String s : list) {
            result.add((s != null) ? percentDecode(s, plusIsSpace) : null);
        }
        return Collections.unmodifiableList((List<? extends String>)result);
    }
    
    static String percentDecode(final String encoded, final int pos, final int limit, final boolean plusIsSpace) {
        for (int i = pos; i < limit; ++i) {
            final char c = encoded.charAt(i);
            if (c == '%' || (c == '+' && plusIsSpace)) {
                final Buffer out = new Buffer();
                out.writeUtf8(encoded, pos, i);
                percentDecode(out, encoded, i, limit, plusIsSpace);
                return out.readUtf8();
            }
        }
        return encoded.substring(pos, limit);
    }
    
    static void percentDecode(final Buffer out, final String encoded, final int pos, final int limit, final boolean plusIsSpace) {
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = encoded.codePointAt(i);
            if (codePoint == 37 && i + 2 < limit) {
                final int d1 = decodeHexDigit(encoded.charAt(i + 1));
                final int d2 = decodeHexDigit(encoded.charAt(i + 2));
                if (d1 != -1 && d2 != -1) {
                    out.writeByte((d1 << 4) + d2);
                    i += 2;
                    continue;
                }
            }
            else if (codePoint == 43 && plusIsSpace) {
                out.writeByte(32);
                continue;
            }
            out.writeUtf8CodePoint(codePoint);
        }
    }
    
    static int decodeHexDigit(final char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return -1;
    }
    
    static String canonicalize(final String input, final int pos, final int limit, final String encodeSet, final boolean alreadyEncoded, final boolean plusIsSpace, final boolean asciiOnly) {
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (codePoint < 32 || codePoint == 127 || (codePoint >= 128 && asciiOnly) || encodeSet.indexOf(codePoint) != -1 || (codePoint == 37 && !alreadyEncoded) || (codePoint == 43 && plusIsSpace)) {
                final Buffer out = new Buffer();
                out.writeUtf8(input, pos, i);
                canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, plusIsSpace, asciiOnly);
                return out.readUtf8();
            }
        }
        return input.substring(pos, limit);
    }
    
    static void canonicalize(final Buffer out, final String input, final int pos, final int limit, final String encodeSet, final boolean alreadyEncoded, final boolean plusIsSpace, final boolean asciiOnly) {
        Buffer utf8Buffer = null;
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (alreadyEncoded) {
                if (codePoint == 9 || codePoint == 10 || codePoint == 12) {
                    continue;
                }
                if (codePoint == 13) {
                    continue;
                }
            }
            if (codePoint == 43 && plusIsSpace) {
                out.writeUtf8(alreadyEncoded ? "+" : "%2B");
            }
            else if (codePoint < 32 || codePoint == 127 || (codePoint >= 128 && asciiOnly) || encodeSet.indexOf(codePoint) != -1 || (codePoint == 37 && !alreadyEncoded)) {
                if (utf8Buffer == null) {
                    utf8Buffer = new Buffer();
                }
                utf8Buffer.writeUtf8CodePoint(codePoint);
                while (!utf8Buffer.exhausted()) {
                    final int b = utf8Buffer.readByte() & 0xFF;
                    out.writeByte(37);
                    out.writeByte((int)HttpUrl.HEX_DIGITS[b >> 4 & 0xF]);
                    out.writeByte((int)HttpUrl.HEX_DIGITS[b & 0xF]);
                }
            }
            else {
                out.writeUtf8CodePoint(codePoint);
            }
        }
    }
    
    static String canonicalize(final String input, final String encodeSet, final boolean alreadyEncoded, final boolean plusIsSpace, final boolean asciiOnly) {
        return canonicalize(input, 0, input.length(), encodeSet, alreadyEncoded, plusIsSpace, asciiOnly);
    }
    
    static {
        HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    }
    
    public static final class Builder
    {
        String scheme;
        String encodedUsername;
        String encodedPassword;
        String host;
        int port;
        final List<String> encodedPathSegments;
        List<String> encodedQueryNamesAndValues;
        String encodedFragment;
        
        public Builder() {
            this.encodedUsername = "";
            this.encodedPassword = "";
            this.port = -1;
            (this.encodedPathSegments = new ArrayList<String>()).add("");
        }
        
        public Builder scheme(final String scheme) {
            if (scheme == null) {
                throw new IllegalArgumentException("scheme == null");
            }
            if (scheme.equalsIgnoreCase("http")) {
                this.scheme = "http";
            }
            else {
                if (!scheme.equalsIgnoreCase("https")) {
                    throw new IllegalArgumentException("unexpected scheme: " + scheme);
                }
                this.scheme = "https";
            }
            return this;
        }
        
        public Builder username(final String username) {
            if (username == null) {
                throw new IllegalArgumentException("username == null");
            }
            this.encodedUsername = HttpUrl.canonicalize(username, " \"':;<=>@[]^`{}|/\\?#", false, false, true);
            return this;
        }
        
        public Builder encodedUsername(final String encodedUsername) {
            if (encodedUsername == null) {
                throw new IllegalArgumentException("encodedUsername == null");
            }
            this.encodedUsername = HttpUrl.canonicalize(encodedUsername, " \"':;<=>@[]^`{}|/\\?#", true, false, true);
            return this;
        }
        
        public Builder password(final String password) {
            if (password == null) {
                throw new IllegalArgumentException("password == null");
            }
            this.encodedPassword = HttpUrl.canonicalize(password, " \"':;<=>@[]^`{}|/\\?#", false, false, true);
            return this;
        }
        
        public Builder encodedPassword(final String encodedPassword) {
            if (encodedPassword == null) {
                throw new IllegalArgumentException("encodedPassword == null");
            }
            this.encodedPassword = HttpUrl.canonicalize(encodedPassword, " \"':;<=>@[]^`{}|/\\?#", true, false, true);
            return this;
        }
        
        public Builder host(final String host) {
            if (host == null) {
                throw new IllegalArgumentException("host == null");
            }
            final String encoded = canonicalizeHost(host, 0, host.length());
            if (encoded == null) {
                throw new IllegalArgumentException("unexpected host: " + host);
            }
            this.host = encoded;
            return this;
        }
        
        public Builder port(final int port) {
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("unexpected port: " + port);
            }
            this.port = port;
            return this;
        }
        
        int effectivePort() {
            return (this.port != -1) ? this.port : HttpUrl.defaultPort(this.scheme);
        }
        
        public Builder addPathSegment(final String pathSegment) {
            if (pathSegment == null) {
                throw new IllegalArgumentException("pathSegment == null");
            }
            this.push(pathSegment, 0, pathSegment.length(), false, false);
            return this;
        }
        
        public Builder addEncodedPathSegment(final String encodedPathSegment) {
            if (encodedPathSegment == null) {
                throw new IllegalArgumentException("encodedPathSegment == null");
            }
            this.push(encodedPathSegment, 0, encodedPathSegment.length(), false, true);
            return this;
        }
        
        public Builder setPathSegment(final int index, final String pathSegment) {
            if (pathSegment == null) {
                throw new IllegalArgumentException("pathSegment == null");
            }
            final String canonicalPathSegment = HttpUrl.canonicalize(pathSegment, 0, pathSegment.length(), " \"<>^`{}|/\\?#", false, false, true);
            if (this.isDot(canonicalPathSegment) || this.isDotDot(canonicalPathSegment)) {
                throw new IllegalArgumentException("unexpected path segment: " + pathSegment);
            }
            this.encodedPathSegments.set(index, canonicalPathSegment);
            return this;
        }
        
        public Builder setEncodedPathSegment(final int index, final String encodedPathSegment) {
            if (encodedPathSegment == null) {
                throw new IllegalArgumentException("encodedPathSegment == null");
            }
            final String canonicalPathSegment = HttpUrl.canonicalize(encodedPathSegment, 0, encodedPathSegment.length(), " \"<>^`{}|/\\?#", true, false, true);
            this.encodedPathSegments.set(index, canonicalPathSegment);
            if (this.isDot(canonicalPathSegment) || this.isDotDot(canonicalPathSegment)) {
                throw new IllegalArgumentException("unexpected path segment: " + encodedPathSegment);
            }
            return this;
        }
        
        public Builder removePathSegment(final int index) {
            this.encodedPathSegments.remove(index);
            if (this.encodedPathSegments.isEmpty()) {
                this.encodedPathSegments.add("");
            }
            return this;
        }
        
        public Builder encodedPath(final String encodedPath) {
            if (encodedPath == null) {
                throw new IllegalArgumentException("encodedPath == null");
            }
            if (!encodedPath.startsWith("/")) {
                throw new IllegalArgumentException("unexpected encodedPath: " + encodedPath);
            }
            this.resolvePath(encodedPath, 0, encodedPath.length());
            return this;
        }
        
        public Builder query(final String query) {
            this.encodedQueryNamesAndValues = ((query != null) ? HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(query, " \"'<>#", false, true, true)) : null);
            return this;
        }
        
        public Builder encodedQuery(final String encodedQuery) {
            this.encodedQueryNamesAndValues = ((encodedQuery != null) ? HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(encodedQuery, " \"'<>#", true, true, true)) : null);
            return this;
        }
        
        public Builder addQueryParameter(final String name, final String value) {
            if (name == null) {
                throw new IllegalArgumentException("name == null");
            }
            if (this.encodedQueryNamesAndValues == null) {
                this.encodedQueryNamesAndValues = new ArrayList<String>();
            }
            this.encodedQueryNamesAndValues.add(HttpUrl.canonicalize(name, " \"'<>#&=", false, true, true));
            this.encodedQueryNamesAndValues.add((value != null) ? HttpUrl.canonicalize(value, " \"'<>#&=", false, true, true) : null);
            return this;
        }
        
        public Builder addEncodedQueryParameter(final String encodedName, final String encodedValue) {
            if (encodedName == null) {
                throw new IllegalArgumentException("encodedName == null");
            }
            if (this.encodedQueryNamesAndValues == null) {
                this.encodedQueryNamesAndValues = new ArrayList<String>();
            }
            this.encodedQueryNamesAndValues.add(HttpUrl.canonicalize(encodedName, " \"'<>#&=", true, true, true));
            this.encodedQueryNamesAndValues.add((encodedValue != null) ? HttpUrl.canonicalize(encodedValue, " \"'<>#&=", true, true, true) : null);
            return this;
        }
        
        public Builder setQueryParameter(final String name, final String value) {
            this.removeAllQueryParameters(name);
            this.addQueryParameter(name, value);
            return this;
        }
        
        public Builder setEncodedQueryParameter(final String encodedName, final String encodedValue) {
            this.removeAllEncodedQueryParameters(encodedName);
            this.addEncodedQueryParameter(encodedName, encodedValue);
            return this;
        }
        
        public Builder removeAllQueryParameters(final String name) {
            if (name == null) {
                throw new IllegalArgumentException("name == null");
            }
            if (this.encodedQueryNamesAndValues == null) {
                return this;
            }
            final String nameToRemove = HttpUrl.canonicalize(name, " \"'<>#&=", false, true, true);
            this.removeAllCanonicalQueryParameters(nameToRemove);
            return this;
        }
        
        public Builder removeAllEncodedQueryParameters(final String encodedName) {
            if (encodedName == null) {
                throw new IllegalArgumentException("encodedName == null");
            }
            if (this.encodedQueryNamesAndValues == null) {
                return this;
            }
            this.removeAllCanonicalQueryParameters(HttpUrl.canonicalize(encodedName, " \"'<>#&=", true, true, true));
            return this;
        }
        
        private void removeAllCanonicalQueryParameters(final String canonicalName) {
            for (int i = this.encodedQueryNamesAndValues.size() - 2; i >= 0; i -= 2) {
                if (canonicalName.equals(this.encodedQueryNamesAndValues.get(i))) {
                    this.encodedQueryNamesAndValues.remove(i + 1);
                    this.encodedQueryNamesAndValues.remove(i);
                    if (this.encodedQueryNamesAndValues.isEmpty()) {
                        this.encodedQueryNamesAndValues = null;
                        return;
                    }
                }
            }
        }
        
        public Builder fragment(final String fragment) {
            this.encodedFragment = ((fragment != null) ? HttpUrl.canonicalize(fragment, "", false, false, false) : null);
            return this;
        }
        
        public Builder encodedFragment(final String encodedFragment) {
            this.encodedFragment = ((encodedFragment != null) ? HttpUrl.canonicalize(encodedFragment, "", true, false, false) : null);
            return this;
        }
        
        Builder reencodeForUri() {
            for (int i = 0, size = this.encodedPathSegments.size(); i < size; ++i) {
                final String pathSegment = this.encodedPathSegments.get(i);
                this.encodedPathSegments.set(i, HttpUrl.canonicalize(pathSegment, "[]", true, false, true));
            }
            if (this.encodedQueryNamesAndValues != null) {
                for (int i = 0, size = this.encodedQueryNamesAndValues.size(); i < size; ++i) {
                    final String component = this.encodedQueryNamesAndValues.get(i);
                    if (component != null) {
                        this.encodedQueryNamesAndValues.set(i, HttpUrl.canonicalize(component, "\\^`{|}", true, true, true));
                    }
                }
            }
            if (this.encodedFragment != null) {
                this.encodedFragment = HttpUrl.canonicalize(this.encodedFragment, " \"#<>\\^`{|}", true, false, false);
            }
            return this;
        }
        
        public HttpUrl build() {
            if (this.scheme == null) {
                throw new IllegalStateException("scheme == null");
            }
            if (this.host == null) {
                throw new IllegalStateException("host == null");
            }
            return new HttpUrl(this, null);
        }
        
        @Override
        public String toString() {
            final StringBuilder result = new StringBuilder();
            result.append(this.scheme);
            result.append("://");
            if (!this.encodedUsername.isEmpty() || !this.encodedPassword.isEmpty()) {
                result.append(this.encodedUsername);
                if (!this.encodedPassword.isEmpty()) {
                    result.append(':');
                    result.append(this.encodedPassword);
                }
                result.append('@');
            }
            if (this.host.indexOf(58) != -1) {
                result.append('[');
                result.append(this.host);
                result.append(']');
            }
            else {
                result.append(this.host);
            }
            final int effectivePort = this.effectivePort();
            if (effectivePort != HttpUrl.defaultPort(this.scheme)) {
                result.append(':');
                result.append(effectivePort);
            }
            HttpUrl.pathSegmentsToString(result, this.encodedPathSegments);
            if (this.encodedQueryNamesAndValues != null) {
                result.append('?');
                HttpUrl.namesAndValuesToQueryString(result, this.encodedQueryNamesAndValues);
            }
            if (this.encodedFragment != null) {
                result.append('#');
                result.append(this.encodedFragment);
            }
            return result.toString();
        }
        
        ParseResult parse(final HttpUrl base, final String input) {
            int pos = this.skipLeadingAsciiWhitespace(input, 0, input.length());
            final int limit = this.skipTrailingAsciiWhitespace(input, pos, input.length());
            final int schemeDelimiterOffset = schemeDelimiterOffset(input, pos, limit);
            if (schemeDelimiterOffset != -1) {
                if (input.regionMatches(true, pos, "https:", 0, 6)) {
                    this.scheme = "https";
                    pos += "https:".length();
                }
                else {
                    if (!input.regionMatches(true, pos, "http:", 0, 5)) {
                        return ParseResult.UNSUPPORTED_SCHEME;
                    }
                    this.scheme = "http";
                    pos += "http:".length();
                }
            }
            else {
                if (base == null) {
                    return ParseResult.MISSING_SCHEME;
                }
                this.scheme = base.scheme;
            }
            boolean hasUsername = false;
            boolean hasPassword = false;
            final int slashCount = slashCount(input, pos, limit);
            if (slashCount >= 2 || base == null || !base.scheme.equals(this.scheme)) {
                pos += slashCount;
                int componentDelimiterOffset = 0;
            Label_0418:
                while (true) {
                    componentDelimiterOffset = delimiterOffset(input, pos, limit, "@/\\?#");
                    final int c = (componentDelimiterOffset != limit) ? input.charAt(componentDelimiterOffset) : -1;
                    switch (c) {
                        case 64: {
                            if (!hasPassword) {
                                final int passwordColonOffset = delimiterOffset(input, pos, componentDelimiterOffset, ":");
                                final String canonicalUsername = HttpUrl.canonicalize(input, pos, passwordColonOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, true);
                                this.encodedUsername = (hasUsername ? (this.encodedUsername + "%40" + canonicalUsername) : canonicalUsername);
                                if (passwordColonOffset != componentDelimiterOffset) {
                                    hasPassword = true;
                                    this.encodedPassword = HttpUrl.canonicalize(input, passwordColonOffset + 1, componentDelimiterOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, true);
                                }
                                hasUsername = true;
                            }
                            else {
                                this.encodedPassword = this.encodedPassword + "%40" + HttpUrl.canonicalize(input, pos, componentDelimiterOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, true);
                            }
                            pos = componentDelimiterOffset + 1;
                            continue;
                        }
                        case -1:
                        case 35:
                        case 47:
                        case 63:
                        case 92: {
                            break Label_0418;
                        }
                    }
                }
                final int portColonOffset = portColonOffset(input, pos, componentDelimiterOffset);
                if (portColonOffset + 1 < componentDelimiterOffset) {
                    this.host = canonicalizeHost(input, pos, portColonOffset);
                    this.port = parsePort(input, portColonOffset + 1, componentDelimiterOffset);
                    if (this.port == -1) {
                        return ParseResult.INVALID_PORT;
                    }
                }
                else {
                    this.host = canonicalizeHost(input, pos, portColonOffset);
                    this.port = HttpUrl.defaultPort(this.scheme);
                }
                if (this.host == null) {
                    return ParseResult.INVALID_HOST;
                }
                pos = componentDelimiterOffset;
            }
            else {
                this.encodedUsername = base.encodedUsername();
                this.encodedPassword = base.encodedPassword();
                this.host = base.host;
                this.port = base.port;
                this.encodedPathSegments.clear();
                this.encodedPathSegments.addAll(base.encodedPathSegments());
                if (pos == limit || input.charAt(pos) == '#') {
                    this.encodedQuery(base.encodedQuery());
                }
            }
            final int pathDelimiterOffset = delimiterOffset(input, pos, limit, "?#");
            this.resolvePath(input, pos, pathDelimiterOffset);
            pos = pathDelimiterOffset;
            if (pos < limit && input.charAt(pos) == '?') {
                final int queryDelimiterOffset = delimiterOffset(input, pos, limit, "#");
                this.encodedQueryNamesAndValues = HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(input, pos + 1, queryDelimiterOffset, " \"'<>#", true, true, true));
                pos = queryDelimiterOffset;
            }
            if (pos < limit && input.charAt(pos) == '#') {
                this.encodedFragment = HttpUrl.canonicalize(input, pos + 1, limit, "", true, false, false);
            }
            return ParseResult.SUCCESS;
        }
        
        private void resolvePath(final String input, int pos, final int limit) {
            if (pos == limit) {
                return;
            }
            final char c = input.charAt(pos);
            if (c == '/' || c == '\\') {
                this.encodedPathSegments.clear();
                this.encodedPathSegments.add("");
                ++pos;
            }
            else {
                this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, "");
            }
            for (int i = pos; i < limit; ++i) {
                final int pathSegmentDelimiterOffset = delimiterOffset(input, i, limit, "/\\");
                final boolean segmentHasTrailingSlash = pathSegmentDelimiterOffset < limit;
                this.push(input, i, pathSegmentDelimiterOffset, segmentHasTrailingSlash, true);
                i = pathSegmentDelimiterOffset;
                if (segmentHasTrailingSlash) {}
            }
        }
        
        private void push(final String input, final int pos, final int limit, final boolean addTrailingSlash, final boolean alreadyEncoded) {
            final String segment = HttpUrl.canonicalize(input, pos, limit, " \"<>^`{}|/\\?#", alreadyEncoded, false, true);
            if (this.isDot(segment)) {
                return;
            }
            if (this.isDotDot(segment)) {
                this.pop();
                return;
            }
            if (this.encodedPathSegments.get(this.encodedPathSegments.size() - 1).isEmpty()) {
                this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, segment);
            }
            else {
                this.encodedPathSegments.add(segment);
            }
            if (addTrailingSlash) {
                this.encodedPathSegments.add("");
            }
        }
        
        private boolean isDot(final String input) {
            return input.equals(".") || input.equalsIgnoreCase("%2e");
        }
        
        private boolean isDotDot(final String input) {
            return input.equals("..") || input.equalsIgnoreCase("%2e.") || input.equalsIgnoreCase(".%2e") || input.equalsIgnoreCase("%2e%2e");
        }
        
        private void pop() {
            final String removed = this.encodedPathSegments.remove(this.encodedPathSegments.size() - 1);
            if (removed.isEmpty() && !this.encodedPathSegments.isEmpty()) {
                this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, "");
            }
            else {
                this.encodedPathSegments.add("");
            }
        }
        
        private int skipLeadingAsciiWhitespace(final String input, final int pos, final int limit) {
            int i = pos;
            while (i < limit) {
                switch (input.charAt(i)) {
                    case '\t':
                    case '\n':
                    case '\f':
                    case '\r':
                    case ' ': {
                        ++i;
                        continue;
                    }
                    default: {
                        return i;
                    }
                }
            }
            return limit;
        }
        
        private int skipTrailingAsciiWhitespace(final String input, final int pos, final int limit) {
            int i = limit - 1;
            while (i >= pos) {
                switch (input.charAt(i)) {
                    case '\t':
                    case '\n':
                    case '\f':
                    case '\r':
                    case ' ': {
                        --i;
                        continue;
                    }
                    default: {
                        return i + 1;
                    }
                }
            }
            return pos;
        }
        
        private static int schemeDelimiterOffset(final String input, final int pos, final int limit) {
            if (limit - pos < 2) {
                return -1;
            }
            final char c0 = input.charAt(pos);
            if ((c0 < 'a' || c0 > 'z') && (c0 < 'A' || c0 > 'Z')) {
                return -1;
            }
            int i = pos + 1;
            while (i < limit) {
                final char c2 = input.charAt(i);
                if ((c2 < 'a' || c2 > 'z') && (c2 < 'A' || c2 > 'Z') && (c2 < '0' || c2 > '9') && c2 != '+' && c2 != '-' && c2 != '.') {
                    if (c2 == ':') {
                        return i;
                    }
                    return -1;
                }
                else {
                    ++i;
                }
            }
            return -1;
        }
        
        private static int slashCount(final String input, int pos, final int limit) {
            int slashCount = 0;
            while (pos < limit) {
                final char c = input.charAt(pos);
                if (c != '\\' && c != '/') {
                    break;
                }
                ++slashCount;
                ++pos;
            }
            return slashCount;
        }
        
        private static int portColonOffset(final String input, final int pos, final int limit) {
            for (int i = pos; i < limit; ++i) {
                switch (input.charAt(i)) {
                    case '[': {
                        while (++i < limit) {
                            if (input.charAt(i) == ']') {
                                break;
                            }
                        }
                        break;
                    }
                    case ':': {
                        return i;
                    }
                }
            }
            return limit;
        }
        
        private static String canonicalizeHost(final String input, final int pos, final int limit) {
            final String percentDecoded = HttpUrl.percentDecode(input, pos, limit, false);
            if (!percentDecoded.startsWith("[") || !percentDecoded.endsWith("]")) {
                return domainToAscii(percentDecoded);
            }
            final InetAddress inetAddress = decodeIpv6(percentDecoded, 1, percentDecoded.length() - 1);
            if (inetAddress == null) {
                return null;
            }
            final byte[] address = inetAddress.getAddress();
            if (address.length == 16) {
                return inet6AddressToAscii(address);
            }
            throw new AssertionError();
        }
        
        private static InetAddress decodeIpv6(final String input, final int pos, final int limit) {
            final byte[] address = new byte[16];
            int b = 0;
            int compress = -1;
            int groupOffset = -1;
            int i = pos;
            while (i < limit) {
                if (b == address.length) {
                    return null;
                }
                if (i + 2 <= limit && input.regionMatches(i, "::", 0, 2)) {
                    if (compress != -1) {
                        return null;
                    }
                    i += 2;
                    b += 2;
                    compress = b;
                    if (i == limit) {
                        break;
                    }
                }
                else if (b != 0) {
                    if (input.regionMatches(i, ":", 0, 1)) {
                        ++i;
                    }
                    else {
                        if (!input.regionMatches(i, ".", 0, 1)) {
                            return null;
                        }
                        if (!decodeIpv4Suffix(input, groupOffset, limit, address, b - 2)) {
                            return null;
                        }
                        b += 2;
                        break;
                    }
                }
                int value = 0;
                groupOffset = i;
                while (i < limit) {
                    final char c = input.charAt(i);
                    final int hexDigit = HttpUrl.decodeHexDigit(c);
                    if (hexDigit == -1) {
                        break;
                    }
                    value = (value << 4) + hexDigit;
                    ++i;
                }
                final int groupLength = i - groupOffset;
                if (groupLength == 0 || groupLength > 4) {
                    return null;
                }
                address[b++] = (byte)(value >>> 8 & 0xFF);
                address[b++] = (byte)(value & 0xFF);
            }
            if (b != address.length) {
                if (compress == -1) {
                    return null;
                }
                System.arraycopy(address, compress, address, address.length - (b - compress), b - compress);
                Arrays.fill(address, compress, compress + (address.length - b), (byte)0);
            }
            try {
                return InetAddress.getByAddress(address);
            }
            catch (UnknownHostException e) {
                throw new AssertionError();
            }
        }
        
        private static boolean decodeIpv4Suffix(final String input, final int pos, final int limit, final byte[] address, final int addressOffset) {
            int b = addressOffset;
            int i = pos;
            while (i < limit) {
                if (b == address.length) {
                    return false;
                }
                if (b != addressOffset) {
                    if (input.charAt(i) != '.') {
                        return false;
                    }
                    ++i;
                }
                int value = 0;
                final int groupOffset = i;
                while (i < limit) {
                    final char c = input.charAt(i);
                    if (c < '0') {
                        break;
                    }
                    if (c > '9') {
                        break;
                    }
                    if (value == 0 && groupOffset != i) {
                        return false;
                    }
                    value = value * 10 + c - 48;
                    if (value > 255) {
                        return false;
                    }
                    ++i;
                }
                final int groupLength = i - groupOffset;
                if (groupLength == 0) {
                    return false;
                }
                address[b++] = (byte)value;
            }
            return b == addressOffset + 4;
        }
        
        private static String domainToAscii(final String input) {
            try {
                final String result = IDN.toASCII(input).toLowerCase(Locale.US);
                if (result.isEmpty()) {
                    return null;
                }
                if (containsInvalidHostnameAsciiCodes(result)) {
                    return null;
                }
                return result;
            }
            catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        private static boolean containsInvalidHostnameAsciiCodes(final String hostnameAscii) {
            for (int i = 0; i < hostnameAscii.length(); ++i) {
                final char c = hostnameAscii.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    return true;
                }
                if (" #%/:?@[\\]".indexOf(c) != -1) {
                    return true;
                }
            }
            return false;
        }
        
        private static String inet6AddressToAscii(final byte[] address) {
            int longestRunOffset = -1;
            int longestRunLength = 0;
            for (int i = 0; i < address.length; i += 2) {
                final int currentRunOffset = i;
                while (i < 16 && address[i] == 0 && address[i + 1] == 0) {
                    i += 2;
                }
                final int currentRunLength = i - currentRunOffset;
                if (currentRunLength > longestRunLength) {
                    longestRunOffset = currentRunOffset;
                    longestRunLength = currentRunLength;
                }
            }
            final Buffer result = new Buffer();
            int j = 0;
            while (j < address.length) {
                if (j == longestRunOffset) {
                    result.writeByte(58);
                    j += longestRunLength;
                    if (j != 16) {
                        continue;
                    }
                    result.writeByte(58);
                }
                else {
                    if (j > 0) {
                        result.writeByte(58);
                    }
                    final int group = (address[j] & 0xFF) << 8 | (address[j + 1] & 0xFF);
                    result.writeHexadecimalUnsignedLong((long)group);
                    j += 2;
                }
            }
            return result.readUtf8();
        }
        
        private static int parsePort(final String input, final int pos, final int limit) {
            try {
                final String portString = HttpUrl.canonicalize(input, pos, limit, "", false, false, true);
                final int i = Integer.parseInt(portString);
                if (i > 0 && i <= 65535) {
                    return i;
                }
                return -1;
            }
            catch (NumberFormatException e) {
                return -1;
            }
        }
        
        enum ParseResult
        {
            SUCCESS, 
            MISSING_SCHEME, 
            UNSUPPORTED_SCHEME, 
            INVALID_PORT, 
            INVALID_HOST;
        }
    }
}
