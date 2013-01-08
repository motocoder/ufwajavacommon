package llc.ufwa.data.resource.provider;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.MemoryCache;

/**
 * This PushProvider caches what is put through it.
 * 
 * If nothing exists in the cache it will use the notInCacheProvider if provided.
 * 
 * @author swagner
 *
 * @param <T>
 */
public class CachedPushProvider<T> implements PushProvider<T> {
 
    private final String KEY = String.valueOf(System.currentTimeMillis()) + this.hashCode();
    private final Cache<String, T> cache;
    private final ResourceProvider<T> notInCacheProvider;
    private final Cache<String, Boolean> searchCache = new MemoryCache<String, Boolean>();

    /**
     * 
     * @param cache
     */
    public CachedPushProvider(final Cache<String, T> cache) {
        
        this.cache = cache;
        this.notInCacheProvider = null;
                
    }
    /**
     * 
     * @param cache
     * @param notInCacheProvider
     */
    public CachedPushProvider(
        final Cache<String, T> cache,
        final ResourceProvider<T> notInCacheProvider
    ) {
        
        this.cache = cache;
        this.notInCacheProvider = notInCacheProvider;
        
    }
    
    @Override
    public boolean exists() throws ResourceException {  
        
        final boolean returnVal;
        
        if(cache.exists(KEY)) {
            returnVal = true;            
        }
        else if(searchCache.exists(KEY)) {
            returnVal = searchCache.get(KEY);
        }
        else if(notInCacheProvider != null) {
            
            returnVal = notInCacheProvider.exists();
            searchCache.put(KEY, returnVal);
            
        }
        else {
            returnVal = false;
        }
      
        return returnVal;
        
    }

    @Override
    public T provide() throws ResourceException {
        
        final T returnVal;
        
        final T temp = cache.get(KEY);
                
        if(temp != null) {
            returnVal = temp;            
        }
        else if(searchCache.exists(KEY) && !searchCache.get(KEY)) {
            returnVal = null;
        }
        else if(notInCacheProvider != null) {
            
            returnVal = notInCacheProvider.provide();
            
            cache.put(KEY, returnVal);
            searchCache.put(KEY, true);
            
        }
        else {
            
            returnVal = null;
            searchCache.put(KEY, false);
            
        }
      
        return returnVal;
        
    }

    @Override
    public void push(T value) throws ResourceException {
        
        cache.put(KEY, value);
        searchCache.put(KEY, true);
        
    }
    
};
