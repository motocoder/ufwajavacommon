package llc.ufwa.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

import llc.ufwa.data.exception.JobRunningException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BatchedJobRunner<Job> {
    
    private static final Logger logger = LoggerFactory.getLogger(ParallelJobRunner.class);

    private final Queue<Job> jobCache; 
    private final int maxSize;

    private boolean enabled = true;

    private final MultiRunAndQueueExecutor threads;

    private final int batchSize;

    /**
     * 
     * @param cache
     * @param concurrentJobs
     * @param maxSize
     * @param batchSize
     * @param bulkThreads
     */
    public BatchedJobRunner(
        final Queue<Job> cache, 
        final int concurrentJobs,
        final int maxSize,
        final int batchSize,
        final Executor bulkThreads
    ) {
      
        this.threads = new MultiRunAndQueueExecutor(bulkThreads, concurrentJobs, maxSize);
        this.maxSize = maxSize;
        this.batchSize = batchSize;
        this.jobCache = cache;
        
        start();
        
    }
    
    protected abstract void doJob(List<Job> jobs) throws JobRunningException;
    protected abstract void prepare() throws JobRunningException;
    protected abstract void onAllJobsComplete();   
    protected abstract void onJobErroredComplete(List<Job> next);  

    /** 
     * Adds a job to be run. If jobs aren't already being executed it starts job execution.
     * 
     * @param job
     */
    public void addJob(Job job) {

        synchronized (jobCache) {

            if(maxSize >= 0 && jobCache.size() < maxSize) {
                
                jobCache.add(job);
                
            }
            
            start();
             
        }

    }
    
    public void removeJob(Job job) {
        
        synchronized (jobCache) {
            jobCache.remove(job);            
        }
        
    }
    
    public void start() {
        
        synchronized (jobCache) {
            
            if (jobCache.size() > 0 && enabled) {
                
                threads.execute(
                        
                    new Runnable() {
    
                        @Override
                        public void run() {
                                                        
                            List<Job> next = new ArrayList<Job>(); 
                            
                            synchronized(jobCache) {
                                
                                //create a batch
                                for(int batchItemCount = 0; batchItemCount < batchSize; batchItemCount++) {
                                    
                                    final Job possible = jobCache.poll();
                                    
                                    if(possible == null) {
                                        
                                        if(next.size() == 0) {
                                            
                                            onAllJobsComplete(); 
                                            return; //nothing to do
                                            
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
                                
                                logger.error("<SequentialJobRunner><1>, COULD NOT PREPARE:", e);
                                
                                synchronized(jobCache) {
                                    
                                    jobCache.addAll(next);
                                                                     
                                    onJobErroredComplete(next);
                                    return;
                                    
                                }
                                
                            }  
                         
                            while(true) {
                                
                                try {
                                    
                                    doJob(next);
                                    
                                    if(!enabled) {
                                        
                                        synchronized(jobCache) {
                                                                         
                                            onAllJobsComplete(); 
                                            
                                        }
                                        
                                        break;
                                        
                                    }
                                    
                                }
                                catch(JobRunningException e) {
                                    
                                    logger.error("<SequentialJobRunner><2>, Error:", e);
                                    
                                    synchronized(jobCache) {
                                        
                                        jobCache.addAll(next);
                                                                            
                                        onJobErroredComplete(next); 
                                         
                                    }
                                    break;
                                    
                                }
                                
                                synchronized(jobCache) {
                                    
                                    //create a batch
                                    for(int batchItemCount = 0; batchItemCount < batchSize; batchItemCount++) {
                                        
                                        final Job possible = jobCache.poll();
                                        
                                        if(possible == null) {
                                            
                                            if(next.size() == 0) {
                                                
                                                onAllJobsComplete(); 
                                                return; //nothing to do
                                                
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
            return threads.running();
        }
        
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}