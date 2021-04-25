// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.tls;

import java.util.Iterator;
import java.security.cert.CertificateParsingException;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import javax.security.auth.x500.X500Principal;
import java.util.Locale;
import java.util.List;
import java.security.cert.Certificate;
import javax.net.ssl.SSLException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;

public final class OkHostnameVerifier implements HostnameVerifier
{
    public static final OkHostnameVerifier INSTANCE;
    private static final Pattern VERIFY_AS_IP_ADDRESS;
    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;
    
    private OkHostnameVerifier() {
    }
    
    @Override
    public boolean verify(final String host, final SSLSession session) {
        try {
            final Certificate[] certificates = session.getPeerCertificates();
            return this.verify(host, (X509Certificate)certificates[0]);
        }
        catch (SSLException e) {
            return false;
        }
    }
    
    public boolean verify(final String host, final X509Certificate certificate) {
        return verifyAsIpAddress(host) ? this.verifyIpAddress(host, certificate) : this.verifyHostName(host, certificate);
    }
    
    static boolean verifyAsIpAddress(final String host) {
        return OkHostnameVerifier.VERIFY_AS_IP_ADDRESS.matcher(host).matches();
    }
    
    private boolean verifyIpAddress(final String ipAddress, final X509Certificate certificate) {
        final List<String> altNames = getSubjectAltNames(certificate, 7);
        for (int i = 0, size = altNames.size(); i < size; ++i) {
            if (ipAddress.equalsIgnoreCase(altNames.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    private boolean verifyHostName(String hostName, final X509Certificate certificate) {
        hostName = hostName.toLowerCase(Locale.US);
        boolean hasDns = false;
        final List<String> altNames = getSubjectAltNames(certificate, 2);
        for (int i = 0, size = altNames.size(); i < size; ++i) {
            hasDns = true;
            if (this.verifyHostName(hostName, altNames.get(i))) {
                return true;
            }
        }
        if (!hasDns) {
            final X500Principal principal = certificate.getSubjectX500Principal();
            final String cn = new DistinguishedNameParser(principal).findMostSpecific("cn");
            if (cn != null) {
                return this.verifyHostName(hostName, cn);
            }
        }
        return false;
    }
    
    public static List<String> allSubjectAltNames(final X509Certificate certificate) {
        final List<String> altIpaNames = getSubjectAltNames(certificate, 7);
        final List<String> altDnsNames = getSubjectAltNames(certificate, 2);
        final List<String> result = new ArrayList<String>(altIpaNames.size() + altDnsNames.size());
        result.addAll(altIpaNames);
        result.addAll(altDnsNames);
        return result;
    }
    
    private static List<String> getSubjectAltNames(final X509Certificate certificate, final int type) {
        final List<String> result = new ArrayList<String>();
        try {
            final Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null) {
                return Collections.emptyList();
            }
            for (final Object subjectAltName : subjectAltNames) {
                final List<?> entry = (List<?>)subjectAltName;
                if (entry != null) {
                    if (entry.size() < 2) {
                        continue;
                    }
                    final Integer altNameType = (Integer)entry.get(0);
                    if (altNameType == null) {
                        continue;
                    }
                    if (altNameType != type) {
                        continue;
                    }
                    final String altName = (String)entry.get(1);
                    if (altName == null) {
                        continue;
                    }
                    result.add(altName);
                }
            }
            return result;
        }
        catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }
    
    private boolean verifyHostName(String hostName, String pattern) {
        if (hostName == null || hostName.length() == 0 || hostName.startsWith(".") || hostName.endsWith("..")) {
            return false;
        }
        if (pattern == null || pattern.length() == 0 || pattern.startsWith(".") || pattern.endsWith("..")) {
            return false;
        }
        if (!hostName.endsWith(".")) {
            hostName += '.';
        }
        if (!pattern.endsWith(".")) {
            pattern += '.';
        }
        pattern = pattern.toLowerCase(Locale.US);
        if (!pattern.contains("*")) {
            return hostName.equals(pattern);
        }
        if (!pattern.startsWith("*.") || pattern.indexOf(42, 1) != -1) {
            return false;
        }
        if (hostName.length() < pattern.length()) {
            return false;
        }
        if ("*.".equals(pattern)) {
            return false;
        }
        final String suffix = pattern.substring(1);
        if (!hostName.endsWith(suffix)) {
            return false;
        }
        final int suffixStartIndexInHostName = hostName.length() - suffix.length();
        return suffixStartIndexInHostName <= 0 || hostName.lastIndexOf(46, suffixStartIndexInHostName - 1) == -1;
    }
    
    static {
        INSTANCE = new OkHostnameVerifier();
        VERIFY_AS_IP_ADDRESS = Pattern.compile("([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)");
    }
}
