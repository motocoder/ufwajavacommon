package llc.ufwa.data.resource.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.LimitingExecutorService;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.SynchronizedCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author seanwagner
 *
 * @param <Key>
 * @param <Value>
 */
public class CachedParallelResourceLoader<Key, Value> implements ParallelResourceLoader<Key, Value> {
    
    private static final Logger logger = LoggerFactory.getLogger(CachedParallelResourceLoader.class);
    
    private final String loggingTag;
    private final Executor threads;
    private final ParallelResourceLoaderImpl<Key, Value> internal;
    private final Cache<Key, Boolean> searchCache;
    private final Cache<Key, Value> cache;

    private Executor getAllRunner;
    
    /**
     * 
     * @param threads
     * @param depth
     * @param uniqueCallLimit
     * @param loggingTag
     * @param pairs
     */
    public CachedParallelResourceLoader(
        final LimitingExecutorService threads,
        final ExecutorService callbackThreads,
        final Executor getAllRunner,
        final int depth,
        final int uniqueCallLimit,
        final String loggingTag,
        final List<CacheLoaderPair<Key, Value>> pairs
    ) {
       
        if(threads == null) {
            throw new NullPointerException(loggingTag + ": Threads cannot be null");
        }
        
        if(loggingTag == null) {
            this.loggingTag = "";
        }
        else {
            this.loggingTag = loggingTag;     
        }
       
        final List<CacheLoaderPair<Key, Value>> pairs2 = new ArrayList<CacheLoaderPair<Key, Value>>();
        
        for(final CacheLoaderPair<Key, Value> pair : pairs) {
            
            Cache<Key, Value> cache = pair.getCache();
            Cache<Key, Boolean> searchCache = pair.getSearchCache();
            
            if(!(cache instanceof SynchronizedCache)) {
                cache = new SynchronizedCache<Key, Value>(cache);
            }

            if(!(searchCache instanceof SynchronizedCache)) {
                searchCache = new SynchronizedCache<Key, Boolean>(searchCache);
            }
            
            pairs2.add(new CacheLoaderPair<Key, Value>(pair.getLoader(), cache, searchCache));
            
        }
        
        final ResourceLoader<Key, Value> wrapper = new ResourceLoader<Key, Value>() {

            @Override
            public boolean exists(Key key) throws ResourceException {
                
                boolean returnVal = false;
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    
                    final Boolean exists = pair.getSearchCache().get(key);
                    
                    if(exists != null && exists) {
                        
                        returnVal = true;
                        break;
                        
                    }
                    
                    if(exists == null && pair.getCache().exists(key)) {
                        
                        pair.getSearchCache().put(key, true);
                        returnVal = true;
                        break;
                        
                    }
                    
                }
                
                if(!returnVal) {
                    
                    for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                        
                        final boolean exists = pair.getLoader().exists(key);
                        
                        pair.getSearchCache().put(key, exists);
                        
                        if(exists) {
                            
                            returnVal = true;
                            break;
                            
                        }
                        
                    }
                    
                }
                
                return returnVal;
               
            }

            @Override
            public Value get(Key key) throws ResourceException {
     
                Value returnVal = null;
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    
                    final Boolean exists = pair.getSearchCache().get(key);
                    
                    if(exists != null) {
                        
                        if(exists) {
                        
                            final Value cacheValue = pair.getCache().get(key);
                            
                            if(cacheValue != null) {
                                
                                returnVal = cacheValue;
                                break;
                                
                            }
                            
                        }
                        
                    }
                    else {
                        
                        final Value cacheValue = pair.getCache().get(key);
                        
                        if(cacheValue != null) {
                            
                            returnVal = cacheValue;
                            break;
                            
                        }
                        
                    }
                    
                }
                
