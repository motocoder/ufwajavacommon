package llc.ufwa.data.beans;

import java.io.Serializable;

public abstract class GUIDEqualable<T> implements Serializable {

    private static final long serialVersionUID = 3277942505427030784L;
    
    protected T id;

    @SuppressWarnings("unused")
    private GUIDEqualable() {
    }
    
    public GUIDEqualable(final T id) {
        this.id = id;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        
        final boolean returnVal;
        
        if(obj.getClass() == this.getClass()) {
            
            final GUIDEqualable equalable = (GUIDEqualable) obj;
            
            if(this.getID() != null) {
                returnVal = equalable.getID().equals(this.getID());
            }
            else {
                returnVal = equalable == this;
            }
        }
        else {
            returnVal = false;
        }
        
        return returnVal;
    }

    @Override
    public int hashCode() {
        return id.toString().hashCode();
    }

    protected void setID(T id) {
        this.id = id;
    }

    public T getID() {
        return id;
    }
    
}
