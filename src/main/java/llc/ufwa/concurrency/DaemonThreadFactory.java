package llc.ufwa.concurrency;

import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {
    
    @Override
    public Thread newThread(Runnable r) {
        
        final Thread newThread = new Thread(r);
        
        newThread.setDaemon(true);
        newThread.setPriority(Thread.MIN_PRIORITY);
        
        return newThread;
    }

}
