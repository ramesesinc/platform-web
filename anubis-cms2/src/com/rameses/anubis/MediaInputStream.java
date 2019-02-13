/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author wflores
 */
public class MediaInputStream extends InputStream {

    private InputStream source;

    public MediaInputStream(InputStream source) {
        this.source = source;
    }

    public long skip(long n) throws IOException {
        return source.skip(n);
    }

    public synchronized void reset() throws IOException {
        source.reset();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return source.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return source.read(b);
    }

    public boolean markSupported() {
        return source.markSupported();
    }

    public synchronized void mark(int readlimit) {
        source.mark(readlimit);
    }

    public void close() throws IOException {
        source.close();
    }

    public int available() throws IOException {
        return source.available();
    }

    public int read() throws IOException {
        return source.read();
    }
}
