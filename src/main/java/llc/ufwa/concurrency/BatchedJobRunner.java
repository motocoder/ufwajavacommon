package llc.ufwa.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

import llc.ufwa.data.exception.JobRunningException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BatchedJobRunner<Job> {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchedJobRunner.class);

    private final Queue<Job> jobCache; 
    private final int maxSize;

    private boolean enabled = true;

    private final MultiRunAndQueueExecutor threads;

    private final int batchSize;

    private final boolean waitForBatch;

    private final int concurrentJobs;

    private String logTag;

    /**
     * 
     * @param cache
     * @param concurrentJobs
     * @param maxSize
     * @param batchSize
     * @param bulkThreads
     * @param waitForBatch
     */
    public BatchedJobRunner(
        final Queue<Job> cache, 
        final int concurrentJobs,
        final int maxSize,
        final int batchSize,
        final Executor bulkThreads,
        boolean waitForBatch
    ) {
      
        this(cache, concurrentJobs, maxSize, batchSize, bulkThreads, waitForBatch, "default");
        
    }
    
    public BatchedJobRunner(
        final Queue<Job> cache, 
        final int concurrentJobs,
        final int maxSize,
        final int batchSize,
        final Executor bulkThreads,
        boolean waitForBatch,
        final String logTag
    ) {
      
        this.logTag = logTag;
        this.threads = new MultiRunAndQueueExecutor(bulkThreads, concurrentJobs, maxSize);
        this.concurrentJobs = concurrentJobs;
        this.maxSize = maxSize;
        this.batchSize = batchSize;
        this.jobCache = cache;
        this.waitForBatch = waitForBatch;
        
        start(false);
        
    }
    
    protected abstract void doJobs(List<Job> jobs) throws JobRunningException;
    protected abstract void prepare() throws JobRunningException;
    protected abstract void onAllJobsComplete();   
    protected abstract void onJobErroredComplete(List<Job> next);  

    /** 
     * Adds a job to be run. If jobs aren't already being executed it starts job execution.
     * 
     * @param job
     */
    public void addJob(Job job) {

        logger.debug( logTag + " adding job");
        
        synchronized (jobCache) {

            if(maxSize >= 0 && jobCache.size() < maxSize) {
                
                jobCache.add(job);
                
            }
            
            startInternal(false);
             
        }

    }
    
    public void removeJob(Job job) {
        
        synchronized (jobCache) {
            jobCache.remove(job);            
        }
        
    }
    
    public void start(final boolean force) {
        
        //create a new job running thread for each concurrentJob.
        for(int i = 0; i < this.concurrentJobs; i++) {
            startInternal(force);
        }
        
    }
    
    private void startInternal(final boolean force) {
        
        logger.debug(logTag + " starting " + force);
        
        synchronized (jobCache) {
            
            if(waitForBatch && !force) {
                
                if(jobCache.size() < batchSize) {
                    
                    logger.debug(logTag + " size " + jobCache.size() + " < " + batchSize);
                    
                    return;
                    
                }
                
            }
            
            logger.debug(logTag + " starting 2");
            
            if (jobCache.size() > 0 && enabled) {
                
                logger.debug(logTag + " starting 3");
                
                threads.execute(
                        
                    new Runnable() {
    
                        @Override
                        public void run() {
                            
                            logger.debug(logTag + " starting 4");
                                                        
                            List<Job> next = new ArrayList<Job>(); 
                            
                            synchronized(jobCache) {
                                
                                //create a batch
                                for(int batchItemCount = 0; batchItemCount < batchSize; batchItemCount++) {
                                    
                                    final Job possible = jobCache.poll();
                                    
                                    if(possible == null) {
                                        
                                        if(next.size() == 0) {
                                            
                                            logger.debug(logTag + " starting 5");
                                            
                                            onAllJobsComplete(); 
                                            return; //nothing to do
                                            
                                        }
                                        else {
                                            break;
                                        }
                                        
                                    }
                                    else {
                                        next.add(possible);
                                    }
                                    
                                }
                                
                            }
                                                            
                            try {
                                prepare();
                            }
                            catch (JobRunningException e) {
                                
                                logger.error(logTag + " <SequentialJobRunner><1>, COULD NOT PREPARE:", e);
                                
                                synchronized(jobCache) {
                                    
                                    jobCache.addAll(next);
                                                                     
                                    onJobErroredComplete(next);
                                    return;
                                    
                                }
                                
                            }  
                         
                            while(true) {
                                
                                try {
                                    
                                    doJobs(next);
                                    
                                    next.clear();
                                    
                                    if(!enabled) {
                                        
                                        break;
                                        
                                    }
                                    
                                }
                                catch(JobRunningException e) {
                                    
                                    logger.error(logTag + " <SequentialJobRunner><2>, Error:", e);
                                    
                                    synchronized(jobCache) {
                                        
                                        jobCache.addAll(next);
                                                                            
                                        onJobErroredComplete(next); 
                                         
                                    }
                                    break;
                                    
                                }
                                
                                synchronized(jobCache) {
                                    
                                    if(waitForBatch && !force) {
                                        
                                        if(jobCache.size() < batchSize) {
                                            return;
                                        }
                                        
                                    }
                                    
                                    //create a batch
                                    for(int batchItemCount = 0; batchItemCount < batchSize; batchItemCount++) {
                                        
                                        final Job possible = jobCache.poll();
                                        
                                        if(possible == null) {
                                            
                                            if(next.size() == 0) {
                                                
                                                onAllJobsComplete(); 
                                                return; //nothing to do
                                                
                                            }
                                            else {
                                                break;
                                            }
                                            
                                        }
                                        else {
                                            next.add(possible);
                                        }
                                        
                                    }
                                    
                                     
                                }
                                  
                            };
                            
                        }
                        
                    }
                    
                );
                
            } 

        }
        
        logger.debug(logTag + " released");
        
    }

    public List<Job> clearPending() {
        
        synchronized(jobCache) {
            
            final List<Job> returnVals = new ArrayList<Job>();
            
            while(true) {
                
                final Job next = jobCache.poll();
                
                if(next != null) {
                    returnVals.add(next);
                }
                else {
                    break;
                }
                
            }
            
            jobCache.clear();
            
            return returnVals;
            
        }
        
    }
    
    public boolean hasJobs() {
        
        synchronized(jobCache) {            
            return jobCache.size() > 0;
        }
        
    }
    
    public boolean isRunning() {
    
        synchronized(jobCache) {
        	if (hasJobs()) {
        		return true;
        	}
            return threads.running();
        }
        
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}