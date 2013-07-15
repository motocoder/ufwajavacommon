package llc.ufwa.concurrency;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import llc.ufwa.concurrency.RunnableStates.SequencedRunnable;
import llc.ufwa.data.exception.GarbageCollectedReferenceException;
import llc.ufwa.data.resource.loader.CallbackControl;
import llc.ufwa.data.resource.provider.ResourceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps an executor and limits how many threads can run on it. 
 * 
 * If you add more tasks than it is capable of it cancels previously added tasks.
 * 
 * 
 * @author seanwagner
 *
 */
public interface LimitingExecutorService extends ExecutorService {

//    private static final Logger logger = LoggerFactory.getLogger(LimitingExecutorService.class);
//    
//    private final ExecutorService internal;
//    private final Executor scheduler = Executors.newSingleThreadExecutor();
//    
//    private final RunnableStates states;
//    
//    private final ResourceProvider<Integer> idProvider = new ResourceProvider<Integer>() {
//
//        private int id = 0;
//        
//        @Override
//        public boolean exists() { 
//            return true;
//        }
//
//        @Override
//        public synchronized Integer provide() {
//            return id++;
//        }
//        
//    };
//    
//    /**
//     * 
//     * @param internal
//     * @param limit
//     */
//    private LimitingExecutorService(
//        final ExecutorService internal,
//        final Executor callbackThreads,
//        final int limit
//    ) {
//        
//        this.states = new RunnableStates(callbackThreads, limit, idProvider);
//        this.internal = internal; 
//        
//        scheduler.execute(
//                
//            new Runnable() {
//                
//                @Override
//                public void run() {
//                    
//                    while(true) {
//                        
//                        try {
//                            
//                            cleaner.run();
//                                
//                            final SequencedRunnable next = states.getNext();
//                            
//                            final Future<Void> future = 
//                                    
//                                internal.submit( 
//                                         
//                                    new Callable<Void>() {
//                        
//                                        @Override
//                                        public Void call() throws Exception {
//                                            
//                                            boolean started = false;
//                                            
//                                            started = states.started(next);
//                                            
//                                            if(started) {
//                                                
//                                                try {
//                                                         
//                                                    try {
//                                                        
//                                                        next.getRunnable().run();
//                                                        
//                                                    }
//                                                    catch(Throwable t) {
//                                                        logger.error("ERROR:", t);
//                                                    }
//                                                    
//                                                }
//                                                finally {
//                                                    states.finish(next);
//                                                }
//                                                
//                                            }
//                                            
//                                            return null;
//                                            
//                                        }
//                                        
//                                    }
//                                    
//                                );
//
//                            states.launched(future, next);     
//                            
//                        } 
//                        catch (InterruptedException e) {
//                            
//                            logger.error("FATAL ERROR:", e);
//                            break;
//                        }
//                        
//                    }
//                    
//                }
//                
//            }
//            
//        );
//       
//    }
//    
//    private final Runnable cleaner = new Runnable() {
//        
//        @Override
//        public void run() {
//            states.cancel();
//        }
//        
//    };
//    
//    public void execute(final Runnable command); /*{
//       
//        states.schedule(
//            command,
//            new Callback<Void, Void>() {
//
//                @Override
//                public Void call(Void value) {
//                    
//                    //placeholder
//                    return null;
//                }
//            }
//        );
//        
//        cleaner.run();
//              
//    }*/
    
    CallbackControl execute(final Runnable command, final Callback<Void, Void> onCanceledBeforeStart); //{					
//    	
//         final CallbackControl control = states.schedule(command, onCanceledBeforeStart);
//       
//        cleaner.run();
//        
//        return control;
//        
//    }
//
//    @Override
//    public void shutdown() {
//        internal.shutdown();
//    }
//
//    @Override
//    public List<Runnable> shutdownNow() {
//        return internal.shutdownNow();
//    }
//
//    @Override
//    public boolean isShutdown() {
//        return internal.isShutdown();
//    }
//
//    @Override
//    public boolean isTerminated() {
//        return internal.isTerminated();
//    }
//
//    @Override
//    public boolean awaitTermination(
//        final long timeout,
//        final TimeUnit unit
//    ) throws InterruptedException {
//        return internal.awaitTermination(timeout, unit);
//    }
//
//    @Override
//    public <T> Future<T> submit(final Callable<T> task) {
//        throw new RuntimeException("Not supported yet");        
//    }
//
//    @Override
//    public <T> Future<T> submit(final Runnable task, final T result) {
//        throw new RuntimeException("not supported yet");
//    }
//
//    @Override
//    public Future<?> submit(Runnable task) {
//        throw new RuntimeException("not supported yet");
//    }
    
    
    
}
