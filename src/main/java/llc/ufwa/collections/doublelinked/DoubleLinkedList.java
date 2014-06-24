package llc.ufwa.collections.doublelinked;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Double linked list implementation. Each element has a reference to what is before and what is after it.
 * Performance is the same with large block moves. Main reason for the creation of this list was a convenient and
 * well performing way to store a sequence in SQL.
 * 
 * @author swagner
 *
 * @param <T>
 */
public class DoubleLinkedList<T extends DoubleLinkedItem> implements Collection<T>, List<T>, Serializable {

	
	private static final long serialVersionUID = 5859557658710067082L;
	
    private T firstItem = null;    
    private Set<T> dataIDMap = new HashSet<T>();

    /**
     * Constructor with the first item in the list as parameters.
     * 
     * @param firstItem
     */
    public DoubleLinkedList(T firstItem) {
        add(firstItem);
    }
    
    /**
     * creates a new double linked list using the old list. Warning these lists can conflict if you do not destroy the original input.
     * 
     * @param oldList
     */
    public DoubleLinkedList(List<T> oldList) {
       
        addAll(oldList);
    }
    
    /**
     * creates a new empty double linked list.
     */
    public DoubleLinkedList() {
    }
    
    /**
     * Returns the last item in the list.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
	public T getLastItem() {
    	T currentItem = firstItem;
    	T lastItem = firstItem;
    	
    	while(dataIDMap.contains(currentItem)) {
    		lastItem = currentItem;
    		currentItem = (T)currentItem.getObjectAfter();
    	}
    	
    	return lastItem;
    }

	public boolean add(T itemToAdd) {
		
		boolean returnVal = false;
		
		itemToAdd.setObjectAfter(null);
		itemToAdd.setObjectBefore(null);
		
		if(firstItem == null) {
			
			firstItem = itemToAdd;
			dataIDMap.add(itemToAdd);				
			
			returnVal = true;
		}
		else {
		
			DoubleLinkedItem lastItem = getLastItem();
			
			lastItem.setObjectAfter(itemToAdd);
			
			itemToAdd.setObjectBefore(lastItem);
			
			dataIDMap.add(itemToAdd);			
			
			returnVal = true;
		}

		return returnVal;
		
	}
	
    /**
     * Adds after the item in the second paramater.
     * 
     * @param itemToAdd
     * @param before
     * @return
     */
	@SuppressWarnings("unchecked")
	public boolean addAfter(T itemToAdd, T before) {
		
		boolean returnVal = false;
	
		
		if(dataIDMap.contains(itemToAdd)) {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><1>, Object already in list");
		}
		
		if(dataIDMap.contains(before)){
			
			T itemAfter = (T)before.getObjectAfter();
			
			before.setObjectAfter(itemToAdd);
			
			itemToAdd.setObjectBefore(before);			
			itemToAdd.setObjectAfter(itemAfter);
			
			if(itemAfter != null) {				
				itemAfter.setObjectBefore(itemToAdd);
			}
			
			dataIDMap.add(itemToAdd);			
			
			returnVal = true;
		}
		else {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><2>, Object to add after is not in the list");
		}

		return returnVal;
		
	}
	
	@SuppressWarnings("unchecked")
	public boolean addBefore(T itemToAdd, T after) {
		
		boolean returnVal = false;
		
			
		if(dataIDMap.contains(itemToAdd)) {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><3>, Object already in the list");
		}
		
		if(dataIDMap.contains(after)) {
			
			T itemBefore = (T)after.getObjectBefore();
			
			after.setObjectBefore(itemToAdd);
			
			itemToAdd.setObjectAfter(after);				
			itemToAdd.setObjectBefore(itemBefore);					
			
			if(itemBefore != null) {					
				itemBefore.setObjectAfter(itemToAdd);
			}
			
			if (firstItem == after) {
				firstItem = itemToAdd;
			}
			
			dataIDMap.add(itemToAdd);		
			
			returnVal = true;		}
		else {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><4>, Object after not in list");
		}
		
		return returnVal;
		
	}
	
	

	
	
