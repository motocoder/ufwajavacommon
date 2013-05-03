package llc.ufwa.data.resource.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;

/**
 *  
 *  DoubleHitParallelResourceLoader is an implementation of ParallelResourceLoader
 *  which is an extension of ResourceLoader. It takes two ParallelResourceLoaders
 *  during construction, a primary and secondary. 
 *  
 *
 */

public class DoubleHitParallelResourceLoader<Key, Value> implements ParallelResourceLoader<Key, Value> {

    private final ParallelResourceLoader<Key, Value> secondary;
    private final ParallelResourceLoader<Key, Value> primary;

    /**
     * 
     * @param primary
     * @param secondary
     */
    
    
    public DoubleHitParallelResourceLoader(
        final ParallelResourceLoader<Key, Value> primary,
        final ParallelResourceLoader<Key, Value> secondary
    ) {
        
        this.primary = primary;
        this.secondary = secondary;
        
    }
    
    /**
     * 
     * If the resource exists, it will return the primary
     * or secondary value associated with the key.
     * 
     * @return boolean
     */
    
    @Override
    public boolean exists(Key key) throws ResourceException {
        return primary.exists(key) || secondary.exists(key);
    }
    
    /**
     * This method returns the secondary ParallelResourceLoader
     * unless it returns a null value in which it would instead 
     * return the primary. 
     * 
     * @return Value
     */

    @Override
    public Value get(Key key) throws ResourceException {
        
        //secondary has priority over primary.
        final Value returnVal;
        
        final Value secondaryVal = secondary.get(key);
        
        if(secondaryVal != null) {
            returnVal = secondaryVal;
        }
        else {
            
            final Value primaryVal = primary.get(key);
            
            returnVal = primaryVal;
            
        }
        
        return returnVal;
        
    }
    
    /**
     * The getAll method returns the secondary List of values
     * unless they are null values in which it would instead 
     * return the primary values. This method performs in a 
     * similar fashion to the get method, except it performs 
     * bulk work. 
     * 
     * 
     * @return List<Value>
     */

    @Override
    public List<Value> getAll(List<Key> keys) throws ResourceException {
        
        //secondary values have priority over primary.
        final Set<Key> uniqueKeys = new HashSet<Key>();
        final Map<Key, Value> values = new HashMap<Key, Value>();
        
        {
            
            final List<Key> firstRequest = new ArrayList<Key>(uniqueKeys);
            
            final List<Value> firstReturn = secondary.getAll(firstRequest);
            
            if(firstReturn.size() != firstRequest.size()) {
                throw new ResourceException("incompatible return list");
            }
            
            for(int i = 0; i < firstReturn.size(); i++) {
                
                final Value val = firstReturn.get(i);
                final Key key = firstRequest.get(i);
                
                if(val != null) {
                    
                    uniqueKeys.remove(key);
                    
                    values.put(key, val);
                    
                }
                
            }
            
        }
        
        if(uniqueKeys.size() > 0) {
            
            final List<Key> secondRequest = new ArrayList<Key>(uniqueKeys);
            
            final List<Value> secondReturn = primary.getAll(secondRequest);
            
            if(secondReturn.size() != secondRequest.size()) {
                throw new ResourceException("incompatible return list");
            }
            
            for(int i = 0; i < secondReturn.size(); i++) {
                
                final Value val = secondReturn.get(i);
                final Key key = secondRequest.get(i);
                
                if(val != null) {
                    
                    uniqueKeys.remove(key);
                    
                    values.put(key, val);
                    
                }
                
            }
            
            
        }
        
        final List<Value> returnVals = new ArrayList<Value>();
        
        for(final Key key : keys) {
            returnVals.add(values.get(key));
        }
        
        
        return returnVals;
        
    }
    
    /**
     * CallbackControl getParallel(
     *  Callback<Object, ResourceEvent<Value>> onComplete, 
     *  final Key key) – onComplete is called with the newly loaded value upon completion of 
     *  the resource loader call. CallbackControl has one method, cancel which allows you to 
     *  cancel the request before it completes.
     * 
     * @return CallbackControl
     */

    @Override
    public CallbackControl getParallel(
        final Callback<Object, ResourceEvent<Value>> onComplete,
        final Key key
    ) throws ResourceException {
        
        final DoubleCallbackControl returnControl = new DoubleCallbackControl();
                
        final CallbackControl control1 = primary.getParallel(
                
            new Callback<Object, ResourceEvent<Value>>() {

                @Override
                public Object call(ResourceEvent<Value> value) {
                    
                    final Object returnVal = onComplete.call(value);
                    
                    try {
                        
                        final CallbackControl control2 = secondary.getParallel(
                                
                            new Callback<Object, ResourceEvent<Value>>() {

                                @Override
                                public Object call(ResourceEvent<Value> value) {                                    
                                    return onComplete.call(value);
                                }
                                
                            },
                            key
                        );
                        
                        returnControl.setSecondary(control2);
                        
                    }
                    catch (ResourceException e) {
                        onComplete.call(new ResourceEvent<Value>(null, e, ResourceEvent.UNKNOWN));
                    }
                    
                    return returnVal;
                    
                }
                
            },
            key
        );
        
        returnControl.setPrimary(control1);
        
        return returnControl;
        
    }
    
