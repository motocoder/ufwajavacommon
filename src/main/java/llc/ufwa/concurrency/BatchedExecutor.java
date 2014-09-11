package llc.ufwa.concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BatchedExecutor implements Executor {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchedExecutor.class); 

    private final Executor threads;
    private final Map<Long, Queue<Runnable>> queueMap;
    private final Map<Long, Boolean> threadRunning = new HashMap<Long, Boolean>();

	private int runs;
	private boolean running;
    
    public BatchedExecutor(
        final Executor executor
    ) {
        
        this.threads = executor;
        this.queueMap = new HashMap<Long, Queue<Runnable>>();
        
    }
    
    public boolean running() {
    	
    	int totalToRun = 0;
    	
    	for (Entry<Long, Queue<Runnable>> o : queueMap.entrySet()) {
    		totalToRun += o.getValue().size();
    	}
    	
    	return ((totalToRun != 0) && (running));
    	
    }

    public void executeOnSpecificThread(final Runnable command, final long threadID) {
        
        if(command == null) {
            throw new NullPointerException("Command cannot be null");
        }
        
        Queue<Runnable> list;
        
        //add command to list and get the list then run it
        synchronized(queueMap) {
            
        	list = queueMap.get(threadID);
        	
        	if(list == null) {
        		
        	    list = new ConcurrentLinkedQueue<Runnable>();
        	    
        	    queueMap.put(threadID, list);
        	    
        	}
        	
        	synchronized(list) {
        	
        	    list.add(command);
        	
            	synchronized(threadRunning) {
                    
                    Boolean threadIsRunning = threadRunning.get(threadID);
                    
                    //Thread is already running
                    if(threadIsRunning != null && threadIsRunning == true) {
                        return;
                    }
                    
                }
        	
        	}
        	
        }
                
        runList(threadID);
    	
    }

	private void runList(final long threadID) {

		final Runnable runner = new Runnable() {

			@Override
			public void run() {
			    
			    final Queue<Runnable> list;

			    synchronized(queueMap) {
			        
			        list = queueMap.get(threadID);
			        
			        if(list == null) {
			            return;
			        }
			        
			        synchronized(threadRunning) {
		                
		                Boolean threadIsRunning = threadRunning.get(threadID);
		                
		                //Thread is already running
		                if(threadIsRunning != null && threadIsRunning == true) {
		                    return;
		                }
		                else {
		                    threadRunning.put(threadID, true);
		                }
		                
		            }
			        
			    }
			    
			    while(true) {			        
			        
		            //grab a task out of the list of things to do.
			        Runnable task = null;
				
    				synchronized(list) {
						
    				    if(list.size() > 0) {
							task = list.poll();    						
    				    }
    				    else {
    				        
    				        //if nothing to run, turn off the thread
    				        synchronized(threadRunning) {
                                
                                threadRunning.put(threadID, false);
                                break;
                                
                            }
    				        
    				    }
						
    				}
	
					try {
						
						task.run();
						
						addRun();
						
					}
					catch (Exception ex) {
					    logger.error("ERROR RUNNING JOB", ex);
					}
									
					
			    }
				
			}
			
		};
		
    	threads.execute(runner);
		
	}
	
	public synchronized void addRun() {
	    this.runs++;
	}
    
	public synchronized int getRuns() {
		
	    return this.runs;
	}

	@Override
    public void execute(final Runnable command) {
        threads.execute(command);
    }
	
	
}

