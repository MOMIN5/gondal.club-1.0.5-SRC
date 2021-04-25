// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal;

import java.util.logging.Level;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import com.squareup.okhttp.internal.tls.AndroidTrustRootIndex;
import java.lang.reflect.Field;
import okio.Buffer;
import java.lang.reflect.Method;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import com.squareup.okhttp.Protocol;
import java.util.List;
import javax.net.ssl.SSLSocket;
import com.squareup.okhttp.internal.tls.RealTrustRootIndex;
import com.squareup.okhttp.internal.tls.TrustRootIndex;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSocketFactory;
import java.net.SocketException;
import java.net.Socket;

public class Platform
{
    private static final Platform PLATFORM;
    
    public static Platform get() {
        return Platform.PLATFORM;
    }
    
    public String getPrefix() {
        return "OkHttp";
    }
    
    public void logW(final String warning) {
        System.out.println(warning);
    }
    
    public void tagSocket(final Socket socket) throws SocketException {
    }
    
    public void untagSocket(final Socket socket) throws SocketException {
    }
    
    public X509TrustManager trustManager(final SSLSocketFactory sslSocketFactory) {
        return null;
    }
    
    public TrustRootIndex trustRootIndex(final X509TrustManager trustManager) {
        return new RealTrustRootIndex(trustManager.getAcceptedIssuers());
    }
    
    public void configureTlsExtensions(final SSLSocket sslSocket, final String hostname, final List<Protocol> protocols) {
    }
    
    public void afterHandshake(final SSLSocket sslSocket) {
    }
    
    public String getSelectedProtocol(final SSLSocket socket) {
        return null;
    }
    
    public void connectSocket(final Socket socket, final InetSocketAddress address, final int connectTimeout) throws IOException {
        socket.connect(address, connectTimeout);
    }
    
    public void log(final String message) {
        System.out.println(message);
    }
    
    private static Platform findPlatform() {
        try {
            Class<?> sslParametersClass;
            try {
                sslParametersClass = Class.forName("com.android.org.conscrypt.SSLParametersImpl");
            }
            catch (ClassNotFoundException e) {
                sslParametersClass = Class.forName("org.apache.harmony.xnet.provider.jsse.SSLParametersImpl");
            }
            final OptionalMethod<Socket> setUseSessionTickets = new OptionalMethod<Socket>(null, "setUseSessionTickets", new Class[] { Boolean.TYPE });
            final OptionalMethod<Socket> setHostname = new OptionalMethod<Socket>(null, "setHostname", new Class[] { String.class });
            Method trafficStatsTagSocket = null;
            Method trafficStatsUntagSocket = null;
            OptionalMethod<Socket> getAlpnSelectedProtocol = null;
            OptionalMethod<Socket> setAlpnProtocols = null;
            try {
                final Class<?> trafficStats = Class.forName("android.net.TrafficStats");
                trafficStatsTagSocket = trafficStats.getMethod("tagSocket", Socket.class);
                trafficStatsUntagSocket = trafficStats.getMethod("untagSocket", Socket.class);
                try {
                    Class.forName("android.net.Network");
                    getAlpnSelectedProtocol = new OptionalMethod<Socket>(byte[].class, "getAlpnSelectedProtocol", new Class[0]);
                    setAlpnProtocols = new OptionalMethod<Socket>(null, "setAlpnProtocols", new Class[] { byte[].class });
                }
                catch (ClassNotFoundException ex) {}
            }
            catch (ClassNotFoundException ex2) {}
            catch (NoSuchMethodException ex3) {}
            return new Android(sslParametersClass, setUseSessionTickets, setHostname, trafficStatsTagSocket, trafficStatsUntagSocket, getAlpnSelectedProtocol, setAlpnProtocols);
        }
        catch (ClassNotFoundException ex4) {
            try {
                final Class<?> sslContextClass = Class.forName("sun.security.ssl.SSLContextImpl");
                try {
                    final String negoClassName = "org.eclipse.jetty.alpn.ALPN";
                    final Class<?> negoClass = Class.forName(negoClassName);
                    final Class<?> providerClass = Class.forName(negoClassName + "$Provider");
                    final Class<?> clientProviderClass = Class.forName(negoClassName + "$ClientProvider");
                    final Class<?> serverProviderClass = Class.forName(negoClassName + "$ServerProvider");
                    final Method putMethod = negoClass.getMethod("put", SSLSocket.class, providerClass);
                    final Method getMethod = negoClass.getMethod("get", SSLSocket.class);
                    final Method removeMethod = negoClass.getMethod("remove", SSLSocket.class);
                    return new JdkWithJettyBootPlatform(sslContextClass, putMethod, getMethod, removeMethod, clientProviderClass, serverProviderClass);
                }
                catch (ClassNotFoundException | NoSuchMethodException ex5) {
                    return new JdkPlatform(sslContextClass);
                }
            }
            catch (ClassNotFoundException ex6) {
                return new Platform();
            }
        }
    }
    
