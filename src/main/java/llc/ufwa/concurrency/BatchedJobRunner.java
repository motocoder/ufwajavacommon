package llc.ufwa.concurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import llc.ufwa.data.exception.JobRunningException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BatchedJobRunner<Job> {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchedJobRunner.class);

    private final Map<Long, Queue<Job>> splitJobCacheMap;
    private final int maxNumJobs;
    private final BatchMultiRunAndQueueExecutor threads;

	private boolean enabled = true;

    public BatchedJobRunner(
        final int concurrentThreads,
        final int maxNumJobs,
        final Executor bulkThreads
    ) {
        
        this.threads = new BatchMultiRunAndQueueExecutor(bulkThreads, concurrentThreads);
        this.maxNumJobs = maxNumJobs;
        this.splitJobCacheMap = new HashMap<Long, Queue<Job>>(concurrentThreads);
        
        final Random randomKeyGen = new Random();
        
        for (int x = 0; x < concurrentThreads; x++) {
        	splitJobCacheMap.put(randomKeyGen.nextLong(), new ConcurrentLinkedQueue<Job>());
        }
        
    }
    
    protected abstract void doJob(Job job) throws JobRunningException;
    protected abstract void prepare() throws JobRunningException;
    protected abstract void onAllJobsComplete();   
    protected abstract void onJobErroredComplete(Job next);  

    /** 
     * Adds a job to be run. If jobs aren't already being executed it starts job execution.
     * 
     * @param job
     */
    public void addJob(final Job job) {

        synchronized (splitJobCacheMap) {
        	
        	if((maxNumJobs >= 0) && (totalJobs() < maxNumJobs)) {
	    		
	        	final ThreadQueue smallestQueue = getSmallestQueue();
	        	
	        	smallestQueue.getQueue().add(job);
	    		
	            start(smallestQueue);
        	
        	}
             
        }

    }
    
    private ThreadQueue getSmallestQueue() {
    	
    	synchronized (splitJobCacheMap) {
	    	
	    	final Iterator<Long> iter = splitJobCacheMap.keySet().iterator();
	    	
	    	if (!iter.hasNext()) {
	    		return null;
	    	}
    		
	    	long smallestThreadID = iter.next();
	    	Queue<Job> smallestQueue = splitJobCacheMap.get(smallestThreadID); // set to first element of list
	    	
	    	while (iter.hasNext()) {
	    		
				final long currentThreadID = iter.next();
				final Queue<Job> currentQueue = splitJobCacheMap.get(currentThreadID);
				
				if (smallestQueue.size() > currentQueue.size()) { // if the current queue is smaller than the current smallest, set it to smallest
					smallestQueue = currentQueue;
					smallestThreadID = currentThreadID;
				}
				
			}
	    	
	    	return new ThreadQueue(smallestThreadID, smallestQueue);
    	
    	}
		
	}

	/** 
     * Removes a job from the queue
     * 
     * @param job
     */
    public void removeJob(final Job job) {
        
        synchronized (splitJobCacheMap) {
        	
        	final Iterator<Queue<Job>> iter = getQueueIterator();
    		
    		while (iter.hasNext()) {
    			iter.next().remove(job);
    		}
    		
        }
        
    }
    
    /** 
     * Starts execution of jobs
     * 
     */
    public void start(final ThreadQueue threadQueue) {
    	
    	final Queue<Job> queue = threadQueue.getQueue();
    	
    	if (getEnabled()) {
    	
	    	threads.execute(
	
	    	            new Runnable() {
	
	    	                @Override
	    	                public void run() {
	    	                	
	    	                	threads.executeOnSpecificThread(
						                new Runnable() {
						
						                    @Override
						                    public void run() {
						                    	
						                    	Job next;

						                    	synchronized(queue) {
						                    		next = queue.poll();
						                    	}
						                            
						                        if(next == null) { 
						                        	
						                        	checkAllComplete();
						                            return;
						                            
						                        }
      
						                        try {
						                            prepare();
						                        }
						                        catch (JobRunningException e) {
						                            
						                            logger.error("<SequentialJobRunner><1>, COULD NOT PREPARE:", e);
						                            addJob(next);
						                            onJobErroredComplete(next);
						                            return;
						                            
						                        }
						                        
						                        try {

						                            doJob(next);
						                            
						                            if(!getEnabled()) {
						                            	checkAllComplete();
						                            }
						                            
						                        }
						                        catch(JobRunningException e) {
						                            logger.error("<SequentialJobRunner><2>, Error:", e);
						                        	addJob(next);                     
						                            onJobErroredComplete(next);
						                            
						                        }
						                        
						                        checkAllComplete();
						                    	
						                    }
						                }, threadQueue.getThreadID());
	    	
		                }
	                }
	            );
	    	
    	}
	
	}
    
	public List<Job> clearPending() {
        
        synchronized (splitJobCacheMap) {
            
            final List<Job> returnVals = new ArrayList<Job>();
        	
        	final Iterator<Queue<Job>> iter = getQueueIterator();
    		
    		while (iter.hasNext()) {
    			
    			final Queue<Job> jobCacheSplit = iter.next();
	            
	            while(true) {
	                
	                final Job next = jobCacheSplit.poll();
	                
	                if(next != null) {
	                    returnVals.add(next);
	                }
	                else {
	                    break;
	                }
	                
	            }

	            jobCacheSplit.clear();
	            
    		}
            
            return returnVals;
            
        }
        
    }
	
	private Iterator<Queue<Job>> getQueueIterator() {
		
		synchronized(splitJobCacheMap) {
	    	return splitJobCacheMap.values().iterator();
		}
		
	}
	
	public void checkAllComplete() {

		synchronized(splitJobCacheMap) {
			
    		if (!hasJobs()) {
    			onAllJobsComplete();
    		}
			
		}
		
	}
    
    public boolean hasJobs() {
        
        synchronized(splitJobCacheMap) {
            return totalJobs() > 0;
        }
        
    }
    
    private int totalJobs() {
    	
    	synchronized(splitJobCacheMap) {
	    	
	    	final Iterator<Queue<Job>> iter = getQueueIterator();
		    
			int totalJobs = 0;
			
			while (iter.hasNext()) {
				final int addSize = iter.next().size();
				totalJobs += addSize;
			}
			
			return totalJobs;
		
    	}
		
	}
    
    public boolean isRunning() {
    	return threads.running();
    }

    public int getRuns() {
    	return threads.getRuns();
    }

    public synchronized void setEnabled(boolean enabled) {
    	this.enabled = enabled;
    }

    public synchronized boolean getEnabled() {
    	return enabled;
    }
    
    private class ThreadQueue {
    	
    	private final long threadID;
        private final Queue<Job> queue;

        public ThreadQueue(final long threadID, final Queue<Job> queue) {
            this.threadID = threadID;
            this.queue = queue;
        }

        public long getThreadID() {
            return threadID;
        }

        public Queue<Job> getQueue() {
            return queue;
        }
        
    }
    
}
