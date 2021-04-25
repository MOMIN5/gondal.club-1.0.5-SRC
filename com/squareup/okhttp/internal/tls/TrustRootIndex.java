// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.tls;

import java.security.cert.X509Certificate;

public interface TrustRootIndex
{
    X509Certificate findByIssuerAndSignature(final X509Certificate p0);
}
