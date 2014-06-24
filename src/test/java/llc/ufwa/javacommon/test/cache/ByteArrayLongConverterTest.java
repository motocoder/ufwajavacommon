package llc.ufwa.javacommon.test.cache;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.ByteArrayLongConverter;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteArrayLongConverterTest {
    
    static {
        BasicConfigurator.configure();
    }
    
    private static final Logger logger = LoggerFactory.getLogger(ByteArrayLongConverterTest.class);
    
    @Test
    public void testConverter() {
        
        final ByteArrayLongConverter converter = new ByteArrayLongConverter();       
        
        try {
            
            final byte[] bytes = converter.restore(50L);
            
            logger.debug("bytes 0: " + bytes[0]);
            logger.debug("bytes 1: " + bytes[1]);
            logger.debug("bytes 2: " + bytes[2]);
            logger.debug("bytes 3: " + bytes[3]);
            logger.debug("bytes 4: " + bytes[4]);
            logger.debug("bytes 5: " + bytes[5]);
            logger.debug("bytes 6: " + bytes[6]);
            logger.debug("bytes 7: " + bytes[7]);
            
            final Long longVal = converter.convert(bytes);
            
            TestCase.assertEquals(50L, (long)longVal);
        
        }
        catch(ResourceException e) {
            TestCase.fail();
        }
    
    }
    
}
