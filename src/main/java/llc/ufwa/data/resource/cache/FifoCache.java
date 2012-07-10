package llc.ufwa.data.resource.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.provider.PushProvider;
import llc.ufwa.data.resource.provider.ResourceProvider;

/**
 * Provided a FIFO implementation using some provided caches.
 * 
 * This class is not synchronized.
 * 
 * @author seanwagner
 *
 * @param <Item>
 */
public class FifoCache<Item> implements Queue<Item> {

    private static final Logger logger = LoggerFactory.getLogger(FifoCache.class);
    
    private final Cache<Long, Item> cache;
    private final ResourceProvider<Long> idProvider;
    private final PushProvider<LinkedList<Long>> idQueueProvider;
    
    /**
     * 
     * @param idQueueProvider
     * @param cache
     */
    public FifoCache(
        final PushProvider<LinkedList<Long>> idQueueProvider, 
        final Cache<Long, Item> cache
    ) {
        
        this.cache = cache;
        this.idQueueProvider = idQueueProvider;
        
        long largest = 0;
        
        for(final Long id : idQueueProvider.provide()) {
            
            if(id > largest) {
                largest = id;
            }
            
        }
        
        final long largestFinal = largest;
        
        idProvider = new ResourceProvider<Long>() {

            private long id = largestFinal;
            
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public Long provide() {
                return id++;
            }

        };
        
    }
    
    @Override
    public int size() {
        return idQueueProvider.provide().size();
    }

    @Override
    public boolean isEmpty() {
        return idQueueProvider.provide().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return idQueueProvider.provide().contains(o);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator iterator() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Object[] toArray() {
        
        final Object [] array = new Object[idQueueProvider.provide().size()];
        
        for(int i = 0; i < array.length; i++) {
            
            try {
                array[i] = cache.get(idQueueProvider.provide().get(i));
            }
            catch (ResourceException e) {
                
                logger.error("ERROR:", e);
                throw new RuntimeException(e);
                
            }
        }
        
        return array;
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] toArray(Object[] a) {
        return toArray();
    }

    @Override
    public boolean remove(Object o) {        
        throw new RuntimeException("not supported");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean containsAll(Collection c) {
        throw new RuntimeException("not supported");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean addAll(Collection c) {
        throw new RuntimeException("not supported");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean removeAll(Collection c) {
        throw new RuntimeException("not supported");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean retainAll(Collection c) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void clear() {
        
        final LinkedList<Long> idQueue = idQueueProvider.provide();
        
        cache.clear();
        idQueue.clear();
        
        idQueueProvider.push(idQueue);
        
    }

    @Override
    public boolean add(Item e) {
        
        final long id = idProvider.provide();
        final LinkedList<Long> idQueue = idQueueProvider.provide();
        
        cache.put(id, e);
        idQueue.add(id);
        
        idQueueProvider.push(idQueue);
        
        return true;
        
    }

    @Override
    public boolean offer(Item e) {
        
        add(e);
        
        return false;
        
    }

    @Override
    public Item remove() {
        
        final Item item = poll();
        
        if(item == null) {
            throw new IndexOutOfBoundsException("nothing in the FIFO");
        }
        else {
            return item;
        }
        
    }

    @Override
    public Item poll() {
        
        final LinkedList<Long> idQueue = idQueueProvider.provide();
        
        if(idQueue.size() > 0) {
            
            final Long id = idQueue.remove(0);
            
            idQueueProvider.push(idQueue);
            
            try {
                
                final Item item = cache.get(id);
                
                cache.remove(id);
                
                return item;
                
            } 
            catch (ResourceException e) {
                throw new RuntimeException(e);
            }
            
            
        }
        else {
            return null;
        }
        
    }

    @Override
    public Item element() {
        
        final Item item = peek();
        
        if(item == null) {
            throw new IndexOutOfBoundsException("nothing in the FIFO");
        }
        else {
            return item;
        }
        
    }

    @Override
    public Item peek() {
        
        final LinkedList<Long> idQueue = idQueueProvider.provide();
        
        if(idQueue.size() > 0) {
            
            final Long id = idQueue.get(0);
            
            idQueueProvider.push(idQueue);
            
            try {
                
                final Item item = cache.get(id);

                return item;
                
            } 
            catch (ResourceException e) {
                throw new RuntimeException(e);
            }
            
        }
        else {
            return null;
        }
        
    }

}
