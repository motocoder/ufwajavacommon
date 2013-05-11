package llc.ufwa.javacommon.test.cache;

import java.io.File;

import junit.framework.TestCase;

import llc.ufwa.data.resource.cache.FileHash;
import llc.ufwa.data.resource.cache.FileSegment;

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
        
        final FileHash hash = new FileHash(30, 10000, root);
        
        for(int i = 0; i < 501; i++) {
            
            logger.debug("put " + i);
        
            hash.put(String.valueOf(i), new FileSegment(i, i));
            
            final FileSegment seg = hash.get(String.valueOf(i));
            
            TestCase.assertNotNull(seg);
            TestCase.assertEquals(seg.getIndex(), i);
            TestCase.assertEquals(seg.getLength(), i);
            
        }
        
        TestCase.assertNull(hash.get("502"));
        
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
