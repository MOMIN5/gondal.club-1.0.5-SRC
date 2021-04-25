// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.io.UnsupportedEncodingException;
import okio.ByteString;

public final class Credentials
{
    private Credentials() {
    }
    
    public static String basic(final String userName, final String password) {
        try {
            final String usernameAndPassword = userName + ":" + password;
            final byte[] bytes = usernameAndPassword.getBytes("ISO-8859-1");
            final String encoded = ByteString.of(bytes).base64();
            return "Basic " + encoded;
        }
        catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }
}
