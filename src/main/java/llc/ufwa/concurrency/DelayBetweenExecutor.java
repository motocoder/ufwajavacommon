package llc.ufwa.concurrency;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayBetweenExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(DelayBetweenExecutor.class);
    
    private final Executor internal;
    private final long delay;
    
    private volatile long lastRun;

    public DelayBetweenExecutor(final Executor internal, final long delay) {
        
        this.internal = internal;
        this.delay = delay;
        lastRun = System.currentTimeMillis() - delay;
        
    }

    @Override
    public synchronized void execute(final Runnable command) {
        
        final long timeSince = System.currentTimeMillis() - lastRun;
        
        if(timeSince <= delay) {
            
            final long sleep = delay - timeSince;
            
            try {
                Thread.sleep(sleep);
            } 
            catch(InterruptedException e) {
                logger.info("interrupted", e);
            }
            
        }
        
        internal.execute(command);
        
    }
}
