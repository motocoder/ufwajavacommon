package llc.ufwa.concurrency;

import llc.ufwa.concurrency.Callback;

/**
 * Finalizer to wrap callback. Mainly used for keeping a solid reference on a callback with a weak reference.
 * 
 * You need a solid reference on a weak reference to allow the context to release itself on proper lifecycle.
 * 
 * AppContext ---- **WEAK**
 *                      \
 * ActivityContext ---- Callback
 * -----------------------------------------------------
 * AppContext --------------- **WEAK**
 *                                 \
 * ActivityContext -- /break/ -- Callback
 * 
 * AppContext ---------------------------------- **WEAK**
 *                                                   \
 * ActivityContext(GarbageCollected) -- /break/ -- Callback(GarbageCollected)
 * 
 * 
 * @author seanwagner
 *
 */
public interface CallbackFinalizer {
    
    void onStart(Callback<?, ?> callback);
    void onFinished(Callback<?, ?> callback);

}
