// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

public final class HeaderParser
{
    public static int skipUntil(final String input, int pos, final String characters) {
        while (pos < input.length() && characters.indexOf(input.charAt(pos)) == -1) {
            ++pos;
        }
        return pos;
    }
    
    public static int skipWhitespace(final String input, int pos) {
        while (pos < input.length()) {
            final char c = input.charAt(pos);
            if (c != ' ' && c != '\t') {
                break;
            }
            ++pos;
        }
        return pos;
    }
    
    public static int parseSeconds(final String value, final int defaultValue) {
        try {
            final long seconds = Long.parseLong(value);
            if (seconds > 2147483647L) {
                return Integer.MAX_VALUE;
            }
            if (seconds < 0L) {
                return 0;
            }
            return (int)seconds;
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private HeaderParser() {
    }
}
