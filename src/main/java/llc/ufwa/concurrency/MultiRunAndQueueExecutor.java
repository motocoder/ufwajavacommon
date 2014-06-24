package llc.ufwa.concurrency;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps an executor and limits the runnables and queueables to given constructor values.  
 * 
 * @author michaelporter
 *
 */

public class MultiRunAndQueueExecutor implements Executor {
    
    private static final Logger logger = LoggerFactory.getLogger(OneThroughLimitingExecutor.class); 

    private final Executor threads;
    private final Set<Runnable> runList = new HashSet<Runnable>();
    private final LinkedList<Runnable> queueList = new LinkedList<Runnable>();
    private final int runners;
    private final int queuers; 
    
    
    public MultiRunAndQueueExecutor(
        final Executor executor,
        final int runners,
        final int queuers
    ) {
        
        this.threads = executor;
        this.runners = runners;
        this.queuers = queuers;
        
    }
    
    private void startIfAvailable() {

        threads.execute(

            new Runnable() {

                @Override
                public void run() {

                    final Runnable toRun;
                    
                    synchronized(runList) {

                        if(runList.size() < runners && queueList.size() > 0) {
                            
                            toRun = queueList.pop();
                            runList.add(toRun);
                            
                        }
                        else {
                            toRun = null;
                        }
                        
                    }

                    if(toRun != null) {

                        try {

                            toRun.run();

                        } 
                        finally {

                            synchronized(runList) {
                                runList.remove(toRun);
                            }

                            startIfAvailable();
                              
                        }

                    
                    }

                }

            }

        );
        
    }
    
    public boolean running() {
        
        synchronized(runList) {
            
            if(runList.size() != 0) {
                return true;
            }
            else {
                return false;
            }
            
        }
    }

    @Override
    public void execute(final Runnable command) {
        
        synchronized(runList) {
            
            if(queueList.size() < queuers) {
             
                queueList.add(command);
                
                startIfAvailable();
                
            }
            
        }
        
        
    }
    
}

