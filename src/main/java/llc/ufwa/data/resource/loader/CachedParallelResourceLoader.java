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
 * 
 */


/**
 *  
 *  The CachedParallelResourceLoader interface extends the ParallelResourceLoader
 *  interface and adds caching functionality. The constructor takes a LimitingExecutorService
 *  object, an ExecutorService object, an Executor, an int, a String, and a List of 
 *  CacheLoaderPairs.
 *  
 * 
 *
 */

 
public class CachedParallelResourceLoader<Key, Value> implements ParallelResourceLoader<Key, Value> {
    
    private static final Logger logger = LoggerFactory.getLogger(CachedParallelResourceLoader.class);
    
    private final String loggingTag;
    private final ParallelResourceLoaderImpl<Key, Value> internal;
    private final Cache<Key, Boolean> searchCache;
    private final Cache<Key, Value> cache;
    
    /**
     * 
     * @param threads
     * @param callbackThreads
     * @param getAllRunner
     * @param depth
     * @param loggingTag
     * @param pairs
     */
    public CachedParallelResourceLoader(
        final LimitingExecutorService threads,
        final ExecutorService callbackThreads,
        final Executor getAllRunner,
        final int depth,
        final String loggingTag,
        final List<CacheLoaderPair<Key, Value>> pairs
    ) {
       
        if(threads == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><1>, " + loggingTag + ": Threads cannot be null");
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
            
            /**
             * This method returns a boolean if the value assigned to the
             * specified key parameter is not null as well as storing
             * the results in a cache.
             * 
             * @return boolean
             * 
             */

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
            
            /**
             * This method returns the Value assigned to the
             * specified key parameter as long as the value
             * is not null. It also stores the results in 
             * a cache.
             * 
             * @return Value
             * 
             */

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
            
            /**
             * This method returns a List of Values assigned to the
             * specified keys parameter as well as storing them
             * in a cache.
             * 
             * @return List<Value>
             * 
             */

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
                        throw new RuntimeException("<CachedParallelResourceLoader><2>, " + "loader must always return same amount as keys");
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
            
            /**
             * 
             * 
             * @return boolean
             * 
             */

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
            
            /**
             * 
             * 
             * @return Boolean
             * 
             */

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
            
            /**
             * 
             * 
             * @return List<Boolean>
             * 
             */

            @Override
            public List<Boolean> getAll(List<Key> keys) throws ResourceException {
                
                final List<Boolean> returnVals = new ArrayList<Boolean>();
                
                for(Key key : keys) {
                    returnVals.add(get(key));
                }
                
                return returnVals;
                
            }
            
            /**
             * @throws ResourceException 
             * 
             * 
             */

            @Override
            public void clear() throws ResourceException {
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    pair.getSearchCache().clear();
                }
                
            }
            
            /**
             * 
             * 
             * @param key
             */

            @Override
            public void remove(Key key) throws ResourceException {

                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    pair.getSearchCache().remove(key);
                }                
                
            }
            
            /**
             * 
             * 
             * @param key
             * @param value
             */

