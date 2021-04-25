// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import com.squareup.okhttp.internal.Util;

public final class Challenge
{
    private final String scheme;
    private final String realm;
    
    public Challenge(final String scheme, final String realm) {
        this.scheme = scheme;
        this.realm = realm;
    }
    
    public String getScheme() {
        return this.scheme;
    }
    
    public String getRealm() {
        return this.realm;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof Challenge && Util.equal(this.scheme, ((Challenge)o).scheme) && Util.equal(this.realm, ((Challenge)o).realm);
    }
    
    @Override
    public int hashCode() {
        int result = 29;
        result = 31 * result + ((this.realm != null) ? this.realm.hashCode() : 0);
        result = 31 * result + ((this.scheme != null) ? this.scheme.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return this.scheme + " realm=\"" + this.realm + "\"";
    }
}
