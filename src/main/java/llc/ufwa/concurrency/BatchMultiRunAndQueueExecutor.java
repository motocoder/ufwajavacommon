package llc.ufwa.concurrency;

import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BatchMultiRunAndQueueExecutor implements Executor {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchMultiRunAndQueueExecutor.class); 

    private final Executor threads;
    private final ConcurrentHashMap<Long, Queue<Runnable>> queueMap;
    private final ConcurrentHashMap<Long, Integer> queueRuns;

	private int runs;
	private boolean running;
	
	private final boolean SHOW_QUEUE_RUNS = false;
    
    public BatchMultiRunAndQueueExecutor(
        final Executor executor,
        final int runners
    ) {
        
        this.threads = executor;
        this.queueMap = new ConcurrentHashMap<Long, Queue<Runnable>>(runners);
        this.queueRuns = new ConcurrentHashMap<Long, Integer>(runners);
        
    }
    
    public boolean running() {
    	
    	int totalToRun = 0;
    	
    	for (Entry<Long, Queue<Runnable>> o : queueMap.entrySet()) {
    		totalToRun += o.getValue().size();
    	}
    	
    	return ((totalToRun != 0) && (running));
    	
    }

    public void executeOnSpecificThread(final Runnable command, final long threadID) {
        
    	Queue<Runnable> list = queueMap.get(threadID);
    	
    	if (list == null) {
    		
    	    final Queue<Runnable> value = new ConcurrentLinkedQueue<Runnable>();
    	    
    	    list = queueMap.putIfAbsent(threadID, value);
    	    
    	    if (list == null) {
    	    	list = value;
    	    }
    	    
    	}
    	
    	list.add(command);
    	
    	runList(list, threadID);
    	
    }

	private void runList(final Queue<Runnable> list, final long threadID) {

		final Runnable runner = new Runnable() {

			@Override
			public void run() {

				setRunning(true);

				synchronized(list) { // synchronize on the list inside the concurrenthashmap since this does not need to be concurrent
					
					while (!list.isEmpty()) { // run the first task in the linkedlist and continue until the list is empty
						
						Runnable task = null;
						
						try {
							task = list.poll();
						}
						catch (NoSuchElementException e) {
							logger.warn("list was empty when runner accessed" + " - " + list.size() + ", " + list.isEmpty());
							break;
						}
						
						if (task != null) {
		
							try {
								
								task.run();

								if (SHOW_QUEUE_RUNS) {
									
									Integer list = queueRuns.get(threadID);
							    	
							    	if (list == null) {
							    		
							    	    final Integer value = new Integer(0);
							    	    
							    	    list = queueRuns.putIfAbsent(threadID, value);
							    	    
							    	    if (list == null) {
							    	    	list = value;
							    	    }
							    	    
							    	}
							    	
							    	list++;
									
							    	queueRuns.put(threadID, list);
							    	
								}
								
								addRun();
								
							}
							catch (Exception ex) {
								ex.printStackTrace();
							}
		
						}
						
					}
					
				}
				
				setRunning(false);
		    	
			}
			
		};
		
    	threads.execute(runner);
		
	}
	
	public synchronized void addRun() {
	    this.runs++;
	}
    
	public synchronized int getRuns() {
		
		if (SHOW_QUEUE_RUNS) {
			for (Entry<Long, Integer> o : queueRuns.entrySet()) {
	    		logger.debug(o.getKey() + " --- " + o.getValue());
	    	}
		}
		
	    return this.runs;
	}

	@Override
    public void execute(final Runnable command) {
        threads.execute(command);
    }
	
	private synchronized void setRunning(final boolean running) {
		this.running = running;
	}
	
}

