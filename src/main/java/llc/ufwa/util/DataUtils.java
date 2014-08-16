package llc.ufwa.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(DataUtils.class);
    
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
            throw new NullPointerException("<DataUtils><1>, " + "object cannot be null");
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
            throw new NullPointerException("<DataUtils><2>, " + "bytes cannot be null");
        }
        
        final ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        final ObjectInputStream objectsIn = new ObjectInputStream(bytesIn);
        
        return (T) objectsIn.readObject();
        
    }

    /**
     * 
     * @param bytesIn
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(final InputStream bytesIn) throws IOException, ClassNotFoundException {
        
        if(bytesIn == null) {
            throw new NullPointerException("<DataUtils><2>, " + "bytes cannot be null");
        }
        
        final ObjectInputStream objectsIn = new ObjectInputStream(bytesIn);
        
    	return (T) objectsIn.readObject();
        
    }
	
    /**
     * 
     * @param object
     * @return
     * @throws IOException
     */
	public static InputStream serializeToStream(final Object object) throws IOException {
        
        if(object == null) {
            throw new NullPointerException("<DataUtils><1>, " + "object cannot be null");
        }
        
        final PipedOutputStream pipedOut = new PipedOutputStream();        
        final PipedInputStream pipedIn = new PipedInputStream(pipedOut);
        
        final ObjectOutputStream objectsOut = new ObjectOutputStream(pipedOut);
        
        Executors.newSingleThreadExecutor().execute(
            new Runnable() {

                @Override
                public void run() {
                    try {
                    objectsOut.writeObject(object);
                    
                    objectsOut.flush();
                    objectsOut.close();
                    
                    }
                    catch (IOException e) {
                        logger.error("ERROR:", e);
                    }
                    finally {
                        
                        try {
                            objectsOut.close();
                        } 
                        catch (IOException e) {
                            logger.error("ERROR:",e);
                        }
                        
                    }
                    
                }
                
            }
            
        );

        
        return pipedIn;
        
    }
}
