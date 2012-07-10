package llc.ufwa.concurrency;

import java.util.concurrent.Executor;

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
public class Debouncer {
	
	private static final Logger logger = LoggerFactory.getLogger(Debouncer.class);
	
	private final Callback<Object, Object> callback;
	private final Executor executor;

	private boolean signalOut;
	private final long delay;
	
	private Object lock = new Object();
	
	/**
	 * 
	 * @param callback - This is run when signaled after the delay
	 * @param executor - Runs the callback call on this.
	 * @param delay - runs the callback after this delay.
	 */
	public Debouncer(
	    final Callback<Object, Object> callback,
	    final Executor executor,
	    final long delay
	) {
	    
		this.callback = callback;
		this.executor = executor;
		this.delay = delay;
		
	}

	/**
	 * Signal the debouncer to execute.
	 */
	public synchronized void signal() {
	
		if(!signalOut) {
			
			signalOut = true;
			
			executor.execute(
				new Runnable() {

					@Override
					public void run() {
						
						synchronized(lock) {
						    
						    try {
                                lock.wait(delay);
                            }
						    catch (InterruptedException e) {
                                logger.error("Error:", e);
                            }
						    
						}
						
						synchronized(Debouncer.this) {
							signalOut = false;
						}
						
						executor.execute(
							new Runnable() {
								public void run() {
									callback.call(null, null);
								}
							}
						);
					}
				}
		    );
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
    
}
