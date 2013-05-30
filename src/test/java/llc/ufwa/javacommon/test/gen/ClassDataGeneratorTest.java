package llc.ufwa.javacommon.test.gen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import llc.ufwa.gen.ClassDataGenerator;
import llc.ufwa.util.StreamUtil;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassDataGeneratorTest {

    static {
        
        BasicConfigurator.configure();
        
    }
    
    private static final Logger logger = LoggerFactory.getLogger(ClassDataGeneratorTest.class);
    
    @Test
    public void testGenerator() {
        
        final File outputFolder = new File("./target/test-files/temp-gen/");
        final File dataIn = new File("./src/test/java/llc/ufwa/javacommon/test/gen/test_data.xml");
        
        int b = 159;
        int c = 11;
        
        logger.debug("magic " + String.valueOf((c << 8) | b));
        
        try {
            ClassDataGenerator.generateClassFilesFor(dataIn, outputFolder, "TestGen", "llc.ufwa.gen");
        } 
        catch (IOException e) {
            
            logger.error("Failed test", e);
            TestCase.fail();
            
        }
        
    }
//    
//    @Test
//    public void testGeneratorValue() {
//        
//        final TestGenReader reader = new TestGenReader();
//        
//        final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
//        
//        
//        try {
//            
//            final InputStream is = reader.getInputStream();
//            
//            try {
//                StreamUtil.copyTo(is, bytesOut);
//            }
//            finally {
//                is.close();
//            }
//        }
//        catch (IOException e) {
//            
//            logger.error("io exception ", e);
//            TestCase.fail();
//            
//        }
//        
//        logger.debug("read " + bytesOut.toString());
//        
//    }
}
