package llc.ufwa.data.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.omg.CORBA_2_3.portable.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.util.StreamUtil;

public class InputStreamConverter implements Converter<InputStream, byte []> {

    private static final Logger logger = LoggerFactory.getLogger(InputStreamConverter.class);
    
    @Override
    public byte[] convert(InputStream old) throws ResourceException {
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            
            StreamUtil.copyTo(old, out);
            
            out.flush();
            
        } 
        catch (IOException e) {
            
            logger.error("ERROR:", e);
            throw new ResourceException("Couldn't convert", e);
            
        }
        finally {
            
            try {
                old.close();
            } 
            catch (IOException e) {
                throw new ResourceException("Couldn't convert", e);
            }
            
        }
        
        return out.toByteArray();
        
    }

    @Override
    public InputStream restore(byte[] newVal) throws ResourceException {
        return new ByteArrayInputStream(newVal);
    }

}