    static byte[] concatLengthPrefixed(final List<Protocol> protocols) {
        final Buffer result = new Buffer();
        for (int i = 0, size = protocols.size(); i < size; ++i) {
            final Protocol protocol = protocols.get(i);
            if (protocol != Protocol.HTTP_1_0) {
                result.writeByte(protocol.toString().length());
                result.writeUtf8(protocol.toString());
            }
        }
        return result.readByteArray();
    }
    
    static <T> T readFieldOrNull(final Object instance, final Class<T> fieldType, final String fieldName) {
        for (Class<?> c = instance.getClass(); c != Object.class; c = c.getSuperclass()) {
            try {
                final Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                final Object value = field.get(instance);
                if (value == null || !fieldType.isInstance(value)) {
                    return null;
                }
                return fieldType.cast(value);
            }
            catch (NoSuchFieldException ex) {}
            catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        }
        if (!fieldName.equals("delegate")) {
            final Object delegate = readFieldOrNull(instance, Object.class, "delegate");
            if (delegate != null) {
                return (T)readFieldOrNull(delegate, (Class<Object>)fieldType, fieldName);
            }
        }
        return null;
    }
    
    static {
        PLATFORM = findPlatform();
    }
    
    private static class Android extends Platform
    {
        private static final int MAX_LOG_LENGTH = 4000;
        private final Class<?> sslParametersClass;
        private final OptionalMethod<Socket> setUseSessionTickets;
        private final OptionalMethod<Socket> setHostname;
        private final Method trafficStatsTagSocket;
        private final Method trafficStatsUntagSocket;
        private final OptionalMethod<Socket> getAlpnSelectedProtocol;
        private final OptionalMethod<Socket> setAlpnProtocols;
        
        public Android(final Class<?> sslParametersClass, final OptionalMethod<Socket> setUseSessionTickets, final OptionalMethod<Socket> setHostname, final Method trafficStatsTagSocket, final Method trafficStatsUntagSocket, final OptionalMethod<Socket> getAlpnSelectedProtocol, final OptionalMethod<Socket> setAlpnProtocols) {
            this.sslParametersClass = sslParametersClass;
            this.setUseSessionTickets = setUseSessionTickets;
            this.setHostname = setHostname;
            this.trafficStatsTagSocket = trafficStatsTagSocket;
            this.trafficStatsUntagSocket = trafficStatsUntagSocket;
            this.getAlpnSelectedProtocol = getAlpnSelectedProtocol;
            this.setAlpnProtocols = setAlpnProtocols;
        }
        
        @Override
        public void connectSocket(final Socket socket, final InetSocketAddress address, final int connectTimeout) throws IOException {
            try {
                socket.connect(address, connectTimeout);
            }
            catch (AssertionError e) {
                if (Util.isAndroidGetsocknameError(e)) {
                    throw new IOException(e);
                }
                throw e;
            }
            catch (SecurityException e2) {
                final IOException ioException = new IOException("Exception in connect");
                ioException.initCause(e2);
                throw ioException;
            }
        }
        
