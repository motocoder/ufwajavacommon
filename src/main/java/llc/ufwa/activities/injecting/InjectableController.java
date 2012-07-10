package llc.ufwa.activities.injecting;

import java.util.concurrent.Executor;

import llc.ufwa.concurrency.SerialExecutor;
import llc.ufwa.convenience.WeakInterface;

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
	
    private final AllDisplays<T> displays;
    private final Class<T> displayClass;
    private final Executor configureRunner;
    
    /**
     * 
     * @param clazz
     * @param configureRunner - must be single thread executors...
     */
    public InjectableController(
        final Class<T> clazz,
        final SerialExecutor configureRunner
    ) {
        
        this.configureRunner = configureRunner;        
        this.displayClass = clazz;        
        displays = new AllDisplays<T>(clazz);
        
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
        }
	    
	    onDisplayRemoved();

	}
	
	protected T getAllDisplays() {
	    return displays.getAllDisplays();
	}
	
	protected boolean hasDisplays() {
	    return displays.hasDisplays();
	}
	
}
