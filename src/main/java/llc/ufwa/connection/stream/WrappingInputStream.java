package llc.ufwa.connection.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used as a default wrapping implementation in the case you 
 * want to override some methods. For example on close() you could delete some files.
 * 
 * @author seanwagner
 *
 */
public abstract class WrappingInputStream extends InputStream {
    
    private final InputStream internal;

    public WrappingInputStream(final InputStream toWrap) {
        
        if(toWrap == null) {
            throw new NullPointerException("ToWrap must not be null");
        }
        
        this.internal = toWrap;
        
    }

    @Override
    public int read() throws IOException {
        return internal.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return internal.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return internal.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return internal.skip(n);
    }

    @Override
    public int available() throws IOException {
        return internal.available();
    }

    @Override
    public void close() throws IOException {
        internal.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        internal.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        internal.reset();
    }

    @Override
    public boolean markSupported() {
        return internal.markSupported();
    }
    
}
