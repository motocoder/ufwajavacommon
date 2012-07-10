package llc.ufwa.javacommon.test.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import llc.ufwa.util.CollectionUtil;

import org.junit.Test;

public class CollectionUtilTest {
    
    @Test
    public void testChunking() {
        
        final List<String> original = new ArrayList<String>();
        
        for(int i = 0; i < 10; i++) {
            original.add(String.valueOf(i));
        }
        
        {
            
            final List<String> sub4 = CollectionUtil.loadChunkAround(original, 4, 1);
            
            for(int i = 0; i < 4; i++) {
                TestCase.assertEquals(sub4.get(i), original.get(i));
            }
            
        }
        
        {
            
            final List<String> sub4 = CollectionUtil.loadChunkAround(original, 4, 0);
            
            for(int i = 0; i < 4; i++) {
                TestCase.assertEquals(sub4.get(i), original.get(i));
            }
            
        }
        
        final List<String> sub6 = CollectionUtil.loadChunkAround(original, 6, 4);
       
        for(int i = 1; i < 7; i++) {
            TestCase.assertEquals(sub6.get(i - 1), original.get(i));
        }
        
        {
            final List<String> last4 = CollectionUtil.loadChunkAround(original, 4, 8);
            
            for(int i = 0; i < 4; i++) {
                TestCase.assertEquals(last4.get(i), original.get(original.size() - 4 + i));
            }
        }
        
        {
            final List<String> last4 = CollectionUtil.loadChunkAround(original, 4, 9);
            
            for(int i = 0; i < 4; i++) {
                TestCase.assertEquals(last4.get(i), original.get(original.size() - 4 + i));
            }
        }
        
    }

}
