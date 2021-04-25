// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal;

import com.squareup.okhttp.HttpUrl;
import java.lang.reflect.Array;
import java.util.concurrent.ThreadFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import okio.ByteString;
import java.security.MessageDigest;
import java.io.InterruptedIOException;
import okio.Buffer;
import java.util.concurrent.TimeUnit;
import okio.Source;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.Closeable;
import java.nio.charset.Charset;

public final class Util
{
    public static final byte[] EMPTY_BYTE_ARRAY;
    public static final String[] EMPTY_STRING_ARRAY;
    public static final Charset UTF_8;
    
    private Util() {
    }
    
    public static void checkOffsetAndCount(final long arrayLength, final long offset, final long count) {
        if ((offset | count) < 0L || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    public static boolean equal(final Object a, final Object b) {
        return a == b || (a != null && a.equals(b));
    }
    
    public static void closeQuietly(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (RuntimeException rethrown) {
                throw rethrown;
            }
            catch (Exception ex) {}
        }
    }
    
    public static void closeQuietly(final Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (AssertionError e) {
                if (!isAndroidGetsocknameError(e)) {
                    throw e;
                }
            }
            catch (RuntimeException rethrown) {
                throw rethrown;
            }
            catch (Exception ex) {}
        }
    }
    
    public static void closeQuietly(final ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch (RuntimeException rethrown) {
                throw rethrown;
            }
            catch (Exception ex) {}
        }
    }
    
    public static void closeAll(final Closeable a, final Closeable b) throws IOException {
        Throwable thrown = null;
        try {
            a.close();
        }
        catch (Throwable e) {
            thrown = e;
        }
        try {
            b.close();
        }
        catch (Throwable e) {
            if (thrown == null) {
                thrown = e;
            }
        }
        if (thrown == null) {
            return;
        }
        if (thrown instanceof IOException) {
            throw (IOException)thrown;
        }
        if (thrown instanceof RuntimeException) {
            throw (RuntimeException)thrown;
        }
        if (thrown instanceof Error) {
            throw (Error)thrown;
        }
        throw new AssertionError((Object)thrown);
    }
    
    public static boolean discard(final Source source, final int timeout, final TimeUnit timeUnit) {
        try {
            return skipAll(source, timeout, timeUnit);
        }
        catch (IOException e) {
            return false;
        }
    }
    
    public static boolean skipAll(final Source source, final int duration, final TimeUnit timeUnit) throws IOException {
        final long now = System.nanoTime();
        final long originalDuration = source.timeout().hasDeadline() ? (source.timeout().deadlineNanoTime() - now) : Long.MAX_VALUE;
        source.timeout().deadlineNanoTime(now + Math.min(originalDuration, timeUnit.toNanos(duration)));
        try {
            final Buffer skipBuffer = new Buffer();
            while (source.read(skipBuffer, 2048L) != -1L) {
                skipBuffer.clear();
            }
            return true;
        }
        catch (InterruptedIOException e) {
            return false;
        }
        finally {
            if (originalDuration == Long.MAX_VALUE) {
                source.timeout().clearDeadline();
            }
            else {
                source.timeout().deadlineNanoTime(now + originalDuration);
            }
        }
    }
    
    public static String md5Hex(final String s) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            final byte[] md5bytes = messageDigest.digest(s.getBytes("UTF-8"));
            return ByteString.of(md5bytes).hex();
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException ex2) {
            final Exception ex;
            final Exception e = ex;
            throw new AssertionError((Object)e);
        }
    }
    
    public static String shaBase64(final String s) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            final byte[] sha1Bytes = messageDigest.digest(s.getBytes("UTF-8"));
            return ByteString.of(sha1Bytes).base64();
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException ex2) {
            final Exception ex;
            final Exception e = ex;
            throw new AssertionError((Object)e);
        }
    }
    
    public static ByteString sha1(final ByteString s) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            final byte[] sha1Bytes = messageDigest.digest(s.toByteArray());
            return ByteString.of(sha1Bytes);
        }
        catch (NoSuchAlgorithmException e) {
            throw new AssertionError((Object)e);
        }
    }
    
    public static <T> List<T> immutableList(final List<T> list) {
        return Collections.unmodifiableList((List<? extends T>)new ArrayList<T>((Collection<? extends T>)list));
    }
    
    public static <T> List<T> immutableList(final T... elements) {
        return Collections.unmodifiableList((List<? extends T>)Arrays.asList((T[])elements.clone()));
    }
    
    public static <K, V> Map<K, V> immutableMap(final Map<K, V> map) {
        return Collections.unmodifiableMap((Map<? extends K, ? extends V>)new LinkedHashMap<K, V>((Map<? extends K, ? extends V>)map));
    }
    
    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                final Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }
    
    public static <T> T[] intersect(final Class<T> arrayType, final T[] first, final T[] second) {
        final List<T> result = intersect(first, second);
        return result.toArray((T[])Array.newInstance(arrayType, result.size()));
    }
    
    private static <T> List<T> intersect(final T[] first, final T[] second) {
        final List<T> result = new ArrayList<T>();
        for (final T a : first) {
            for (final T b : second) {
                if (a.equals(b)) {
                    result.add(b);
                    break;
                }
            }
        }
        return result;
    }
    
    public static String hostHeader(final HttpUrl url) {
        return (url.port() != HttpUrl.defaultPort(url.scheme())) ? (url.host() + ":" + url.port()) : url.host();
    }
    
    public static String toHumanReadableAscii(final String s) {
        int c;
        for (int i = 0, length = s.length(); i < length; i += Character.charCount(c)) {
            c = s.codePointAt(i);
            if (c <= 31 || c >= 127) {
                final Buffer buffer = new Buffer();
                buffer.writeUtf8(s, 0, i);
                for (int j = i; j < length; j += Character.charCount(c)) {
                    c = s.codePointAt(j);
                    buffer.writeUtf8CodePoint((c > 31 && c < 127) ? c : 63);
                }
                return buffer.readUtf8();
            }
        }
        return s;
    }
    
    public static boolean isAndroidGetsocknameError(final AssertionError e) {
        return e.getCause() != null && e.getMessage() != null && e.getMessage().contains("getsockname failed");
    }
    
    public static boolean contains(final String[] array, final String value) {
        return Arrays.asList(array).contains(value);
    }
    
    public static String[] concat(final String[] array, final String value) {
        final String[] result = new String[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[result.length - 1] = value;
        return result;
    }
    
    static {
        EMPTY_BYTE_ARRAY = new byte[0];
        EMPTY_STRING_ARRAY = new String[0];
        UTF_8 = Charset.forName("UTF-8");
    }
}
