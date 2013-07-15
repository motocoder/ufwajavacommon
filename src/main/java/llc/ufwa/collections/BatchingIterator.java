package llc.ufwa.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Just an iterator that wraps a collection and provides a batch size of values from
 * it with each next();
 * 
 * @author seanwagner
 *
 * @param <Key>
 */
public class BatchingIterator<Key> implements Iterator<List<Key>> {

    private final List<Key> items;
    private final int batchSize; 

    /**
     * 
     * @param items 
     * @param batchSize
     */
    public BatchingIterator(
        final List<Key> items,
        final int batchSize
    ) {
        
        this.batchSize = batchSize;
        this.items = new ArrayList<Key>(items);
        
    }
    
    @Override
    public boolean hasNext() {
        return items.size() > 0;
    }

    @Override
    public List<Key> next() {
        
        final List<Key> returnVals = new ArrayList<Key>();
        
        if(items.size() > 0) {
            
            if(items.size() > batchSize) {
                
                for(int i = 0; i < batchSize; i++) {
                    returnVals.add(items.remove(0));
                }
                
            }
            else {
                returnVals.addAll(items);
                items.clear();
            }
        }
        
        return returnVals;
    }

    @Override
    public void remove() {
        throw new RuntimeException("<BatchingIterator><1>, not supported");
    }

}
