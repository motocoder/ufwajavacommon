package llc.ufwa.data;

import java.util.Map;

public class DefaultEntry<Key, Value> implements Map.Entry<Key, Value> {

    private final Key key;
    private volatile Value value;

    public DefaultEntry(
        final Key key,
        final Value value
    ) {
        
        this.key = key;
        this.value = value;
        
    }
    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public Value setValue(Value value) {
        
        this.value = value;
        
        return value;
        
    }
    
}
