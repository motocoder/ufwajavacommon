package llc.ufwa.concurrency;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * Taken from Executor javadocs.
 * @author seanwagner
 *
 */
public class SerialExecutor implements Executor {
    
    private final Queue<Runnable> tasks = new LinkedList<Runnable>();
    private final Executor executor;
    private Runnable active;
    private final boolean noQueue;

    /**
     * 
     * @param executor
     * @param noQueue will not allow anything to queue
     */
    public SerialExecutor(Executor executor, boolean noQueue) {
        
        this.noQueue = noQueue;
        this.executor = executor;
        
    }
    
    public SerialExecutor(Executor executor) {
        
        this.noQueue = false;
        this.executor = executor;
        
    }

    public synchronized void execute(final Runnable r) {
        
        if(!noQueue || active == null) {
            tasks.offer(
                    
                new Runnable() {
                    
                    public void run() {
                    
                        try {
                            r.run();
                        }
                        finally {
                            scheduleNext();
                        }
                        
                    }
                    
                }
                
            );  
        }
        
        if (active == null) {
            scheduleNext();
        }
        
    }

    protected synchronized void scheduleNext() {
        
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
        
    }
    
}
