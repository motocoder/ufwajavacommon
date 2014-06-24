package llc.ufwa.data.resource.loader;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.util.StopWatch;

public class LoggingResourceLoader<Key, Value> implements ResourceLoader<Key, Value> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingResourceLoader.class);
    
    private final ResourceLoader<Key, Value> internal;
    private final String tag;

    public LoggingResourceLoader(ResourceLoader<Key, Value> internal, String tag) {
        
        this.internal = internal;
        this.tag = tag;
    }
    
    @Override
    public boolean exists(Key key) throws ResourceException {
        
        final StopWatch watch = new StopWatch();
        watch.start();
        
        final boolean returnVal = internal.exists(key);
        
        logger.info("LoggingResourceLoader: " + tag + ": exists for key: " + key + ":" + returnVal + " time: " + watch.getTime());
        
        return returnVal;
    }

    @Override
    public Value get(Key key) throws ResourceException {
        
        final StopWatch watch = new StopWatch();
        watch.start();
        
        final Value returnVal = internal.get(key);
        
        logger.info("LoggingResourceLoader: " + tag + ": get for key: " + key + ":" + returnVal+ " time: " + watch.getTime());
        
        return returnVal;
    }

    @Override
    public List<Value> getAll(List<Key> keys) throws ResourceException {
        
        final StopWatch watch = new StopWatch();
        watch.start();
        
        final List<Value> returnVals = internal.getAll(keys);
        
        for(int i = 0; i < keys.size(); i++) {
            
            final Value returnVal = returnVals.get(i);
            final Key key = keys.get(i);
            
            logger.info("LoggingResourceLoader: " + tag + ": getAll for key: " + key + ":" + returnVal+ " time: " + watch.getTime());
            
        }
        
        
        return returnVals;
    }

}
