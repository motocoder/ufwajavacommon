package llc.ufwa.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Identity hashset based off identity hashmap.
 * 
 * TODO write from scratch remove IdentityHashMap
 * TODO write test case
 * 
 * @author swagner
 *
 * @param <T>
 */
public class IdentityHashSet<T> implements Set<T>, Serializable {
    
    private static final long serialVersionUID = 7945618847840882705L;
    
    private final Map<T, String> internal = new HashMap<T, String>();

    public IdentityHashSet() {};
    
    public IdentityHashSet(Set<T> c) {
        
        this.addAll(c);
        
    }

    @Override
    public int size() {
        return internal.size();
    }

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return internal.containsKey(o);
    }

    @Override
    public Iterator iterator() {
        return internal.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return internal.keySet().toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return internal.keySet().toArray(a);
    }

    @Override
    public boolean add(Object e) {
        
        final boolean returnVal;
        
        if(!internal.containsKey(e)) {
            
            internal.put((T)e, "");
            returnVal = true;
            
        }       
        else {
            returnVal = false;
        }
        
        return returnVal;
    }

    @Override
    public boolean remove(Object o) {
        return internal.remove(o) != null;
    }

    @Override
    public boolean containsAll(Collection c) {
        return internal.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        
        boolean returnVal = false;
        
        for(Object ob : c) {
            if(add(c)) {
                returnVal = true;
            }
        }
        
        return returnVal;
        
    }

    @Override
    public boolean retainAll(Collection c) {
        return internal.keySet().retainAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return internal.keySet().removeAll(c);
    }

    @Override
    public void clear() {
        internal.keySet().clear();        
    }

}
