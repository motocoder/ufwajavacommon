package llc.ufwa.data.resource.loader;

import java.util.List;

import llc.ufwa.data.exception.ResourceException;

public class SynchronizedResourceLoader<Key, Value> implements ResourceLoader<Key, Value> {

    private final ResourceLoader<Key, Value> internal;

    public SynchronizedResourceLoader(ResourceLoader<Key, Value> internal) {
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

}
