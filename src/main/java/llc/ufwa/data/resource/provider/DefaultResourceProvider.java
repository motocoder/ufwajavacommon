package llc.ufwa.data.resource.provider;

public abstract class DefaultResourceProvider<Value> implements ResourceProvider<Value> {

    @Override
    public boolean exists() {
        return provide() != null;
    }
    
}
