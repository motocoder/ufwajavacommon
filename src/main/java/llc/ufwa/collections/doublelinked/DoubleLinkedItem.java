package llc.ufwa.collections.doublelinked;

import java.io.Serializable;

import llc.ufwa.data.beans.IDEqualable;


public abstract class DoubleLinkedItem extends IDEqualable implements Serializable {
	
	private static final long serialVersionUID = 2398269579783273221L;
	
	private DoubleLinkedItem before;
	private DoubleLinkedItem after;
	
	private DoubleLinkedItem() {
	    super(-1);
	}
	
	protected DoubleLinkedItem(long id) {
	    super(id);
	}
	
	final public DoubleLinkedItem getObjectBefore() {
		return before;
	}
	
	final public DoubleLinkedItem getObjectAfter() {
		return after;
	}
	
	protected final void setObjectAfter(DoubleLinkedItem after) {
		if(after == null) {
			objectAfterID = -1L;
		}
		this.after = after;;
	}

	protected final void setObjectBefore(DoubleLinkedItem before) {
		if(before == null) {
			objectBeforeID = -1L;
		}
		this.before = before;
	}

	private long objectBeforeID = -1L;
	
	protected final void setObjectBeforeID(long id) {
		this.objectBeforeID = id;
	}
	
	public final long getObjectBeforeID() {
		if(this.getObjectBefore() == null) {
			return objectBeforeID;
		}
		else {
			return this.getObjectBefore().getID();
		}
	}
	
	private long objectAfterID = -1L;
	
	protected final void setObjectAfterID(long id) {
		this.objectAfterID = id;
	}
	
	public final long getObjectAfterID() {
		if(this.getObjectAfter() == null) {
			return objectAfterID;
		}
		else {
			return this.getObjectAfter().getID();
		}
	}	

}
