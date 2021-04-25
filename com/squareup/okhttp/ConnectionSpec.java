// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.util.Arrays;
import javax.net.ssl.SSLSocket;
import com.squareup.okhttp.internal.Util;
import java.util.List;

public final class ConnectionSpec
{
    private static final CipherSuite[] APPROVED_CIPHER_SUITES;
    public static final ConnectionSpec MODERN_TLS;
    public static final ConnectionSpec COMPATIBLE_TLS;
    public static final ConnectionSpec CLEARTEXT;
    private final boolean tls;
    private final boolean supportsTlsExtensions;
    private final String[] cipherSuites;
    private final String[] tlsVersions;
    
    private ConnectionSpec(final Builder builder) {
        this.tls = builder.tls;
        this.cipherSuites = builder.cipherSuites;
        this.tlsVersions = builder.tlsVersions;
        this.supportsTlsExtensions = builder.supportsTlsExtensions;
    }
    
    public boolean isTls() {
        return this.tls;
    }
    
    public List<CipherSuite> cipherSuites() {
        if (this.cipherSuites == null) {
            return null;
        }
        final CipherSuite[] result = new CipherSuite[this.cipherSuites.length];
        for (int i = 0; i < this.cipherSuites.length; ++i) {
            result[i] = CipherSuite.forJavaName(this.cipherSuites[i]);
        }
        return Util.immutableList(result);
    }
    
    public List<TlsVersion> tlsVersions() {
        if (this.tlsVersions == null) {
            return null;
        }
        final TlsVersion[] result = new TlsVersion[this.tlsVersions.length];
        for (int i = 0; i < this.tlsVersions.length; ++i) {
            result[i] = TlsVersion.forJavaName(this.tlsVersions[i]);
        }
        return Util.immutableList(result);
    }
    
    public boolean supportsTlsExtensions() {
        return this.supportsTlsExtensions;
    }
    
    void apply(final SSLSocket sslSocket, final boolean isFallback) {
        final ConnectionSpec specToApply = this.supportedSpec(sslSocket, isFallback);
        if (specToApply.tlsVersions != null) {
            sslSocket.setEnabledProtocols(specToApply.tlsVersions);
        }
        if (specToApply.cipherSuites != null) {
            sslSocket.setEnabledCipherSuites(specToApply.cipherSuites);
        }
    }
    
    private ConnectionSpec supportedSpec(final SSLSocket sslSocket, final boolean isFallback) {
        String[] cipherSuitesIntersection = (this.cipherSuites != null) ? Util.intersect(String.class, this.cipherSuites, sslSocket.getEnabledCipherSuites()) : sslSocket.getEnabledCipherSuites();
        final String[] tlsVersionsIntersection = (this.tlsVersions != null) ? Util.intersect(String.class, this.tlsVersions, sslSocket.getEnabledProtocols()) : sslSocket.getEnabledProtocols();
        if (isFallback && Util.contains(sslSocket.getSupportedCipherSuites(), "TLS_FALLBACK_SCSV")) {
            cipherSuitesIntersection = Util.concat(cipherSuitesIntersection, "TLS_FALLBACK_SCSV");
        }
        return new Builder(this).cipherSuites(cipherSuitesIntersection).tlsVersions(tlsVersionsIntersection).build();
    }
    
    public boolean isCompatible(final SSLSocket socket) {
        return this.tls && (this.tlsVersions == null || nonEmptyIntersection(this.tlsVersions, socket.getEnabledProtocols())) && (this.cipherSuites == null || nonEmptyIntersection(this.cipherSuites, socket.getEnabledCipherSuites()));
    }
    
