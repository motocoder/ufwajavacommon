package llc.ufwa.data.beans;

import java.io.Serializable;

public abstract class IDEqualable implements Serializable {

    private static final long serialVersionUID = 3277942505427030784L;
    
    protected long id;

    @SuppressWarnings("unused")
    private IDEqualable() {
        this.id = -1;
    }
    
    public IDEqualable(final long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        
        final boolean returnVal;
        
        if(obj.getClass() == this.getClass()) {
            final IDEqualable equalable = (IDEqualable) obj;
            
            if(this.getID() >= 0) {
                returnVal = equalable.getID() == this.getID();
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
        return (int)id;
    }

    @SuppressWarnings("unused")
    private final void setID(long id) {
        this.id = id;
    }

    public final long getID() {
        return id;
    }
}
