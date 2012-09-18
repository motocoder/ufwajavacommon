package llc.ufwa.concurrency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Just manages a set of callbacks currently out and has a hard reference to them.
 * 
 * This is used when using weak callbacks so they aren't garbage collected until they should be.
 * 
 * Basically this allows the caller to IOC on something IOC so that it can control
 * when they get garbage collected even though it has passed control to the loader.
 * 
 * Crazy shit huh.
 * 
 * @author seanwagner
 *
 */
public class DefaultCallbackFinalizer implements CallbackFinalizer {

    @SuppressWarnings("rawtypes")
    private Set<Callback> callbacksOut = Collections.synchronizedSet(new HashSet<Callback>());
    
    @Override
    public void onStart(Callback<?, ?> callback) {
        callbacksOut.add(callback);
        
    }

    @Override
    public void onFinished(Callback<?, ?> callback) {
        callbacksOut.remove(callback);            
    }


}
