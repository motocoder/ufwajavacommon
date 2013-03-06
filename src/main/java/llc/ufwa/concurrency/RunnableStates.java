package llc.ufwa.concurrency;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.provider.ResourceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnableStates {
    
	private static final Logger logger = LoggerFactory.getLogger(RunnableStates.class);
	
    private final SortedSet<SequencedRunnable> running = new TreeSet<SequencedRunnable>();
    private final List<SequencedRunnable> waitingToRun = new LinkedList<SequencedRunnable>();
    private final SortedSet<SequencedRunnable> starting = new TreeSet<SequencedRunnable>();
    final SortedSet<SequencedRunnable> combined = new TreeSet<SequencedRunnable>();
    
    private final int limit;
     
    @SuppressWarnings("rawtypes")
    private final Map<Integer, Future> tasks = new HashMap<Integer, Future>();       
    private final Map<SequencedRunnable, Integer> runnableToID = new HashMap<SequencedRunnable, Integer>();
    private final Map<Integer, SequencedRunnable> IDToRunnable = new HashMap<Integer, SequencedRunnable>();
    private final ResourceProvider<Integer> idProvider;
    private final Map<SequencedRunnable, Callback<Void, Void>> canceledBeforeStarts = new HashMap<SequencedRunnable, Callback<Void, Void>>();
	private final Executor callbackThreads;
	
	private int currentSequence;
    
    public RunnableStates(
        final Executor callbackThreads,
        final int limit,
        final ResourceProvider<Integer> idProvider
    ) {
        
    	this.callbackThreads = callbackThreads;
        this.limit = limit;
        this.idProvider = idProvider;
        
    }
    
    /**
     * Schedule a runnable to run.
     * 
     * @param runnable
     */
    public synchronized void schedule(Runnable runnable) {
            
        waitingToRun.add(0, new SequencedRunnable(currentSequence++, runnable));       
        this.notifyAll();
        
    }
    
    /**
     * @param future
     * @param runnable
     */
    @SuppressWarnings("rawtypes")
    public synchronized void launched(Future future, SequencedRunnable runnable) {

        final Integer id;
        
        try {
            id = idProvider.provide();
        } 
        catch (ResourceException e) {
            throw new RuntimeException("This should never happen");
        }
        
        if(combined.contains(runnable)) {
            
            tasks.put(id, future);
            runnableToID.put(runnable, id);
            IDToRunnable.put(id, runnable);
            
            this.notifyAll();
            
        }
        else {
            future.cancel(true);
        }
        
    }
    
    /**
     * 
     * @param runnable
     * @return
     * @throws InterruptedException 
     */
    @SuppressWarnings("rawtypes")
    public synchronized Future getTask(SequencedRunnable runnable) {
            
        Integer id = runnableToID.get(runnable);
        
        return tasks.get(id);    
        
    }
    
    /**
     * @throws InterruptedException 
     * 
     */
    @SuppressWarnings("rawtypes")
    public synchronized void cancel() {
        
        final SequencedRunnable returnVal;
        
        if((combined.size()) > limit) {
            throw new RuntimeException("Should never have more than limit runing");
        }

        if(combined.size() == limit && this.waitingToRun.size() != 0) {
 
        	returnVal = combined.first();
        	
            if(running.contains(returnVal)) {
                
                final Future task = getTask(returnVal);

                task.cancel(true);
                
            }
            else if(starting.contains(returnVal)) {
                
                final Callback<Void, Void> onCanceledBeforeStart = this.canceledBeforeStarts.get(returnVal);
                
                if(onCanceledBeforeStart != null) {
                	
                	try {
                		callbackThreads.execute(
                		    new Runnable() {

								@Override
								public void run() {
									onCanceledBeforeStart.call(null);
									
								}
						    }
	                	);
                	}
                	catch(Throwable t) {
                		logger.error("ERROR IN CANCEL:", t);
                	}
                	
                }
                
                finish(returnVal); //was never started we can just terminate successfully.
  
                this.notifyAll();
                
            }
            else {
                throw new RuntimeException("should not get here");
            }
            
        }
        else {
        	
	        while(this.waitingToRun.size() > limit) {
	            
	            final SequencedRunnable runnable = waitingToRun.get(0);
	            
	            final Callback<Void, Void> onCanceledBeforeStart = this.canceledBeforeStarts.get(runnable);
	            
	            if(onCanceledBeforeStart != null) {
	            	
	            	try {
	            		callbackThreads.execute(
	            		    new Runnable() {
	
								@Override
								public void run() {
									onCanceledBeforeStart.call(null);
									
								}
						    }
	                	);
	            	}
	            	catch(Throwable t) {
	            		logger.error("ERROR IN CANCEL:", t);
	            	}
	            	
	            }
	            
	            finish(runnable);
	            
	        }
        }
        
    }
    
    /**
     * 
     * @param runnable
     * @return
     * @throws InterruptedException
     */
    public synchronized boolean started(SequencedRunnable runnable) throws InterruptedException {
        
        //was cancelled before starting
        if(!starting.contains(runnable)) {
            
            finish(runnable);
            return false;
            
        }
        else if(starting.contains(runnable)) {
        	
        	while(!runnableToID.containsKey(runnable) && starting.contains(runnable)) {
        		this.wait(100);        		
        	}
        	
        	if(!starting.contains(runnable)) {
    			finish(runnable);
                return false;
    		}
        	
            starting.remove(runnable);            
            running.add(runnable);
            
            if(combined.size() > limit) {
                throw new RuntimeException("what the fuck");
            }
            
            this.notifyAll();
            
            return true;
            
        }
        else {
            throw new RuntimeException("shouldn't get here");
        }
        
    }
    
    /**
     * Gets the next available runnable. If at limit blocks until one is finished and one is waiting to run.
     * 
     * 
     * @return
     * @throws InterruptedException
     */
    public synchronized SequencedRunnable getNext() throws InterruptedException {
         
        while((combined.size()) >= limit || waitingToRun.size() == 0) {
            this.wait();
        }
        
        final SequencedRunnable waited = waitingToRun.remove(0);
        
        starting.add(waited);
        combined.add(waited);
                
        return waited;

    }
    
    /**
     * 
     * @param runnable
     */
    public synchronized void finish(SequencedRunnable runnable) {
        
        final Integer id = runnableToID.remove(runnable);
        
        if(id != null) {
            
            tasks.remove(id);                
            IDToRunnable.remove(id);
            
        }
        
        canceledBeforeStarts.remove(runnable);        
        running.remove(runnable);
        starting.remove(runnable);
        waitingToRun.remove(runnable);
        combined.remove(runnable);
        
        this.notifyAll();

    }

    public synchronized void schedule(
        final Runnable runnable,
        final Callback<Void, Void> onCanceledBeforeStart
    ) {
        
    	final SequencedRunnable sequenced = new SequencedRunnable(currentSequence++, runnable);
        canceledBeforeStarts.put(sequenced, onCanceledBeforeStart);
        waitingToRun.add(sequenced);
        
        this.notifyAll();
        
    }
    
    public static class SequencedRunnable implements Comparable<SequencedRunnable> {
    	
    	private final Runnable runnable;
		private final int sequence;

		public SequencedRunnable(
		    final int sequence,
		    final Runnable runnable
		) {
			
    		this.runnable = runnable;
    		this.sequence = sequence;
    		
    	}

		public Runnable getRunnable() {
			return runnable;
		}

		public int getSequence() {
			return sequence;
		}

		@Override
		public int compareTo(SequencedRunnable toCompare) {
			return this.sequence - toCompare.sequence;
		}
		
    }
    
}