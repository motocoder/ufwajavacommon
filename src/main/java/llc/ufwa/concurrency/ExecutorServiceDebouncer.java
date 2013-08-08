package llc.ufwa.concurrency;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.provider.PushProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class basically works like a debouncer. If lots of calls are being 
 * made to something it eliminates the possiblity of more than a few hitting
 * it within a certain time frame.
 * 
 * @author seanwagner
 *
 */
public class ExecutorServiceDebouncer {
	
	private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceDebouncer.class);
	
	private final Callback<Object, Object> callback;
	private final long delay;
	private final Object lock = new Object();
    private final PushProvider<Boolean> shouldRun;
    private final ScheduledThreadPoolExecutor pool;
    private boolean signalOut;
    
    public enum RunType { RUN_BEFORE, RUN_AFTER };
	private final RunType run;
	
	/**
	 * Debouncer to ignore repeated calls within a set delay.
	 * 
	 * @param callback callback used to execute code
	 * @param threadFactory ThreadFactory passed in
	 * @param delay time to ignore subsequent calls
	 * @param run RunType to run the code before or after the delay
	 * 
	 */
	public ExecutorServiceDebouncer(
        final Callback<Object, Object> callback,
        final ThreadFactory threadFactory,
        final long delay,
        final RunType run
    ) {
	    this(
	        callback, 
	        threadFactory,
	        delay, 
	        new PushProvider<Boolean>() {

                @Override
                public boolean exists() throws ResourceException {
                    return true;
                }
    
                @Override
                public Boolean provide() throws ResourceException {
                    return true;
                }

                @Override
                public void push(Boolean value) throws ResourceException {                    
                }
            },
            run
	    );
	}
	
	public ExecutorServiceDebouncer(
        final Callback<Object, Object> callback,
        final ThreadFactory threadFactory,
        final Executor callbackThreads,
        final long delay,
        final RunType run
    ) {
        this(
            callback, 
            threadFactory,
            delay, 
            new PushProvider<Boolean>() {

                @Override
                public boolean exists() throws ResourceException {
                    return true;
                }
    
                @Override
                public Boolean provide() throws ResourceException {
                    return true;
                }

                @Override
                public void push(Boolean value) throws ResourceException {                    
                }
            },
            run
        );
    }
	
	public ExecutorServiceDebouncer(
	        final Callback<Object, Object> callback,
	        final ThreadFactory threadFactory,
	        final long delay,
	        final PushProvider<Boolean> shouldRun,
	        final RunType run
	    ) {
	    		
	    this.shouldRun = shouldRun;
		this.callback = callback;
		this.delay = delay;
		this.pool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10, threadFactory);
		this.run = run;
		
	}

	/**
	 * Signal the debouncer to execute.
	 */
	public synchronized void signal() {
	    
		Runnable runnable = new Runnable() {
		       
			@Override
			public void run() {
			    
				synchronized(lock) {
				    
					if (run == RunType.RUN_AFTER) {
						
						if (pool.getActiveCount() == 1) {
							
					        try {
					        	signalOut = false;
					        	callback.call(null);
	                        }
	    				    catch (Exception e) {
	                            logger.error("<Debouncer><1>, Debouncer interrupted:", e);
	                        }
					        
						}
						
					}
					
					else if (run == RunType.RUN_BEFORE) {
						
						if (pool.getActiveCount() == 1) {
							
					        try {
					        	signalOut = false;
					        	callback.call(null);
	                        }
	    				    catch (Exception e) {
	                            logger.error("<Debouncer><1>, Debouncer interrupted:", e);
	                        }
					        
						}
						
					}
				    
				}
						    		
			}
			
		};
	
		if(!signalOut) {
		   
		    try {
		        
		        synchronized(shouldRun) {
		            
                    if(shouldRun.provide()) {
                       
                        shouldRun.push(false);
                        
                    	signalOut = true;
                    	
                    	if (run == RunType.RUN_AFTER) {
                    		pool.schedule(runnable, delay, TimeUnit.MILLISECONDS);
                    	}
                    	else if (run == RunType.RUN_BEFORE) {
                    		
                    		if (pool.getTaskCount() == 0) {
                    			pool.schedule(runnable, 0, TimeUnit.MILLISECONDS);
                    		} 
                    		else {
                    			pool.schedule(runnable, delay, TimeUnit.MILLISECONDS);
                    		}
                    		
                    	}
                    	
                    }
                    
		        }
                
            }
		    catch (ResourceException e) {
                throw new RuntimeException("<Debouncer><2>, this shouldn't happen", e);
            }
		    
		}
		
		
	}

	/**
	 * Force the debouncer to run.
	 * 
	 */
    public void force() {
        
        synchronized(lock) {
            lock.notifyAll();
        }
        
    }

    public void push(boolean b) {
        
        synchronized(shouldRun) {
            
            try {
                shouldRun.push(b);
            }
            catch (ResourceException e) {
                throw new RuntimeException("<Debouncer><3>, Debouncer failed to push. " + e.getMessage(), e);
            }
            
        }
        
    }
    /*
     * Force the thread pool to stop, ending all current running threads
     * 
     */
    public void shutdownNow() {
    	pool.shutdownNow();
    }
    
}
