package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import llc.ufwa.data.exception.ResourceException;

public class SingleFileCache implements Cache<String, InputStream> {
    
    private final TreeSet<ValueMetaData> sortedFiles =
        new TreeSet<ValueMetaData>(

            new Comparator<ValueMetaData>() {
    
                @Override
                public int compare(ValueMetaData item1, ValueMetaData item2) {
                    return (int)(item2.getModified() - item1.getModified());                
                }
    
            }
    
        );

    private final long expiresTimeout;
    private final States states = new States();
    private final long maxSize;
    private final File rootFolder;
    private final int maxKeySize;
    private final int hashSize;
    private final float maxFragmentation;
    
    private final long cacheLastModified = 0;

    /**
     * 
     * @param rootFolder
     * @param maxSize
     * @param expiresTimeout
     * @param maxKeySize
     * @param hashSize
     */
    public SingleFileCache(
        final File rootFolder,
        final long maxSize,
        final long expiresTimeout,
        final int maxKeySize,
        final int hashSize,
        final float maxFragmentation
    ) {
        
        this.maxFragmentation = maxFragmentation;
        this.maxKeySize = maxKeySize;
        this.hashSize = hashSize;
        this.rootFolder = rootFolder;
        this.maxSize = maxSize;
        this.expiresTimeout = expiresTimeout;
        
        if(!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        
        if(!rootFolder.isDirectory()) {
            throw new RuntimeException("must either not exist or be a directory: " + rootFolder.getAbsolutePath());
        }
        
    }
    
    private void clean() {

        if(maxSize >= 0) {            

            while(states.getCurrentSize() > maxSize) {

                if(sortedFiles.size() <= 0)  {
                    throw new RuntimeException("Cache thinks it is bigger than max size but contains zero files.");
                }

                final ValueMetaData last = sortedFiles.last();

                sortedFiles.remove(last);

                final long length = last.length();

                states.removeFromSize(length);

                remove(last.getKey());

            }
        }
    }
    
    private static class ValueMetaData {
        
        private final long modified;
        private final long length;
        private final String key;
        
        public ValueMetaData(
            final String key,
            final long modified,
            final long length
        ) {
            
            this.key = key;
            this.modified = modified;
            this.length = length;
            
        }

        public long length() {
            return length;
        }

        public String getKey() {
            return key;
        }

        public long getModified() {
            return modified;
        }
        
    }
    
    private static final class States {

        private long currentSize;

        public States() {

        }

        public void addSize(long length) {
            this.currentSize += length;
        }

        public long getCurrentSize() {
            return currentSize;
        }

        public void setCurrentSize(long currentSize) {
            this.currentSize = currentSize;
        }

        public void removeFromSize(long remove) {
            this.currentSize -= remove;
        }

    }

    @Override
    public boolean exists(String key) throws ResourceException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public InputStream get(String key) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<InputStream> getAll(List<String> keys) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void remove(String key) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void put(String key, InputStream value) {
        // TODO Auto-generated method stub
        
    }

}