            @Override
            public void put(Key key, Boolean value) {
                //do not implement this we don't want to cache different loaders values
            }
            
        };
        
        this.cache = new Cache<Key, Value>() {

            /**
             * 
             * 
             * @return boolean
             * 
             */
            
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
            
            /**
             * 
             * 
             * @return Value
             * 
             */

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

            /**
             * 
             * 
             * @return List<Value>
             * 
             */
            
            @Override
            public List<Value> getAll(List<Key> keys) throws ResourceException {
                
                final List<Value> returnVals = new ArrayList<Value>();
                
                for(Key key : keys) {
                    returnVals.add(get(key));
                }
                
                return returnVals;
                
            }
            
            /**
             * 
             * 
             */

            @Override
            public void clear() throws ResourceException {
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    pair.getCache().clear();
                }
                
            }
            
            /**
             * 
             * 
             * @param key
             * 
             */

            @Override
            public void remove(Key key) throws ResourceException {
                
                for(final CacheLoaderPair<Key, Value> pair : pairs2) {
                    pair.getCache().remove(key);
                } 
                
            }
            
            /**
             * 
             * 
             * @param key
             * @param value
             * 
             */

            @Override
            public void put(Key key, Value value) {
              //do not implement this we don't want to cache different loaders values
            }
            
        };
        
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
            throw new NullPointerException("<CachedParallelResourceLoader><3>, " + loggingTag + ": Internal loader cannot be null");
        }
        
        if(threads == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><4>, " + loggingTag + ": Threads cannot be null");
        }
        
        if(cache == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><5>, " + cache + ": cache cannot be null");
        }
        
        if(searchCache == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><6>, " + searchCache + ": searchCache cannot be null");
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
        
        this.internal = 
            new ParallelResourceLoaderImpl<Key, Value>(
                internal,
                threads,
                callbackThreads,
                depth,
                loggingTag
            );
        
    }

    /**
     * 
     * 
     * @return boolean
     * 
     */
    
    @Override
    public boolean exists(Key key) throws ResourceException {
        
        if(key == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><7>, " + "Key cannot be null");
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
    
    /**
     * 
     * 
     * @return Value
     * 
     */

    @Override
    public Value get(Key key) throws ResourceException {
        
        if(key == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><8>, " + loggingTag + ": key cannot be null");
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
    
    /**
     * 
     * 
     * @return List<Value>
     * 
     */

    @Override
    public List<Value> getAll(List<Key> keys) throws ResourceException {

        if(keys == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><9>, " + loggingTag + ": key cannot be null");
        }
        
        if(keys.contains(null)) {
            throw new NullPointerException("<CachedParallelResourceLoader><10>, " + loggingTag + ": key cannot be null");
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
    
    /**
     * CallbackControl existsParallel(
     *  Callback<Object, ResourceEvent<Boolean>> onComplete, 
     *  final Key key) – onComplete is called with the newly loaded value upon completion of 
     *  the resource loader call. CallbackControl has one method, cancel which allows you to 
     *  cancel the request before it completes.
     * 
     * @return CallbackControl
     * 
     */

    @Override
    public CallbackControl existsParallel(
        final Callback<Object, ResourceEvent<Boolean>> onComplete, 
        final Key key
    ) {
        
        CallbackControl returnVal;
        
        if(onComplete == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><11>, " + loggingTag + ": onComplete must not be null");
        }
        
        if(key == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><12>, " + loggingTag + ": key must not be null");
        }
        
        try {
            
            final Boolean searchValue = searchCache.get(key);
            
            if(searchValue != null) {
                                
                final ResourceEvent<Boolean> completed = new ResourceEvent<Boolean>(searchValue, null, ResourceEvent.CACHED);
                onComplete.call(completed);
                
                returnVal = 
                    new CallbackControl() {

                    @Override
                    public void cancel() {
                        //was cached nothing to cancel
                    }
                };
                    
            }
            else {
                
                final Value val = cache.get(key);
                
                if(val != null) {
                    
                    final ResourceEvent<Boolean> completed = new ResourceEvent<Boolean>(true, null, ResourceEvent.CACHED);
                    onComplete.call(completed);
                    
                    returnVal = 
                        new CallbackControl() {

                        @Override
                        public void cancel() {
                            //was cached nothing to cancel
                        }
                    };
                    
                }
                else {
                
                    returnVal = internal.existsParallel(
                            
                        new Callback<Object, ResourceEvent<Boolean>>() {
        
                            @Override
                            public Object call(
                                final ResourceEvent<Boolean> value
                            ) {
                                
                            	if(value.getThrowable() == null) {
                            		try {
                                        searchCache.put(key, value.getVal() != null && value.getVal());
                                    }
                            		catch (ResourceException e) {
                                        logger.error("ERROR:", e);
                                    }
                            	}
                                
                                onComplete.call(value);
                                
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
            onComplete.call(completed);
            
            returnVal = 
                new CallbackControl() {

                @Override
                public void cancel() {
                    //was cached nothing to cancel
                }
            };
            
        }
        
        return returnVal;
        
    }
    
    /**
     * void getAllParallel(final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap) - 
     *  This method works with the same principals as getParallel and existsParallel except it is 
     *  a bulk request. There is no CallbackControl returned with it.
     * 
     * 
     */
    
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
    			 
    			callbackMap.get(key).call(event);
    			
    		}
    		else {
    			
    			if(searchValue == null || searchValue) {
    				needToCall.add(key);
    			}
    			else {
    				
    				final ResourceEvent<Value> event = new ResourceEvent<Value>(null, null, ResourceEvent.CACHED);
        			
        			callbackMap.get(key).call(event);
        			
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
    

    /**
     * 
     * 
     * 
     *
     * @param <Key>
     * @param <Value>
     */
    
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
        
        /**
         * 
         * 
         * @return ResourceLoader<Key, Value>
         * 
         */

        public ResourceLoader<Key, Value> getLoader() {
            return loader;
        }
        
        /**
         * 
         * 
         * @return Cache<Key, Value>
         * 
         */

        public Cache<Key, Value> getCache() {
            return cache;
        }
        
        /**
         * 
         * 
         * @return Cache<Key, Boolean>
         * 
         */

        public Cache<Key, Boolean> getSearchCache() {
            return searchCache;
        }
        
        
        
    }
    
    /**
     * CallbackControl getParallel(
     *  Callback<Object, ResourceEvent<Value>> onComplete, 
     *  final Key key) – onComplete is called with the newly loaded value upon completion of 
     *  the resource loader call. CallbackControl has one method, cancel which allows you to 
     *  cancel the request before it completes.
     * 
     * @return CallbackControl
     * 
     */

    @Override
    public CallbackControl getParallel(
        final Callback<Object, ResourceEvent<Value>> onComplete,
        final Key key
    ) {
    	
        if(onComplete == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><13>, " + loggingTag + ": onComplete must not be null");
        }
        
        if(key == null) {
            throw new NullPointerException("<CachedParallelResourceLoader><14>, " + loggingTag + ": key must not be null");
        }
        
        CallbackControl returnVal;
        
        try {
            
            final Boolean searchValue = searchCache.get(key);
            
            if(searchValue == null || searchValue) {
                
                final Value value = cache.get(key);
                
                if(value != null) {
                                    
                    final ResourceEvent<Value> completed = new ResourceEvent<Value>(value, null, ResourceEvent.CACHED);
                    
                    onComplete.call(completed);
                    
                    returnVal = 
                        new CallbackControl() {

                        @Override
                        public void cancel() {
                            //was cached nothing to cancel
                        }
                    };
                    
    
                }
                else {
                    
                    returnVal = internal.getParallel(
                            
                        new Callback<Object, ResourceEvent<Value>>() {
    
                            @Override
                            public Object call(
                                final ResourceEvent<Value> value
                            ) {
                                
                            	if(value.getThrowable() == null) {
                            		try {
                                        searchCache.put(key, value.getVal() != null);
                                    } 
                            		catch (ResourceException e) {
                                        logger.error("ERROR", e);
                                    }
                            	}
                                
                                if(value.getVal() != null) {
                                    try {
                                        cache.put(key, value.getVal());
                                    }
                                    catch (ResourceException e) {
                                        logger.error("ERROR", e);
                                    }
                                }
                                
                                onComplete.call(value);
                                
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
                onComplete.call(completed);
                
                returnVal = 
                    new CallbackControl() {

                    @Override
                    public void cancel() {
                        //was cached nothing to cancel
                    }
                };
                
            }    
                
        }
        catch(final ResourceException e) {
            
            logger.warn("<CachedParallelResourceLoader><15>, " + "exception thrown:", e);
            
            final ResourceEvent<Value> completed = new ResourceEvent<Value>(null, e, ResourceEvent.UNKNOWN);

            onComplete.call(completed);
            
            returnVal = 
                new CallbackControl() {

                @Override
                public void cancel() {
                    //was cached nothing to cancel
                }
            };

        }      
        
        return returnVal;
        
    }
    
    /**
     * 
     * 
     * @return Cache<Key, Boolean>
     * 
     */
    
    public Cache<Key, Boolean> getSearchCache() {
        return this.searchCache;
    }
    
    /**
     * 
     * 
     * @return Cache<Key, Value>
     * 
     */
    
    public Cache<Key, Value> getCache() {
        return this.cache;
    }

}
