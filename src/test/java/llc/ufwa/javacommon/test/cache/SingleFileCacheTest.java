package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import llc.ufwa.data.resource.cache.FileHash;
import llc.ufwa.data.resource.cache.HashDataBlob;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleFileCacheTest {
    
    static {
        BasicConfigurator.configure();
    }
    
    private static final Logger logger = LoggerFactory.getLogger(SingleFileCacheTest.class);
    @Test
    public void testHashing() {
        
        final File root = new File("./target/test-files/temp-hash/");
        deleteRoot(root);
        
        final FileHash<String, String> hash = new FileHash<String, String>(root, new FakeHashManagerImpl<String, String>(), 1000);
        
        checkFileEmpty(root);
        
        final int TEST_COUNT = 1000;
        
        for(int i = 0; i < TEST_COUNT; i++) {
        
            hash.put(String.valueOf(i), String.valueOf(i));
            
            final String seg = hash.get(String.valueOf(i));
            
            TestCase.assertNotNull(seg);
            
            TestCase.assertEquals(String.valueOf(i), seg);
            
        }
        
        TestCase.assertNull(hash.get(String.valueOf(TEST_COUNT + 1)));
        
        for(int i = 0; i < TEST_COUNT; i++) {
            hash.remove(String.valueOf(i));            
        }
        
        for(int i = 0; i < TEST_COUNT; i++) {
            
            final String val = hash.get(String.valueOf(i));
            
            TestCase.assertNull(val);
            
        }
        
        checkFileEmpty(root);
        
        deleteRoot(root);
        
    }
    
    private void checkFileEmpty(final File root) {
        
        try {
            
            final InputStream in = new FileInputStream(root);
            
            try {
                
                int totalRead = 0;
                
                final byte [] buffer = new byte[1000];
                
                while(true) {
                    
                    final int read = in.read(buffer);
                    
                    if(read > 0) {
                        
                        for(int i = 0; i < read; i++) {
                            
                            if(buffer[i] != -1) {
                                
                                logger.debug("hash not empty " + totalRead + " " + buffer[i]);
                                TestCase.fail();
                                
                            }
                            
                            totalRead++;
                            
                        }
                        
                    }
                    else {
                        break;
                    }
                    
                }
                
            }
            finally {
                in.close();
            }
        } 
        catch (FileNotFoundException e) {
            TestCase.fail();
        }
        catch (IOException e) {
            TestCase.fail();
        }
        
    }

    void deleteRoot (File root) {
        if (root.exists()) {
            
            if(root.listFiles() != null) {
                for (File cacheFile: root.listFiles()){
                    cacheFile.delete();
                }
            }
            root.delete();
        }
    }

}
