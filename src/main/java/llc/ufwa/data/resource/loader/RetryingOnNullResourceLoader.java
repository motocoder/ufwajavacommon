package llc.ufwa.data.resource.loader;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryingOnNullResourceLoader<Key, Value> extends DefaultResourceLoader<Key, Value> {

    private static final Logger logger = LoggerFactory.getLogger(RetryingOnNullResourceLoader.class);
    
    //TODO better implementation of getAll
    
    private final ResourceLoader<Key, Value> internal;
    private final Callback<Void, Key> onRetrying;
    private final int retries;

    /**
     * 
     * @param internal
     * @param onRetrying
     * @param retries
     */
    public RetryingOnNullResourceLoader(
        final ResourceLoader<Key, Value> internal,
        final Callback<Void, Key> onRetrying,
        final int retries
    ) {
        
        this.retries = retries;
        this.internal = internal;
        this.onRetrying = onRetrying;
        
    }
    
    @Override
    public boolean exists(Key key) throws ResourceException {
        
        for(int i = 0; retries < 0 || i < retries; i++) {
            
            boolean val = internal.exists(key);
            
            if(val) {
                return val;
            }
            else {
                
                if(!onRetrying.call(null, key)) {
                    
                    logger.info("Retry callback decided not to continue");
                    break;
                    
                }
                
            }
            
        }
        
        return false;
        
    }

    @Override
    public Value get(Key key) throws ResourceException {

        for(int i = 0; retries < 0 || i < retries; i++) {
            
            Value val = internal.get(key);
            
            if(val != null) {
                return val;
            }
            else {
                
                if(!onRetrying.call(null, key)) {
                    
                    logger.info("Retry callback decided not to continue");
                    break;
                    
                }
                
            }
            
        }
        
        return null;
        
    }

}
