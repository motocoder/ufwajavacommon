package llc.ufwa.data.resource.cache;

import java.util.List;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.util.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingCache<Key, Value> implements Cache<Key, Value> {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingCache.class);
    private final Cache<Key, Value> internal;
    private final String tag;

    public LoggingCache(final Cache<Key, Value> internal, final String tag) {
        
        this.internal = internal;
        this.tag = tag;
        
    }
    @Override
    public boolean exists(Key key) throws ResourceException {
        
        final StopWatch watch = new StopWatch();
        watch.start();
        
        final boolean returnVal = internal.exists(key);
        
        logger.info("LoggingCache: " + tag + ": exists for key: " + key + ":" + returnVal + " time: " + watch.getTime());
        
        return returnVal;
    }

    @Override
    public Value get(Key key) throws ResourceException {
        
        final StopWatch watch = new StopWatch();
        watch.start();
        
        final Value returnVal = internal.get(key);
        
        logger.info("LoggingCache: " + tag + ": get for key: " + key + ":" + returnVal+ " time: " + watch.getTime());
        
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
            
            logger.info("LoggingCache: " + tag + ": getAll for key: " + key + ":" + returnVal+ " time: " + watch.getTime());
            
        }
        
        
        return returnVals;
    }

    @Override
    public void clear() throws ResourceException {
        
        final StopWatch watch = new StopWatch();
        watch.start();
        
        internal.clear();
        
        logger.info("LoggingCache: " + tag + ": clear"+ " time: " + watch.getTime());
        
    }

    @Override
    public void remove(Key key) throws ResourceException {
        
        final StopWatch watch = new StopWatch();
        watch.start();
        
        internal.remove(key);
        
        logger.info("LoggingCache: " + tag + ": remove for key: " + key + " time: " + watch.getTime());
        
    }

    @Override
    public void put(Key key, Value value) throws ResourceException {
        
        final StopWatch watch = new StopWatch();
        watch.start();
        
        internal.put(key, value);
        
        logger.info("LoggingCache: " + tag + ": put for key: " + key + ":" + value+ " time: " + watch.getTime());
        
    }

}
