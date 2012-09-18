package llc.ufwa.convenience;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import llc.ufwa.data.exception.GarbageCollectedReferenceException;

public class WeakInterface<Interface> {
    
    private final Interface weakInterface;
    
    @SuppressWarnings("unchecked")
    public WeakInterface(Interface internal, Class<Interface> interfaceClass) throws GarbageCollectedReferenceException {
        
        if(internal == null) {
            throw new NullPointerException("Class cannot be null");
        }
        
        final WeakReference<Interface> weakInternal = new WeakReference<Interface>(internal);
        
        final ClassLoader classloader = this.getClass().getClassLoader();
        
        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(
                final Object proxy,
                final Method method,
                final Object[] args
            ) throws Throwable {
                
                final Interface internal = weakInternal.get();
                
                if(internal == null) {
                    throw new GarbageCollectedReferenceException();
                }
                
                return method.invoke(internal, args);
                
            }
            
        };
        
        weakInterface = (Interface) Proxy.newProxyInstance(classloader, new Class[] { interfaceClass }, handler);
        
    }
    
    public Interface getWeakInterface() {
        
        return weakInterface;
        
    }

}