	public boolean moveBefore(T itemToMove, T target) {
		
		boolean returnVal = false;
		
		if (!dataIDMap.contains(itemToMove)) {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><5>, Moving object not in list");
		}
		
		if (!dataIDMap.contains(target)) {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><6>, Target object not in list");
		}
		
		if (itemToMove != target) {

			this.remove(itemToMove);
			this.addBefore(itemToMove, target);

			returnVal = true;
			
		}
		
		return returnVal;		
	}
	
	
	public boolean moveAfter(T itemToMove, T target) {
		
		boolean returnVal = false;				

		if (!dataIDMap.contains(itemToMove)) {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><7>, Moving object not in list");
		}
		
		if (!dataIDMap.contains(target)) {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><8>, Target object not in list");
		}

		if (itemToMove != target) {
			
			this.remove(itemToMove);			
			this.addAfter(itemToMove, target);

			returnVal = true;
		}
		
		return returnVal;	
		
	}
	
	

	public boolean addAll(Collection<? extends T> items) {
		
		boolean returnVal = false;
		
		for(T item : items) {
			add(item);
		}
		
		return returnVal;
	}

	
	public void clear() {
		
		dataIDMap.clear();
		firstItem = null;		
	}

	public boolean contains(Object ob) {
		
		return dataIDMap.contains(ob);
		
	}
	
	public T getFirstItem() {
		return firstItem;
	}

	public boolean containsAll(Collection<?> items) {
		
		boolean returnVal = true;
		
		for(Object item : items) {
			returnVal = dataIDMap.contains(item);
			if(!returnVal) {
				break;
			}
		}
		
		return returnVal;
	}

	public boolean isEmpty() {
		return firstItem == null;
	}

	public Iterator<T> iterator() {
		return new LinkedIterator<T>(this);
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object obj) {	
		
		final boolean returnVal;
		
		DoubleLinkedItem item = null;
		
		if(obj instanceof DoubleLinkedItem) {
			returnVal = dataIDMap.contains(obj);		
		}
		else {
			returnVal = false;
		}
		
		if(returnVal) {
			
			for(T data : dataIDMap) {
				if(data.equals(obj)) {
					item = data;
					break;
				}
			}
			
			T after = (T)item.getObjectAfter();
			T before = (T)item.getObjectBefore();
			
			if(after != null) {
				after.setObjectBefore(before);
			}
			
			if(before != null) {
				before.setObjectAfter(after);
			}
			else {
				firstItem = after;
			}
			
			item.setObjectAfter(null);
			item.setObjectBefore(null);
			
			dataIDMap.remove(item);
		}
		
		return returnVal;
	}

	public boolean removeAll(Collection<?> items) {
		
		boolean returnVal = false;
		
		for(Object item : items) {
			
			if(dataIDMap.contains(item)) {	
				 returnVal = remove(item);	
			}
		}
		
		return returnVal;
	}
	
	public boolean retainAll(Collection<?> collection) {
		
		boolean returnVal = false;
		
		for(DoubleLinkedItem item : this) {
			
			if(!collection.contains(item)) {
				remove(item);
				returnVal = true;
			}
			
		}
		return returnVal;
	}

	public int size() {
		return dataIDMap.size();
	}