        @Override
        public X509TrustManager trustManager(final SSLSocketFactory sslSocketFactory) {
            Object context = Platform.readFieldOrNull(sslSocketFactory, this.sslParametersClass, "sslParameters");
            if (context == null) {
                try {
                    final Class<?> gmsSslParametersClass = Class.forName("com.google.android.gms.org.conscrypt.SSLParametersImpl", false, sslSocketFactory.getClass().getClassLoader());
                    context = Platform.readFieldOrNull(sslSocketFactory, gmsSslParametersClass, "sslParameters");
                }
                catch (ClassNotFoundException e) {
                    return null;
                }
            }
            final X509TrustManager x509TrustManager = Platform.readFieldOrNull(context, X509TrustManager.class, "x509TrustManager");
            if (x509TrustManager != null) {
                return x509TrustManager;
            }
            return Platform.readFieldOrNull(context, X509TrustManager.class, "trustManager");
        }
        
        @Override
        public TrustRootIndex trustRootIndex(final X509TrustManager trustManager) {
            final TrustRootIndex result = AndroidTrustRootIndex.get(trustManager);
            if (result != null) {
                return result;
            }
            return super.trustRootIndex(trustManager);
        }
        
        @Override
        public void configureTlsExtensions(final SSLSocket sslSocket, final String hostname, final List<Protocol> protocols) {
            if (hostname != null) {
                this.setUseSessionTickets.invokeOptionalWithoutCheckedException(sslSocket, true);
                this.setHostname.invokeOptionalWithoutCheckedException(sslSocket, hostname);
            }
            if (this.setAlpnProtocols != null && this.setAlpnProtocols.isSupported(sslSocket)) {
                final Object[] parameters = { Platform.concatLengthPrefixed(protocols) };
                this.setAlpnProtocols.invokeWithoutCheckedException(sslSocket, parameters);
            }
        }
        
        @Override
        public String getSelectedProtocol(final SSLSocket socket) {
            if (this.getAlpnSelectedProtocol == null) {
                return null;
            }
            if (!this.getAlpnSelectedProtocol.isSupported(socket)) {
                return null;
            }
            final byte[] alpnResult = (byte[])this.getAlpnSelectedProtocol.invokeWithoutCheckedException(socket, new Object[0]);
            return (alpnResult != null) ? new String(alpnResult, Util.UTF_8) : null;
        }
        
        @Override
        public void tagSocket(final Socket socket) throws SocketException {
            if (this.trafficStatsTagSocket == null) {
                return;
            }
            try {
                this.trafficStatsTagSocket.invoke(null, socket);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e2) {
                throw new RuntimeException(e2.getCause());
            }
        }
        
        @Override
        public void untagSocket(final Socket socket) throws SocketException {
            if (this.trafficStatsUntagSocket == null) {
                return;
            }
            try {
                this.trafficStatsUntagSocket.invoke(null, socket);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e2) {
                throw new RuntimeException(e2.getCause());
            }
        }
        
        @Override
        public void log(final String message) {
            for (int i = 0, length = message.length(); i < length; ++i) {
                int newline = message.indexOf(10, i);
                newline = ((newline != -1) ? newline : length);
                do {
                    final int end = Math.min(newline, i + 4000);
                    Log.d("OkHttp", message.substring(i, end));
                    i = end;
                } while (i < newline);
            }
        }
    }
    
    private static class JdkPlatform extends Platform
    {
        private final Class<?> sslContextClass;
        
        public JdkPlatform(final Class<?> sslContextClass) {
            this.sslContextClass = sslContextClass;
        }
        
        @Override
        public X509TrustManager trustManager(final SSLSocketFactory sslSocketFactory) {
            final Object context = Platform.readFieldOrNull(sslSocketFactory, this.sslContextClass, "context");
            if (context == null) {
                return null;
            }
            return Platform.readFieldOrNull(context, X509TrustManager.class, "trustManager");
        }
    }
    
