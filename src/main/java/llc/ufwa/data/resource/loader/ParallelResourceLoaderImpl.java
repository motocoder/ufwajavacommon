package llc.ufwa.data.resource.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.LimitingExecutorService;
import llc.ufwa.data.exception.CanceledResourceException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.provider.SettableResourceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelResourceLoaderImpl<Key, Value> implements ParallelResourceLoader<Key, Value> {

    private static final Logger logger = LoggerFactory.getLogger(ParallelResourceLoaderImpl.class);
    
    private final String loggingTag;
    private final ResourceLoader<Key, Value> internal;
    private final int depth;
    private final LimitingExecutorService threads; 
    
    private final CurrentlyOutStates<Key> outStates = new CurrentlyOutStates<Key>();
    
    private final Map<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>> respondTo = 
        new HashMap<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>>();
    
    private final Map<Key, LinkedList<Callback<Object, ResourceEvent<Boolean>>>> respondToExists = 
        new HashMap<Key, LinkedList<Callback<Object, ResourceEvent<Boolean>>>>();

    private final ExecutorService callbackThreads;
 
    /**
     * 
     * @param internal
     * @param threads
     * @param callbackThreads
     * @param depth
     * @param loggingTag
     */
    public ParallelResourceLoaderImpl(
        final ResourceLoader<Key, Value> internal,
        final LimitingExecutorService threads,
        final ExecutorService callbackThreads,
        final int depth,
        final String loggingTag
    ) {
        
        if(internal == null || threads == null || callbackThreads == null || loggingTag == null) {
            throw new NullPointerException("No constructor arguments can be null.");
        }
        
        if(depth < 1) {
            throw new IllegalArgumentException("You cannot specify a depth of less than 1");
        }
        
        this.loggingTag = loggingTag; 
        this.internal = internal;
        this.depth = depth;
        
        this.threads = threads;
        this.callbackThreads = callbackThreads;
        
    }
    
    @Override
    public boolean exists(Key key) throws ResourceException {
       
        final ResourceLoader<Key, Boolean> booleanInternal = 
                new DefaultResourceLoader<Key, Boolean>() {

            @Override
            public Boolean get(Key key) throws ResourceException {
                return internal.exists(key);
            }
            
        };
        
        return getSerially(key, loggingTag, respondToExists, depth, outStates, threads, callbackThreads, booleanInternal, true);
        
    }

    @Override
    public Value get(Key key) throws ResourceException {
        return getSerially(key, loggingTag, respondTo, depth, outStates, threads, callbackThreads, internal, false);
    }

    @Override
    public List<Value> getAll(List<Key> keysOrig) throws ResourceException {

        if(keysOrig == null) {
            throw new NullPointerException("List of keys must not be null");
        }
        
        if(keysOrig.size() == 0) {
            return new ArrayList<Value>(); 
        }
        
        final Map<Key, ResourceEvent<Value>> responses = new HashMap<Key, ResourceEvent<Value>>();
         
        final List<Key> keys = new ArrayList<Key>(keysOrig);
        
        final Map<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>> mappedCallbacks = new HashMap<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>>();
        final List<Key> uniques = new ArrayList<Key>(new HashSet<Key>(keys));
        
        if(uniques.contains(null)) {
            throw new NullPointerException("Null is not a valid key");
        }
        
        synchronized(outStates) { 
            synchronized(respondTo) {
                
                for(final Key key : uniques) {
                    
                    LinkedList<Callback<Object, ResourceEvent<Value>>> callbacks = respondTo.get(key);
                    
                    if(callbacks == null) { //create callbacks to be run on completion.
                        callbacks = new LinkedList<Callback<Object, ResourceEvent<Value>>>();
                    }
                    
                    if(callbacks.size() >= depth) {

                        logger.warn(callbacks.size() + " too deep1 " + depth);
                        
                        final Callback<Object, ResourceEvent<Value>> removedCallback = 
                                callbacks.remove(0); //remove oldest if we have too many calls on this key.
                        
                        callbackThreads.execute(
                            new Runnable() {
        
                                @Override
                                public void run() {
                                    
                                    removedCallback.call(
                                        new ResourceEvent<Value>(
                                            null,
                                            new CanceledResourceException("Too deep canceled"),
                                            ResourceEvent.UNKNOWN
                                        )
                                    );
                                    
                                }
                                
                            }
                            
                        );
                        
                    }
                    
                    respondTo.put(key, callbacks);
                    
                    mappedCallbacks.put(key, callbacks);
                    
                    final Callback<Object, ResourceEvent<Value>> onComplete = new Callback<Object, ResourceEvent<Value>>() {

                        @Override
                        public Object call(
                            final ResourceEvent<Value> value
                        ) {
                            
                            synchronized(responses) {
                                responses.put(key, value);
                                responses.notify();
                            }
                            
                            return false;
                        }
                        
                    };
                    
                    callbacks.add(0, onComplete);   
                   
                }
                
                final List<Key> newOuts = new ArrayList<Key>();
                
                for(final Key key : uniques) {
                    
                    if(!outStates.contains(key, false)) {
                        
                        newOuts.add(key);
                        outStates.add(key, false);
                        
                    }
                    
                }
                
                threads.execute(
                        
                    new Runnable() {
        
                        @Override
                        public void run() {
                            
                            final List<Value> valueList = new ArrayList<Value>();
                            final List<Throwable> thrownList = new ArrayList<Throwable>();
                            
                            try {                                
                                valueList.addAll(internal.getAll(newOuts));       
                            }
                            catch(ResourceException e) {
                                
                                for(int i = 0; i < newOuts.size(); i++) {
                                    thrownList.add(e);
                                }
                                
                                logger.info("Error loading resource in ParellelResourceLoaderImpl " + loggingTag, e);
                                
                            }
                            catch(Exception e) {
                                
                                for(int i = 0; i < newOuts.size(); i++) {
                                    thrownList.add(new ResourceException(e));
                                }
                                
                                logger.error("Error loading resource in ParellelResourceLoaderImpl " + loggingTag, e);
                                
                            }
                            finally {
                                
                                if(newOuts.size() != valueList.size()) {
                                    
                                    logger.error("Invalid getAll() response, failing all keys!");
                                    
                                    for(int i = 0; i < newOuts.size(); i++) {
                                        thrownList.add(new ResourceException("Invalid getAll response length, failing all keys"));
                                    } 
                                    
                                }
                                    
                                for(int i = 0; i < newOuts.size(); i++) {
                                    
                                    final Key key = newOuts.get(i);
                                    
                                    final Throwable thrown;
                                    
                                    if(thrownList.size() > i) {
                                        thrown = thrownList.get(i);
                                    }
                                    else {
                                        thrown = null;
                                    }
                                    
                                    final Value value;
                                    
                                    if(thrown == null) {
                                        value = valueList.get(i);
                                    }
                                    else {
                                        value = null;
                                    }
                                    
                                    final LinkedList<Callback<Object, ResourceEvent<Value>>> origCallbacks;
                                    final List<Callback<Object, ResourceEvent<Value>>> callbacks;
                                    
                                    synchronized(outStates) {
                                        synchronized(respondTo) {
                                            
                                            origCallbacks = respondTo.get(key);
                                            
                                            outStates.remove(key, false);
                                            
                                            respondTo.put(key, null);
                                            
                                            if(origCallbacks != null) {
                                                callbacks = new ArrayList<Callback<Object, ResourceEvent<Value>>>(origCallbacks);
                                            }
                                            else {
                                                
                                                logger.error("We just did a call to the server for no reason1! " + key + loggingTag);
                                                callbacks =  new ArrayList<Callback<Object, ResourceEvent<Value>>>();
                                                
                                            }
                                            
                                        }
                                        
                                    }
                                            
                                    final ResourceEvent<Value> event;
                                    
                                    if(thrown == null) {
                                        event = new ResourceEvent<Value>(value, null, ResourceEvent.NEW_LOADED);
                                    }
                                    else {
                                        event = new ResourceEvent<Value>(null, thrown, ResourceEvent.UNKNOWN);
                                    }
                                    
                                    for(final Callback<Object, ResourceEvent<Value>> callback : callbacks) {
                                        
                                        callbackThreads.execute(
                                                
                                            new Runnable() {

                                                @Override
                                                public void run() {
                                                    callback.call(event);
                                                }
                                                
                                            }
                                            
                                        );
                                            
                                    }
                                 
                                }
                                
                            }
                        }
                        
                    },
                    new Callback<Void, Void>() {

                        @Override
                        public Void call(Void value) {
                            cancel(newOuts, outStates, respondTo, callbackThreads);
                            return null;
                        }
                    }
                    
                );
               
            } // end respondToOut.
        } //end sync out.
        
        synchronized(responses) {
            
            while(responses.size() != uniques.size()) {

                try {
                    responses.wait(100);
                } 
                catch (InterruptedException e) {
                    throw new ResourceException(e);
                }
                
            }
            
        }
        
        final List<Value> returnVals = new ArrayList<Value>();
        
        for(final Key key : keys) {
            
            final ResourceEvent<Value> event = responses.get(key);
            
            if(event.getThrowable() != null) {
                throw new ResourceException(event.getThrowable());
            }
            
            returnVals.add(event.getVal());
            
        }
        
        return returnVals;
        
    }

    @Override
    public void getParallel(
        final Callback<Object, ResourceEvent<Value>> onComplete,
        final Key key
    ) {
        parellelCall(onComplete, key, respondTo, depth, outStates, loggingTag, threads, callbackThreads, internal, false);
    }

    @Override
    public void existsParallel(
        final Callback<Object, ResourceEvent<Boolean>> onComplete,
        final Key key
    ) {
        
        final ResourceLoader<Key, Boolean> booleanInternal = 
                new DefaultResourceLoader<Key, Boolean>() {

            @Override
            public Boolean get(Key key) throws ResourceException {
                return internal.exists(key);
            }
            
        };
        
        parellelCall(onComplete, key, respondToExists, depth, outStates, loggingTag, threads, callbackThreads, booleanInternal, true);
       
        
    }

    @Override
    public void getAllParallel(
        final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap
    ) {
                            
        final List<Key> keys = new ArrayList<Key>(callbackMap.keySet());
        
        final Map<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>> mappedCallbacks = new HashMap<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>>();
        final List<Key> uniques = new ArrayList<Key>(new HashSet<Key>(keys));
        
        if(uniques.contains(null)) {
            throw new NullPointerException("Null is not a valid key");
        }
        
        synchronized(outStates) {
            synchronized(respondTo) {
                
                for(final Key key : uniques) {
                    
                    LinkedList<Callback<Object, ResourceEvent<Value>>> callbacks = respondTo.get(key);
                    
                    if(callbacks == null) { //create callbacks to be run on completion.
                        callbacks = new LinkedList<Callback<Object, ResourceEvent<Value>>>();
                    }
                    
                    if(callbacks.size() >= depth) {

                        logger.warn(callbacks.size() + " too deep1 " + depth);
                        
                        final Callback<Object, ResourceEvent<Value>> removedCallback = 
                                callbacks.remove(0); //remove oldest if we have too many calls on this key.
                        
                        callbackThreads.execute(
                            new Runnable() {
        
                                @Override
                                public void run() {
                                    
                                    removedCallback.call(
                                        new ResourceEvent<Value>(
                                            null,
                                            new CanceledResourceException("Too deep canceled"),
                                            ResourceEvent.UNKNOWN
                                        )
                                    );
                                    
                                }
                                
                            }
                            
                        );
                        
                    }
                    
                    respondTo.put(key, callbacks);
                    
                    mappedCallbacks.put(key, callbacks);
                    
                    final Callback<Object, ResourceEvent<Value>> onComplete = callbackMap.get(key);
                    
                    callbacks.add(0, onComplete);   
                   
                }
                
                final List<Key> newOuts = new ArrayList<Key>();
                
                for(final Key key : uniques) {
                    
                    if(!outStates.contains(key, false)) {
                        
                        newOuts.add(key);
                        outStates.add(key, false);
                        
                    }
                    
                }
                
                threads.execute(
                        
                    new Runnable() {
        
                        @Override
                        public void run() {
                            
                            final List<Value> valueList = new ArrayList<Value>();
                            final List<Throwable> thrownList = new ArrayList<Throwable>();
                            
                            try {                                
                                valueList.addAll(internal.getAll(newOuts));       
                            }
                            catch(ResourceException e) {
                                
                                for(int i = 0; i < newOuts.size(); i++) {
                                    thrownList.add(e);
                                }
                                
                                logger.info("Error loading resource in ParellelResourceLoaderImpl " + loggingTag, e);
                                
                            }
                            catch(Exception e) {
                                
                                for(int i = 0; i < newOuts.size(); i++) {
                                    thrownList.add(new ResourceException(e));
                                }
                                
                                logger.error("Error loading resource in ParellelResourceLoaderImpl " + loggingTag, e);
                                
                            }
                            finally {
                                
                                if(newOuts.size() != valueList.size()) {
                                    
                                    logger.error("Invalid getAll() response, failing all keys!");
                                    
                                    for(int i = 0; i < newOuts.size(); i++) {
                                        thrownList.add(new ResourceException("Invalid getAll response length, failing all keys"));
                                    } 
                                    
                                }
                                    
                                for(int i = 0; i < newOuts.size(); i++) {
                                    
                                    final Key key = newOuts.get(i);
                                    
                                    final Throwable thrown;
                                    
                                    if(thrownList.size() > i) {
                                        thrown = thrownList.get(i);
                                    }
                                    else {
                                        thrown = null;
                                    }
                                    
                                    final Value value;
                                    
                                    if(thrown == null) {
                                        value = valueList.get(i);
                                    }
                                    else {
                                        value = null;
                                    }
                                    
                                    final List<Callback<Object, ResourceEvent<Value>>> callbacks;
                                    
                                    synchronized(outStates) {
                                        synchronized(respondTo) {
                                            
                                            final LinkedList<Callback<Object, ResourceEvent<Value>>>origCallbacks = respondTo.get(key);
                                            
                                            outStates.remove(key, false);
                                            
                                            respondTo.put(key, null);
                                            
                                            if(origCallbacks != null) {
                                                callbacks = new ArrayList<Callback<Object, ResourceEvent<Value>>>(origCallbacks);
                                            }
                                            else {
                                                
                                                logger.error("We just did a call to the server for no reason1! " + key + loggingTag);
                                                callbacks =  new ArrayList<Callback<Object, ResourceEvent<Value>>>();
                                                
                                            }
                                            
                                        }
                                    }
                                            
                                    final ResourceEvent<Value> event;
                                    
                                    if(thrown == null) {
                                        event = new ResourceEvent<Value>(value, null, ResourceEvent.NEW_LOADED);
                                    }
                                    else {
                                        event = new ResourceEvent<Value>(null, thrown, ResourceEvent.UNKNOWN);
                                    }
                                    
                                    for(final Callback<Object, ResourceEvent<Value>> callback : callbacks) {
                                    	
                                        callbackThreads.execute(
                                                
                                            new Runnable() {

                                                @Override
                                                public void run() {
                                                	                                                	
                                                	try {
                                                		callback.call(event);
                                                	}
                                                	catch(Throwable t) {
                                                		logger.error("Thrown: ", t);
                                                	}
                                                }
                                                
                                            }
                                            
                                        );
                                            
                                    }
                                 
                                }
                                
                            }
                        }
                        
                    },
                    new Callback<Void, Void>() {

                        @Override
                        public Void call(Void value) {
                            
                            cancel(newOuts, outStates, respondTo, callbackThreads);
                            return null;
                        }
                    }
                    
                );
               
            } // end respondToOut.
        } //end sync out.
                     
        
    }
    
    private static <Key, Value> void cancel(
        final Collection<Key> newOuts,
        final CurrentlyOutStates<Key> outStates,
        final Map<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>> respondTo,
        final ExecutorService callbackThreads
    ) {
        
    	logger.info("Cancelling resourceLoader request");
    	
        for(final Key key : newOuts) {
            
            final List<Callback<Object, ResourceEvent<Value>>> callbacks;
        
            synchronized(outStates) {
                synchronized(respondTo) {
                    
                    final LinkedList<Callback<Object, ResourceEvent<Value>>>origCallbacks = respondTo.get(key);
                    
                    outStates.remove(key, false);
                    
                    respondTo.put(key, null);
                     
                    if(origCallbacks != null) {
                        callbacks = new ArrayList<Callback<Object, ResourceEvent<Value>>>(origCallbacks);
                    }
                    else {
                        
                        logger.error("We just did a call to the server for no reason1! " + key);
                        callbacks =  new ArrayList<Callback<Object, ResourceEvent<Value>>>();
                        
                    }
                    
                }
                
            }
                    
            final ResourceEvent<Value> event = new ResourceEvent<Value>(null, new CanceledResourceException("Was cancelled"), ResourceEvent.UNKNOWN);
            
            for(final Callback<Object, ResourceEvent<Value>> callback : callbacks) {
                
                callbackThreads.execute(
                        
                    new Runnable() {

                        @Override
                        public void run() {
                            callback.call(event);
                        }
                        
                    }
                    
                );
                    
            }
            
        }
        
        logger.info("Cancelled!");
        
    }
    
    private static <Key, Value> Value getSerially (
        final Key key,
        final String loggingTag,
        final Map<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>> respondTo,
        final int depth,
        final CurrentlyOutStates<Key> outStates,
        final LimitingExecutorService threads,
        final ExecutorService callbackThreads,
        final ResourceLoader<Key, Value> internal,
        final boolean isExistsCall
    ) throws ResourceException {
        
        if(key == null) {
            throw new NullPointerException("Key must not be null");
        }
        
        final SettableResourceProvider<ResourceEvent<Value>> provider = new SettableResourceProvider<ResourceEvent<Value>>();
        
        final Callback<Object, ResourceEvent<Value>> callback =
                new Callback<Object, ResourceEvent<Value>>() {

            @Override
            public Object call(ResourceEvent<Value> value) {
                
                synchronized(provider) {
                    
                    provider.setInternal(value);
                    provider.notify();
                    
                }
                
                return false;
            }
            
        }; 
        
        parellelCall(callback, key, respondTo, depth, outStates, loggingTag, threads, callbackThreads, internal, isExistsCall);
        
        synchronized(provider) {
            
            while(!provider.exists()) {
                
                try {
                    provider.wait();
                } 
                catch (InterruptedException e) {
                   throw new RuntimeException("get was interrupted" + loggingTag);
                }
                
            }
            
        }
        
        final ResourceEvent<Value> event = provider.provide();
        
        if(event.getThrowable() != null) {
            
            if(event.getThrowable() instanceof ResourceException) {
                
                final ResourceException e = (ResourceException)event.getThrowable();
                throw e;
                
            }
            else {
            
                throw new RuntimeException(event.getThrowable());
                
            }
            
        }
        
        return provider.provide().getVal();
        
    }
    
    private static <Key, Value> void parellelCall(
        final Callback<Object, ResourceEvent<Value>> onComplete, 
        final Key key, 
        final Map<Key, LinkedList<Callback<Object, ResourceEvent<Value>>>> respondTo,
        final int depth,
        final CurrentlyOutStates<Key> outStates,
        final String loggingTag,
        final LimitingExecutorService threads,
        final ExecutorService callbackThreads,
        final ResourceLoader<Key, Value> internal,
        final boolean isExistsCall
    ) {
              
        if(onComplete == null) {
            throw new NullPointerException("Callback must not be null");
        } 
        
        if(key == null) {
            throw new NullPointerException("Key must not be null");
        }
        
        synchronized(outStates) {
        synchronized(respondTo) {
            
            LinkedList<Callback<Object, ResourceEvent<Value>>> callbacks = respondTo.get(key);
            
            if(callbacks == null) { //create callbacks to be run on completion.
                callbacks = new LinkedList<Callback<Object, ResourceEvent<Value>>>();
            }
            
            if(callbacks.size() >= depth) {
                
                logger.warn(callbacks.size() + " too deep " + depth);                
                
                final Callback<Object, ResourceEvent<Value>> removedCallback = 
                        callbacks.remove(0); //remove oldest if we have too many calls on this key.
                
                callbackThreads.execute(
                    new Runnable() {

                        @Override
                        public void run() {
                            
                            removedCallback.call(
                                new ResourceEvent<Value>(
                                    null,
                                    new CanceledResourceException("Too deep canceled"),
                                    ResourceEvent.UNKNOWN
                                )
                            );
                            
                        }
                        
                    }
                    
                );
                
            }
            
            respondTo.put(key, callbacks);
                        
            if(!outStates.contains(key, isExistsCall)) { //if no call is being made to a specific key
                            	
                outStates.add(key, isExistsCall);       
                callbacks.add(0, onComplete);   
                
                     
                threads.execute(
                        
                    new Runnable() {
                        
                        @Override
                        public void run() {
                        
                            Value value = null;
                            Throwable thrown = null;
                            
                            try {
                                
                                value = internal.get(key);
                            
                            }
                            catch(ResourceException e) {
                                
                                thrown = e;
                                logger.info("ResourceException Error loading resource in ParellelResourceLoaderImpl " + loggingTag, e);
                                
                            }
                            catch(Exception e) {
                                
                                thrown = e;
                                logger.error("Exception Error loading resource in ParellelResourceLoaderImpl " + loggingTag, e);
                                
                            }
                            finally {
                                
                                final LinkedList<Callback<Object, ResourceEvent<Value>>> origCallbacks;
                                final List<Callback<Object, ResourceEvent<Value>>> callbacks;
                                
                                synchronized(outStates) {                                    
                                    synchronized(respondTo) {
                                        
                                        origCallbacks = respondTo.get(key);
                                        
                                        outStates.remove(key, isExistsCall);
                                        
                                        respondTo.put(key, null);
                                        
                                        if(origCallbacks != null) {
                                            
                                            callbacks = new ArrayList<Callback<Object, ResourceEvent<Value>>>(origCallbacks);
                                            
                                        }
                                        else {
                                            
                                            callbacks = new ArrayList<Callback<Object, ResourceEvent<Value>>>();
                                            logger.error("We just did a call to the server for no reason2! " + loggingTag);
                                            
                                        }
                                        
                                    }
                                                                        
                                }
                                   
                                final ResourceEvent<Value> event;
                                
                                if(thrown == null) {
                                    event = new ResourceEvent<Value>(value, null, ResourceEvent.NEW_LOADED);
                                }
                                else {
                                    event = new ResourceEvent<Value>(null, thrown, ResourceEvent.UNKNOWN);
                                }
                                
                                for(final Callback<Object, ResourceEvent<Value>> callback : callbacks) {
                                        
                                    callbackThreads.execute(
                                            
                                        new Runnable() {
    
                                            @Override
                                            public void run() {
                                                callback.call(event);
                                            }
                                            
                                        }
                                        
                                    );
                                    
                                }
                                   
                            }
                            
                        }
                        
                    },
                    new Callback<Void, Void>() {

                        @Override
                        public Void call(Void value) {
                            
                            final List<Key> keys = new ArrayList<Key>();
                            keys.add(key);
                            
                            cancel(keys, outStates, respondTo, callbackThreads);
                            return null;
                        }
                    }
                    
                );                
                
            }//Out doesn't contain the key already
            else {
                callbacks.add(0, onComplete);                
            }
                                
            } //end sync out.
            
        } // end respondToOut.
        
    }
    
    private static class CurrentlyOutStates<Key> {
        
        private final Set<Key> out = new HashSet<Key>();
        private final Set<Key> outExists = new HashSet<Key>();
        
        public void addOutSingle(Key key) {
            out.add(key);            
        }
        
        public void addOutExistsSingle(Key key) {
            outExists.add(key);
        }
                
        public void removeOutSingle(Key key) {    
            out.remove(key);
        }
        
        
        public void removeOutExistsSingle(Key key) {
            outExists.remove(key); 
        }
        
        public boolean contains(Key key, boolean exists) {
            
            final boolean returnVal;
            
            if(exists) {
                returnVal = outExists.contains(key);
            }
            else {
                returnVal = out.contains(key);
            }
            
            return returnVal;
            
        }
        
        public void add(Key key, boolean exists) {
            
            if(exists) {
                this.addOutExistsSingle(key);
            }
            else {
                this.addOutSingle(key);
            }

        }
        
        public void remove(Key key, boolean exists) {
            
            if(exists) {
                this.removeOutExistsSingle(key);
            }
            else {
                this.removeOutSingle(key);
            }

        }
        
        
    }

}
