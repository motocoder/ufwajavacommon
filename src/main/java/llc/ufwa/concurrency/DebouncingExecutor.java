package llc.ufwa.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import llc.ufwa.util.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebouncingExecutor implements Executor {
	
	private static final Logger logger = LoggerFactory.getLogger(DebouncingExecutor.class);
        
    private final List<Runnable> toExecute = new ArrayList<Runnable>();
    
    private final Debouncer debouncer;
    
    public DebouncingExecutor(
        final Executor rootExecutor,
        final Executor worker,
        final long debounceTime
    ) {
    	    	
    	debouncer = new Debouncer(new Callback<Object, Object>() {

            @Override
            public boolean call(Object source, Object value) {
                
                synchronized(toExecute) {
                    
                    final StopWatch clock = new StopWatch();
                    clock.start();
                    
                    final List<Runnable> executing = new ArrayList<Runnable>(toExecute);
                    toExecute.clear();
                    
                    rootExecutor.execute(
                        new Runnable() {

	                        @Override
	                        public void run() {
	                            	                            
	                            for(final Runnable runnable : executing) {
	                                
	                            	final StopWatch watch = new StopWatch();
	                            	watch.start();
	                            	
	                                try {
	                                    runnable.run();
	                                }
	                                catch(Exception e) {
	                                    throw new RuntimeException(e);
	                                }
	                                
	                                if(watch.getTime() > 500) {
	                                	logger.warn("UI THREAD WAS BLOCKED FOR: " + watch.getTime() + "ms");
	                                }
	                                
	                            }
	                            
	                        }
	                    }
                    );
                   
                }
                return false;
            }
        },
        worker,
        debounceTime
    );
    }
	
	@Override 
    public void execute(Runnable command) {
        
        synchronized(toExecute) {
            toExecute.add(command);
        }
        
        debouncer.signal();
        
    }

}
