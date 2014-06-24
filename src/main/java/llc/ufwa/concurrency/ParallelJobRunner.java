package llc.ufwa.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

import llc.ufwa.data.exception.JobRunningException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ParallelJobRunner<Job> {
    
    private static final Logger logger = LoggerFactory.getLogger(ParallelJobRunner.class);

    private final Queue<Job> jobCache; 
    private int run = 0;
    private final int maxSize;

    private boolean enabled = true;

    private final MultiRunAndQueueExecutor threads;

    public ParallelJobRunner(
        final Queue<Job> cache, 
        final int concurrentJobs,
        final int maxSize,
        final Executor bulkThreads
    ) {
      
        this.threads = new MultiRunAndQueueExecutor(bulkThreads, concurrentJobs, maxSize);
        this.maxSize = maxSize;
        this.jobCache = cache;
        
        start();
        
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
                                                        
                            Job next; 
                            
                            synchronized(jobCache) {
                                
                                run++;
                                
                                next = jobCache.poll();
                                
                                if(next == null) { 
                                                               
                                    onAllJobsComplete(); 
                                    return; //nothing to do
                                     
                                }
                                 
                            }
                                                            
                            try {
                                prepare();
                            }
                            catch (JobRunningException e) {
                                
                                logger.error("<SequentialJobRunner><1>, COULD NOT PREPARE:", e);
                                
                                synchronized(jobCache) {
                                    
                                    jobCache.add(next);
                                                                     
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
                                        
                                        jobCache.add(next);
                                                                            
                                        onJobErroredComplete(next); 
                                         
                                    }
                                    break;
                                    
                                }
                                
                                synchronized(jobCache) {
                                    
                                    next = jobCache.peek();
                                    
                                    if(next == null) {
                                                                          
                                        onAllJobsComplete();
                                        break;
                                        
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

    public int getRuns() {
        
        synchronized(jobCache) {
            return run;
        }
        
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}
