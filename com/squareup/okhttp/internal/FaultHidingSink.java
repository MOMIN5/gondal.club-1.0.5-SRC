// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal;

import java.io.IOException;
import okio.Buffer;
import okio.Sink;
import okio.ForwardingSink;

class FaultHidingSink extends ForwardingSink
{
    private boolean hasErrors;
    
    public FaultHidingSink(final Sink delegate) {
        super(delegate);
    }
    
    @Override
    public void write(final Buffer source, final long byteCount) throws IOException {
        if (this.hasErrors) {
            source.skip(byteCount);
            return;
        }
        try {
            super.write(source, byteCount);
        }
        catch (IOException e) {
            this.hasErrors = true;
            this.onException(e);
        }
    }
    
    @Override
    public void flush() throws IOException {
        if (this.hasErrors) {
            return;
        }
        try {
            super.flush();
        }
        catch (IOException e) {
            this.hasErrors = true;
            this.onException(e);
        }
    }
    
    @Override
    public void close() throws IOException {
        if (this.hasErrors) {
            return;
        }
        try {
            super.close();
        }
        catch (IOException e) {
            this.hasErrors = true;
            this.onException(e);
        }
    }
    
    protected void onException(final IOException e) {
    }
}
