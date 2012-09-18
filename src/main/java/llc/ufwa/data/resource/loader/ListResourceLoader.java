package llc.ufwa.data.resource.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import llc.ufwa.data.exception.ResourceException;

public class ListResourceLoader<Key, Value> implements ResourceLoader<Key, Value> {
    
    private final List<ResourceLoader<Key, Value>> caches;

    public ListResourceLoader(final List<ResourceLoader<Key, Value>> caches) {
        
        if(caches == null) {
            throw new NullPointerException("Caches cannot be null");
        }
        
        if(caches.contains(null)) {
            throw new NullPointerException("caches cannot contain null");
        }      
        this.caches = new ArrayList<ResourceLoader<Key, Value>>(caches);
        
    }

    @Override
    public boolean exists(Key key) throws ResourceException {
       
        if(key == null) {
            throw new NullPointerException("key cannot be null");
        }
        
        for(final ResourceLoader<Key, Value> cache : caches) {
            
            if(cache.exists(key)) {
                return true;
            }
            
        }
        
        return false;
    }

    @Override
    public Value get(Key key) throws ResourceException {
        
        if(key == null) {
            throw new NullPointerException("key cannot be null");
        }
        int i = 0;
        
        final int size = caches.size();
        
        Value returnVal = null;
        
        //find the value
        
        for(; i < size; i++) {
            
            final ResourceLoader<Key, Value> cache = caches.get(i);
            final Value fromCache = cache.get(key); 
            
            if(fromCache != null) {
                
                returnVal = fromCache;
                break;
                
            }
        }
                
        return returnVal;
    }

    @Override
    public List<Value> getAll(List<Key> keys) throws ResourceException {
        
        if(keys == null) {
            throw new NullPointerException("keys cannot be null");
        }
        
        if(keys.contains(null)) {
            throw new NullPointerException("Keys cannot contain null");
        }
        
        final Set<Key> unfound = new HashSet<Key>(keys);
        final Map<Key, Value> values = new HashMap<Key, Value>();
        
        final Map<ResourceLoader<Key, Value>, Set<Key>> foundIn = new HashMap<ResourceLoader<Key, Value>, Set<Key>>();
        
        int index = 0;
        
        for(; index < caches.size(); index++) {
            
            if(unfound.size() == 0) {
                break;
            }
            
            final ResourceLoader<Key, Value> cache = caches.get(index);
            
            final List<Key> querying = new ArrayList<Key>(unfound);
            final List<Value> queried = cache.getAll(querying);
            
            if(querying.size() != queried.size()) {
                throw new ResourceException("something wierd just happened");
            }
            
            final int size = querying.size();
            
            final Set<Key> foundInSet = new HashSet<Key>();
            
            for(int i = 0; i < size; i++) {
                
                final Key key = querying.get(i);
                final Value value = queried.get(i);
                
                if(value != null) {
                    values.put(key, value);
                    unfound.remove(key);
                    foundInSet.add(key);
                    
                }
                
            }
            
            foundIn.put(cache, foundInSet);
            
        }
        
        final List<Value> returnVals = new ArrayList<Value>();
        
        for(final Key key : keys) {
            returnVals.add(values.get(key));
        }
        
        return returnVals;
        
    }


}