                if(returnVal == null) {
                    
                    for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                        
                        final Value value = pair.getLoader().get(key);
                        
                        pair.getSearchCache().put(key, value != null);
                        
                        if(value != null) {
                            
                            pair.getCache().put(key, value);
                            
                            returnVal = value;
                            break;
                            
                        }
                        
                    }
                    
                }
                
                return returnVal;
            }

            @Override
            public List<Value> getAll(List<Key> keys) throws ResourceException {
              
                final Set<Key> uniques = new HashSet<Key>(keys);
                final Map<Key, Value> valueMap = new HashMap<Key, Value>();
                final Set<Key> toGet = new HashSet<Key>();
                
                for(final Key key : uniques) {
                    
                    Value returnVal = null;
                    
                    for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                        
                        final Boolean exists = pair.getSearchCache().get(key);
                        
                        if(exists != null) {
                            
                            if(exists) {
                            
                                final Value cacheValue = pair.getCache().get(key);
                                
                                if(cacheValue != null) {
                                    
                                    returnVal = cacheValue;
                                    break;
                                    
                                }
                                
                            }
  
                            
                        }
                        else {
                            
                            final Value cacheValue = pair.getCache().get(key);
                            
                            if(cacheValue != null) {
                                
                                returnVal = cacheValue;
                                break;
                                
                            }
                            
                        }
                        
                    }
                    
                    if(returnVal != null) {
                        valueMap.put(key, returnVal);
                    }
                    else {
                        toGet.add(key);
                    }
                    
                }
                                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    
                    final List<Key> getting = new ArrayList<Key>(toGet);
                    
                    final List<Value> got = pair.getLoader().getAll(getting);
                    
                    if(got.size() != getting.size()) {
                        throw new RuntimeException("loader must always return same amount as keys");
                    }
                    
                    for(int i = 0; i < got.size(); i++) {
                        
                        final Key key = getting.get(i);
                        final Value value = got.get(i);
                        
                        if(value != null) {
                            
                            valueMap.put(key, value);
                            toGet.remove(key);
                            pair.getCache().put(key, value);
                            pair.getSearchCache().put(key, true);
                            
                        }
                        else {
                            pair.getSearchCache().put(key, false);
                        }
                    }
                    
                }
                
                final List<Value> returnVals = new ArrayList<Value>();
                
                for(final Key key : uniques) {
                    returnVals.add(valueMap.get(key));
                }
                
                return returnVals;
            }
        };
        
        this.searchCache = new Cache<Key, Boolean>() {

            @Override
            public boolean exists(Key key) throws ResourceException {
                
                boolean returnVal = false;
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    
                    if(pair.getSearchCache().exists(key)) {
                        
                        returnVal = true;
                        break;
                        
                    }
                    
                }
                
                return returnVal;
            }

            @Override
            public Boolean get(Key key) throws ResourceException {

                Boolean returnVal = null;
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    
                    Boolean value = pair.getSearchCache().get(key);
                    
                    if(value != null && value) {
                        
                        returnVal = value;
                        break;
                        
                    }
                    
                }
                
                return returnVal;
                
            }

            @Override
            public List<Boolean> getAll(List<Key> keys) throws ResourceException {
                
                final List<Boolean> returnVals = new ArrayList<Boolean>();
                
                for(Key key : keys) {
                    returnVals.add(get(key));
                }
                
                return returnVals;
                
            }

            @Override
            public void clear() {
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    pair.getSearchCache().clear();
                }
                
            }

            @Override
            public void remove(Key key) {

                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    pair.getSearchCache().remove(key);
                }                
                
            }

            @Override
            public void put(Key key, Boolean value) {
                //do not implement this we don't want to cache different loaders values
            }
            
        };
        
        this.cache = new Cache<Key, Value>() {

            @Override
            public boolean exists(Key key) throws ResourceException {

                boolean returnVal = false;
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    
                    if(pair.getCache().exists(key)) {
                        
                        returnVal = true;
                        break;
                        
                    }
                    
                }
                
                return returnVal;
                
            }

            @Override
            public Value get(Key key) throws ResourceException {

                Value returnVal = null;
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    
                    Value value = pair.getCache().get(key);
                    
                    if(value != null) {
                        
                        returnVal = value;
                        break;
                        
                    }
                    
                }
                
                return returnVal;
                
            }

            @Override
            public List<Value> getAll(List<Key> keys) throws ResourceException {
                
                final List<Value> returnVals = new ArrayList<Value>();
                
                for(Key key : keys) {
                    returnVals.add(get(key));
                }
                
                return returnVals;
                
            }

            @Override
            public void clear() {
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    pair.getCache().clear();
                }
                
            }

            @Override
            public void remove(Key key) {
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    pair.getCache().remove(key);
                } 
                
            }

            @Override
            public void put(Key key, Value value) {
              //do not implement this we don't want to cache different loaders values
            }
            
        };
        
        this.threads = threads;
        this.getAllRunner = getAllRunner;
        this.internal = new ParallelResourceLoaderImpl<Key, Value>(
            wrapper,
            threads,
            callbackThreads,
            depth,
            loggingTag
        );
        
    }
    /**
     * 
     * @param internal
     * @param threads
     * @param callbackThreads
     * @param getAllRunner
     * @param depth
     * @param loggingTag
     * @param cache
     * @param searchCache
     */
    public CachedParallelResourceLoader(
        final ResourceLoader<Key, Value> internal,
        final LimitingExecutorService threads,
        final ExecutorService callbackThreads,
        final ExecutorService getAllRunner,
        final int depth,
        final String loggingTag,
        Cache<Key, Value> cache,
        Cache<Key, Boolean> searchCache
    ) {
        
        if(internal == null) {
            throw new NullPointerException(loggingTag + ": Internal loader cannot be null");
        }
        
        if(threads == null) {
            throw new NullPointerException(loggingTag + ": Threads cannot be null");
        }
        
        if(cache == null) {
            throw new NullPointerException(cache + ": cache cannot be null");
        }
        
        if(searchCache == null) {
            throw new NullPointerException(searchCache + ": searchCache cannot be null");
        }
        
        if(loggingTag == null) {
            this.loggingTag = "";
        }
        else {
            this.loggingTag = loggingTag;     
        }
        
        if(!(cache instanceof SynchronizedCache)) {
            cache = new SynchronizedCache<Key, Value>(cache);
        }
         
        this.cache = cache;
        
        if(!(searchCache instanceof SynchronizedCache)) {
            searchCache = new SynchronizedCache<Key, Boolean>(searchCache);
        }
        
        this.searchCache = searchCache;
        this.getAllRunner = getAllRunner;
        this.threads = threads;
        
        this.internal = 
            new ParallelResourceLoaderImpl<Key, Value>(
                internal,
                threads,
                callbackThreads,
                depth,
                loggingTag
            );
        
    }

    @Override
    public boolean exists(Key key) throws ResourceException {
        
        if(key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        
        final boolean returnVal;
        final Boolean searchValue = searchCache.get(key);
        
        //If doesn't exist in cache check internal
        if(searchValue == null) {
            
            returnVal = internal.exists(key);
            searchCache.put(key, returnVal);
            
        }
        else { //use cached value
            returnVal = searchValue;
        }
        
        return returnVal;
    }

    @Override
    public Value get(Key key) throws ResourceException {
        
        if(key == null) {
            throw new NullPointerException(loggingTag + ": key cannot be null");
        }
        
        final Boolean searchValue = searchCache.get(key);
        
        final Value returnVal;
        
        if(searchValue == null || searchValue) {
        
            final Value value = cache.get(key);
                            
            if(value != null) {
                returnVal = value;
            }
            else {
                
                returnVal = internal.get(key);
                
                searchCache.put(key, returnVal != null);
                
                if(returnVal != null) {
                    cache.put(key, returnVal);
                }
                
            }
            
        }
        else {
            //search cache had false.
            returnVal = null;
        }
        
        return returnVal;
        
    }

    @Override
    public List<Value> getAll(List<Key> keys) throws ResourceException {

        if(keys == null) {
            throw new NullPointerException(loggingTag + ": key cannot be null");
        }
        
        if(keys.contains(null)) {
            throw new NullPointerException(loggingTag + ": key cannot be null");
        }
        
        final Map<Key, Value> returnVals = new HashMap<Key, Value>();
        final Set<Key> needToGet = new HashSet<Key>();
        
        for(final Key key : keys) {
        
            final Boolean searchValue = searchCache.get(key);
            
            final Value returnVal; 
            
            if(searchValue == null || searchValue) {
            
                final Value value = cache.get(key);
                                
                if(value != null) {
                    returnVal = value;
                }
                else {
                    
                    needToGet.add(key);
                    returnVal = null;
                    
                }
                
            }
            else {
                //search cache had false.
                returnVal = null;
            }
            
            returnVals.put(key, returnVal);
            
        }
        
        final List<Key> gettingKeys = new ArrayList<Key>(needToGet);
        
        final List<Value> gottenValues = internal.getAll(gettingKeys);
        
        for(int i = 0; i < gottenValues.size(); i++) {
            
            final Key key = gettingKeys.get(i);
            final Value value = gottenValues.get(i);
            
            returnVals.put(key, value);
            
        }
        
        final List<Value> finalReturn = new ArrayList<Value>();
        
        for(final Key key : keys) {
            finalReturn.add(returnVals.get(key));
        }
        
        return finalReturn;
        
        
    }

    @Override
    public void existsParallel(
        final Callback<Object, ResourceEvent<Boolean>> onComplete, 
        final Key key
    ) {
        
        if(onComplete == null) {
            throw new NullPointerException(loggingTag + ": onComplete must not be null");
        }
        
        if(key == null) {
            throw new NullPointerException(loggingTag + ": key must not be null");
        }
        
        try {
            
            final Boolean searchValue = searchCache.get(key);
            
            if(searchValue != null) {
                                
                final ResourceEvent<Boolean> completed = new ResourceEvent<Boolean>(searchValue, null, ResourceEvent.CACHED);
                onComplete.call(null, completed);
                    
            }
            else {
                
                final Value val = cache.get(key);
                
                if(val != null) {
                    
                    final ResourceEvent<Boolean> completed = new ResourceEvent<Boolean>(true, null, ResourceEvent.CACHED);
                    onComplete.call(null, completed);
                    
                }
                else {
                
                    internal.existsParallel(
                            
                        new Callback<Object, ResourceEvent<Boolean>>() {
        
                            @Override
                            public boolean call(
                                final Object source,
                                final ResourceEvent<Boolean> value
                            ) {
                                
                            	if(value.getThrowable() == null) {
                            		searchCache.put(key, value.getVal() != null && value.getVal());
                            	}
                                
                                onComplete.call(null, value);
                                
                                return false;
                                
                            }
                            
                        },
                        key
                    );
                
                }
                
            }
        }
        catch(ResourceException e) {
            
            final ResourceEvent<Boolean> completed = new ResourceEvent<Boolean>(null, e, ResourceEvent.UNKNOWN);
            onComplete.call(null, completed);
            
        }
        
    }
    
    @Override
    public void getAllParallel(//TODO a real implementation that doesn't just wrap getAll
        final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap
     ) throws ResourceException {
        
    	final Set<Key> keys = new HashSet<Key>(callbackMap.keySet());
    	final Set<Key> needToCall = new HashSet<Key>();
    	
    	for(Key key : keys) {
    		
    		final Value value = cache.get(key);
    		final Boolean searchValue = searchCache.get(key);
    		
    		if(value != null) {
    			
    			final ResourceEvent<Value> event = new ResourceEvent<Value>(value, null, ResourceEvent.CACHED);
    			 
    			callbackMap.get(key).call(null, event);
    			
    		}
    		else {
    			
    			if(searchValue == null || searchValue) {
    				needToCall.add(key);
    			}
    			else {
    				
    				final ResourceEvent<Value> event = new ResourceEvent<Value>(null, null, ResourceEvent.CACHED);
        			
        			callbackMap.get(key).call(null, event);
        			
    			}
    			
    		}
    		
    	}
    	
    	if(needToCall.size() == 1) {
    		
    		final Key key = needToCall.iterator().next(); 
    		
    		internal.getParallel(callbackMap.get(key), key);
    		
    	}  	
    	else if(needToCall.size() > 1) {
    		
    		final Map<Key, Callback<Object, ResourceEvent<Value>>> newCallbackMap = new HashMap<Key, Callback<Object, ResourceEvent<Value>>>();
    		
    		for(final Key key : needToCall) {
    			newCallbackMap.put(key, callbackMap.get(key));
    		}
    		
    		internal.getAllParallel(newCallbackMap);
    		
    	}
    	
//        getAllRunner.execute(
//            new Runnable() { 
//
//                @Override
//                public void run() {
//                    
//                    final List<Key> keys = new ArrayList<Key>(callbackMap.keySet());
//                    final Map<Key, ResourceEvent<Value>> events = new HashMap<Key, ResourceEvent<Value>>();
//                    
//                    try {
//                        
//                        final List<Value> values = getAll(keys);
//                        
//                        if(values.size() != keys.size()) {
//                            throw new ResourceException("Getall returned invalid number or arguments");
//                        }
//                        
//                        for(int i = 0; i < keys.size(); i++) {
//                            
//                            final Key key = keys.get(i);
//                            final Value value = values.get(i);
//                            
//                            final ResourceEvent<Value> event = new ResourceEvent<Value>(value, null);
//                            
//                            events.put(key, event);
//                            
//                        }
//                        
//                    }
//                    catch (ResourceException e) {
//                        
//                        for(int i = 0; i < keys.size(); i++) {
//                            
//                            final Key key = keys.get(i);
//                            
//                            final ResourceEvent<Value> event = new ResourceEvent<Value>(null, new ResourceException("Getall failed"));
//                            
//                            events.put(key, event);
//                            
//                        }
//                        
//                    }
//                    finally {
//                    
//                        for(final Map.Entry<Key, ResourceEvent<Value>> entry : events.entrySet()) {
//                            
//                            threads.execute(
//                                new Runnable() {
//    
//                                    @Override
//                                    public void run() {
//                                        
//                                        final Callback<Object, ResourceEvent<Value>> callback = callbackMap.get(entry.getKey());
//                                        callback.call(null, entry.getValue());
//                                        
//                                    }
//                                    
//                                }
//                                
//                            );
//                            
//                        }
//                        
//                    }
//                    
//                }
//                
//            }
//            
//        );
        
    }
    
    public static class CacheLoaderPair<Key, Value> {
        
        private final ResourceLoader<Key, Value> loader;
        private final Cache<Key, Value> cache;
        private final Cache<Key, Boolean> searchCache;

        public CacheLoaderPair(
            final ResourceLoader<Key, Value> loader,
            final Cache<Key, Value> cache,
            final Cache<Key, Boolean> searchCache
        ) {
            
            this.loader = loader;
            this.cache = cache;
            this.searchCache = searchCache;
            
        }

        public ResourceLoader<Key, Value> getLoader() {
            return loader;
        }

        public Cache<Key, Value> getCache() {
            return cache;
        }

        public Cache<Key, Boolean> getSearchCache() {
            return searchCache;
        }
        
        
        
    }

    @Override
    public void getParallel(
        final Callback<Object, ResourceEvent<Value>> onComplete,
        final Key key
    ) {
    	
        if(onComplete == null) {
            throw new NullPointerException(loggingTag + ": onComplete must not be null");
        }
        
        if(key == null) {
            throw new NullPointerException(loggingTag + ": key must not be null");
        }
            
        try {
        final Boolean searchValue = searchCache.get(key);
        
        if(searchValue == null || searchValue) {
            
            final Value value = cache.get(key);
            
            if(value != null) {
                                
                final ResourceEvent<Value> completed = new ResourceEvent<Value>(value, null, ResourceEvent.CACHED);
                
                onComplete.call(null, completed);

            }
            else {
                
                internal.getParallel(
                        
                    new Callback<Object, ResourceEvent<Value>>() {

                        @Override
                        public boolean call(
                            final Object source,
                            final ResourceEvent<Value> value
                        ) {
                            
                        	if(value.getThrowable() == null) {
                        		searchCache.put(key, value.getVal() != null);
                        	}
                            
                            if(value.getVal() != null) {
                                cache.put(key, value.getVal());
                            }
                            
                            onComplete.call(null, value);
                            
                            return false;
                            
                        }
                        
                    },
                    key
                );
                
            }
            
        }
        else {
            
            final ResourceEvent<Value> completed = new ResourceEvent<Value>(null, null, ResourceEvent.CACHED);
            
            //search cache had false.
            onComplete.call(null, completed);
            
        }    
                
        }
        catch(final ResourceException e) {
            
            logger.warn("exception thrown:", e);
            
            final ResourceEvent<Value> completed = new ResourceEvent<Value>(null, e, ResourceEvent.UNKNOWN);

            onComplete.call(null, completed);

        }                
        
    }
    public Cache<Key, Boolean> getSearchCache() {
        return this.searchCache;
    }
    
    public Cache<Key, Value> getCache() {
        return this.cache;
    }

}
