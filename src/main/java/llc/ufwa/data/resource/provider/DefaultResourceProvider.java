package llc.ufwa.data.resource.provider;

import java.net.SocketException;

import llc.ufwa.data.exception.ResourceException;

public abstract class DefaultResourceProvider<Value> implements ResourceProvider<Value> {

    @Override
    public boolean exists() throws ResourceException, SocketException {
        return provide() != null;
    }
    
}
