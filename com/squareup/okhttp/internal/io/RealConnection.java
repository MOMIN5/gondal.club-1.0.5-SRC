// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.io;

import java.net.SocketTimeoutException;
import com.squareup.okhttp.internal.Version;
import okio.Source;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.internal.http.OkHeaders;
import java.util.concurrent.TimeUnit;
import com.squareup.okhttp.internal.http.Http1xStream;
import javax.net.ssl.X509TrustManager;
import com.squareup.okhttp.internal.tls.CertificateChainCleaner;
import javax.net.ssl.SSLPeerUnverifiedException;
import com.squareup.okhttp.internal.tls.OkHostnameVerifier;
import java.security.cert.Certificate;
import com.squareup.okhttp.CertificatePinner;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSocket;
import okio.Okio;
import java.net.ConnectException;
import com.squareup.okhttp.internal.Platform;
import com.squareup.okhttp.Address;
import com.squareup.okhttp.internal.Util;
import java.net.Proxy;
import java.io.IOException;
import com.squareup.okhttp.internal.http.RouteException;
import java.net.UnknownServiceException;
import com.squareup.okhttp.internal.ConnectionSpecSelector;
import com.squareup.okhttp.ConnectionSpec;
import java.util.ArrayList;
import com.squareup.okhttp.internal.tls.TrustRootIndex;
import javax.net.ssl.SSLSocketFactory;
import com.squareup.okhttp.internal.http.StreamAllocation;
import java.lang.ref.Reference;
import java.util.List;
import okio.BufferedSink;
import okio.BufferedSource;
import com.squareup.okhttp.internal.framed.FramedConnection;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Handshake;
import java.net.Socket;
import com.squareup.okhttp.Route;
import com.squareup.okhttp.Connection;

public final class RealConnection implements Connection
{
    private final Route route;
    private Socket rawSocket;
    public Socket socket;
    private Handshake handshake;
    private Protocol protocol;
    public volatile FramedConnection framedConnection;
    public int streamCount;
    public BufferedSource source;
    public BufferedSink sink;
    public final List<Reference<StreamAllocation>> allocations;
    public boolean noNewStreams;
    public long idleAtNanos;
    private static SSLSocketFactory lastSslSocketFactory;
    private static TrustRootIndex lastTrustRootIndex;
    
    public RealConnection(final Route route) {
        this.allocations = new ArrayList<Reference<StreamAllocation>>();
        this.idleAtNanos = Long.MAX_VALUE;
        this.route = route;
    }
    
