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

    public SerialExecutor(Executor executor) {
        this.executor = executor;
    }

    public synchronized void execute(final Runnable r) {
        
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
