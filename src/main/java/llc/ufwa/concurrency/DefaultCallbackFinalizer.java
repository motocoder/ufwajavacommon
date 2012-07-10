package llc.ufwa.concurrency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Just manages a set of callbacks currently out and has a hard reference to them.
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
