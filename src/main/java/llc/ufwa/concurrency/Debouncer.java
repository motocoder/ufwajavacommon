package llc.ufwa.concurrency;

import java.util.concurrent.Executor;

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
@Deprecated
public class Debouncer {
	
	private static final Logger logger = LoggerFactory.getLogger(Debouncer.class);
	
	private final Callback<Object, Object> callback;
	private final Executor executor;
	private final long delay;
	private final Object lock = new Object();
    private final PushProvider<Boolean> shouldRun;
    
    private boolean signalOut;
    
	public Debouncer(
        final Callback<Object, Object> callback,
        final Executor executor,
        final long delay
    ) {
	    this(
	        callback, 
	        executor,
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
            }
	    );
	}
	
	public Debouncer(
        final Callback<Object, Object> callback,
        final Executor executor,
        final Executor callbackThreads,
        final long delay
    ) {
        this(
            callback, 
            executor,
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
            }
        );
    }
	
	public Debouncer(
	        final Callback<Object, Object> callback,
	        final Executor executor,
	        final long delay,
	        final PushProvider<Boolean> shouldRun
	    ) {
	    
	    this.shouldRun = shouldRun;
		this.callback = callback;
		this.executor = executor;
		this.delay = delay;
		
	}

	/**
	 * Signal the debouncer to execute.
	 */
	public synchronized void signal() {
	
	    logger.debug("debouncer signaled");
	    
		if(!signalOut) {
		    
		    logger.debug("signal wasn't out");
		    
		    try {
		        
		        synchronized(shouldRun) {
		            
                    if(shouldRun.provide()) {
                        
                        logger.debug("shouldRun");
                    
                        shouldRun.push(false);
                        
                    	signalOut = true;
                    	
                    	executor.execute(
                    		new Runnable() {
       
                    			@Override
                    			public void run() {
                    			    
                    			    logger.debug("debouncer waiting " + System.currentTimeMillis());
                    			    
                    				synchronized(lock) {
                    				    
                    				    if(delay != 0) {
                        				    
                    				        try {
                                                lock.wait(delay);
                                            }
                        				    catch (InterruptedException e) {
                                                logger.error("<Debouncer><1>, Deboucer interrupted:", e);
                                            }
                        				    
                    				    }
                    				    
                    				}
                    				
                    				logger.debug("debouncer not waiting" + System.currentTimeMillis());

        						    logger.debug("started debouncer");
        						    
        						    executor.execute(
                                            
                                        new Runnable() {

                                            @Override
                                            public void run() {
                                                callback.call(null);                                                
                                            }
                                            
                                        }
                                        
                                    );
        						    
        						    synchronized(Debouncer.this) {
                                        signalOut = false;
                                    }
        						    			
                    			}
                    			
                    		}
                    		
                        );
                    
                    }
                    else {
                        logger.debug("shouldn't run");
                    }
		        }
                
            }
		    catch (ResourceException e) {
                throw new RuntimeException("<Debouncer><2>, this shouldn't happen", e);
            }
		    
		}
		else {
		    logger.debug("signal was out already doing nothing");
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
    
}