    public void connect(final int connectTimeout, final int readTimeout, final int writeTimeout, final List<ConnectionSpec> connectionSpecs, final boolean connectionRetryEnabled) throws RouteException {
        if (this.protocol != null) {
            throw new IllegalStateException("already connected");
        }
        RouteException routeException = null;
        final ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);
        final Proxy proxy = this.route.getProxy();
        final Address address = this.route.getAddress();
        if (this.route.getAddress().getSslSocketFactory() == null && !connectionSpecs.contains(ConnectionSpec.CLEARTEXT)) {
            throw new RouteException(new UnknownServiceException("CLEARTEXT communication not supported: " + connectionSpecs));
        }
        while (this.protocol == null) {
            try {
                this.rawSocket = ((proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP) ? address.getSocketFactory().createSocket() : new Socket(proxy));
                this.connectSocket(connectTimeout, readTimeout, writeTimeout, connectionSpecSelector);
            }
            catch (IOException e) {
                Util.closeQuietly(this.socket);
                Util.closeQuietly(this.rawSocket);
                this.socket = null;
                this.rawSocket = null;
                this.source = null;
                this.sink = null;
                this.handshake = null;
                this.protocol = null;
                if (routeException == null) {
                    routeException = new RouteException(e);
                }
                else {
                    routeException.addConnectException(e);
                }
                if (!connectionRetryEnabled || !connectionSpecSelector.connectionFailed(e)) {
                    throw routeException;
                }
                continue;
            }
        }
    }
    
    private void connectSocket(final int connectTimeout, final int readTimeout, final int writeTimeout, final ConnectionSpecSelector connectionSpecSelector) throws IOException {
        this.rawSocket.setSoTimeout(readTimeout);
        try {
            Platform.get().connectSocket(this.rawSocket, this.route.getSocketAddress(), connectTimeout);
        }
        catch (ConnectException e) {
            throw new ConnectException("Failed to connect to " + this.route.getSocketAddress());
        }
        this.source = Okio.buffer(Okio.source(this.rawSocket));
        this.sink = Okio.buffer(Okio.sink(this.rawSocket));
        if (this.route.getAddress().getSslSocketFactory() != null) {
            this.connectTls(readTimeout, writeTimeout, connectionSpecSelector);
        }
        else {
            this.protocol = Protocol.HTTP_1_1;
            this.socket = this.rawSocket;
        }
        if (this.protocol == Protocol.SPDY_3 || this.protocol == Protocol.HTTP_2) {
            this.socket.setSoTimeout(0);
            final FramedConnection framedConnection = new FramedConnection.Builder(true).socket(this.socket, this.route.getAddress().url().host(), this.source, this.sink).protocol(this.protocol).build();
            framedConnection.sendConnectionPreface();
            this.framedConnection = framedConnection;
        }
    }
    
    private void connectTls(final int readTimeout, final int writeTimeout, final ConnectionSpecSelector connectionSpecSelector) throws IOException {
        if (this.route.requiresTunnel()) {
            this.createTunnel(readTimeout, writeTimeout);
        }
        final Address address = this.route.getAddress();
        final SSLSocketFactory sslSocketFactory = address.getSslSocketFactory();
        boolean success = false;
        SSLSocket sslSocket = null;
        try {
            sslSocket = (SSLSocket)sslSocketFactory.createSocket(this.rawSocket, address.getUriHost(), address.getUriPort(), true);
            final ConnectionSpec connectionSpec = connectionSpecSelector.configureSecureSocket(sslSocket);
            if (connectionSpec.supportsTlsExtensions()) {
                Platform.get().configureTlsExtensions(sslSocket, address.getUriHost(), address.getProtocols());
            }
            sslSocket.startHandshake();
            final Handshake unverifiedHandshake = Handshake.get(sslSocket.getSession());
            if (!address.getHostnameVerifier().verify(address.getUriHost(), sslSocket.getSession())) {
                final X509Certificate cert = unverifiedHandshake.peerCertificates().get(0);
                throw new SSLPeerUnverifiedException("Hostname " + address.getUriHost() + " not verified:" + "\n    certificate: " + CertificatePinner.pin(cert) + "\n    DN: " + cert.getSubjectDN().getName() + "\n    subjectAltNames: " + OkHostnameVerifier.allSubjectAltNames(cert));
            }
            if (address.getCertificatePinner() != CertificatePinner.DEFAULT) {
                final TrustRootIndex trustRootIndex = trustRootIndex(address.getSslSocketFactory());
                final List<Certificate> certificates = new CertificateChainCleaner(trustRootIndex).clean(unverifiedHandshake.peerCertificates());
                address.getCertificatePinner().check(address.getUriHost(), certificates);
            }
            final String maybeProtocol = connectionSpec.supportsTlsExtensions() ? Platform.get().getSelectedProtocol(sslSocket) : null;
            this.socket = sslSocket;
            this.source = Okio.buffer(Okio.source(this.socket));
            this.sink = Okio.buffer(Okio.sink(this.socket));
            this.handshake = unverifiedHandshake;
            this.protocol = ((maybeProtocol != null) ? Protocol.get(maybeProtocol) : Protocol.HTTP_1_1);
            success = true;
        }
        catch (AssertionError e) {
            if (Util.isAndroidGetsocknameError(e)) {
                throw new IOException(e);
            }
            throw e;
        }
        finally {
            if (sslSocket != null) {
                Platform.get().afterHandshake(sslSocket);
            }
            if (!success) {
                Util.closeQuietly(sslSocket);
            }
        }
    }
    
    private static synchronized TrustRootIndex trustRootIndex(final SSLSocketFactory sslSocketFactory) {
        if (sslSocketFactory != RealConnection.lastSslSocketFactory) {
            final X509TrustManager trustManager = Platform.get().trustManager(sslSocketFactory);
            RealConnection.lastTrustRootIndex = Platform.get().trustRootIndex(trustManager);
            RealConnection.lastSslSocketFactory = sslSocketFactory;
        }
        return RealConnection.lastTrustRootIndex;
    }
    
    private void createTunnel(final int readTimeout, final int writeTimeout) throws IOException {
        Request tunnelRequest = this.createTunnelRequest();
        final HttpUrl url = tunnelRequest.httpUrl();
        final String requestLine = "CONNECT " + url.host() + ":" + url.port() + " HTTP/1.1";
        while (true) {
            final Http1xStream tunnelConnection = new Http1xStream(null, this.source, this.sink);
            this.source.timeout().timeout(readTimeout, TimeUnit.MILLISECONDS);
            this.sink.timeout().timeout(writeTimeout, TimeUnit.MILLISECONDS);
            tunnelConnection.writeRequest(tunnelRequest.headers(), requestLine);
            tunnelConnection.finishRequest();
            final Response response = tunnelConnection.readResponse().request(tunnelRequest).build();
            long contentLength = OkHeaders.contentLength(response);
            if (contentLength == -1L) {
                contentLength = 0L;
            }
            final Source body = tunnelConnection.newFixedLengthSource(contentLength);
            Util.skipAll(body, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            body.close();
            switch (response.code()) {
                case 200: {
                    if (!this.source.buffer().exhausted() || !this.sink.buffer().exhausted()) {
                        throw new IOException("TLS tunnel buffered too many bytes!");
                    }
                }
                case 407: {
                    tunnelRequest = OkHeaders.processAuthHeader(this.route.getAddress().getAuthenticator(), response, this.route.getProxy());
                    if (tunnelRequest != null) {
                        continue;
                    }
                    throw new IOException("Failed to authenticate with proxy");
                }
                default: {
                    throw new IOException("Unexpected response code for CONNECT: " + response.code());
                }
            }
        }
    }
    
    private Request createTunnelRequest() throws IOException {
        return new Request.Builder().url(this.route.getAddress().url()).header("Host", Util.hostHeader(this.route.getAddress().url())).header("Proxy-Connection", "Keep-Alive").header("User-Agent", Version.userAgent()).build();
    }
    
    boolean isConnected() {
        return this.protocol != null;
    }
    
    @Override
    public Route getRoute() {
        return this.route;
    }
    
    public void cancel() {
        Util.closeQuietly(this.rawSocket);
    }
    
    @Override
    public Socket getSocket() {
        return this.socket;
    }
    
    public int allocationLimit() {
        final FramedConnection framedConnection = this.framedConnection;
        return (framedConnection != null) ? framedConnection.maxConcurrentStreams() : 1;
    }
    
    public boolean isHealthy(final boolean doExtensiveChecks) {
        if (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
            return false;
        }
        if (this.framedConnection != null) {
            return true;
        }
        if (doExtensiveChecks) {
            try {
                final int readTimeout = this.socket.getSoTimeout();
                try {
                    this.socket.setSoTimeout(1);
                    return !this.source.exhausted();
                }
                finally {
                    this.socket.setSoTimeout(readTimeout);
                }
            }
            catch (SocketTimeoutException ex) {}
            catch (IOException e) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Handshake getHandshake() {
        return this.handshake;
    }
    
    public boolean isMultiplexed() {
        return this.framedConnection != null;
    }
    
    @Override
    public Protocol getProtocol() {
        return (this.protocol != null) ? this.protocol : Protocol.HTTP_1_1;
    }
    
    @Override
    public String toString() {
        return "Connection{" + this.route.getAddress().url().host() + ":" + this.route.getAddress().url().port() + ", proxy=" + this.route.getProxy() + " hostAddress=" + this.route.getSocketAddress() + " cipherSuite=" + ((this.handshake != null) ? this.handshake.cipherSuite() : "none") + " protocol=" + this.protocol + '}';
    }
}
