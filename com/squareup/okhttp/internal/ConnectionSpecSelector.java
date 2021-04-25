// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal;

import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLHandshakeException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.io.IOException;
import java.net.UnknownServiceException;
import java.util.Arrays;
import javax.net.ssl.SSLSocket;
import com.squareup.okhttp.ConnectionSpec;
import java.util.List;

public final class ConnectionSpecSelector
{
    private final List<ConnectionSpec> connectionSpecs;
    private int nextModeIndex;
    private boolean isFallbackPossible;
    private boolean isFallback;
    
    public ConnectionSpecSelector(final List<ConnectionSpec> connectionSpecs) {
        this.nextModeIndex = 0;
        this.connectionSpecs = connectionSpecs;
    }
    
    public ConnectionSpec configureSecureSocket(final SSLSocket sslSocket) throws IOException {
        ConnectionSpec tlsConfiguration = null;
        for (int i = this.nextModeIndex, size = this.connectionSpecs.size(); i < size; ++i) {
            final ConnectionSpec connectionSpec = this.connectionSpecs.get(i);
            if (connectionSpec.isCompatible(sslSocket)) {
                tlsConfiguration = connectionSpec;
                this.nextModeIndex = i + 1;
                break;
            }
        }
        if (tlsConfiguration == null) {
            throw new UnknownServiceException("Unable to find acceptable protocols. isFallback=" + this.isFallback + ", modes=" + this.connectionSpecs + ", supported protocols=" + Arrays.toString(sslSocket.getEnabledProtocols()));
        }
        this.isFallbackPossible = this.isFallbackPossible(sslSocket);
        Internal.instance.apply(tlsConfiguration, sslSocket, this.isFallback);
        return tlsConfiguration;
    }
    
    public boolean connectionFailed(final IOException e) {
        this.isFallback = true;
        return this.isFallbackPossible && !(e instanceof ProtocolException) && !(e instanceof InterruptedIOException) && (!(e instanceof SSLHandshakeException) || !(e.getCause() instanceof CertificateException)) && !(e instanceof SSLPeerUnverifiedException) && (e instanceof SSLHandshakeException || e instanceof SSLProtocolException);
    }
    
    private boolean isFallbackPossible(final SSLSocket socket) {
        for (int i = this.nextModeIndex; i < this.connectionSpecs.size(); ++i) {
            if (this.connectionSpecs.get(i).isCompatible(socket)) {
                return true;
            }
        }
        return false;
    }
}
