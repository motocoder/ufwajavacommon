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
        
        if(finalizer != null) {
            finalizer.onStart(internal);
        }
        this.throwExceptionOnNotThere = throwExceptionOnNotThere;
        this.internalWeak = new WeakReference<Callback<Caller, Value>>(internal);
        
    }
    @Override
    public Caller call(Value value) {
        
        final Callback<Caller, Value> internal = internalWeak.get();
        
        final Caller returnVal; 
        
        if(internal != null) {
            
            returnVal = internal.call(value);
            
            if(finalizer != null) {
                finalizer.onFinished(internal);    
            }
            
        }
        else {
            
            if(throwExceptionOnNotThere) {
                throw new RuntimeException("<WeakCallback><1>, Callback was garbage collected");
            }
            else {
                logger.warn("weakcallback was garbage collected");
            }
            
            returnVal = null;
            
        }
        
        return returnVal;
        
    }

}
