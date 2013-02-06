package llc.ufwa.concurrency;

import java.util.LinkedList;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps an executor and limits the runnables and queueables to given constructor values.  
 * 
 * @author michaelporter
 *
 */

public class MultiRunAndQueueExecutor implements Executor {
    
    private static final Logger logger = LoggerFactory.getLogger(OneThroughLimitingExecutor.class); 

    private final Executor threads;
    private final LinkedList<Runnable> runList = new LinkedList<Runnable>();
    private final LinkedList<Runnable> queueList = new LinkedList<Runnable>();
    private final int runners;
    private final int queuers; 
    
    
    public MultiRunAndQueueExecutor(
        final Executor executor,
        final int runners,
        final int queuers
    ) {
        
        this.threads = executor;
        this.runners = runners;
        this.queuers = queuers;
        
    }
    
    private void startIfAvailable() {

        synchronized (runList) {

            if (runList.size() > 0) {

                threads.execute(

                        new Runnable() {

                            @Override
                            public void run() {

                                final Runnable toRun;

                                if (runList.size() != 0) {
                                    toRun = runList.getFirst();
                                } else if (runList.size() == 0 && queueList.size() > 0) {
                                    
                                    runList.add(queueList.pop());
                                    toRun = runList.getFirst();
                                    
                                } else {
                                    toRun = null;
                                }

                                if (toRun != null) {

                                    try {

                                        toRun.run();
                                        logger.debug("if runList.size() >0  push and call startIfAvailable just ran");

                                    } finally {

                                        logger.debug("finally rerun startIfAvailable after pop run... before");
                                        runList.removeFirst();
                                        logger.debug("finally rerun startIfAvailable after pop run... after");

                                        if (runList.size() < runners && queueList.size() > 0) {
                                            runList.add(queueList.pop());
                                        }

                                        startIfAvailable();

                                    }

                                }

                            }

                        }

                    );
              }
          }

    }

    @Override
    public void execute(final Runnable command) {

        synchronized(runList) {
            synchronized(queueList) {
           
                if(runList.size() >= 0 && runList.size() < runners) {

                    runList.add(command);
                    startIfAvailable();
                    logger.debug("");

                }
                else if(runList.size() >= runners) {
                
                    if(queueList.size() < queuers) {

                        queueList.add(command);
                        logger.debug("");
                        
                    }
                    else if(queueList.size() == queuers) {
                    
                        queueList.removeLast();
                        queueList.add(command);
                    
                    }
                }
                if(runList.size() < runners && queueList.size() > 0) {
                
                    runList.add(queueList.pop());
                    startIfAvailable();
                
                }
            }
        }
    }
    
}

