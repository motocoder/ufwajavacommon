package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.cache.AlwaysNullCache;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.DiskCache;
import llc.ufwa.data.resource.cache.KeyEncodingCache;
import llc.ufwa.data.resource.cache.ResourceLoaderCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ResourceLoader;

import org.junit.Test;

public class ResourceLoaderCacheTest {
	
    @Test
    public void test() {
    	
    	try{
    		
			final Cache<String, String> cache = 
				new ResourceLoaderCache<String, String>(
					new DefaultResourceLoader<String, String>() {
	
						@Override
						public String get(String key) throws ResourceException {
			                return key;
						}
					}
				);
	        
            cache.put("key", "test");
            TestCase.assertEquals(cache.get("key"), "key");
            cache.remove("key");
            TestCase.assertEquals(cache.exists("key"), true);
            cache.clear();
            
            {
            	
        	final List<String> keys = new ArrayList<String>();
            
            keys.add("test1");
            keys.add("test2");
            
            final List<String> results = cache.getAll(keys);
            	
            TestCase.assertEquals(results.get(0), cache.get("test1"));
            TestCase.assertEquals(results.get(1), cache.get("test2"));
            
            }
            
    	}
    	
        catch(ResourceException e) {
            TestCase.fail("failed");
        }
        
    }
    
}
