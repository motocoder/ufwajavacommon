package llc.ufwa.data.resource;

import java.nio.ByteBuffer;

import llc.ufwa.data.exception.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteArrayLongConverter implements Converter<byte [], Long>{

    private static final Logger logger = LoggerFactory.getLogger(ByteArrayLongConverter.class);
    
    @Override
    public Long convert(byte[] value) throws ResourceException {
                
        long returnVal = 0;

        for (int i = 0; i < value.length; i++)
        {
            returnVal = (returnVal << 8) + (value[i] & 0xff);
        }
                
        return returnVal;
    }

    @Override
    public byte[] restore(Long newVal) throws ResourceException {
                
        final byte[] returnVal = ByteBuffer.allocate(8).putLong(newVal).array();
                
        return returnVal;
    }

}
