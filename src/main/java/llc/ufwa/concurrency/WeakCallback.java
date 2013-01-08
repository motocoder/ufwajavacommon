package llc.ufwa.concurrency;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeakCallback<Caller, Value> implements Callback<Caller, Value>{

    private static final Logger logger = LoggerFactory.getLogger(WeakCallback.class);
    
    private final WeakReference<Callback<Caller, Value>> internalWeak;
    private final boolean throwExceptionOnNotThere;
    private final CallbackFinalizer finalizer;
    
    public WeakCallback(
        final Callback<Caller, Value> internal,
        final boolean throwExceptionOnNotThere,
        CallbackFinalizer finalizer) {
        
        this.finalizer = finalizer;
        finalizer.onStart(internal);
        this.throwExceptionOnNotThere = throwExceptionOnNotThere;
        this.internalWeak = new WeakReference<Callback<Caller, Value>>(internal);
        
    }
    @Override
    public boolean call(Caller source, Value value) {
        
        final Callback<Caller, Value> internal = internalWeak.get();
        
        if(internal != null) {
            
            internal.call(source, value);
            finalizer.onFinished(internal);
            
        }
        else {
            
            if(throwExceptionOnNotThere) {
                throw new RuntimeException("Callback was garbage collected");
            }
            else {
                logger.warn("weakcallback was garbage collected");
            }
            
        }
        
        return false;
    }

}
