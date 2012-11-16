package llc.ufwa.javacommon.test.cache;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.SoftMemoryCache;

import org.junit.Test;

public class SoftMemoryCacheTest {

    @Test
    public void testMemoryCache() {
            
        try {
            
            final Cache<String, String> cache = new SoftMemoryCache<String, String>();
            
            {
                
                TestCase.assertNull(cache.get("hi"));
                TestCase.assertFalse(cache.exists("hi"));
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("hi1");
                keys.add("hi2");
                
                final List<String> results = cache.getAll(keys);
                
                TestCase.assertEquals(2, results.size());
                
                cache.put("hi", "test");
                
                TestCase.assertTrue(cache.exists("hi"));
                TestCase.assertEquals("test", cache.get("hi"));
                
                cache.put("hi1", "test1");
                cache.put("hi2", "test2");
                
                final List<String> results2 = cache.getAll(keys);
                
                TestCase.assertEquals(results2.get(0), "test1");
                TestCase.assertEquals(results2.get(1), "test2");
                
                cache.clear();
                
            }
            
            {
                TestCase.assertNull(cache.get("hi"));
                TestCase.assertFalse(cache.exists("hi"));
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("hi1");
                keys.add("hi2");
                
                final List<String> results = cache.getAll(keys);
                
                TestCase.assertEquals(2, results.size());
                
                cache.clear();
                
            }
            
            {
                
                cache.put("hi", "test");
                
                TestCase.assertEquals("test", cache.get("hi"));
                
                cache.remove("hi");
                
                TestCase.assertNull(cache.get("hi"));
                
            }
        
        }
        catch(Exception e) {
            TestCase.fail("should not get here");
        }

    }
    
}
