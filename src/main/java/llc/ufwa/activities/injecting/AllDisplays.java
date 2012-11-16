package llc.ufwa.activities.injecting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a wrapper for handling a collection of displays. It provides an
 * interface implemented using reflection. Due to the fact method calls may going to
 * multiple display interfaces you cannot have your display return
 * any values. Thus would be a violation of the Injecting framework anyway.
 * 
 * @author seanwagner
 *
 * @param <Display>
 */

public class AllDisplays<Display extends InjectingDisplay> {
    
    private final Set<Display> displays = new HashSet<Display>();
    private final Display internalDisplay;
    
    /**
     * 
     * @param clazz - class of your displays to wrap.
     */
    @SuppressWarnings("unchecked")
    public AllDisplays(Class<Display> clazz, ClassLoader classloader) {
        
        if(clazz == null) {
            throw new NullPointerException("Class cannot be null");
        }
        
        final InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(
                final Object proxy,
                final Method method,
                final Object[] args
            ) throws Throwable {
                
                synchronized(displays) {
                    
                    for(final Display display : displays) {
                        method.invoke(display, args);
                    }
                    
                }
                
                return null; // cannot return a value, multiple targets are being called.
                
            }
            
        };
        
        internalDisplay = (Display) Proxy.newProxyInstance(classloader, new Class[] { clazz }, handler);
        
    }
    
    /**
     * Adds a display to the collection of displays
     * 
     * @param display
     */
    public void addDisplay(Display display) {
        
        synchronized(displays) {
            displays.add(display);
        }
        
    }
    
    /**
     * Adds a display to the collection of displays
     * 
     * @param display
     */
    public void removeDisplay(Display display) {
        
        synchronized(displays) {
            displays.remove(display);
        }
        
    }
    
    /**
     * Adds a display to the collection of displays
     * 
     * @return
     */
    public Display getAllDisplays() {
        return internalDisplay;        
    }
    
    public boolean hasDisplays() {
        
        final boolean returnVal;
        
        synchronized(displays) {
            
            if(displays.size() == 0) {
                returnVal = false;
            }
            else {
                returnVal = true;
            }
            
        }
        
        return returnVal;
        
    }

}
