package llc.ufwa.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

import llc.ufwa.data.exception.JobRunningException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SequentialJobRunner<Job> {
    
    private static final Logger logger = LoggerFactory.getLogger(SequentialJobRunner.class);

    private final Executor worker;
    private boolean running;

    private final Queue<Job> jobCache; 
    private int run = 0;

    public SequentialJobRunner(final Executor worker, Queue<Job> cache) {
      
        this.worker = worker;
        this.jobCache = cache;
        
        start();
        
    }
    
    protected abstract void doJob(Job job) throws JobRunningException;
    protected abstract void prepare() throws JobRunningException;
    protected abstract void onAllJobsComplete();   

    /** 
     * Adds a job to be run. If jobs aren't already being executed it starts job execution.
     * 
     * @param job
     */
    public void addJob(Job job) {

        synchronized (jobCache) {

            jobCache.add(job);

            start();
             
        }

    }
    
    public void start() {
        
        synchronized (jobCache) {
            
            if (!running && jobCache.size() > 0) {
                
                running = true;
                
                worker.execute(
                    new Runnable() {
    
                        @Override
                        public void run() {
                            
                            Job next; 
                            
                            synchronized(jobCache) {
                                
                                run++;
                                
                                next = jobCache.peek();
                                
                                if(next == null) { 
                                    
                                    running = false;                                     
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
                                    
                                    running = false;                                     
                                    onAllJobsComplete();
                                    return;
                                    
                                }
                                
                            }  
                         
                            while(true) {
                                
                                try {
                                    doJob(next);
                                }
                                catch(JobRunningException e) {
                                    
                                    logger.error("<SequentialJobRunner><2>, Error:", e);
                                    
                                    synchronized(jobCache) {
                                         
                                        running = false;                                     
                                        onAllJobsComplete(); 
                                         
                                    }
                                    break;
                                    
                                }
                                
                                synchronized(jobCache) {
                                    
                                    jobCache.poll(); //TODO mmmm wont work if multithreaded. 
                                    
                                    next = jobCache.peek();
                                    
                                    if(next == null) {
                                        
                                        running = false;                                     
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
            return jobCache.size() > 0;
        }
        
    }

    public int getRuns() {
        
        synchronized(jobCache) {
            return run;
        }
        
    }
    
}
