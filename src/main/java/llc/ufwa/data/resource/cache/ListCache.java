package llc.ufwa.data.resource.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import llc.ufwa.data.exception.ResourceException;

public class ListCache<Key, Value> implements Cache<Key, Value> {
    
    private final List<Cache<Key, Value>> caches;
    private final boolean pullThrough;

    public ListCache(final List<Cache<Key, Value>> caches, boolean pullThrough) {
        
        if(caches == null) {
            throw new NullPointerException("Caches cannot be null");
        }
        
        if(caches.contains(null)) {
            throw new NullPointerException("caches cannot contain null");
        }
        
        this.pullThrough = pullThrough;        
        this.caches = new ArrayList<Cache<Key, Value>>(caches);
        
    }

    @Override
    public boolean exists(Key key) throws ResourceException {
       
        if(key == null) {
            throw new NullPointerException("key cannot be null");
        }
        
        for(final Cache<Key, Value> cache : caches) {
            
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
            
            final Cache<Key, Value> cache = caches.get(i);
            final Value fromCache = cache.get(key); 
            
            if(fromCache != null) {
                
                returnVal = fromCache;
                break;
                
            }
        }
        
        //if pull through put it into the other caches it wasn't in.
        if(returnVal != null && pullThrough) {
            
            i--;
            
            for(; i >= 0; i--) {
                
                final Cache<Key, Value> cache = caches.get(i);
                
                cache.put(key, returnVal);
                
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
        
        final Map<Cache<Key, Value>, Set<Key>> foundIn = new HashMap<Cache<Key, Value>, Set<Key>>();
        
        int index = 0;
        
        for(; index < caches.size(); index++) {
            
            if(unfound.size() == 0) {
                break;
            }
            
            final Cache<Key, Value> cache = caches.get(index);
            
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
        
        if(pullThrough) {
            
            //Pull values through
            for(int i = 0; i < caches.size(); i++) {
                
                if(i == 0) {
                    continue;
                }
                
                final Cache<Key, Value> cache = caches.get(i);
                
                final Set<Key> foundInSet = foundIn.get(cache);
                
                if(foundInSet != null && foundInSet.size() > 0) {
                    
                    for(int j = i - 1; j >= 0; j--) {
                        
                        final Cache<Key, Value> pullingThrough = caches.get(j);
                        
                        for(final Key key : foundInSet) {
                            pullingThrough.put(key, values.get(key));
                        }
                        
                    }
                    
                }
                
            }
        
        }
        
        return returnVals;
        
    }

    @Override
    public void clear() {
        
        for(final Cache<Key, Value> cache : caches) {
            cache.clear();
        }
        
    }

    @Override
    public void remove(Key key) {
        
        if(key == null) {
            throw new NullPointerException("key cannot be null");
        }
        
        for(final Cache<Key, Value> cache : caches) {
            cache.remove(key);
        }
        
    }

    @Override
    public void put(Key key, Value value) {
        
        if(key == null) {
            throw new NullPointerException("key cannot be null");
        }
        
        for(final Cache<Key, Value> cache : caches) {
            cache.put(key, value);
        }
        
    }

}
