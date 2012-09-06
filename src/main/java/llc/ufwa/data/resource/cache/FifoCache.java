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
        
        try {
            
            for(final Long id : idQueueProvider.provide()) {
                
                if(id > largest) {
                    largest = id;
                }
                
            }
            
        } 
        catch (ResourceException e) {
            throw new RuntimeException("There is something wrong with your provider");
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
        
        try {
            return idQueueProvider.provide().size();
        } 
        catch (ResourceException e) {
            throw new RuntimeException("There is something wrong with your provider");
        }
        
    }

    @Override
    public boolean isEmpty() {
        
        try {
            return idQueueProvider.provide().isEmpty();
        } 
        catch (ResourceException e) {
            throw new RuntimeException("There is something wrong with your provider");
        }
        
    }

    @Override
    public boolean contains(Object o) {
        try {
            return idQueueProvider.provide().contains(o);
        } 
        catch (ResourceException e) {
            throw new RuntimeException("There is something wrong with your provider");
        }
        
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator iterator() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Object[] toArray() {
        
        try {
            
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
        catch (ResourceException e) {
            throw new RuntimeException("There is something wrong with your provider");
        }
        
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
        
        try {
            
            final LinkedList<Long> idQueue = idQueueProvider.provide();
            
            cache.clear();
            idQueue.clear();
            
            idQueueProvider.push(idQueue);
            
        } 
        catch (ResourceException e) {
            throw new RuntimeException("There is something wrong with your provider");
        }
        
    }

    @Override
    public boolean add(Item e) {
        
        try {
            
            final long id = idProvider.provide();
            final LinkedList<Long> idQueue = idQueueProvider.provide();
            
            cache.put(id, e);
            idQueue.add(id);
            
            idQueueProvider.push(idQueue);
            
            return true;
            
        } 
        catch (ResourceException t) {
            throw new RuntimeException("There is something wrong with your provider");
        }
        
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
        
        try {
            
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
        catch (ResourceException e) {
            throw new RuntimeException("There is something wrong with your provider");
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
        
        try {
           
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
        catch (ResourceException e) {
            throw new RuntimeException("There is something wrong with your provider");
        }
        
    }

}
