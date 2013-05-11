package llc.ufwa.util;

import java.util.concurrent.Executor;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.CallbackFinalizer;
import llc.ufwa.concurrency.WeakCallback;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.loader.ParallelResourceLoader;
import llc.ufwa.data.resource.loader.ResourceEvent;

public class ResourceLoaderUtil {
    
    public static <Key, Value> void callParallelAndRun(
        final Executor runner, 
        final ParallelResourceLoader<Key, Value> loader, 
        final CallbackFinalizer finalizer,
        final Callback<Object, ResourceEvent<Value>> callback, 
        final Key key
    ) throws ResourceException {
        
        loader.getParallel(
            new WeakCallback<Object, ResourceEvent<Value>>(
                new Callback<Object, ResourceEvent<Value>>() {

                    @Override
                    public Object call(final ResourceEvent<Value> value) {
                        
                        runner.execute(
                            new Runnable() {

                                @Override
                                public void run() {
                                    callback.call(value);
                                }
                            }
                        );
                        
                        return null;
                        
                    }
                },
                false,
                finalizer
            ),
            key
        );
        
        
    }

}
