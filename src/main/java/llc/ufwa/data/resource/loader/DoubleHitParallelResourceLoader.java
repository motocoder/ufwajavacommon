package llc.ufwa.data.resource.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;

public class DoubleHitParallelResourceLoader<Key, Value> implements ParallelResourceLoader<Key, Value> {

    private final ParallelResourceLoader<Key, Value> secondary;
    private final ParallelResourceLoader<Key, Value> primary;

    public DoubleHitParallelResourceLoader(
        final ParallelResourceLoader<Key, Value> primary,
        final ParallelResourceLoader<Key, Value> secondary
    ) {
        
        this.primary = primary;
        this.secondary = secondary;
        
    }
    
    @Override
    public boolean exists(Key key) throws ResourceException {
        return primary.exists(key) || secondary.exists(key);
    }

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

    @Override
    public void getParallel(
        final Callback<Object, ResourceEvent<Value>> onComplete,
        final Key key
    ) throws ResourceException {
        
        primary.getParallel(
                
            new Callback<Object, ResourceEvent<Value>>() {

                @Override
                public Object call(ResourceEvent<Value> value) {
                    
                    final Object returnVal = onComplete.call(value);
                    
                    try {
                        
                        secondary.getParallel(
                                
                            new Callback<Object, ResourceEvent<Value>>() {

                                @Override
                                public Object call(ResourceEvent<Value> value) {                                    
                                    return onComplete.call(value);
                                }
                                
                            },
                            key
                        );
                        
                    }
                    catch (ResourceException e) {
                        onComplete.call(new ResourceEvent<Value>(null, e, ResourceEvent.UNKNOWN));
                    }
                    
                    return returnVal;
                    
                }
                
            },
            key
        );
        
    }

    @Override
    public void existsParallel(
        final Callback<Object, ResourceEvent<Boolean>> onComplete, 
        final Key key
    ) {
        
        primary.existsParallel(
                
            new Callback<Object, ResourceEvent<Boolean>>() {

                @Override
                public Object call(ResourceEvent<Boolean> value) {
                    
                    final Object returnVal = onComplete.call(value);
                    
                    secondary.existsParallel(
                            
                        new Callback<Object, ResourceEvent<Boolean>>() {

                            @Override
                            public Object call(ResourceEvent<Boolean> value) {                                    
                                return onComplete.call(value);
                            }
                            
                        },
                        key
                    );
                    
                    return returnVal;
                    
                }
                
            },
            key
        );
    
    }

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

}
