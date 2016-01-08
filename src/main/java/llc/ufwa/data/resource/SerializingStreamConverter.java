package llc.ufwa.data.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.util.DataUtils;

public class SerializingStreamConverter<Value> implements Converter<Value, InputStream> {
    
    private final Executor executors;

    public SerializingStreamConverter(Executor executors) {
        this.executors = executors;
    }
    
    public SerializingStreamConverter() {
        this.executors = Executors.newSingleThreadExecutor();
    }

    @Override
    public InputStream convert(Value old) throws ResourceException {
        
        try {
            return DataUtils.serializeToStream(old, executors);
        }
        catch (IOException e) {
            throw new ResourceException("<SerializingConverter><1>, " + e);
        }
        
    }

    @Override
	@SuppressWarnings("unchecked")  // Supresses warning from unchecked cast.  Cast was added to fix error in Jenkins build environment.
    public Value restore(InputStream newVal) throws ResourceException {
        
        try {
            return (Value) DataUtils.deserialize(newVal);
        }
        catch (IOException e) {
            throw new ResourceException("<SerializingConverter><2>, " + e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceException("<SerializingConverter><3>, " + e);
        }
        
    }

}
