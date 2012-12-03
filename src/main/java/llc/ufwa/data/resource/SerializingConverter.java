package llc.ufwa.data.resource;

import java.io.IOException;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.util.DataUtils;

public class SerializingConverter<Value> implements Converter<Value, byte []> {

    @Override
    public byte[] convert(Value old) throws ResourceException {
        
        try {
            return DataUtils.serialize(old);
        }
        catch (IOException e) {
            throw new ResourceException(e);
        }
        
    }

    @Override
	@SuppressWarnings("unchecked")  // Supresses warning from unchecked cast.  Cast was added to fix error in Jenkins build environment.
    public Value restore(byte[] newVal) throws ResourceException {
        
        try {
            return (Value) DataUtils.deserialize(newVal);
        }
        catch (IOException e) {
            throw new ResourceException(e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceException(e);
        }
        
    }

}
