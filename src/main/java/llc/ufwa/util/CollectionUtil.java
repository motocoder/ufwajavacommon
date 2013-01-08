package llc.ufwa.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CollectionUtil.class);
    
    private CollectionUtil() {
        
    }
    
    /**
     * 
     * @param original
     * @param chunkSize
     * @param position
     * @return
     */
    public static <T> List<T> loadChunkAround(
        final List<T> original,
        final int chunkSize,
        final int position
    ) {
                
        if(chunkSize % 2 != 0) {
            throw new IllegalArgumentException("Chunk size must be factor of 2");
        }
        
        if(position >= original.size()) {
            throw new IllegalArgumentException("You cannot start at a position bigger than the original");
        }
        
        if(chunkSize > original.size()) {
            throw new IllegalArgumentException("You cannot get a chunk bigger than the original");
        }
        
        final int halfChunk = chunkSize / 2;
        
        final int start;
        final int end;
        
        if(position <= halfChunk) {
            
            start = 0;
            end = chunkSize;

        }
        else if(position > halfChunk && (position + halfChunk) < original.size()) {
            
            start = position - halfChunk;
            end = position + halfChunk;
            
        }
        else {
            
            start = original.size() - chunkSize;
            end = original.size();
            
        }
        
        return original.subList(start, end);
        
    }
    
    @SuppressWarnings("unchecked")
	public static <Key, Value> List<Map<Key, Value>> breakApart(Map<Key, Value> toBreakUp, final int size) {
    	
    	if(toBreakUp.size() == 0) {
    		return new ArrayList<Map<Key, Value>>();
    	}
    	
    	final List<Map<Key, Value>> returnVal = new ArrayList<Map<Key, Value>>();
    	
    	Map<Key, Value> currentMap;
		try {
			currentMap = toBreakUp.getClass().newInstance();
		}
		catch (InstantiationException e) {
			
			logger.error("ERROR:", e);
			throw new RuntimeException("Error:");
			
		}
		catch (IllegalAccessException e) {
			
			logger.error("ERROR:", e);
			throw new RuntimeException("Error:");
			
		}
		
		returnVal.add(currentMap);
    	
    	for(Map.Entry<Key, Value> entry : toBreakUp.entrySet()) {
    		
    		currentMap.put(entry.getKey(), entry.getValue());
    		
    		if(currentMap.size() == size) {
    			
    			try {
    				currentMap = toBreakUp.getClass().newInstance();
    			}
    			catch (InstantiationException e) {
    				
    				logger.error("ERROR:", e);
    				throw new RuntimeException("Error:");
    				
    			}
    			catch (IllegalAccessException e) {
    				
    				logger.error("ERROR:", e);
    				throw new RuntimeException("Error:");
    				
    			}
    			
    			returnVal.add(currentMap);
    			
    		}
    		
    	}
    	
    	return returnVal;
    }

}
