package llc.ufwa.javacommon.test.cache;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.AlwaysNullCache;
import llc.ufwa.data.resource.cache.Cache;

import org.junit.Test;

public class AlwaysNullCacheTest {
	
	@Test
    public void test() {
		
    	try {
    		
    		final Cache<String, String> cache = new AlwaysNullCache<String, String>();
            
    		cache.put("key", "test");
            
    		TestCase.assertNull(cache.get("key"));
            cache.remove("key");
            TestCase.assertEquals(cache.exists("key"), true);
            
            final List<String> keys = new ArrayList<String>();
	        
	        keys.add("test1");
	        keys.add("test2");
	        
	        final List<String> results = cache.getAll(keys);
	        
	        TestCase.assertNull(results.get(0));
	        TestCase.assertNull(results.get(1));
	        
	        cache.clear();
	        
	        TestCase.assertNull(cache.get("test1"));
	        TestCase.assertNull(cache.get("test2"));
	        
	        {
    	
    		final Cache<String, String> cache2 = new AlwaysNullCache<String, String>(true);
            
            cache2.put("test", "hello");
            
            TestCase.assertNull(cache2.get("test"));
            cache2.remove("test");
            TestCase.assertEquals(cache2.exists("test"), true);
            cache2.clear();
            
            TestCase.assertNull(cache2.get("test"));
            
	        }
	        
    	}
    	
    	catch(ResourceException e) {
            TestCase.fail("failed");
        }
    	
	}
	
}
