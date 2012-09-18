package llc.ufwa.data.resource.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;

public class CachedResourceLoader<Key, Value> implements ResourceLoader<Key, Value> {
    
    private final Cache<Key, Value> valueCache;
    private final Cache<Key, Boolean> searchCache;
    private final ResourceLoader<Key, Value> internal;

    /**
     * 
     * @param internal
     * @param valueCache
     * @param searchCache
     */
    public CachedResourceLoader(
        final ResourceLoader<Key, Value> internal, 
        final Cache<Key, Value> valueCache,
        final Cache<Key, Boolean> searchCache
    ) {
        
        this.valueCache = valueCache;
        this.searchCache = searchCache;
        this.internal = internal;
        
    }

    @Override
    public boolean exists(Key key) throws ResourceException {
        
        if(key == null){
            throw new NullPointerException("key cannot be null");
        }
        
        final Boolean searchCacheValue = searchCache.get(key);
        
        final boolean returnVal;
        
        if(searchCacheValue != null) {
            returnVal = searchCacheValue;
        }
        else {
            
            final Value value = valueCache.get(key);
            
            if(value != null) {
                returnVal = true;
            }
            else {
                
                
                returnVal = internal.exists(key);
                
                searchCache.put(key, returnVal);
                
            }
            
        }
        
        return returnVal;
    }

    @Override
    public Value get(Key key) throws ResourceException {
        
        if(key == null){
            throw new NullPointerException("key cannot be null");
        }
        
        final Value cachedValue = valueCache.get(key);
        
        final Value returnVal;
        
        if(cachedValue != null) {
            returnVal = cachedValue;
        }
        else {
            
            final Boolean searchVal = searchCache.get(key);
            
            if(searchVal != null) {
                
                if(searchVal) {
                    returnVal = queryAndCacheValue(key);
                }
                else {
                    returnVal = null;
                }
            }
            else {
                returnVal =  queryAndCacheValue(key);
            }
            
        }
        
        return returnVal;
        
    }
    
    private Value queryAndCacheValue(final Key key) throws ResourceException {
        
        if(key == null){
            throw new NullPointerException("key cannot be null");
        }
        
        final Value returnVal = internal.get(key);
        
        if(returnVal == null) {
            searchCache.put(key, false);
        }
        else {
            
            searchCache.put(key, true);
            valueCache.put(key, returnVal);
            
        }
        
        return returnVal;
        
    }

    @Override
    public List<Value> getAll(List<Key> keys) throws ResourceException {
        
        if(keys == null) {
            throw new NullPointerException("Keys cannot be null");
        }
        
        if(keys.contains(null)) {
            throw new NullPointerException("Keys cannot contain null");
        }
        
        final Map<Key, Value> values = new HashMap<Key, Value>();
        final Set<Key> wasSearchFalse = new HashSet<Key>();
        final Set<Key> makeCallFor = new HashSet<Key>();
        
        for(final Key key : keys) {
            
            final Value cacheValue = valueCache.get(key);
            
            if(cacheValue != null) {
                values.put(key, cacheValue);
            }
            else {
                
                final Boolean searchValue = searchCache.get(key);
                
                if(searchValue == null) {
                    makeCallFor.add(key);
                }
                else {
                    
                    if(searchValue) {
                        makeCallFor.add(key);
                    }
                    else {
                        wasSearchFalse.add(key);
                    }
                    
                }
                
            }
            
        }
        
        if(makeCallFor.size() > 0) {
        
            final List<Key> toQuery = new ArrayList<Key>(makeCallFor);
            
            final List<Value> returned = internal.getAll(toQuery);
            
            if(toQuery.size() != returned.size()) {
                throw new ResourceException("queried values size not the same as keys size");
            }
            
            for(int i = 0; i < toQuery.size(); i++) {
                
                final Key key = toQuery.get(i);
                final Value value = returned.get(i);

                values.put(key, value);
                
            }
            
        }
        
        final List<Value> returnVals = new ArrayList<Value>(keys.size());
        
        for(final Key key : keys) {
            
            final Value value = values.get(key);
            
            if(value != null) {
                
                searchCache.put(key, true);
                valueCache.put(key, value);
                
            }
            else {
                searchCache.put(key, false);
            }
            
            returnVals.add(values.get(key));
        }
        
        return returnVals;
    }

}
