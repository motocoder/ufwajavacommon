package llc.ufwa.concurrency;

import java.util.LinkedList;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps an executor and limits the queue to 1.  
 * 
 * If you add another runnable it pushes the previously queued runnable out and becomes first in queue.
 * 
 * 
 * @author michaelporter
 *
 */

public class OneThroughLimitingExecutor implements Executor {
	
	private static final Logger logger = LoggerFactory.getLogger(OneThroughLimitingExecutor.class);	

    private final Executor threads;
    private final LinkedList<Runnable> runList = new LinkedList<Runnable>();
    
    public OneThroughLimitingExecutor(
        final Executor executor
    ) {
        
        this.threads = executor;
        
    }
    
	private void startIfAvailable() {
		
		if(runList.size() > 0) {
			
			threads.execute(
					
			    new Runnable() {
	
					@Override
					public void run() {
	
					    final Runnable toRun;
                        
                        synchronized(runList) {
                            
                            if(runList.size() != 0) {
                                toRun = runList.getFirst();
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
    						    
    							runList.removeFirst();
    							
    							startIfAvailable();
    							
    						}
    						
                        }
	
					}
					
				}
			    
			);
			
		}
		
	}

    @Override
	public void execute(final Runnable command) {

		synchronized (runList) {
			if (runList.size() == 0) {

				runList.add(command);
				startIfAvailable();
				

			}
			else if (runList.size() == 1) {

				runList.add(command);
				startIfAvailable();

			}
			else if (runList.size() == 2) {

				runList.removeLast();
				runList.add(command);
				startIfAvailable();
				
			}
		}

	}
    
}
