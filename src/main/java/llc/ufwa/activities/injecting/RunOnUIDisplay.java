package llc.ufwa.activities.injecting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This wraps a class with a WeakInterface and runs it on the provided executor.
 * 
 * @author seanwagner
 *
 * @param <Interface>
 */
public class RunOnUIDisplay<Interface extends InjectingDisplay> {
    
    private static final Logger logger = LoggerFactory.getLogger(RunOnUIDisplay.class);
    
    private final Interface weakInterface;
    
    @SuppressWarnings("unchecked")
    public RunOnUIDisplay(
        final Executor runner,
        final Interface internal,
        final Class<Interface> interfaceClazz
    ) {
        
        if(internal == null) {
            throw new NullPointerException("Class cannot be null");
        }
        
        final ClassLoader classloader = this.getClass().getClassLoader();
        
        InvocationHandler handler = new MyInvocationHandler(internal, runner);
        
        weakInterface = (Interface) Proxy.newProxyInstance(classloader, new Class<?>[] { interfaceClazz }, handler);
        
    }
    
    public Interface getWrapped() {
        return weakInterface;        
    }
    
    private class MyInvocationHandler implements InvocationHandler {

        private final Executor runner;
        private final Interface internal;
        
        private MyInvocationHandler(Interface internal, Executor runner) {
            this.runner = runner;
            this.internal = internal;
        }
        
        @Override
        public Object invoke(
            final Object proxy,
            final Method method,
            final Object[] args
        ) throws Throwable {
            
            runner.execute(
                new Runnable() {

                    @Override
                    public void run() {
                                        
                        try {
                            method.invoke(internal, args);
                        } 
                        catch (IllegalArgumentException e) {
                            logger.error("ERROR:", e);
                        } 
                        catch (IllegalAccessException e) {
                            logger.error("ERROR:", e);
                        }
                        catch (InvocationTargetException e) {
                            logger.error("ERROR:", e);
                        }   

                    }
                    
                }
                
            );
            
            Class<?> returnType = method.getReturnType();
            
            if(returnType.equals(int.class)) {
                return 0;
            }
            else if(method.getName().equals("toString") && returnType.equals(String.class)) {
                return "RunOnUIDisplayProxy.class";
            }
            if(returnType.equals(boolean.class)) {
                return true;
            }
            if(returnType.equals(long.class)) {
                return 0L;
            }
            else {
                return null;
            }
            
        }
        
    };

}
