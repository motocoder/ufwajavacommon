package llc.ufwa.data.beans;

public class Entry<Key, Value> {
    
    private final Key key;
    private final Value value;

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
   
}
