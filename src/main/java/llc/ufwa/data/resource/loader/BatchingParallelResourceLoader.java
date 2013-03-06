package llc.ufwa.data.resource.loader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.util.CollectionUtil;

public class BatchingParallelResourceLoader<Key, Value> implements ParallelResourceLoader<Key, Value> {
    
    private final ParallelResourceLoader<Key, Value> internal;
    private final int batchSize;
    private final int batchRadius;
    private final Map<Key, Integer> positionMap = new HashMap<Key, Integer>();
    
    private int lastIndex = -1;
    private final List<Key> positions;
    private final Cache<Key, Boolean> searchCache;
    private final Cache<Key, Value> cache;

    /**
     * 
     * @param internal
     * @param batchSize
     * @param batchRadius
     * @param searchCache
     * @param cache
     * @param positions
     */
    public BatchingParallelResourceLoader(
        final ParallelResourceLoader<Key, Value> internal,
        final int batchSize, 
        final int batchRadius,
        final Cache<Key, Boolean> searchCache,
        final Cache<Key, Value> cache,
        final List<Key> positions
    ) {
        
        if(new HashSet<Key>(positions).size() != positions.size()) {
            throw new IllegalArgumentException("Positions must contain no duplicates");
        }
        
        this.internal = internal;
        this.batchSize = batchSize;
        this.batchRadius = batchRadius;
        
        for(int i = 0; i < positions.size(); i++) {
            positionMap.put(positions.get(i), i);
        }
        this.positions = positions;
        this.searchCache = searchCache;
        this.cache = cache;
        
    }
    
    @Override
    public boolean exists(Key key) throws ResourceException {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Value get(Key key) throws ResourceException {
        throw new RuntimeException("Not supported");
    }

    @Override
    public List<Value> getAll(List<Key> keys) throws ResourceException {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void getParallel(
        final Callback<Object, ResourceEvent<Value>> onComplete,
        final Key key
    ) throws ResourceException {
                
        final Integer newIndex = positionMap.get(key);
        
        if(lastIndex >= 0) { 
            
            if(newIndex == null) {
                throw new IllegalArgumentException("Key was not available in positions list");
            }
                        
            if(Math.abs(newIndex - lastIndex) > batchRadius) {
                 
                doGetAll(key, onComplete);
                lastIndex = newIndex;
                
            }
            else {
                
                final Boolean searchValue = searchCache.get(key);
                final Value value = cache.get(key);
                
                if(value == null) {
                    
                    if(searchValue != null && !searchValue) {
                        onComplete.call(new ResourceEvent<Value>(null, null, ResourceEvent.UNKNOWN));
                    }
                    else {
                        internal.getParallel(onComplete, key);
                    }
                    
                }
                else {                    
                    onComplete.call(new ResourceEvent<Value>(value, null, ResourceEvent.CACHED));
                }
                
            }
            
        }
        else {
            
            if(newIndex == null) {
                throw new IllegalArgumentException("Key was not available in positions list");
            }
            
            doGetAll(key, onComplete);
            lastIndex = newIndex;
            
        }
        
    }

    private void doGetAll(final Key key, final Callback<Object, ResourceEvent<Value>> onComplete) throws ResourceException {
    	
        final Integer newIndex = positionMap.get(key);
        
        int diameter = batchRadius * 2;
        
        if(diameter > positions.size()) {
            diameter = positions.size();
        }
        
        final List<Key> chunk = CollectionUtil.loadChunkAround(positions, diameter, newIndex);
        
        final Map<Key, Callback<Object, ResourceEvent<Value>>> callbacks =
                new HashMap<Key, Callback<Object, ResourceEvent<Value>>>();
        
        for(final Key chunkKey : chunk) {
            
            final Boolean searchValue = searchCache.get(chunkKey);
            final Value value = cache.get(chunkKey);
            
            if(value == null) {
                
                if(searchValue == null || searchValue) {
                    
                    callbacks.put(
                        chunkKey, 
                        new Callback<Object, ResourceEvent<Value>>() {

                            @Override
                            public Object call(
                                final ResourceEvent<Value> value
                            ) {
                                
                                if(key == chunkKey || key.equals(chunkKey)) {
                                    onComplete.call( value);
                                }
                                
                                if(value.getVal() != null && value.getValueType() == ResourceEvent.NEW_LOADED) {
                                    
                                    cache.put(chunkKey, value.getVal());
                                    searchCache.put(chunkKey, true);
                                    
                                }
                                else {
                                    if(value.getThrowable() == null && value.getValueType() == ResourceEvent.NEW_LOADED) {
                                        searchCache.put(chunkKey, false);
                                    }
                                }
                                
                                return false;
                                
                            }
                            
                        }
                        
                    );
                    
                }
                else {
                    
                    if(chunkKey == key || key.equals(chunkKey)) {
                        
                        final ResourceEvent<Value> event = new ResourceEvent<Value>(null, null, ResourceEvent.UNKNOWN);
                        onComplete.call(event);
                        
                    }
                    
                }
                
            }
            else {
            	
                if(chunkKey == key || key.equals(chunkKey)) {
                    
                    final ResourceEvent<Value> event = new ResourceEvent<Value>(value, null, ResourceEvent.CACHED);
                    
                    onComplete.call(event);
                }
                
            }
            
        }
        
        if(callbacks.size() != 0) {
        	
        	final List<Map<Key, Callback<Object, ResourceEvent<Value>>>> seperated = CollectionUtil.breakApart(callbacks, batchSize);
        	
        	Map<Key, Callback<Object, ResourceEvent<Value>>> lastBatch = null;
        	
        	for(Map<Key, Callback<Object, ResourceEvent<Value>>> batch  : seperated) {
        		
        		if(batch.keySet().contains(key)) {
        			
        			lastBatch = batch;
        			continue;
        			
        		}
        		else if(batch.size() == 1) {
        			
        			Entry<Key, Callback<Object, ResourceEvent<Value>>> first = batch.entrySet().iterator().next();
        			internal.getParallel(first.getValue(), first.getKey());
        			
        		}
        		else {
        			internal.getAllParallel(batch); 
        		}
        		
        	}
        	
        	if(lastBatch != null && lastBatch.size() > 0) {
        		
        		if(lastBatch.size() == 1) {
        			
        			Entry<Key, Callback<Object, ResourceEvent<Value>>> first = lastBatch.entrySet().iterator().next();
        			internal.getParallel(first.getValue(), first.getKey());
        			
        		}
        		else {
        			internal.getAllParallel(lastBatch); 
        		}
        		
        	}
            
        }
        
    }

    @Override
    public void existsParallel(
        final Callback<Object, ResourceEvent<Boolean>> onComplete, Key key
    ) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void getAllParallel(
        final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap
    ) {
        throw new RuntimeException("Not supported");
    }

}
