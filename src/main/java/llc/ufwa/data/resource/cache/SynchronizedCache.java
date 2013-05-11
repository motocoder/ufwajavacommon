package llc.ufwa.data.resource.cache;

import java.util.List;

import llc.ufwa.data.exception.ResourceException;

public class SynchronizedCache<Key, Value> implements Cache<Key, Value> {
    
    private final Cache<Key, Value> internal;

    public SynchronizedCache(
        final Cache<Key, Value> internal
    ) {
        this.internal = internal;
    }

    @Override
    public synchronized boolean exists(Key key) throws ResourceException {
        return internal.exists(key);
    }

    @Override
    public synchronized Value get(Key key) throws ResourceException {
        return internal.get(key);
    }

    @Override
    public synchronized List<Value> getAll(List<Key> keys) throws ResourceException {
        return internal.getAll(keys);
    }

    @Override
    public synchronized void clear() {
        internal.clear();
    }

    @Override
    public synchronized void remove(Key key) {
        internal.remove(key);
    }

    @Override
    public synchronized void put(Key key, Value value) {
        internal.put(key, value);    
    }

}
