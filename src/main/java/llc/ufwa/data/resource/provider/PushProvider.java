package llc.ufwa.data.resource.provider;

public interface PushProvider<Value> extends ResourceProvider<Value> {

    void push(Value value);
    
}