    private static boolean nonEmptyIntersection(final String[] a, final String[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0) {
            return false;
        }
        for (final String toFind : a) {
            if (Util.contains(b, toFind)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ConnectionSpec)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        final ConnectionSpec that = (ConnectionSpec)other;
        if (this.tls != that.tls) {
            return false;
        }
        if (this.tls) {
            if (!Arrays.equals(this.cipherSuites, that.cipherSuites)) {
                return false;
            }
            if (!Arrays.equals(this.tlsVersions, that.tlsVersions)) {
                return false;
            }
            if (this.supportsTlsExtensions != that.supportsTlsExtensions) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        if (this.tls) {
            result = 31 * result + Arrays.hashCode(this.cipherSuites);
            result = 31 * result + Arrays.hashCode(this.tlsVersions);
            result = 31 * result + (this.supportsTlsExtensions ? 0 : 1);
        }
        return result;
    }
    
    @Override
    public String toString() {
        if (!this.tls) {
            return "ConnectionSpec()";
        }
        final String cipherSuitesString = (this.cipherSuites != null) ? this.cipherSuites().toString() : "[all enabled]";
        final String tlsVersionsString = (this.tlsVersions != null) ? this.tlsVersions().toString() : "[all enabled]";
        return "ConnectionSpec(cipherSuites=" + cipherSuitesString + ", tlsVersions=" + tlsVersionsString + ", supportsTlsExtensions=" + this.supportsTlsExtensions + ")";
    }
    
    static {
        APPROVED_CIPHER_SUITES = new CipherSuite[] { CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA };
        MODERN_TLS = new Builder(true).cipherSuites(ConnectionSpec.APPROVED_CIPHER_SUITES).tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0).supportsTlsExtensions(true).build();
        COMPATIBLE_TLS = new Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_0).supportsTlsExtensions(true).build();
        CLEARTEXT = new Builder(false).build();
    }
    
    public static final class Builder
    {
        private boolean tls;
        private String[] cipherSuites;
        private String[] tlsVersions;
        private boolean supportsTlsExtensions;
        
        Builder(final boolean tls) {
            this.tls = tls;
        }
        
        public Builder(final ConnectionSpec connectionSpec) {
            this.tls = connectionSpec.tls;
            this.cipherSuites = connectionSpec.cipherSuites;
            this.tlsVersions = connectionSpec.tlsVersions;
            this.supportsTlsExtensions = connectionSpec.supportsTlsExtensions;
        }
        
        public Builder allEnabledCipherSuites() {
            if (!this.tls) {
                throw new IllegalStateException("no cipher suites for cleartext connections");
            }
            this.cipherSuites = null;
            return this;
        }
        
        public Builder cipherSuites(final CipherSuite... cipherSuites) {
            if (!this.tls) {
                throw new IllegalStateException("no cipher suites for cleartext connections");
            }
            final String[] strings = new String[cipherSuites.length];
            for (int i = 0; i < cipherSuites.length; ++i) {
                strings[i] = cipherSuites[i].javaName;
            }
            return this.cipherSuites(strings);
        }
        
        public Builder cipherSuites(final String... cipherSuites) {
            if (!this.tls) {
                throw new IllegalStateException("no cipher suites for cleartext connections");
            }
            if (cipherSuites.length == 0) {
                throw new IllegalArgumentException("At least one cipher suite is required");
            }
            this.cipherSuites = cipherSuites.clone();
            return this;
        }
        
        public Builder allEnabledTlsVersions() {
            if (!this.tls) {
                throw new IllegalStateException("no TLS versions for cleartext connections");
            }
            this.tlsVersions = null;
            return this;
        }
        
        public Builder tlsVersions(final TlsVersion... tlsVersions) {
            if (!this.tls) {
                throw new IllegalStateException("no TLS versions for cleartext connections");
            }
            final String[] strings = new String[tlsVersions.length];
            for (int i = 0; i < tlsVersions.length; ++i) {
                strings[i] = tlsVersions[i].javaName;
            }
            return this.tlsVersions(strings);
        }
        
        public Builder tlsVersions(final String... tlsVersions) {
            if (!this.tls) {
                throw new IllegalStateException("no TLS versions for cleartext connections");
            }
            if (tlsVersions.length == 0) {
                throw new IllegalArgumentException("At least one TLS version is required");
            }
            this.tlsVersions = tlsVersions.clone();
            return this;
        }
        
        public Builder supportsTlsExtensions(final boolean supportsTlsExtensions) {
            if (!this.tls) {
                throw new IllegalStateException("no TLS extensions for cleartext connections");
            }
            this.supportsTlsExtensions = supportsTlsExtensions;
            return this;
        }
        
        public ConnectionSpec build() {
            return new ConnectionSpec(this, null);
        }
    }
}
