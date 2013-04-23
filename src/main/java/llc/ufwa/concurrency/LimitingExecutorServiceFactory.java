package llc.ufwa.concurrency;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.concurrency.RunnableStates.SequencedRunnable;
import llc.ufwa.data.resource.loader.CallbackControl;
import llc.ufwa.data.resource.provider.ResourceProvider;

public class LimitingExecutorServiceFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(LimitingExecutorServiceFactory.class);
    
    public static LimitingExecutorService createExecutorService(
        final ExecutorService internal,
        final Executor callbackThreads,
        final int limit
    ) {
        
        final Executor scheduler = Executors.newSingleThreadExecutor();
        final ResourceProvider<Integer> idProvider = new ResourceProvider<Integer>() {

            private int id = 0;
            
            @Override
            public boolean exists() { 
                return true;
            }

            @Override
            public synchronized Integer provide() {
                return id++;
            }
            
        };
        
        final RunnableStates states = new RunnableStates(callbackThreads, limit, idProvider);
        
        final Runnable cleaner = new Runnable() {
            
            @Override
            public void run() {
                states.cancel();
            }
            
        };
        
        scheduler.execute(
                
            new Runnable() {
                
                @Override
                public void run() {
                    
                    while(true) {
                        
                        try {
                            
                            cleaner.run();
                                
                            final SequencedRunnable next = states.getNext();
                            
                            final Future<Void> future = 
                                    
                                internal.submit( 
                                         
                                    new Callable<Void>() {
                        
                                        @Override
                                        public Void call() throws Exception {
                                            
                                            boolean started = false;
                                            
                                            started = states.started(next);
                                            
                                            if(started) {
                                                
                                                try {
                                                         
                                                    try {
                                                        
                                                        next.getRunnable().run();
                                                        
                                                    }
                                                    catch(Throwable t) {
                                                        logger.error("ERROR:", t);
                                                    }
                                                    
                                                }
                                                finally {
                                                    states.finish(next);
                                                }
                                                
                                            }
                                            
                                            return null;
                                            
                                        }
                                        
                                    }
                                    
                                );

                            states.launched(future, next);     
                            
                        } 
                        catch (InterruptedException e) {
                            
                            logger.error("FATAL ERROR:", e);
                            break;
                        }
                        
                    }
                    
                }
                
            }
            
        );
        
        final ClassLoader classloader = LimitingExecutorService.class.getClassLoader();
        
        InvocationHandler handler = new InvocationHandler() {

            @SuppressWarnings("unchecked")
            @Override
            public Object invoke(
                final Object proxy,
                final Method method,
                final Object[] args
            ) throws Throwable {

                final Class<?>[] paramTypes = method.getParameterTypes();
                
                final Object returnVal;
                
                if(method.getDeclaringClass().equals(Executor.class) && paramTypes.length == 1 && paramTypes[0].equals(Runnable.class)) {
                    
                    states.schedule(
                        (Runnable)args[0],
                        new Callback<Void, Void>() {

                            @Override
                            public Void call(Void value) {
                                
                                //placeholder
                                return null;
                            }
                        }
                    );
                    
                    cleaner.run();
                    
                    returnVal = null;
                    
                }
                else if(
                    method.getDeclaringClass().equals(LimitingExecutorService.class)
                    && paramTypes.length == 2 
                    && paramTypes[0].equals(Runnable.class)
                    && paramTypes[1].equals(Callback.class)
                    && method.getReturnType().equals(CallbackControl.class)
                ) {
                    
                    final CallbackControl control = states.schedule((Runnable)args[0], (Callback<Void, Void>)args[1]);
                    
                    cleaner.run();
                    
                    returnVal = control;
                    
                }
                else {
                    returnVal = method.invoke(internal, args);
                }
                    
                return returnVal;
                
            }
            
        };
        
        return (LimitingExecutorService) Proxy.newProxyInstance(classloader, new Class[] { LimitingExecutorService.class }, handler);
        
    }

}
