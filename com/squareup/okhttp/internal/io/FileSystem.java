// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.io;

import okio.Okio;
import java.io.IOException;
import okio.Sink;
import java.io.FileNotFoundException;
import okio.Source;
import java.io.File;

public interface FileSystem
{
    public static final FileSystem SYSTEM = new FileSystem() {
        @Override
        public Source source(final File file) throws FileNotFoundException {
            return Okio.source(file);
        }
        
        @Override
        public Sink sink(final File file) throws FileNotFoundException {
            try {
                return Okio.sink(file);
            }
            catch (FileNotFoundException e) {
                file.getParentFile().mkdirs();
                return Okio.sink(file);
            }
        }
        
        @Override
        public Sink appendingSink(final File file) throws FileNotFoundException {
            try {
                return Okio.appendingSink(file);
            }
            catch (FileNotFoundException e) {
                file.getParentFile().mkdirs();
                return Okio.appendingSink(file);
            }
        }
        
        @Override
        public void delete(final File file) throws IOException {
            if (!file.delete() && file.exists()) {
                throw new IOException("failed to delete " + file);
            }
        }
        
        @Override
        public boolean exists(final File file) throws IOException {
            return file.exists();
        }
        
        @Override
        public long size(final File file) {
            return file.length();
        }
        
        @Override
        public void rename(final File from, final File to) throws IOException {
            this.delete(to);
            if (!from.renameTo(to)) {
                throw new IOException("failed to rename " + from + " to " + to);
            }
        }
        
        @Override
        public void deleteContents(final File directory) throws IOException {
            final File[] files = directory.listFiles();
            if (files == null) {
                throw new IOException("not a readable directory: " + directory);
            }
            for (final File file : files) {
                if (file.isDirectory()) {
                    this.deleteContents(file);
                }
                if (!file.delete()) {
                    throw new IOException("failed to delete " + file);
                }
            }
        }
    };
    
    Source source(final File p0) throws FileNotFoundException;
    
    Sink sink(final File p0) throws FileNotFoundException;
    
    Sink appendingSink(final File p0) throws FileNotFoundException;
    
    void delete(final File p0) throws IOException;
    
    boolean exists(final File p0) throws IOException;
    
    long size(final File p0);
    
    void rename(final File p0, final File p1) throws IOException;
    
    void deleteContents(final File p0) throws IOException;
}
