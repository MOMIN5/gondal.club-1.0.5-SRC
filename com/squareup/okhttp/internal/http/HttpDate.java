// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.http;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.ParsePosition;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public final class HttpDate
{
    private static final TimeZone GMT;
    private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT;
    private static final String[] BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS;
    private static final DateFormat[] BROWSER_COMPATIBLE_DATE_FORMATS;
    
    public static Date parse(final String value) {
        if (value.length() == 0) {
            return null;
        }
        final ParsePosition position = new ParsePosition(0);
        Date result = HttpDate.STANDARD_DATE_FORMAT.get().parse(value, position);
        if (position.getIndex() == value.length()) {
            return result;
        }
        synchronized (HttpDate.BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS) {
            for (int i = 0, count = HttpDate.BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length; i < count; ++i) {
                DateFormat format = HttpDate.BROWSER_COMPATIBLE_DATE_FORMATS[i];
                if (format == null) {
                    format = new SimpleDateFormat(HttpDate.BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS[i], Locale.US);
                    format.setTimeZone(HttpDate.GMT);
                    HttpDate.BROWSER_COMPATIBLE_DATE_FORMATS[i] = format;
                }
                position.setIndex(0);
                result = format.parse(value, position);
                if (position.getIndex() != 0) {
                    return result;
                }
            }
        }
        return null;
    }
    
    public static String format(final Date value) {
        return HttpDate.STANDARD_DATE_FORMAT.get().format(value);
    }
    
    private HttpDate() {
    }
    
    static {
        GMT = TimeZone.getTimeZone("GMT");
        STANDARD_DATE_FORMAT = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                final DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                rfc1123.setLenient(false);
                rfc1123.setTimeZone(HttpDate.GMT);
                return rfc1123;
            }
        };
        BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS = new String[] { "EEE, dd MMM yyyy HH:mm:ss zzz", "EEEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM d HH:mm:ss yyyy", "EEE, dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MMM-yyyy HH-mm-ss z", "EEE, dd MMM yy HH:mm:ss z", "EEE dd-MMM-yyyy HH:mm:ss z", "EEE dd MMM yyyy HH:mm:ss z", "EEE dd-MMM-yyyy HH-mm-ss z", "EEE dd-MMM-yy HH:mm:ss z", "EEE dd MMM yy HH:mm:ss z", "EEE,dd-MMM-yy HH:mm:ss z", "EEE,dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MM-yyyy HH:mm:ss z", "EEE MMM d yyyy HH:mm:ss z" };
        BROWSER_COMPATIBLE_DATE_FORMATS = new DateFormat[HttpDate.BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length];
    }
}
