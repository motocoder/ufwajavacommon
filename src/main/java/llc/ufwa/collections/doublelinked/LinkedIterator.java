package llc.ufwa.collections.doublelinked;

import java.io.Serializable;
import java.util.Iterator;

class LinkedIterator<T extends DoubleLinkedItem> implements Iterator<T>, Serializable {

	private static final long serialVersionUID = -3031618047808737371L;

	private T current;
	
	private DoubleLinkedList<T> parent;
	
	/**
	 * for GWT
	 */
	@SuppressWarnings("unused")
	private LinkedIterator() {}
	
	protected LinkedIterator(DoubleLinkedList<T> parent) {
		this.parent = parent;
		current = parent.getFirstItem();
	}

	public boolean hasNext() {
		
		return current != null;
	}

	@SuppressWarnings("unchecked")
	public T next() {
		T returnVal = current;
		
		current = (T)current.getObjectAfter();
		
		return returnVal;
	}

	@SuppressWarnings("unchecked")
	public void remove() {
		
		T returnVal = (T)current.getObjectAfter();
		
		parent.remove(current);
		
		current = returnVal;		
	}
	
}