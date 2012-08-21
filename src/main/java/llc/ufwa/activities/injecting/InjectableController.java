package llc.ufwa.activities.injecting;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.concurrency.SerialExecutor;

/**
 * This is the controller to manage displays being injected into it. 
 * 
 * It handles lifecycle of displays.
 * 
 * @author seanwagner
 *
 * @param <T>
 */
public abstract class InjectableController<T extends InjectingDisplay> {
    
    private static final Logger logger = LoggerFactory.getLogger(InjectableController.class);
    
    private final AllDisplays<T> displays;
    private final Class<T> displayClass;
    private final Executor configureRunner;
    
    
    public InjectableController(
        final Class<T> clazz,
        final SerialExecutor configureRunner
     
    ) {
        
        this.configureRunner = configureRunner;        
        this.displayClass = clazz;        
        displays = new AllDisplays<T>(clazz, getClass().getClassLoader());
        
    }

    
    protected abstract void configureDisplay(T display);
    
	@SuppressWarnings("unchecked")
	protected final void addDisplay(final InjectingDisplay display) {
	    
		if(displayClass.isInstance(display)) {
		    
		    displays.addDisplay((T)display);
		    
		    configureRunner.execute(
		        new Runnable() {
        
                    @Override
                    public void run() {
                        configureDisplay((T)display);
                    }
                    
                }
		        
		    );
			
		}
		
	}
	
    protected abstract void onDisplayRemoved();
    
	@SuppressWarnings("unchecked")
    protected final void removeDisplay(InjectingDisplay display) {
	    
	    if(displayClass.isInstance(display)) { 
	        
            displays.removeDisplay((T)display);
            onDisplayRemoved();
            
        }
	    
	}
	
	public T getAllDisplays() {
	    return displays.getAllDisplays();
	}
	
	public boolean hasDisplays() {
	    return displays.hasDisplays();
	}
	
	
}
