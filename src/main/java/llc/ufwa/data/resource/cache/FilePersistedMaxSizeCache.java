package llc.ufwa.data.resource.cache;

import java.util.List;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;

public class FilePersistedMaxSizeCache<Value> implements Cache<String, Value>{

    private final long maxSize;
    private final Cache<String, Value> internal;

    public FilePersistedMaxSizeCache(
        final Cache<String, Value> internal,
        final Converter<Integer, Value> sizeConverter,
        final long maxSize
    ) {
        
        this.maxSize = maxSize;
        this.internal = internal;
    }
    
    @Override
    public boolean exists(String key) throws ResourceException {
        return internal.exists(key);
    }

    @Override
    public Value get(String key) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Value> getAll(List<String> keys) throws ResourceException {
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
    public void put(String key, Value value) {
        // TODO Auto-generated method stub
        
    }

}
