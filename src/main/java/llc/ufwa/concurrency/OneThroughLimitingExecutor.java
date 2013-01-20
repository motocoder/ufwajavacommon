package llc.ufwa.concurrency;

import java.util.concurrent.Executor;

public class OneThroughLimitingExecutor implements Executor {
     
    private final Object lock = new Object();
    private final LimitingExecutorService limiting;
    private final Executor threads;
    
    private volatile boolean running;
    
    public OneThroughLimitingExecutor(
        final Executor executor,
        final LimitingExecutorService limited
    ) {
        
        this.limiting = limited;
        this.threads = executor;
        
    }

    @Override
    public void execute(final Runnable command) {
        
        //limit one waiting
        limiting.execute(
                
            new Runnable() {

                @Override
                public void run() {
                        
                    synchronized(lock) {
                        
                        while(running) {
                            
                            try {
                                lock.wait(100);
                            }
                            catch(InterruptedException e) {
                                return;
                            }
                            
                        }
                        
                        threads.execute(
                                
                            new Runnable() {

                                @Override
                                public void run() {
                                    
                                    
                                    
                                    try {
                                        
                                        command.run();
                                        
                                    }
                                    finally {
                                        
                                        synchronized(lock) {
                                            
                                            running = false;
                                        
                                            lock.notify();
                                            
                                        }
                                        
                                    }
                                    
                                }
                                
                            }
                            
                        );
                        
                        running = true;
                        
                    }
                                    
                }
                
            }
            
        );
        
    }

}
