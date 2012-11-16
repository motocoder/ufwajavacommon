package llc.ufwa.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DataUtils {
    
    private DataUtils() {
        
    }
    
    /**
     * 
     * @param object
     * @return
     * @throws IOException
     */
    public static byte [] serialize(final Object object) throws IOException {
        
        if(object == null) {
            throw new NullPointerException("object cannot be null");
        }
        
        final ByteArrayOutputStream returnVal = new ByteArrayOutputStream();
        final ObjectOutputStream objectsOut = new ObjectOutputStream(returnVal);
        
        objectsOut.writeObject(object);
        objectsOut.flush();
        objectsOut.close();
        
        return returnVal.toByteArray();
        
    }
    
    /**
     * Beware class case exception if you expect the wrong type of return value.
     * 
     * If you fear you wont know what the return value is, put the return value into an object reference 
     * then do instanceof until you figure it out.
     * 
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(final byte [] bytes) throws IOException, ClassNotFoundException {
        
        if(bytes == null) {
            throw new NullPointerException("bytes cannot be null");
        }
        
        final ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        final ObjectInputStream objectsIn = new ObjectInputStream(bytesIn);
        
        return (T) objectsIn.readObject();
        
    }

}
