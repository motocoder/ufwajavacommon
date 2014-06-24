package llc.ufwa.concurrency;

import java.util.HashSet;
import java.util.Set;

public class CallbackPublisher<Value> {
    
    private final Set<Callback<Void, Value>> callbacks = new HashSet<Callback<Void, Value>>();
    
    public synchronized void addCallback(final Callback<Void, Value> callback) {
        callbacks.add(callback);
    }
    
    public synchronized void clear() {
        callbacks.clear();
    }
    
    public void publish(final Value value) {
        
        for(final Callback<Void, Value> callback : callbacks) {
            callback.call(value);
        }
        
    }
    

}
