package llc.ufwa.data.resource.provider;

import llc.ufwa.data.exception.ResourceException;

public abstract class DefaultResourceProvider<Value> implements ResourceProvider<Value> {

    @Override
    public boolean exists() throws ResourceException {
        return provide() != null;
    }
    
}
