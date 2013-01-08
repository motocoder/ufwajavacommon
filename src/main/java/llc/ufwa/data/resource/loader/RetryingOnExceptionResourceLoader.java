package llc.ufwa.data.resource.loader;

import java.util.Set;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.OutOfRetriesException;
import llc.ufwa.data.exception.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryingOnExceptionResourceLoader<Key, Value> extends DefaultResourceLoader<Key, Value> {

    private static final Logger logger = LoggerFactory.getLogger(RetryingOnExceptionResourceLoader.class);
    
    //TODO better implementation of getAll
    
    private final ResourceLoader<Key, Value> internal;
    private final Callback<Void, Key> onRetrying;
    private final int retries;
    private final Set<Class<? extends ResourceException>> exceptionTypes;

    public RetryingOnExceptionResourceLoader(
        final ResourceLoader<Key, Value> internal,
        final Callback<Void, Key> onRetrying,
        final int retries,
        final Set<Class<? extends ResourceException>> exceptionTypes
    ) {
        
        this.exceptionTypes = exceptionTypes;
        this.retries = retries;
        this.internal = internal;
        this.onRetrying = onRetrying;
        
    }
    
    @Override
    public boolean exists(Key key) throws ResourceException {
        
        for(int i = 0; retries < 0 || i < retries; i++) {
            
            try {
                return internal.exists(key);
            }
            catch(ResourceException e) {
                
                logger.info("Caught Exception: ", e);
                
                if(exceptionTypes.contains(e.getClass())) {
                    
                    if(!onRetrying.call(null, key)) {
                        break;
                    }
                    
                }
                else {
                    throw e;
                }
                
            }
            
        }
        
        throw new OutOfRetriesException();     
        
    }

    @Override
    public Value get(Key key) throws ResourceException {
        
        for(int i = 0; retries < 0 || i < retries; i++) {
            
            try {
                return internal.get(key);
            }
            catch(ResourceException e) {

                logger.info("Caught Exception: ", e);
                
                if(exceptionTypes.contains(e.getClass())) {
                    
                    if(!onRetrying.call(null, key)) {
                        break;
                    }
                    
                }
                else {
                    throw e;
                }
                
            }
            
        }
        
        throw new OutOfRetriesException();
        
    }

}
