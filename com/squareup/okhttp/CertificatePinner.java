// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Iterator;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.util.List;
import com.squareup.okhttp.internal.Util;
import okio.ByteString;
import java.util.Set;
import java.util.Map;

public final class CertificatePinner
{
    public static final CertificatePinner DEFAULT;
    private final Map<String, Set<ByteString>> hostnameToPins;
    
    private CertificatePinner(final Builder builder) {
        this.hostnameToPins = Util.immutableMap(builder.hostnameToPins);
    }
    
    public void check(final String hostname, final List<Certificate> peerCertificates) throws SSLPeerUnverifiedException {
        final Set<ByteString> pins = this.findMatchingPins(hostname);
        if (pins == null) {
            return;
        }
        for (int i = 0, size = peerCertificates.size(); i < size; ++i) {
            final X509Certificate x509Certificate = peerCertificates.get(i);
            if (pins.contains(sha1(x509Certificate))) {
                return;
            }
        }
        final StringBuilder message = new StringBuilder().append("Certificate pinning failure!").append("\n  Peer certificate chain:");
        for (int j = 0, size2 = peerCertificates.size(); j < size2; ++j) {
            final X509Certificate x509Certificate2 = peerCertificates.get(j);
            message.append("\n    ").append(pin(x509Certificate2)).append(": ").append(x509Certificate2.getSubjectDN().getName());
        }
        message.append("\n  Pinned certificates for ").append(hostname).append(":");
        for (final ByteString pin : pins) {
            message.append("\n    sha1/").append(pin.base64());
        }
        throw new SSLPeerUnverifiedException(message.toString());
    }
    
    @Deprecated
    public void check(final String hostname, final Certificate... peerCertificates) throws SSLPeerUnverifiedException {
        this.check(hostname, Arrays.asList(peerCertificates));
    }
    
    Set<ByteString> findMatchingPins(final String hostname) {
        final Set<ByteString> directPins = this.hostnameToPins.get(hostname);
        Set<ByteString> wildcardPins = null;
        final int indexOfFirstDot = hostname.indexOf(46);
        final int indexOfLastDot = hostname.lastIndexOf(46);
        if (indexOfFirstDot != indexOfLastDot) {
            wildcardPins = this.hostnameToPins.get("*." + hostname.substring(indexOfFirstDot + 1));
        }
        if (directPins == null && wildcardPins == null) {
            return null;
        }
        if (directPins != null && wildcardPins != null) {
            final Set<ByteString> pins = new LinkedHashSet<ByteString>();
            pins.addAll(directPins);
            pins.addAll(wildcardPins);
            return pins;
        }
        if (directPins != null) {
            return directPins;
        }
        return wildcardPins;
    }
    
    public static String pin(final Certificate certificate) {
        if (!(certificate instanceof X509Certificate)) {
            throw new IllegalArgumentException("Certificate pinning requires X509 certificates");
        }
        return "sha1/" + sha1((X509Certificate)certificate).base64();
    }
    
    private static ByteString sha1(final X509Certificate x509Certificate) {
        return Util.sha1(ByteString.of(x509Certificate.getPublicKey().getEncoded()));
    }
    
    static {
        DEFAULT = new Builder().build();
    }
    
    public static final class Builder
    {
        private final Map<String, Set<ByteString>> hostnameToPins;
        
        public Builder() {
            this.hostnameToPins = new LinkedHashMap<String, Set<ByteString>>();
        }
        
        public Builder add(final String hostname, final String... pins) {
            if (hostname == null) {
                throw new IllegalArgumentException("hostname == null");
            }
            final Set<ByteString> hostPins = new LinkedHashSet<ByteString>();
            final Set<ByteString> previousPins = this.hostnameToPins.put(hostname, Collections.unmodifiableSet((Set<? extends ByteString>)hostPins));
            if (previousPins != null) {
                hostPins.addAll(previousPins);
            }
            for (final String pin : pins) {
                if (!pin.startsWith("sha1/")) {
                    throw new IllegalArgumentException("pins must start with 'sha1/': " + pin);
                }
                final ByteString decodedPin = ByteString.decodeBase64(pin.substring("sha1/".length()));
                if (decodedPin == null) {
                    throw new IllegalArgumentException("pins must be base64: " + pin);
                }
                hostPins.add(decodedPin);
            }
            return this;
        }
        
        public CertificatePinner build() {
            return new CertificatePinner(this, null);
        }
    }
}
