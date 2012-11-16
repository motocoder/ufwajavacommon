package llc.ufwa.data.beans;

import java.io.Serializable;

public class Entry<Key, Value> implements Serializable {

    private static final long serialVersionUID = -4369912094445839931L;
    
    private Key key;
    private Value value;
    
    @SuppressWarnings("unused")
    private Entry() {
        
    }

    public Entry(final Key key, final Value value) {
        
        this.key = key;
        this.value = value;
        
    }

    public Key getKey() {
        return key;
    }

    public Value getValue() {
        return value;
    }

    void setKey(Key key) {
        this.key = key;
    }

    void setValue(Value value) {
        this.value = value;
    }
   
}