	public Object[] toArray() {
		
		Object [] returnArray = new Object[size()];
		
		for(int i = 0; i < size(); i++) {
		    returnArray[i] = get(i);
		}
		
		return returnArray;
	}



	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] arg0) {
		// TODO stupid not implementing this.
		return null;
	}

	@SuppressWarnings("unchecked")
	public void add(int index, T toInsert) {
		
		toInsert.setObjectAfter(null);
		toInsert.setObjectBefore(null);
		
		if(index == 0) {
			dataIDMap.add(toInsert);	
			
			toInsert.setObjectAfter(firstItem);
			
			if(firstItem != null) {
				firstItem.setObjectBefore(toInsert);
			}
			
			firstItem = toInsert;
		}
		else {
			
			T currentItem = firstItem;
			for(int i = 0; i < index; i++) {
				
				if(currentItem == null) {
					break;
				}
				
				currentItem = (T)currentItem.getObjectAfter();
			}
			
			if(currentItem != null) {
				addBefore(toInsert, currentItem);
			}
			else {
				if(index > size() ) {
					throw new IndexOutOfBoundsException("<DoubleLinkedList><9>, index " + index + " is out of bounds");
				}
				else {
					add(toInsert);
				}
			}
		}
		
	}

	public boolean addAll(int index, Collection<? extends T> items) {
		int i = 0;
		for(T item : items) {
			this.add(index + i, item);
			i++;
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	public T get(int index) {
		
		if(index >= size() || index < 0) {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><10>, index " + index + " is out of bounds!");
		}
		
		T currentItem = firstItem;
		
		for(int i = 0; i < index; i++) {
			currentItem = (T)currentItem.getObjectAfter();
		}
		
		return currentItem;
	}

	@SuppressWarnings("unchecked")
	public int indexOf(Object item) {
		int returnVal;
		
		if(!dataIDMap.contains(item)) {
			returnVal = -1;
		}
		else {
		
			returnVal = -1;
			T currentItem = firstItem;
			
			for( int i = 0; i < size(); i++) {
				if(currentItem.equals(item)) {
					returnVal = i;
					break;
				}
				
				currentItem = (T)currentItem.getObjectAfter();
			}			
		}
		return returnVal;
	}

	public int lastIndexOf(Object item) {
		int returnVal;
		
		if(!dataIDMap.contains(item)) {
			returnVal = -1;
		}
		else {
		
			returnVal = -1;
			
			DoubleLinkedItem currentItem = getLastItem();
			
			for(int i = size(); i >= 0; i--) {
				currentItem = currentItem.getObjectBefore();
				if(currentItem.equals(item)) {
					returnVal = i;
					break;
				}
			}
			
		}
		return returnVal;
	}

	public ListIterator<T> listIterator() {
		// TODO Being lazy
		return null;
	}

	public ListIterator<T> listIterator(int arg0) {
		// TODO Being lazy
		return null;
	}

	public T remove(int index) {		
		
		final T returnVal = get(index);
		
		if(returnVal == null) {
			throw new IndexOutOfBoundsException("<DoubleLinkedList><11>, get(index) returned null");
		}
		
		remove(returnVal);
		
		return returnVal;
	}

	public T set(int index, T item) {
		T previous = remove(index);
		add(index, item);
		return previous;
	}

	public List<T> subList(int arg0, int arg1) {
		// TODO Being lazy
		return null;
	}

	public void move(Collection<T> toMove, int direction) {

        if(toMove.size() == 0) {
            return;
        }

        Set<Integer> newRows = new HashSet<Integer>();
        List<T> rows = new ArrayList<T>(toMove);
        
        final DoubleLinkedList<T> thiz = this;
        
        Collections.sort(rows, new Comparator<T>() {

			@Override
			public int compare(T arg0, T arg1) {
				final T firmware1 = (T)arg0;
				final T firmware2 = (T)arg1;
				
				return thiz.indexOf(firmware1) - thiz.indexOf(firmware2);
			}});  


        if(direction < 0) { // up
            int lastIndex = -1;

            for(final T firmware : rows) {
            	
            	final int index = this.indexOf(firmware);

                if(index == 0) {
                    newRows.add(index);
                    lastIndex = index;
                    continue;
                }

                final int newIndex = index + direction;
                if ( newIndex > lastIndex ) {
                    T param = this.remove(index);
                    this.add( newIndex, param);
                    newRows.add( newIndex );
                }
                else if ( newIndex <= lastIndex ) {
                    newRows.add( index );
                    lastIndex = index;
                }
            }
        }
        else { // down
            int lastIndex = this.size();

            for(int i = rows.size()-1; i >= 0; i--) {

                final int index = this.indexOf(rows.get(i));
                if(index == this.size()-1) {
                    newRows.add(index);
                    lastIndex = index;
                    continue;
                }

                final int newIndex = index + direction;
                if ( newIndex < lastIndex ) {
                	T param = this.remove(index);
                	this.add( newIndex, param);
                    newRows.add( newIndex );
                }
                else if ( newIndex >= lastIndex ) {
                    newRows.add( index );
                    lastIndex = index;
                }
            }
        }

        return;
    }

}


