package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;

/**
 * 
 * @author sean
 *
 * @param <Value>
 */
public class FilePersistedMaxSizeCache<Value> implements Cache<String, Value> {

    private final long maxSize;
    private final Cache<String, Value> internal;
    private final Cache<String, Serializable> persistCache;
    private final Converter<Integer, Value> sizeConverter;
    
    private volatile int currentSize;
    
    /**
     * 
     * @param rootFolder
     * @param internal
     * @param sizeConverter
     * @param maxSize
     */
    public FilePersistedMaxSizeCache(
        final File rootFolder,
        final Cache<String, Value> internal,
        final Converter<Integer, Value> sizeConverter,
        final long maxSize
    ) {
        
        this.maxSize = maxSize;
        this.internal = internal;
        
        final File persistRoot = new File(rootFolder, "sizePersisted");
        
        persistRoot.mkdirs();
        
        if(!persistRoot.isDirectory()) {
            throw new IllegalArgumentException("persist root must be a folder");
        }
        
        final File dataFolder = new File(persistRoot, "data");
        final File tempFolder = new File(persistRoot, "temp");
        
        dataFolder.mkdirs();
        tempFolder.mkdirs();
        
        if(!dataFolder.isDirectory()) {
            throw new IllegalArgumentException("Data folder must be a folder");
        }
        
        if(!tempFolder.isDirectory()) {
            throw new IllegalArgumentException("Temp folder must be folder");
        }
        
        final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        final Cache<String, Serializable> cache = 
            new ValueConvertingCache<String, Serializable, byte []>(
                new ValueConvertingCache<String, byte [], InputStream>( 
                        diskCache,
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<Serializable>()
                );
            
        this.persistCache = cache;
        this.sizeConverter = sizeConverter;
        
    }
    
    @Override
    public boolean exists(String key) throws ResourceException {
        return internal.exists(key);
    }

    @Override
    public Value get(String key) throws ResourceException {
        return internal.get(key);
    }

    @Override
    public List<Value> getAll(List<String> keys) throws ResourceException {
        return internal.getAll(keys);
    }

    @Override
    public void clear() throws ResourceException {
        
        this.currentSize = 0;
        this.persistCache.clear();
        
        internal.clear();
        
    }

    @Override
    public void remove(String key) throws ResourceException {
        
        final Value removing = internal.get(key);
        
        if(removing != null) {
            
            final int removingSize = this.sizeConverter.restore(removing);
            
            this.currentSize -= removingSize;
            
            internal.remove(key);
            
        }
        
    }

    @Override
    public void put(String key, Value value) throws ResourceException {

        if (this.exists(key)) {
        	this.remove(key);
        }
        
        final int sizeOfAdding = this.sizeConverter.restore(value);

        this.currentSize += sizeOfAdding;
                
        internal.put(key, value);
        
        final LinkedData topKey = (LinkedData) persistCache.get("topKey");

    	System.out.println("Current Size: " + currentSize + ", Max Size: " + maxSize + ", Putting " + key);
        
        if(topKey == null) {

        	System.out.println("topkey");

            final LinkedData myLinkedData = new LinkedData(key, null, null);
                    
            persistCache.put("bottomKey", myLinkedData);
            persistCache.put("topKey", myLinkedData);
            
        }
        else {
        	
        	System.out.println("here");
            
            final LinkedData myLinkedData = new LinkedData(key, topKey.getMyKey(), null);
            
            final LinkedData keyAfterMine = new LinkedData(topKey.getMyKey(), topKey.getKeyBefore(), myLinkedData.getMyKey());
            
            persistCache.put("linked:" + keyAfterMine.getMyKey(), keyAfterMine);
            persistCache.put("topKey", myLinkedData);
            
            final LinkedData bottom = (LinkedData) this.persistCache.get("bottomKey");
            
            if (bottom.getKeyAfter() == null) {
            	
            	System.out.println(bottom.getMyKey());
            
	            final LinkedData newBottom = new LinkedData(bottom.getMyKey(), null, key);
	            
	            persistCache.put("bottomKey", newBottom);
	            
            }
            
        }
        
        {
        	
            while(this.currentSize > this.maxSize) {
                 
                final LinkedData bottom = (LinkedData) this.persistCache.get("bottomKey");
                
                System.out.println(bottom.getMyKey() + "   " + internal.exists(bottom.getMyKey()));
                
                final LinkedData afterBottom = (LinkedData) this.persistCache.get("linked:" + bottom.getKeyAfter()); 
                
                final Value valueRemoving = internal.get(bottom.getMyKey());
                        
                if(valueRemoving != null) {
                   
                    final int size = this.sizeConverter.restore(valueRemoving);
                    
                    this.currentSize -= size;
                    
                    if(currentSize < 0) {
                        currentSize = 0;
                    }
                    
                    internal.remove(bottom.getMyKey());
                    
                }
                
                if (afterBottom != null) {
                	
                	final LinkedData newBottom = new LinkedData(afterBottom.getMyKey(), null, afterBottom.getKeyAfter());

                    persistCache.remove("linked:" + bottom.getKeyAfter());
                    
                    persistCache.put("bottomKey", newBottom);
                    
                }
                
            }
            
        }
        
    }
    
    private static class LinkedData implements Serializable {
        
        private static final long serialVersionUID = 3976267230111713664L;
        
        private final String keyBefore;
        private final String keyAfter;
        private final String myKey;

        public LinkedData(
            final String myKey,
            final String keyBefore,
            final String keyAfter
        ) {
            
            this.myKey = myKey;
            this.keyBefore = keyBefore;
            this.keyAfter = keyAfter;
            
        }
        
        public String getMyKey() {
            return myKey;
        }

        public String getKeyBefore() {
            return keyBefore;
        }

        public String getKeyAfter() {
            return keyAfter;
        }
        
    }

}
