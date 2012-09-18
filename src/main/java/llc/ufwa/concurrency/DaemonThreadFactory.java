package llc.ufwa.concurrency;

import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaemonThreadFactory implements ThreadFactory {

    private static final Logger logger = LoggerFactory.getLogger(DaemonThreadFactory.class);
    
    @Override
    public Thread newThread(Runnable r) {
        
        final Thread newThread = new Thread(r);
        
        newThread.setDaemon(true);
        newThread.setPriority(Thread.MIN_PRIORITY);
        
        return newThread;
    }

}
