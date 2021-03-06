package llc.ufwa.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.IdentityHashMap;
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
public class IdentityHashSet<T> implements Set<T>, Serializable, Iterable<T>, Collection<T> {
    
    private static final long serialVersionUID = 7945618847840882705L;
    
    private final Map<T, String> internal = new IdentityHashMap<T, String>();

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
    public Iterator<T> iterator() {
        return internal.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return internal.keySet().toArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] toArray(Object[] a) {
        return internal.keySet().toArray(a);
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("rawtypes")
    @Override
    public boolean containsAll(Collection c) {
        return internal.keySet().containsAll(c);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean addAll(Collection c) {
        
        boolean returnVal = false;
        
        for(Object ob : c) {
            if(add(ob)) {
                returnVal = true;
            }
        }
        
        return returnVal;
        
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean retainAll(Collection c) {
        return internal.keySet().retainAll(c);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean removeAll(Collection c) {
        return internal.keySet().removeAll(c);
    }

    @Override
    public void clear() {
        internal.keySet().clear();        
    }

}
