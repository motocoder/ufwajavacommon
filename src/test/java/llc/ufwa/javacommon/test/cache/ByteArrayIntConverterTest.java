package llc.ufwa.javacommon.test.cache;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.ByteArrayIntegerConverter;
import llc.ufwa.data.resource.ByteArrayLongConverter;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteArrayIntConverterTest {
    
    static {
        BasicConfigurator.configure();
    }
    
    private static final Logger logger = LoggerFactory.getLogger(ByteArrayIntConverterTest.class);
    
    @Test
    public void testConverter() {
        
        final ByteArrayIntegerConverter converter = new ByteArrayIntegerConverter();       
        
        try {
            
            final byte[] bytes = converter.restore(50);
            
            logger.debug("bytes 0: " + bytes[0]);
            logger.debug("bytes 1: " + bytes[1]);
            logger.debug("bytes 2: " + bytes[2]);
            logger.debug("bytes 3: " + bytes[3]);
            
            
            final Integer longVal = converter.convert(bytes);
            
            TestCase.assertEquals(50, (int)longVal);
        
        }
        catch(ResourceException e) {
            TestCase.fail();
        }
    
    }
    
}