    private static class JdkWithJettyBootPlatform extends JdkPlatform
    {
        private final Method putMethod;
        private final Method getMethod;
        private final Method removeMethod;
        private final Class<?> clientProviderClass;
        private final Class<?> serverProviderClass;
        
        public JdkWithJettyBootPlatform(final Class<?> sslContextClass, final Method putMethod, final Method getMethod, final Method removeMethod, final Class<?> clientProviderClass, final Class<?> serverProviderClass) {
            super(sslContextClass);
            this.putMethod = putMethod;
            this.getMethod = getMethod;
            this.removeMethod = removeMethod;
            this.clientProviderClass = clientProviderClass;
            this.serverProviderClass = serverProviderClass;
        }
        
        @Override
        public void configureTlsExtensions(final SSLSocket sslSocket, final String hostname, final List<Protocol> protocols) {
            final List<String> names = new ArrayList<String>(protocols.size());
            for (int i = 0, size = protocols.size(); i < size; ++i) {
                final Protocol protocol = protocols.get(i);
                if (protocol != Protocol.HTTP_1_0) {
                    names.add(protocol.toString());
                }
            }
            try {
                final Object provider = Proxy.newProxyInstance(Platform.class.getClassLoader(), new Class[] { this.clientProviderClass, this.serverProviderClass }, new JettyNegoProvider(names));
                this.putMethod.invoke(null, sslSocket, provider);
            }
            catch (InvocationTargetException | IllegalAccessException ex2) {
                final ReflectiveOperationException ex;
                final ReflectiveOperationException e = ex;
                throw new AssertionError((Object)e);
            }
        }
        
        @Override
        public void afterHandshake(final SSLSocket sslSocket) {
            try {
                this.removeMethod.invoke(null, sslSocket);
            }
            catch (IllegalAccessException | InvocationTargetException ex2) {
                final ReflectiveOperationException ex;
                final ReflectiveOperationException ignored = ex;
                throw new AssertionError();
            }
        }
        
        @Override
        public String getSelectedProtocol(final SSLSocket socket) {
            try {
                final JettyNegoProvider provider = (JettyNegoProvider)Proxy.getInvocationHandler(this.getMethod.invoke(null, socket));
                if (!provider.unsupported && provider.selected == null) {
                    Internal.logger.log(Level.INFO, "ALPN callback dropped: SPDY and HTTP/2 are disabled. Is alpn-boot on the boot class path?");
                    return null;
                }
                return provider.unsupported ? null : provider.selected;
            }
            catch (InvocationTargetException | IllegalAccessException ex2) {
                final ReflectiveOperationException ex;
                final ReflectiveOperationException e = ex;
                throw new AssertionError();
            }
        }
    }
    
    private static class JettyNegoProvider implements InvocationHandler
    {
        private final List<String> protocols;
        private boolean unsupported;
        private String selected;
        
        public JettyNegoProvider(final List<String> protocols) {
            this.protocols = protocols;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, Object[] args) throws Throwable {
            final String methodName = method.getName();
            final Class<?> returnType = method.getReturnType();
            if (args == null) {
                args = Util.EMPTY_STRING_ARRAY;
            }
            if (methodName.equals("supports") && Boolean.TYPE == returnType) {
                return true;
            }
            if (methodName.equals("unsupported") && Void.TYPE == returnType) {
                this.unsupported = true;
                return null;
            }
            if (methodName.equals("protocols") && args.length == 0) {
                return this.protocols;
            }
            if ((methodName.equals("selectProtocol") || methodName.equals("select")) && String.class == returnType && args.length == 1 && args[0] instanceof List) {
                final List<String> peerProtocols = (List<String>)args[0];
                for (int i = 0, size = peerProtocols.size(); i < size; ++i) {
                    if (this.protocols.contains(peerProtocols.get(i))) {
                        return this.selected = peerProtocols.get(i);
                    }
                }
                return this.selected = this.protocols.get(0);
            }
            if ((methodName.equals("protocolSelected") || methodName.equals("selected")) && args.length == 1) {
                this.selected = (String)args[0];
                return null;
            }
            return method.invoke(this, args);
        }
    }
}
