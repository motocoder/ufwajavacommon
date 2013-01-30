package llc.ufwa.concurrency;



import java.util.LinkedList;
import java.util.concurrent.Executor;

import llc.ufwa.javacommon.test.concurrency.OneThroughLimitingExecutorTest;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneThroughLimitingExecutor implements Executor {
	
	private static final Logger logger = LoggerFactory.getLogger(OneThroughLimitingExecutorTest.class);	

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
    							
    							logger.debug("if runList.size() >0  push and call startIfAvailable just ran");
    							
    						} 
    						finally {
    						    
    							logger.debug("finally rerun startIfAvailable after pop run... before");
    							runList.removeFirst();
    							logger.debug("finally rerun startIfAvailable after pop run... after");
    							
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
				logger.debug("if runList.size() == 0 push and call to startIfAvailable just ran");

			}
			else if (runList.size() == 1) {

				runList.add(command);
				startIfAvailable();
				logger.debug("if runList.size() == 1 add to end of list and call to theQueue() just ran");

			}
			else if (runList.size() == 2) {

				runList.removeLast();
				runList.add(command);
				startIfAvailable();
				logger.debug("if runList.size() == 2 remove last item in list and add to end of list and call to theQueue() just ran");

			}
		}

	}
    
}