    /**
     * CallbackControl existsParallel(
     *  Callback<Object, ResourceEvent<Boolean>> onComplete, 
     *  final Key key) – onComplete is called with the newly loaded value upon completion of 
     *  the resource loader call. CallbackControl has one method, cancel which allows you to 
     *  cancel the request before it completes.
     * 
     * 
     * @return CallbackControl
     */

    @Override
    public CallbackControl existsParallel(
        final Callback<Object, ResourceEvent<Boolean>> onComplete, 
        final Key key
    ) {
        
        final DoubleCallbackControl returnControl = new DoubleCallbackControl();
        
        final CallbackControl control1 = 
            primary.existsParallel(
                
                new Callback<Object, ResourceEvent<Boolean>>() {
    
                    @Override
                    public Object call(ResourceEvent<Boolean> value) {
                        
                        final Object returnVal = onComplete.call(value);
                        
                        final CallbackControl control2 = secondary.existsParallel(
                                
                            new Callback<Object, ResourceEvent<Boolean>>() {
    
                                @Override
                                public Object call(ResourceEvent<Boolean> value) {                                    
                                    return onComplete.call(value);
                                }
                                
                            },
                            key
                        );
                        
                        returnControl.setSecondary(control2);
                        
                        return returnVal;
                        
                    }
                    
                },
                key
            );
        
        returnControl.setPrimary(control1);
        
        return returnControl;
    
    }
    
    /**
     * 
     *  void getAllParallel(final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap) - 
     *  This method works with the same principals as getParallel and existsParallel except it is 
     *  a bulk request. There is no CallbackControl returned with it.
     * 
     * 
     */

    @Override
    public void getAllParallel(
        final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap
    ) throws ResourceException {
        
        //put together a first request.
        final Map<Key, Callback<Object, ResourceEvent<Value>>> firstRequest = new HashMap<Key, Callback<Object, ResourceEvent<Value>>>();
        
        final Set<Key> doneKeys = new HashSet<Key>(); //keys that have completed first request
        
        for(final Map.Entry<Key, Callback<Object, ResourceEvent<Value>>> entry : callbackMap.entrySet()) {
            
            firstRequest.put(
                entry.getKey(),
                new Callback<Object, ResourceEvent<Value>>() {
    
                    @Override
                    public Object call(ResourceEvent<Value> value) {
                        
                        entry.getValue().call(value);
                        
                        doneKeys.add(entry.getKey());
                        
                        if(doneKeys.size() == firstRequest.size()) { //if all first request keys come back do second request.
                            
                            final Map<Key, Callback<Object, ResourceEvent<Value>>> secondRequest = 
                                new HashMap<Key, Callback<Object, ResourceEvent<Value>>>();
                            
                            for(final Map.Entry<Key, Callback<Object, ResourceEvent<Value>>> entry : callbackMap.entrySet()) {
                                
                                final Callback<Object, ResourceEvent<Value>> origCallback = callbackMap.get(entry.getKey());
                                
                                secondRequest.put(entry.getKey(), origCallback);
                                
                            }
                            
                            try {
                                secondary.getAllParallel(secondRequest);
                            }
                            catch (ResourceException e) {
                                
                                for(final Map.Entry<Key, Callback<Object, ResourceEvent<Value>>> entry : callbackMap.entrySet()) {
                                    entry.getValue().call(new ResourceEvent<Value>(null, e, ResourceEvent.UNKNOWN));
                                }
                                
                            }
                            
                        }
                        
                        return null;
                        
                    }
                    
                }
                
            );
                
        }
        
        try {
            primary.getAllParallel(firstRequest);
        }
        catch (ResourceException e) {
            
            for(final Map.Entry<Key, Callback<Object, ResourceEvent<Value>>> entry : callbackMap.entrySet()) {
                entry.getValue().call(new ResourceEvent<Value>(null, e, ResourceEvent.UNKNOWN));
            }
            
        }
        
    }
    
    /**
     * 
     * 
     *
     */
    
    private static class DoubleCallbackControl implements CallbackControl {
  
        
        private CallbackControl primary; 
        private CallbackControl secondary;

        
        @Override
        public synchronized void cancel() {
            
            if(primary != null) {
                primary.cancel();
            }
            
            if(secondary != null) {
                secondary.cancel();
            }
        }

        /**
         * 
         * 
         * @param primary
         */
        
        public synchronized void setPrimary(CallbackControl primary) {
            this.primary = primary;
        }
        
        /**
         * 
         * 
         * @param secondary
         */

        public synchronized void setSecondary(CallbackControl secondary) {
            this.secondary = secondary;
        }
        
    }

}
