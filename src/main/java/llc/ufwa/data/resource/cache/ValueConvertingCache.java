package llc.ufwa.data.resource.cache;

import java.util.ArrayList;
import java.util.List;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.loader.ResourceLoader;

public class ValueConvertingCache<Key, Value, OldValue> implements Cache<Key, Value> {
    
    private final Cache<Key, OldValue> internal;
    private final Converter<Value, OldValue> converter;

    /**
     * 
     * @param internal
     */
    public ValueConvertingCache(
        final Cache<Key, OldValue> internal,
        final Converter<Value, OldValue> converter
    ) {
        
        if(converter == null) {
            throw new NullPointerException("<ValueConvertingCache><1>, converter must not be null");
        }
        
        if(internal == null) {
            throw new NullPointerException("<ValueConvertingCache><2>, Internal must not be null");
        }
                
        this.converter = converter;
        this.internal = internal;
        
    }
    
    public ValueConvertingCache(
        final ResourceLoader<Key, OldValue> internal,
        final Converter<Value, OldValue> converter
    ) {
        
        if(internal == null) {
            throw new NullPointerException("<ValueConvertingCache><3>, Internal must not be null");
        }
        
        if(converter == null) {
            throw new NullPointerException("<ValueConvertingCache><4>, converter must not be null");
        }
        
        this.converter = converter;
        this.internal = new ResourceLoaderCache<Key, OldValue>(internal);
        
    }
    
    @Override
    public boolean exists(Key key) throws ResourceException {
        return internal.exists(key);
    }

    @Override
    public Value get(Key key) throws ResourceException {
        
        final OldValue bytes = internal.get(key);
        
        if(bytes == null) {
            return null;
        }
        
        return converter.restore(bytes);
        
    }

    @Override
    public List<Value> getAll(List<Key> keys) throws ResourceException {
        
        final List<OldValue> all = internal.getAll(keys);
        final List<Value> returnVals = new ArrayList<Value>();
        
        for(OldValue gotten : all) {
            
            if(gotten == null) {
                returnVals.add(null);
            }
            else {
                returnVals.add(converter.restore(gotten));
            }
            
        }
        
        return returnVals;
        
    }

    @Override
    public void clear() throws ResourceException {
        internal.clear();
    }

    @Override
    public void remove(Key key) throws ResourceException {
        internal.remove(key);
    }

    @Override
    public void put(Key key, Value value) {
        try {
            internal.put(key, converter.convert(value));
        } 
        catch (ResourceException e) {
            throw new RuntimeException("<ValueConvertingCache><5>, " + e);
        }
    }
    
}