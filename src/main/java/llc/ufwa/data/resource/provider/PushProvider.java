package llc.ufwa.data.resource.provider;

import llc.ufwa.data.exception.ResourceException;

public interface PushProvider<Value> extends ResourceProvider<Value> {

    void push(Value value) throws ResourceException;
    
}
