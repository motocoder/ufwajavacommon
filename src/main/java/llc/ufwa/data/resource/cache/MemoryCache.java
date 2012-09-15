package llc.ufwa.data.resource.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.data.beans.Entry;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.util.StopWatch;

/**
 * 
 * @author seanwagner
 *
 * @param <Key>
 * @param <Value>
 */
public class MemoryCache<Key, Value> extends DefaultResourceLoader<Key, Value> implements Cache<Key, Value> {
    
    private static final Logger logger = LoggerFactory.getLogger(MemoryCache.class);
    
    private final Converter<Value, Integer> sizer;
    private final TreeMap<Long, Map<Key, Value>> mapped = new TreeMap<Long, Map<Key, Value>>();
    private final Map<Key, Long> insertionTimes = new HashMap<Key, Long>();
    
    private final int maxSize;
    
    private int currentSize;
    
    public MemoryCache() {
    	
        this(
            new Converter<Value, Integer>() {

                @Override
                public Integer convert(Value old) throws ResourceException {
                    return 0;
                }
    
                @Override
                public Value restore(Integer newVal) throws ResourceException {
                    return null;
                }
            },
            -1
        );
        
    }

    /**
     * 
     * @param sizer
     * @param maxSize -1 for unlimited
     */
    public MemoryCache(
        final Converter<Value, Integer> sizer, 
        final int maxSize
    ) {

        if(sizer == null) {
            throw new NullPointerException("Size cannot be null");
        }
        
        this.sizer = sizer;
        this.maxSize = maxSize;
        
    }

    @Override
    public Value get(Key key) throws ResourceException {
        
        final Long insertionTime = insertionTimes.get(key);
        
        final Value returnVal;
        
        if(insertionTime == null) {
            returnVal = null;
        }
        else {
            
            final Map<Key, Value> insertedValues = mapped.get(insertionTime);
            
            returnVal = insertedValues.get(key);
            
        }
        
        return returnVal;
        
    }

    @Override
    public void clear() {
        
        this.mapped.clear();
        this.insertionTimes.clear();
        
        this.currentSize = 0;
        
        
    }

    @Override
    public void remove(Key key) {
        
        if(key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        
        final Long insertionTime = insertionTimes.get(key);
        
        //Remove and update current size.
        if(insertionTime != null) {
            
            final Map<Key, Value> insertedValues = mapped.get(insertionTime);
           
            final Value value = insertedValues.get(key);
            
            if(value != null) {
                
                final int size;
                
                try {
                    size = sizer.convert(value);
                } 
                catch (ResourceException e) {
                    
                    logger.error("What the heck?");
                    throw new RuntimeException(e);
                    
                }
                
                insertedValues.remove(key);
                
                currentSize -= size;
                
                if(insertedValues.size() == 0) {
                    mapped.remove(insertionTime);                    
                }
                
            }

            insertionTimes.remove(key);
            
        }
        
    }

    @Override
    public void put(Key key, Value value) {
        
        if(value == null) {
            throw new NullPointerException("value cannot be null this isn't a map");
        }
        
        if(key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        
        final long time = System.currentTimeMillis();
        
        final Long insertionTime = insertionTimes.get(key);
        
        if(insertionTime != null) {
            
            {   //remove old value.
                final Map<Key, Value> entries = mapped.get(insertionTime);
                
                entries.remove(key);
            }
            
        }
        else {
            
            final int size;
            
            try {
                size = sizer.convert(value);
            } 
            catch (ResourceException e) {
                
                logger.error("ERROR:", e);
                throw new RuntimeException(e);
                
            }
            
            currentSize += size;
            
        }

        insertionTimes.put(key, time);
        
        {
            //insert into new timeslot
            Map<Key, Value> entries = mapped.get(time);
            
            if(entries == null) {
                
                entries = new HashMap<Key, Value>();
                mapped.put(time, entries);
                
            }
            
            entries.put(key, value);
            
        }
        
        if(currentSize > maxSize && maxSize >= 0) {

        	final StopWatch watch = new StopWatch();
        	watch.start();
	        	
	        while(currentSize > maxSize && maxSize >= 0) {
	            
	            if(mapped.size() == 0) {
	                break;
	            }
	            
	            final Long firstKey = mapped.firstKey();
	            
	            final Map<Key, Value> firstValues = mapped.get(firstKey);
	            
	            if(firstValues.size() == 0) {
	                
	                mapped.remove(firstKey);
	                continue;
	                
	            }
	            
	            final Key toRemove = firstValues.keySet().iterator().next();
	            
	            remove(toRemove);
	            
	        }
        
        }
        
    }
    
}
